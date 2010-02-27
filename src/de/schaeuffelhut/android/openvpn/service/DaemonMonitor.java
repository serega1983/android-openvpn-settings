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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;
import de.schaeuffelhut.android.openvpn.Intents;
import de.schaeuffelhut.android.openvpn.Notifications;
import de.schaeuffelhut.android.openvpn.Preferences;
import de.schaeuffelhut.android.openvpn.util.Shell;
import de.schaeuffelhut.android.openvpn.util.TrafficStats;
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
	final String mTagDaemonMonitor;

	final Context mContext;
	final NotificationManager mNotificationManager;
		
	final File mConfigFile;
	final File mPidFile;
	final File mMgmgPwFile;
	final File mMgmgPortFile;
	final int mNotificationId;
	
	Shell mShell;
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
			
		mNotificationId = Preferences.getNotificationId( mContext, mConfigFile );
		
		reattach();
	}
	
	private boolean reattach()
	{
		mShell = null;

		mManagementThread = new ManagementThread( this );
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
					mManagementThread = new ManagementThread( DaemonMonitor.this );
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

	void stopAndWaitForTermination() throws InterruptedException
	{
		if ( !isAlive() )
		{
			Log.w( mTagDaemonMonitor, "Can't stop, daemon is not running!" );
		}
		else
		{
			mManagementThread.stopAndWaitForTermination();
		}
	}

	void waitForTermination() throws InterruptedException {
		if ( !isAlive() )
		{
			Log.w( mTagDaemonMonitor, "Can't wait for termination, daemon is already dead!" );
		}
		else
		{
			mManagementThread.waitForTermination();
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
			mManagementThread.sendUserPassword(username, password);
		}
	}

	boolean isAlive()
	{
		return mManagementThread != null && mManagementThread.isAlive();
	}
}

final class ManagementThread extends Thread
{
	private final DaemonMonitor mDaemonMonitor;
	private final String mTAG_MT;
//	private final String mTAG_MT = mDaemonMonitor.mTagDaemonMonitor + "-mgmt";
	
	ManagementThread(DaemonMonitor daemonMonitor)
	{
		mDaemonMonitor = daemonMonitor;
		mTAG_MT = daemonMonitor.mTagDaemonMonitor + "-mgmt";
	}
	
	private final CountDownLatch mReady = new CountDownLatch(1);
	private final CountDownLatch mTerminated = new CountDownLatch(1);
	
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
		while( !(success = attach()) && mDaemonMonitor.mShell != null && mDaemonMonitor.mShell.isAlive() )
		{
			try {sleep(1000);} catch (InterruptedException e) {}
		}
		
