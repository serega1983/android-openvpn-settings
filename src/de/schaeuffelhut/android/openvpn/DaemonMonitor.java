package de.schaeuffelhut.android.openvpn;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Stack;

import de.schaeuffelhut.android.openvpn.DaemonMonitor.ManagementThread.ReplyHandler;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * Starts an OpenVPN process and monitors it until its death;
 * s
 * @author fries
 *
 */
public class DaemonMonitor
{
//	private final boolean LOCAL_LOGD = true;
	private final String mTAG_DM;

	private final Context mContext;
	
	private final File mOpenvpnBinary;
	
	private final File mConfigFile;
	private final File mPidFile;
	private final File mMgmgPwFile;
	private final File mMgmgPortFile;
	
	private final String mConfig;
	
	private Shell mOpenVPNShell;
	private ManagementThread mManagementThread;
	
	public DaemonMonitor(Context context, File openvpnBinary, File configFile, File comDir )
	{
		mContext = context;

		mOpenvpnBinary = openvpnBinary;
		
		mConfigFile = configFile;
		mPidFile = new File( comDir, configFile.getName() + "-pid" );
		mMgmgPwFile = new File( comDir, configFile.getName() + "-pw" );
		mMgmgPortFile = new File( comDir, configFile.getName() + "-port" );
		
		mConfig = configFile.getName();
		mTAG_DM = String.format("OpenVPN-DaemonMonitor[%s]", mConfig);
		
		reattach();
	}
	
	private void reattach()
	{
		mOpenVPNShell = null;

		mManagementThread = new ManagementThread();
		if ( mManagementThread.attach() )
			mManagementThread.start();
		else
			mManagementThread = null;
	}

	void start()
	{
		if ( isAlive() )
		{
			Log.w( mTAG_DM, "Can't start, daemon is already running!" );
			return;
		}

		mPidFile.delete();
		mMgmgPwFile.delete();
		mMgmgPortFile.delete();

		mOpenVPNShell = new Shell( mTAG_DM + "-daemon" )
		{
			@Override
			void onShellPrepared()
			{
				cmd( "cd " + mOpenvpnBinary.getParentFile().getAbsolutePath() );
				su();
				exec( String.format( 
						"%s --cd %s --config %s --writepid %s --management 127.0.0.1 30000",
						mOpenvpnBinary.getAbsolutePath(),				
						mConfigFile.getParentFile().getAbsolutePath(),
						mConfigFile.getName(),
						mPidFile.getAbsolutePath()					
				));
			}

			boolean waitForMgmt = true;
			@Override
			protected void onStdout(String line)
			{
				if ( waitForMgmt && line.indexOf( "MANAGEMENT: TCP Socket listening on" ) != -1 )
				{
					waitForMgmt = false;
					mManagementThread = new ManagementThread();
					mManagementThread.start();
				}
			}

			void onShellTerminated()
			{
				waitForQuietly();
				//TODO: set null, but synchronize
//				mOpenVPNShell = null;
			}
		};
		mOpenVPNShell.start();
	}

	void restart()
	{
		if ( !isAlive() )
		{
			Log.w( mTAG_DM, "Can't restart, daemon is not running!" );
		}
		else
		{
			mManagementThread.signal( ManagementThread.SIGUSR1 );
		}
	}

	void stop()
	{
		if ( !isAlive() )
		{
			Log.w( mTAG_DM, "Can't stop, daemon is not running!" );
		}
		else
		{
			mManagementThread.signal( ManagementThread.SIGTERM );
		}
	}

	boolean isAlive()
	{
		return mManagementThread != null && mManagementThread.isAlive();
	}
	
	class ManagementThread extends Thread
	{
		private static final String MGMG_MSG_STATE = ">STATE:";
		
		private final String mTAG_MT = DaemonMonitor.this.mTAG_DM + "-mgmt";
		private Socket socket;
		private PrintWriter out;
		private int mCurrentState = Intents.NETWORK_STATE_UNKNOWN;
		
