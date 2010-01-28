/**
 * Copyright 2009 Friedrich Schäuffelhut
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package de.schaeuffelhut.android.openvpn.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Stack;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;
import de.schaeuffelhut.android.openvpn.Intents;
import de.schaeuffelhut.android.openvpn.Notifications;
import de.schaeuffelhut.android.openvpn.Preferences;
import de.schaeuffelhut.android.openvpn.util.Shell;
import de.schaeuffelhut.android.openvpn.util.UnexpectedSwitchValueException;
import de.schaeuffelhut.android.openvpn.util.Util;

/**
 * Starts an OpenVPN process and monitors it until its death;
 * s
 * @author fries
 *
 */
public final class DaemonMonitor
{
//	private final boolean LOCAL_LOGD = true;
	private final String mTagDaemonMonitor;

	private final Context mContext;
	private final NotificationManager mNotificationManager;
		
	private final File mConfigFile;
	private final File mPidFile;
	private final File mMgmgPwFile;
	private final File mMgmgPortFile;
	
	private Shell mShell;
	private ManagementThread mManagementThread;

	
	public DaemonMonitor(Context context, File configFile, File comDir )
	{
		mContext = context;
		mConfigFile = configFile;
		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

		mPidFile = new File( comDir, configFile.getName() + "-pid" );
		mMgmgPwFile = new File( comDir, configFile.getName() + "-pw" );
		mMgmgPortFile = new File( comDir, configFile.getName() + "-port" );
		mTagDaemonMonitor = String.format("OpenVPN-DaemonMonitor[%s]", mConfigFile);
		reattach();
	}
	
	private boolean reattach()
	{
		mShell = null;

		mManagementThread = new ManagementThread();
		if ( mManagementThread.attach() )
			mManagementThread.start();
		else
			mManagementThread = null;
		
		return mManagementThread != null && mManagementThread.isAlive();
	}