		try
		{
			if ( success )
			{
				Log.v(mTAG_MT, "Successfully attached to OpenVPN monitor port");
				mDaemonMonitor.mContext.sendStickyBroadcast( 
						Intents.daemonStateChanged(
								mDaemonMonitor.mConfigFile.getAbsolutePath(),
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
			mDaemonMonitor.mContext.sendStickyBroadcast( 
					Intents.daemonStateChanged(
							mDaemonMonitor.mConfigFile.getAbsolutePath(),
							Intents.DAEMON_STATE_DISABLED
					)
			);

			Notifications.cancel( mDaemonMonitor.mNotificationId, mDaemonMonitor.mContext );

			mTerminated.countDown();
			
			Log.d( mTAG_MT, "terminated");
		}
	}
	
	boolean attach()
	{
		int mgmtPort = Preferences.getMgmtPort(mDaemonMonitor.mContext, mDaemonMonitor.mConfigFile);
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

	private LinkedList<Command> ms_PendingCommandFifo = new LinkedList<Command>();
	
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

			sendCommandNoLock( new StateCommand() );
			sendCommandNoLock( new SimpleCommand( "state on" ) );
			sendCommandNoLock( new SimpleCommand( "bytecount 5" ) );
			
			// allow other threads to submit commands
			mReady.countDown(); 
			
			//block until input is available
			while( block(lnr) )
				handleResponse(lnr);
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
			
			// make sure nobody is waiting for us
			mReady.countDown();
		}
	}
	
	/**
	 * Block until data becomes available or EOF is reached.
	 * 
	 * @param lnr
	 * @return true if data is available, false if EOF is reached.
	 * @throws IOException
	 */
	private boolean block(LineNumberReader lnr) throws IOException
	{
		lnr.mark(1);
		boolean eof = lnr.read() == -1;
		if ( !eof )
			lnr.reset();
		return !eof;
	}

	private final synchronized void handleResponse(LineNumberReader lnr) throws IOException
	{
		Command command;
		if ( ms_PendingCommandFifo.isEmpty() )
			command = mDummyCommandInstance;
		else
			command = ms_PendingCommandFifo.remove();
		command.handleResponse( lnr );
	}

	
	abstract class Command
	{
		final boolean expectsMultilineResponse;
		final boolean expectsSuccessOrError;
		
		public Command(boolean expectsSuccessOrError, boolean expectsMultilineResponse)
		{
			this.expectsMultilineResponse = expectsMultilineResponse;
			this.expectsSuccessOrError = expectsSuccessOrError;
		}

		abstract String getCommand();

		final void handleResponse(LineNumberReader lnr) throws IOException
		{
			ArrayList<String> multilineResponse = expectsMultilineResponse ? new ArrayList<String>() : null;
			
			String line;
			while( (line = lnr.readLine()) != null )
			{
				final boolean responseFinished;
				if ( line.startsWith( ">" ) )
				{
					// always handle asynchronous real-time messages
					handleRealtimeMessage( line );

					// go on reading command response, if expected
					responseFinished = expectsSuccessOrError || expectsMultilineResponse ? false : true;
				}
				else if ( line.startsWith( "SUCCESS:" ) )
				{
					if ( !expectsSuccessOrError )
						throw new RuntimeException( "Unexpected message: " + line );
					
					handleSuccess( line );
					
					responseFinished = expectsMultilineResponse ? false : true;
				}
				else if ( line.startsWith( "ERROR:" ) )
				{
					if ( !expectsSuccessOrError )
						throw new RuntimeException( "Unexpected message: " + line );
					
					handleError( line );
					
					responseFinished = expectsMultilineResponse ? false : true;
				}
				else if ( expectsMultilineResponse )
				{
					if ( line.equals( "END" ) )
					{
						handleMultilineResponse( multilineResponse );
						responseFinished = true;
					}
					else
					{
						multilineResponse.add( line );
						responseFinished = false;
					}
				}
				else
				{
					throw new RuntimeException( "Unexpected message: " + line );
				}
				
				if ( responseFinished )
					break;
			}
		}

		protected void handleSuccess(String line)
		{
			Log.d( mTAG_MT, line );
		}

		protected void handleError(String line)
		{
			Log.d( mTAG_MT, line );
		}

		protected void handleMultilineResponse(ArrayList<String> multilineResponse)
		{
			for( String line : multilineResponse )
				Log.d( mTAG_MT, line );
		}
	}

	private final Command mDummyCommandInstance = new DummyCommand();

	/**
	 * {@link DummyCommand} will only handle asynchronous real-time messages
	 * in case there is no pending command in the fifo.
	 * 
	 * @author Friedrich Schäuffelhut
	 */
	final class DummyCommand extends Command
	{
		public DummyCommand() {
			super( false, false );
		}
		@Override final String getCommand() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Simple command only returning SUCESS or ERROR. Response is ignored.
	 * @author Friedrich Schäuffelhut
	 */
	final class SimpleCommand extends Command
	{
		final String command;
		public SimpleCommand(String command) {
			super(true, false);
			this.command = command;
		}
		
		@Override public String getCommand()
		{
			return command;
		}
	}

	final class StateCommand extends Command
	{
		StateCommand() {
			super(false, true);
		}
		
		@Override public String getCommand()
		{
			return "state";
		}
		
		@Override
		protected void handleMultilineResponse(ArrayList<String> multilineResponse)
		{
			for( String line : multilineResponse )
				onState( line );
		}
	}

	final StatusCommand mStatusCommand = new StatusCommand();
	
	private final class StatusCommand extends Command {

		final TrafficStats trafficStats = new TrafficStats();
		
		StatusCommand()
		{
			super(false, true);
		}
		
		@Override final String getCommand()
		{
			return "status";
		}

		@Override
		protected void handleMultilineResponse(ArrayList<String> multilineResponse)
		{
			trafficStats.setStats( multilineResponse );
			Notifications.notifyBytes( mDaemonMonitor.mNotificationId, mDaemonMonitor.mContext, mDaemonMonitor.mNotificationManager, mDaemonMonitor.mConfigFile, trafficStats.toSmallInOutPerSecString() );
		}
	}


	
	/*
	 * SEND MANAGEMENT COMMANDS, may be invoked by any thread.
	 */
	
	/**
	 * Query current state
	 */
	void state()
	{
		sendCommand( new StateCommand() );
	}
		
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
			sendCommand( new SimpleCommand( "signal SIGHUP" ) );
			break;
		case SIGTERM:
			sendCommand( new SimpleCommand( "signal SIGTERM" ) );
			break;
		case SIGUSR1:
			sendCommand( new SimpleCommand( "signal SIGUSR1" ) );
			break;
		case SIGUSR2:
			sendCommand( new SimpleCommand( "signal SIGUSR2" ) );
			break;
		default:
			throw new UnexpectedSwitchValueException( s );
		}
	}
	