		@Override
		public void run()
		{
			Log.d( mTAG_MT, "started");

			// try to attach to OpenVPN monitor port, as long as 
			// the startup shell is alive 
			boolean success;
			while( !(success = attach()) && mOpenVPNShell != null && mOpenVPNShell.isAlive() )
			{
				try {sleep(1000);} catch (InterruptedException e) {}
			}
			
			try
			{
				if ( success )
				{
					Log.v(mTAG_MT, "attached to OpenVPN monitor port");
					mContext.sendStickyBroadcast( 
							Intents.daemonStateChanged(
									mConfig,
									Intents.DAEMON_STATE_ENABLED
							)
					);
					
					monitor();
				}
				else
				{
					Log.v(mTAG_MT, "not attached to OpenVPN monitor port");
				}
			}
			finally
			{
				mContext.sendStickyBroadcast( 
						Intents.daemonStateChanged(
								mConfig,
								Intents.DAEMON_STATE_DISABLED
						)
				);

				Log.d( mTAG_MT, "terminated");
			}
		}
		
		boolean attach()
		{
			if ( socket == null || !socket.isConnected() )
			{
				try
				{
					socket = new Socket( InetAddress.getLocalHost(), 30000 );
				}
				catch (UnknownHostException e)
				{
					socket = null;
					Log.e( mTAG_MT, "attaching to OpenVPN daemon", e );
				}
				catch (IOException e)
				{
					socket = null;
					Log.e( mTAG_MT, "attaching to OpenVPN daemon: " + e.getMessage() );
				}
			}
			return socket != null && socket.isConnected();
		}

		private Stack<ReplyHandler> mReplyHandlers = new Stack<ReplyHandler>();
		abstract class ReplyHandler {
			abstract void onReply(String line);
		}
		
		private void monitor()
		{
			try {
				out = new PrintWriter( socket.getOutputStream() );

				LineNumberReader lnr = new LineNumberReader(
						new InputStreamReader( socket.getInputStream() ),
						128
				);

				synchronized ( this ) {
					Log.v(mTAG_MT, "Socket IO established, notifying waiting threads");
					notifyAll();
				}
				
				networkStateChanged( Intents.NETWORK_STATE_CONNECTING );

				state();
				state( true );

				String line;
				while ( (line = lnr.readLine() ) != null )
				{
					Log.v( mTAG_MT, line );
					if ( line.startsWith( ">" ) ) // async realtime notifications start with ">"
					{
						if ( line.startsWith( MGMG_MSG_STATE ) )
							onState( line );
						// read log msgs via mgmt interface
					}
					else if ( line.startsWith( "SUCCESS:" ) )
					{
						// ignore
					}
					else // synchronous reply to command 
					{
						if ( mReplyHandlers.isEmpty() )
						{
							Log.w( mTAG_MT, "unexpected reply" );
						}
						else if ( "END".equals( line ) )
						{
							ReplyHandler replyHandler = mReplyHandlers.pop();
							Log.w( mTAG_MT, "removed ReplyHandler: " + replyHandler );
						}
						else
						{
							mReplyHandlers.peek().onReply( line );
						}
					}
				}
			}
			catch(IOException e)
			{
				Log.e(mTAG_MT, "lost connection to OpenVPN daemon", e );
			}
		}

		private void networkStateChanged(int newState)
		{
			int oldState = mCurrentState;
			mCurrentState = newState;
			mContext.sendStickyBroadcast( Intents.networkStateChanged(
					mConfig,
					newState,
					oldState,
					System.currentTimeMillis()
			) );
		}
		
		private final static int STATE_FIELD_TIME = 0;
		private final static int STATE_FIELD_STATE = 1;
		private final static int STATE_FIELD_INFO0 = 2;
		private final static int STATE_FIELD_INFO1 = 3;
		private final static int STATE_FIELD_INFO2 = 4;
		
		private final static String STATE_RECONNECTING = "RECONNECTING";
		private final static String STATE_RESOLVE = "RESOLVE";
		private final static String STATE_WAIT = "WAIT";
		private final static String STATE_AUTH = "AUTH";
		private final static String STATE_GET_CONFIG = "GET_CONFIG";
		private final static String STATE_CONNECTED = "CONNECTED";
		private final static String STATE_ASSIGN_IP = "ASSIGN_IP";
		private final static String STATE_ADD_ROUTES = "ADD_ROUTES";
		private final static String STATE_EXITING = "EXITING";