	void start()
	{
		if ( isAlive() ) {
			Log.w( mTagDaemonMonitor, "start(): ManagementThread is already alive!" );
			return;
		}

		final File openvpnBinary = Preferences.getPathToBinaryAsFile( PreferenceManager.getDefaultSharedPreferences( mContext ) );
		if ( !openvpnBinary.exists() ) {
			Log.w( mTagDaemonMonitor, "start(): file not found: " + openvpnBinary );
			return;
		}
		
		if ( !mConfigFile.exists() ) {
			Log.w( mTagDaemonMonitor, "start(): file not found: " + mConfigFile );
			return;
		}
		
		final int mgmtPort = 10000 + (int)(Math.random() * 50000); 
		Log.w( mTagDaemonMonitor, "start(): choosing random port for management interface: " + mgmtPort );
		Preferences.setMgmtPort( mContext, mConfigFile, mgmtPort );
		
		if ( mPidFile.exists() )      mPidFile.delete();
		if ( mMgmgPwFile.exists() )   mMgmgPwFile.delete();
		if ( mMgmgPortFile.exists() ) mMgmgPortFile.delete();

		mShell = new Shell( mTagDaemonMonitor + "-daemon" )
		{
			@Override
			protected void onShellPrepared()
			{
				mContext.sendStickyBroadcast( 
						Intents.daemonStateChanged(
								mConfigFile.getAbsolutePath(),
								Intents.DAEMON_STATE_STARTUP
						)
				);

				cmd( "cd " + openvpnBinary.getParentFile().getAbsolutePath() );
				su();
				
				if ( Preferences.getDoModprobeTun( PreferenceManager.getDefaultSharedPreferences(mContext) ) )
					cmd( "modprobe tun" );
				
				exec( String.format( 
						"%s --cd %s --config %s --writepid %s --management 127.0.0.1 %d --management-query-passwords",
						openvpnBinary.getAbsolutePath(),				
						mConfigFile.getParentFile().getAbsolutePath(),
						mConfigFile.getName(),
						mPidFile.getAbsolutePath(),
						mgmtPort
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

			protected void onShellTerminated()
			{
				// while mManagementThread == null, system is in startup phase
				// and a DAEMON_STATE_DISABLED message is expected
				if ( mManagementThread == null )
					mContext.sendStickyBroadcast( 
							Intents.daemonStateChanged(
									mConfigFile.getAbsolutePath(),
									Intents.DAEMON_STATE_DISABLED
							)
					);
				
				waitForQuietly();
				mShell = null;
			}
		};
		mShell.start();
	}

	void restart()
	{
		if ( !isAlive() )
		{
			Log.w( mTagDaemonMonitor, "Can't restart, daemon is not running!" );
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
			Log.w( mTagDaemonMonitor, "Can't stop, daemon is not running!" );
		}
		else
		{
			mManagementThread.signal( ManagementThread.SIGTERM );
		}
	}

	void queryState()
	{
		if ( !isAlive() )
		{
			Log.w( mTagDaemonMonitor, "Can't query state, daemon is not running!" );
		}
		else
		{
			mManagementThread.state();
		}
	}

	public void supplyPassphrase(String passphrase)
	{
		if ( !isAlive() )
		{
			Log.w( mTagDaemonMonitor, "Can't supply passphrase, daemon is not running!" );
		}
		else
		{
			mManagementThread.sendPassphrase( passphrase );
		}
	}

	public void supplyUsernamePassword(String username, String password)
	{
		if ( !isAlive() )
		{
			Log.w( mTagDaemonMonitor, "Can't supply username/password, daemon is not running!" );
		}
		else
		{
			mManagementThread.sendPassword(username, password);
		}
	}

	boolean isAlive()
	{
		return mManagementThread != null && mManagementThread.isAlive();
	}
	
	class ManagementThread extends Thread
	{
		private static final String MGMG_MSG_STATE = ">STATE:";
		
		private final String mTAG_MT = DaemonMonitor.this.mTagDaemonMonitor + "-mgmt";
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
			while( !(success = attach()) && mShell != null && mShell.isAlive() )
			{
				try {sleep(1000);} catch (InterruptedException e) {}
			}
			
			try
			{
				if ( success )
				{
					Log.v(mTAG_MT, "Successfully attached to OpenVPN monitor port");
					mContext.sendStickyBroadcast( 
							Intents.daemonStateChanged(
									mConfigFile.getAbsolutePath(),
									Intents.DAEMON_STATE_ENABLED
							)
					);
					
					monitor();
				}
				else
				{
					Log.v(mTAG_MT, "Could not attach to OpenVPN monitor port");
				}
			}
			finally
			{
				mContext.sendStickyBroadcast( 
						Intents.daemonStateChanged(
								mConfigFile.getAbsolutePath(),
								Intents.DAEMON_STATE_DISABLED
						)
				);

				Log.d( mTAG_MT, "terminated");
			}
		}
		
		boolean attach()
		{
			int mgmtPort = Preferences.getMgmtPort(mContext, mConfigFile);
			Log.d( mTAG_MT, "attach(): using management port at " + mgmtPort );
			
			if ( mgmtPort == -1 )
			{
				Log.d( mTAG_MT, "attach(): unspecified management port, not attaching" );
			}
			else if ( socket == null || !socket.isConnected() )
			{
				try
				{
					socket = new Socket( InetAddress.getLocalHost(), mgmtPort );
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
		private boolean mMonitorLoopStarted = false; // set to true, when in final monitoring loop

		private boolean mWaitingForPassphrase = false;
		private boolean mWaitingForUserPassword = false;

		abstract class ReplyHandler {
			abstract void onReply(String line);
		}
		
		private void monitor()
		{
			LineNumberReader lnr = null;
			try {
				out = new PrintWriter( socket.getOutputStream() );

				lnr = new LineNumberReader(
						new InputStreamReader( socket.getInputStream() ),
						128
				);

				Log.v(mTAG_MT, "Socket IO established");

				networkStateChanged( Intents.NETWORK_STATE_CONNECTING );

				state();
				state( true );
				
				String line;
				while ( (line = lnr.readLine() ) != null )
					handleLine(line);
			}
			catch(IOException e)
			{
				Log.e(mTAG_MT, "Lost connection to OpenVPN daemon", e );
				Util.closeQuietly( lnr );
				networkStateChanged( Intents.NETWORK_STATE_UNKNOWN );
			}
			finally
			{
				Util.closeQuietly( lnr );
				Util.closeQuietly( out );
				Util.closeQuietly( socket );
				
				if ( !mMonitorLoopStarted )
				{
					synchronized ( this ) {
						Log.v(mTAG_MT, "Monitor loop never run, notifying any remainig waiting threads");
						notifyAll();
					}
				}
			}
		}

		private synchronized void handleLine(String line)
		{
			if ( !mMonitorLoopStarted )
			{
				mMonitorLoopStarted = true;
				synchronized ( this ) {
					Log.v(mTAG_MT, "Monitor loop is ready, notifying waiting threads");
					notifyAll();
				}
			}
			
			Log.v( mTAG_MT, line );
			if ( line.startsWith( ">" ) ) // async realtime notifications start with ">"
			{
				if ( line.startsWith( MGMG_MSG_STATE ) )
				{
					onState( line );
				}
				else if ( line.startsWith( ">PASSWORD:" ) )
				{
					if ( line.equals( ">PASSWORD:Need 'Private Key' password" ) )
					{
						mWaitingForPassphrase = true;
						Notifications.sendPassphraseRequired(mContext, mNotificationManager, mConfigFile);
					}
					else if ( line.equals( ">PASSWORD:Need 'Auth' username/password" ) )
					{
						mWaitingForUserPassword = true;
						Notifications.sendUsernamePasswordRequired(mContext, mConfigFile, mNotificationManager);
					}
					else if ( line.equals( ">PASSWORD:Verification Failed: 'Private Key'" ) )
					{
						mWaitingForPassphrase = true;
						Notifications.sendPassphraseRequired(mContext, mNotificationManager, mConfigFile);
					}
					else if ( line.equals( ">PASSWORD:Verification Failed: 'Auth'" ) )
					{
						mWaitingForUserPassword = true;
						Notifications.sendUsernamePasswordRequired(mContext, mConfigFile, mNotificationManager);
					}
					else
					{
						Log.w(mTAG_MT, "unexpected 'PASSWORD:' notification" + line );
					}
				}
				// read log msgs via mgmt interface
				else
				{
					Log.d(mTAG_MT, "unexpected realtime notification: " + line );
				}
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


		private void networkStateChanged(int newState)
		{
			int oldState = mCurrentState;
			mCurrentState = newState;
			mContext.sendStickyBroadcast( Intents.networkStateChanged(
					mConfigFile.getAbsolutePath(),
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
					mConfigFile.getAbsolutePath(),
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
			// if monitor loop has not been started and this is not the monitor thread it self, then wait
			if ( !mMonitorLoopStarted && Thread.currentThread() != this )
			{
				Log.v(mTAG_MT, String.format("sendCommand(\"%s\"): waiting for monitor loop to come ready", cmd) );
				try {wait();} catch (InterruptedException e) {}
			}
			
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
		
		/**
		 * Respond to a password request
		 * 
		 * @param password
		 */
		final synchronized void sendPassphrase(String password)
		{
			if ( password == null )
				Log.w(mTAG_MT, "Won't send <null> as passphrase to openvpn daemon!");
			else if ( !mWaitingForPassphrase )
				Log.w(mTAG_MT, "Won't send unexpected passphrase to openvpn daemon!");
			else
			{
				mWaitingForPassphrase = false;
				sendCommand( String.format("password 'Private Key' '%s'", escape( password ) ), null );
			}
		}

		final synchronized void sendPassword(String user, String password)
		{
			if ( user == null )
				Log.w(mTAG_MT, "Won't send <null> as user to openvpn daemon!");
			else if ( password == null )
				Log.w(mTAG_MT, "Won't send <null> as password to openvpn daemon!");
			else if ( !mWaitingForPassphrase )
				Log.w(mTAG_MT, "Won't send unexpected user/password to openvpn daemon!");
			else
			{
				mWaitingForUserPassword = false;
				sendCommand( String.format( "username 'Auth' '%s'", escape( user ) ), null );
				sendCommand( String.format( "password 'Auth' '%s'", escape(password) ), null );
			}
		}

		private String escape(String s) {
			return s.replace( "\\", "\\\\" ).replace("'", "\\'" );
		}

	}

}