	void stopAndWaitForTermination() throws InterruptedException {
		signal( ManagementThread.SIGTERM );
		mTerminated.await();
	}

	void waitForTermination() throws InterruptedException {
		mTerminated.await();
	}

	/**
	 * Respond to a password request
	 * 
	 * @param password
	 */
	final void sendPassphrase(String password)
	{
		if ( password == null )
			Log.w(mTAG_MT, "Won't send <null> as passphrase to openvpn daemon!");
		else if ( !mWaitingForPassphrase )
			Log.w(mTAG_MT, "Won't send unexpected passphrase to openvpn daemon!");
		else
		{
			mWaitingForPassphrase = false;
			sendCommand( new SimpleCommand( String.format("password 'Private Key' '%s'", escape( password ) ) ) );
		}
	}

	final void sendUserPassword(String user, String password)
	{
		if ( user == null )
			Log.w(mTAG_MT, "Won't send <null> as user to openvpn daemon!");
		else if ( password == null )
			Log.w(mTAG_MT, "Won't send <null> as password to openvpn daemon!");
		else if ( !mWaitingForUserPassword )
			Log.w(mTAG_MT, "Won't send unexpected user/password to openvpn daemon!");
		else
		{
			mWaitingForUserPassword = false;
			sendCommand( new SimpleCommand( String.format( "username 'Auth' '%s'", escape( user ) ) ) );
			sendCommand( new SimpleCommand( String.format( "password 'Auth' '%s'", escape(password) ) ) );
		}
	}

	private String escape(String s) {
		return s.replace( "\\", "\\\\" ).replace("'", "\\'" );
	}

	
	private void sendCommand( Command command )
	{
		try
		{
			mReady.await();
			sendCommandNoLock(command);
		}
		catch (InterruptedException e)
		{
			//TODO: make exception checked
			throw new RuntimeException( e );
		}
	}

	private synchronized void sendCommandNoLock(Command command)
	{
		ms_PendingCommandFifo.add( command );
		out.println( command.getCommand() );
		out.flush();
	}
	
	
	/*
	 * ASYNCHRONOUS REAL-TIME MESSAGES
	 */

	private static final String RTMSG_ECHO = ">ECHO:";
	private static final String RTMSG_FATAL = ">FATAL:";
	private static final String RTMSG_HOLD = ">HOLD:";
	private static final String RTMSG_INFO = ">INFO:";
	private static final String RTMSG_LOG = ">LOG:";
	private static final String RTMSG_PASSWORD = ">PASSWORD:";
	private static final String RTMSG_STATE = ">STATE:";
	private static final String RTMSG_BYTECOUNT = ">BYTECOUNT:";