		private void onState(String line)
		{
			Log.v(mTAG_MT, String.format("onState(\"%s\")", line ) );
			
			String fieldString = line.startsWith(MGMG_MSG_STATE) ? line.substring( MGMG_MSG_STATE.length() ) : line;
			String[] stateFields = fieldString.split( "," );
			String state = stateFields[STATE_FIELD_STATE];
			
			final int newState;
			String info0Extra = null;
			String info1Extra = null;
			String info2Extra = null;
			
			if (STATE_RECONNECTING.equals(state)) {
				newState = Intents.NETWORK_STATE_RECONNECTING;
				info0Extra = Intents.EXTRA_NETWORK_CAUSE;
			} else if (STATE_RESOLVE.equals(state)) {
				newState = Intents.NETWORK_STATE_RESOLVE;
			} else if (STATE_WAIT.equals(state)) {
				newState = Intents.NETWORK_STATE_WAIT;
			} else if (STATE_AUTH.equals(state)) {
				newState = Intents.NETWORK_STATE_AUTH;
			} else if (STATE_GET_CONFIG.equals(state)) {
				newState = Intents.NETWORK_STATE_GET_CONFIG;
			} else if (STATE_CONNECTED.equals(state)) {
				newState = Intents.NETWORK_STATE_CONNECTED;
				info1Extra = Intents.EXTRA_NETWORK_LOCALIP;
				info2Extra = Intents.EXTRA_NETWORK_REMOTEIP;
			} else if (STATE_ASSIGN_IP.equals(state)) {
				newState = Intents.NETWORK_STATE_ASSIGN_IP;
				info1Extra = Intents.EXTRA_NETWORK_LOCALIP;
			} else if (STATE_ADD_ROUTES.equals(state)) {
				newState = Intents.NETWORK_STATE_ADD_ROUTES;
			} else if (STATE_EXITING.equals(state)) {
				newState = Intents.NETWORK_STATE_EXITING;
				info0Extra = Intents.EXTRA_NETWORK_CAUSE;
			} else {
				Log.d(mTAG_MT, "unknown state: " + state);
				newState = Intents.NETWORK_STATE_UNKNOWN;
			}
			
			Intent intent = Intents.networkStateChanged(
					mConfig,
					newState,
					mCurrentState,
					Long.parseLong( stateFields[STATE_FIELD_TIME] ) * 1000
			);
			if ( info0Extra != null )
				intent.putExtra( info0Extra, stateFields[STATE_FIELD_INFO0] );
			if ( info1Extra != null )
				intent.putExtra( info1Extra, stateFields[STATE_FIELD_INFO1] );
			if ( info2Extra != null )
				intent.putExtra( info2Extra, stateFields[STATE_FIELD_INFO2] );

			mContext.sendStickyBroadcast( intent );
			
			mCurrentState = newState;
		}

		/*
		 * management commands, may be invoked by any thread.
		 */
		
		private synchronized void sendCommand( String cmd, ReplyHandler reply )
		{
			Log.v(mTAG_MT, String.format("sendCommand(\"%s\")", cmd) );
			if ( out == null )
			{
				Log.d(mTAG_MT, "sendCommand(), Socket IO not yet established, waiting");
				try {wait();} catch (InterruptedException e) {}
				Log.d(mTAG_MT, "sendCommand(), Socket IO is now established");
			}
			if ( reply != null )
				mReplyHandlers.push( reply );
			out.println( cmd );
			out.flush();
		}
		
		/**
		 * Query current state
		 */
		void state()
		{
			sendCommand( "state", new ReplyHandler() {
				void onReply(String line) {
					Log.v(mTAG_MT, "received synchronous reply to state command");
					onState(line);
				}
			});
		}
		
		/**
		 *  Turn on/off realtime state display;
		 */
		void state(boolean b){ sendCommand( ( b ? "state on" : "state off" ), null ); }
		
		final static int SIGHUP = 1;
		final static int SIGTERM = 2;
		final static int SIGUSR1 = 3;
		final static int SIGUSR2 = 4;
		/*
		 * Send signal s to daemon,
		 * s = SIGHUP|SIGTERM|SIGUSR1|SIGUSR2.
		 */
		public void signal(int s) {
			switch (s) {
			case SIGHUP:
				sendCommand( "signal SIGHUP", null );
				break;
			case SIGTERM:
				sendCommand( "signal SIGTERM", null );
				break;
			case SIGUSR1:
				sendCommand( "signal SIGUSR1", null );
				break;
			case SIGUSR2:
				sendCommand( "signal SIGUSR2", null );
				break;
			default:
				throw new UnexpectedSwitchValueException( s );
			}
		}
	}
}