	private void handleRealtimeMessage(String line)
	{
		if ( !line.startsWith( ">" ) )
			throw new RuntimeException( "Not an asynchronus real-time message: " + line );
		
		else if ( line.startsWith( RTMSG_ECHO ) )
			onEcho(line);
		else if ( line.startsWith( RTMSG_FATAL ) )
			onFatal(line);
		else if ( line.startsWith( RTMSG_HOLD ) )
			onHold(line);
		else if ( line.startsWith( RTMSG_INFO ) )
			onInfo(line);
		else if ( line.startsWith( RTMSG_LOG ) )
			onLog(line);
		else if ( line.startsWith( RTMSG_PASSWORD ) )
			onPassword(line);
		else if ( line.startsWith( RTMSG_STATE ) )
			onState( line );
		else if ( line.startsWith( RTMSG_BYTECOUNT ) )
			onByteCount(line);
		
		else
			Log.w(mTAG_MT, "Unexpected real-time message: " + line );
	}

	private void onEcho(String line) {
		// TODO Auto-generated method stub		
		Log.d(mTAG_MT, line );
	}

	private void onFatal(String line) {
		// There is nothing we can do. OpenVPN will exit anyway. 
		Log.d(mTAG_MT, line );
	}

	private void onHold(String line) {
		// TODO Auto-generated method stub
		Log.d(mTAG_MT, line );
	}

	private void onInfo(String line) {
		// TODO Auto-generated method stub
		Log.d(mTAG_MT, line );
	}

	private void onLog(String line) {
		// TODO Auto-generated method stub
		Log.d(mTAG_MT, line );
	}

	private boolean mWaitingForPassphrase = false;
	private boolean mWaitingForUserPassword = false;

	private void onPassword(String line) {
		if ( line.equals( ">PASSWORD:Need 'Private Key' password" ) )
		{
			mWaitingForPassphrase = true;
			Notifications.sendPassphraseRequired(mDaemonMonitor.mNotificationId, mDaemonMonitor.mContext, mDaemonMonitor.mNotificationManager, mDaemonMonitor.mConfigFile);
		}
		else if ( line.equals( ">PASSWORD:Need 'Auth' username/password" ) )
		{
			mWaitingForUserPassword = true;
			Notifications.sendUsernamePasswordRequired(mDaemonMonitor.mNotificationId, mDaemonMonitor.mContext, mDaemonMonitor.mConfigFile, mDaemonMonitor.mNotificationManager);
		}
		else if ( line.equals( ">PASSWORD:Verification Failed: 'Private Key'" ) )
		{
			mWaitingForPassphrase = true;
			Notifications.sendPassphraseRequired(mDaemonMonitor.mNotificationId, mDaemonMonitor.mContext, mDaemonMonitor.mNotificationManager, mDaemonMonitor.mConfigFile);
		}
		else if ( line.equals( ">PASSWORD:Verification Failed: 'Auth'" ) )
		{
			mWaitingForUserPassword = true;
			Notifications.sendUsernamePasswordRequired(mDaemonMonitor.mNotificationId, mDaemonMonitor.mContext, mDaemonMonitor.mConfigFile, mDaemonMonitor.mNotificationManager);
		}
		else
		{
			Log.w(mTAG_MT, "unexpected 'PASSWORD:' notification" + line );
		}
	}

	private final static int STATE_FIELD_TIME = 0;
	private final static int STATE_FIELD_STATE = 1;
	private final static int STATE_FIELD_INFO0 = 2;
	private final static int STATE_FIELD_INFO1 = 3;
	private final static int STATE_FIELD_INFO2 = 4;
	
	private final static String STATE_CONNECTING = "CONNECTING";
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
		
		String fieldString = line.startsWith(RTMSG_STATE) ? line.substring( RTMSG_STATE.length() ) : line;
		String[] stateFields = fieldString.split( "," );
		String state = stateFields[STATE_FIELD_STATE];
		
		final int newState;
		String info0Extra = null;
		String info1Extra = null;
		String info2Extra = null;
		
		if (STATE_CONNECTING.equals(state)) {
			newState = Intents.NETWORK_STATE_CONNECTING;
		} else if (STATE_RECONNECTING.equals(state)) {
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
				mDaemonMonitor.mConfigFile.getAbsolutePath(),
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

		mDaemonMonitor.mContext.sendStickyBroadcast( intent );
		
		mCurrentState = newState;
		
		// notification
		if ( mWaitingForPassphrase || mWaitingForUserPassword ) {
			// noop,there is already a notification out there
		} else if (STATE_CONNECTED.equals(state)) {
			Notifications.notifyConnected( mDaemonMonitor.mNotificationId, mDaemonMonitor.mContext, mDaemonMonitor.mNotificationManager, mDaemonMonitor.mConfigFile, "Connected");
		} else if (STATE_EXITING.equals(state)) {
			Notifications.cancel( mDaemonMonitor.mNotificationId, mDaemonMonitor.mContext );
		} else {
			Notifications.notifyDisconnected( mDaemonMonitor.mNotificationId, mDaemonMonitor.mContext, mDaemonMonitor.mNotificationManager, mDaemonMonitor.mConfigFile, "Connecting");
		}
//		if ( mWaitingForPassphrase || mWaitingForUserPassword ) {
//			// noop,there is already a notification out there
//		} else if (STATE_CONNECTING.equals(state)) {
//			Notifications.notifyDisconnected( mContext, mConfigFile, "Connecting");
//		} else if (STATE_RECONNECTING.equals(state)) {
//			Notifications.notifyDisconnected( mContext, mConfigFile, "Reconnecting");
//		} else if (STATE_RESOLVE.equals(state)) {
//			Notifications.notifyDisconnected( mContext, mConfigFile, "Resolve");
//		} else if (STATE_WAIT.equals(state)) {
//			Notifications.notifyDisconnected( mContext, mConfigFile, "Wait");
//		} else if (STATE_AUTH.equals(state)) {
//			Notifications.notifyDisconnected( mContext, mConfigFile, "Auth");
//		} else if (STATE_GET_CONFIG.equals(state)) {
//			Notifications.notifyDisconnected( mContext, mConfigFile, "Get Config");
//		} else if (STATE_CONNECTED.equals(state)) {
//			Notifications.notifyConnected( mContext, mConfigFile, "Connected");
//		} else if (STATE_ASSIGN_IP.equals(state)) {
//			Notifications.notifyDisconnected( mContext, mConfigFile, "Assign IP");
//		} else if (STATE_ADD_ROUTES.equals(state)) {
//			Notifications.notifyDisconnected( mContext, mConfigFile, "Add Routes");
//		} else if (STATE_EXITING.equals(state)) {
//			Notifications.notifyDisconnected( mContext, mConfigFile, "Exiting");
//		} else {
//			//..
//		}
	}

	private void onByteCount(String line) {
		// TODO Auto-generated method stub
//		Log.d(mTAG_MT, line );
//		int startOfOut = line.indexOf(',');
//		int startOfIn = RTMSG_BYTECOUNT.length();
//		int in = Integer.parseInt( line.substring( startOfIn, startOfOut) );
//		int out = Integer.parseInt( line.substring( startOfOut+1) );
//		String msg = String.format( "in: %dytes - out: %dbytes", in, out );
//		Notifications.notifyBytes( mDaemonMonitor.mNotificationId, mDaemonMonitor.mContext, mDaemonMonitor.mNotificationManager, mDaemonMonitor.mConfigFile, msg );
		sendCommandNoLock( mStatusCommand );
	}

	/*
	 * broadcasting intents 
	 */
	
	private void networkStateChanged(int newState)
	{
		int oldState = mCurrentState;
		mCurrentState = newState;
		mDaemonMonitor.mContext.sendStickyBroadcast( Intents.networkStateChanged(
				mDaemonMonitor.mConfigFile.getAbsolutePath(),
				newState,
				oldState,
				System.currentTimeMillis()
		) );
	}
}
