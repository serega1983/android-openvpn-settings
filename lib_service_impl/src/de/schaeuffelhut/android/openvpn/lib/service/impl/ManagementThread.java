/*
 * This file is part of OpenVPN-Settings.
 *
 * Copyright © 2009-2012  Friedrich Schäuffelhut
 *
 * OpenVPN-Settings is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenVPN-Settings is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenVPN-Settings.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Report bugs or new features at: http://code.google.com/p/android-openvpn-settings/
 * Contact the author at:          android.openvpn@schaeuffelhut.de
 */
package de.schaeuffelhut.android.openvpn.lib.service.impl;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;
import de.schaeuffelhut.android.openvpn.Intents;
import de.schaeuffelhut.android.openvpn.service.api.OpenVpnPasswordRequest;
import de.schaeuffelhut.android.openvpn.shared.util.*;
import de.schaeuffelhut.android.openvpn.util.DnsUtil;
import de.schaeuffelhut.android.openvpn.util.TrafficStats;

final class ManagementThread extends Thread
{
	private final DaemonMonitor mDaemonMonitor;
    private final Notification mNotification2;
    private final LocalSocketAddress mMgmtSocket;
    private final Preferences2  mPreferences2;
	private final String mTAG_MT;
    private final IfConfigFactory mIfConfigFactory;
    private IfConfig ifConfig;

    ManagementThread(DaemonMonitorImpl daemonMonitor, Notification notification2, Preferences2 preferences2, LocalSocketAddress mgmtSocket, IfConfigFactory ifConfigFactory)
	{
		mDaemonMonitor = daemonMonitor;
        mNotification2 = notification2;
        mPreferences2 = preferences2;
		mTAG_MT = daemonMonitor.mTagDaemonMonitor + "-mgmt";
        mMgmtSocket = mgmtSocket;
        mIfConfigFactory = ifConfigFactory;
        ifConfig = mIfConfigFactory.createIfConfig();
    }
	
	private final CountDownLatch mReadyForCommands = new CountDownLatch(1);
	final CountDownLatch mTerminated = new CountDownLatch(1);
	
	private final LinkedList<Command> ms_PendingCommandFifo = new LinkedList<Command>();

    private LocalSocket mSocket = null;
	private PrintWriter mOut = null;
	private int mCurrentState = Intents.NETWORK_STATE_UNKNOWN;

	private final Command mRealtimeMessageHandler = new RealTimeMessageHandler();
	private final StatusCommand mStatusCommand = new StatusCommand();

	// TODO: access should be synchronized
	private boolean mWaitingForPassphrase = false;
	private boolean mWaitingForUserPassword = false;


	@Override
	public void run()
	{
		Log.d( mTAG_MT, "started");

		// try to attach to OpenVPN management interface port,
		// keep trying while startup shell is alive 
		boolean attached;
        for(int i=0; !(attached = attach()) && mDaemonMonitor.isDaemonProcessAlive() && i < 10; i++ )
		{
			try {sleep(1000);} catch (InterruptedException e) {}
		}

		try
		{
			if ( attached )
			{
				Log.v(mTAG_MT, "Successfully attached to OpenVPN monitor port");
                mNotification2.daemonStateChangedToEnabled();
                monitor();
			}
			else
			{
				Log.v(mTAG_MT, "Could not attach to OpenVPN monitor port");
			}
		}
		finally
		{
            mNotification2.daemonStateChangedToDisabled();

            mNotification2.cancel();

            mTerminated.countDown();
			
			Log.d( mTAG_MT, "terminated" );
		}
	}

    boolean attach()
	{
		if ( mMgmtSocket == null )
		{
			Log.d( mTAG_MT, "attach(): unspecified management port, not attaching" );
		}
		else if ( mSocket == null || !mSocket.isConnected() )
		{
    		Log.d( mTAG_MT, "attach(): using management port at " + mMgmtSocket );
			try
			{
                mSocket = new LocalSocket();
                mSocket.connect( mMgmtSocket );
			}
			catch (IOException e)
			{
				mSocket = null;
				Log.e( mTAG_MT, "attaching to OpenVPN daemon: " + e.getMessage() );
			}
		}
		return mSocket != null && mSocket.isConnected();
	}

    private void monitor()
	{
		LineNumberReader lnr = null;
		try {
			mOut = new PrintWriter( mSocket.getOutputStream() );

			lnr = new LineNumberReader(
					new InputStreamReader( mSocket.getInputStream() ),
					128
			);

			Log.v(mTAG_MT, "Socket IO established");

			networkStateChanged( Intents.NETWORK_STATE_CONNECTING );

			sendCommandImmediately( new StateCommand() );
			sendCommandImmediately( new SimpleCommand( "state on" ) );
			
			// allow other threads to submit commands
			mReadyForCommands.countDown(); 
			
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
			Util.closeQuietly( mOut );
			Util.closeQuietly( mSocket );
			
			// make sure nobody is waiting for us
			mReadyForCommands.countDown();
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
		lnr.mark(2);
		boolean eof = lnr.read() == -1;
		if ( !eof )
			lnr.reset();
		return !eof;
	}

	/**
	 * Retrieve the next pending command and call its response handler. If no
	 * command is available delegate to mRealTimeMessageHandler to just handle
	 * asynchronous real-time message.
	 * 
	 * @param lnr The {@link LineNumberReader} to read the response from.
	 * @throws IOException
	 */
	private final synchronized void handleResponse(LineNumberReader lnr) throws IOException
	{
		Command command;
		if ( ms_PendingCommandFifo.isEmpty() )
			command = mRealtimeMessageHandler;
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

	/**
	 * {@link RealTimeMessageHandler} is a no operation command only used to
	 * handle asynchronous real-time messages in case there is no pending
	 * command in the fifo.
	 * 
	 * @author Friedrich Schäuffelhut
	 */
	private final class RealTimeMessageHandler extends Command
	{
		RealTimeMessageHandler() {
			super( false, false );
		}
		@Override final String getCommand() {
			throw new UnsupportedOperationException(); // could also send "" as this should not harm the protocol.
		}
	}

	/**
	 * SimpleCommand encapsulates all those commands only returning SUCESS or
	 * ERROR. The response is ignored.
	 * 
	 * @author Friedrich Schäuffelhut
	 */
	private final class SimpleCommand extends Command
	{
		final String command;
		SimpleCommand(String command) {
			super(true, false);
			this.command = command;
		}
		
		@Override public String getCommand()
		{
			return command;
		}
	}

	private final class StateCommand extends Command
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

	
	private final class StatusCommand extends Command
	{
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
            mNotification2.notifyBytes( trafficStats.toSmallInOutPerSecString(), mTrafficStats.getTuntapReadBytes(), mTrafficStats.getTuntapWriteBytes() );
        }
	}

	/*
	 * Send commands to management interface, may be invoked by any thread.
	 */

	/**
	 * Send 'state' command to OpenVPN daemon. This method is thread safe.
	 */
	public void sendState()
	{
		sendCommand( new StateCommand() );
	}
	
	final static int SIGHUP = 1;
	final static int SIGTERM = 2;
	final static int SIGUSR1 = 3;
	final static int SIGUSR2 = 4;

	/**
	 * Send signal s to OpenVPN daemon. This method is thread safe.
	 * 
	 * @param s
	 *            SIGHUP|SIGTERM|SIGUSR1|SIGUSR2
	 */
	public void sendSignal(int s)
	{
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

	/**
	 * Send given passphrase in response to a passphrase request. This method is
	 * thread safe.
	 * 
	 * @param passphrase
	 */
	final void sendPassphrase(String passphrase)
	{
		if ( passphrase == null )
			Log.w(mTAG_MT, "Won't send <null> as passphrase to openvpn daemon!");
		else if ( !mWaitingForPassphrase )
			Log.w(mTAG_MT, "Won't send unexpected passphrase to openvpn daemon!");
		else
		{
			mWaitingForPassphrase = false;
			sendCommand( new SimpleCommand( String.format("password 'Private Key' '%s'", escape( passphrase ) ) ) );
		}
	}

	/**
	 * Send given user and password in response to a password request. This method is
	 * thread safe.
	 * 
	 * @param user
	 * @param password
	 */
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

	/**
	 * Waits for the management thread to come ready, then calls
	 * sendCommandImmediately to enqueue the given command. This method may be
	 * invoked by any thread besides the management thread. This method is
	 * thread safe.
	 * 
	 * @param command
	 *            The command to be issued.
	 */
	private void sendCommand( Command command )
	{
		try
		{
			mReadyForCommands.await();
			sendCommandImmediately(command);
		}
		catch (InterruptedException e)
		{
			//TODO: make exception checked
			throw new RuntimeException( e );
		}
	}

	/**
	 * Enqueues the given command in the fifo and prints it to the management
	 * interface. The command is sent immediately, This method should only be
	 * invoked from within the management thread itself or through
	 * sendCommand(). This method is thread safe.
	 * 
	 * @param command
	 *            The command to be issued.
	 */
	private synchronized void sendCommandImmediately(Command command)
	{
		ms_PendingCommandFifo.add( command );
		mOut.println( command.getCommand() );
		mOut.flush();
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
	private static final String RTMSG_NEED_OK = ">NEED-OK:";

	/**
	 * Dispatch given message to its message handler.
	 * 
	 * @param msg
	 *            The real-time message to dispatch.
	 */
	private void handleRealtimeMessage(String msg)
	{
		if ( !msg.startsWith( ">" ) )
			throw new RuntimeException( "Not an asynchronus real-time message: " + msg );
		
		else if ( msg.startsWith( RTMSG_ECHO ) )
			onEcho(msg);
		else if ( msg.startsWith( RTMSG_FATAL ) )
			onFatal(msg);
		else if ( msg.startsWith( RTMSG_HOLD ) )
			onHold(msg);
		else if ( msg.startsWith( RTMSG_INFO ) )
			onInfo(msg);
		else if ( msg.startsWith( RTMSG_LOG ) )
			onLog(msg);
		else if ( msg.startsWith( RTMSG_PASSWORD ) )
			onPassword(msg);
		else if ( msg.startsWith( RTMSG_STATE ) )
			onState( msg );
		else if ( msg.startsWith( RTMSG_BYTECOUNT ) )
			onByteCount(msg);
		else if ( msg.startsWith( RTMSG_NEED_OK ) )
			onNeedOk( msg );

		else
			Log.w(mTAG_MT, "Unexpected real-time message: " + msg );
	}

	private void onEcho(String line) {
		// TODO: implement echo handling, e.g. reading DNS settings etc...		
		Log.d(mTAG_MT, line );
	}

	private void onFatal(String line) {
		// There is nothing we can do. OpenVPN will exit anyway. 
		Log.d(mTAG_MT, line );
        mNotification2.toastMessage( line.substring( 1 ) );
        //TODO: use handler to communicate back to context
	}

	private void onHold(String line) {
		// TODO: implement on hold strategy
		Log.d(mTAG_MT, line );
	}

	private void onInfo(String line) {
		// TODO: read version of management interface if necessary
		Log.d(mTAG_MT, line );
	}

	private void onLog(String line) {
		// TODO: send log to GUI if requested
		Log.d(mTAG_MT, line );
	}

	private void onPassword(String line) {
		if ( line.equals( ">PASSWORD:Need 'Private Key' password" ) )
		{
			mWaitingForPassphrase = true;
            if ( mPreferences2.hasPassphrase() )
                sendPassphrase( mPreferences2.getPassphrase() );
            else
                mNotification2.sendPassphraseRequired();
        }
		else if ( line.equals( ">PASSWORD:Need 'Auth' username/password" ) )
		{
			mWaitingForUserPassword = true;
            if ( mPreferences2.hasCredentials() )
                sendUserPassword( mPreferences2.getUsername(), mPreferences2.getPassword() );
            else
                mNotification2.sendUsernamePasswordRequired();
        }
		else if ( line.equals( ">PASSWORD:Verification Failed: 'Private Key'" ) )
		{
			mWaitingForPassphrase = true;
            mNotification2.sendPassphraseRequired();
        }
		else if ( line.equals( ">PASSWORD:Verification Failed: 'Auth'" ) )
		{
			mWaitingForUserPassword = true;
            mNotification2.sendUsernamePasswordRequired();
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

		final String fieldString = line.startsWith(RTMSG_STATE) ? line.substring( RTMSG_STATE.length() ) : line;
		final String[] stateFields = TextUtils.split( fieldString, "," ); // TODO: put under test, make sure array size is fixed and empty strings are returend
		final String state = stateFields[STATE_FIELD_STATE];

		if (STATE_CONNECTED.equals(state)) {
			onConnected();
		} else {
			onDisconnected();
		}

        final int newState;
        {
            String info0ExtraName  = null;
            String info0ExtraValue = null;
            String info1ExtraName  = null;
            String info1ExtraValue = null;
            String info2ExtraName  = null;
            String info2ExtraValue = null;

            //TODO: introduce STATE enum, then Replace Conditional with Polymorphism
            if (STATE_CONNECTING.equals( state )) {
                newState = Intents.NETWORK_STATE_CONNECTING;
            } else if (STATE_RECONNECTING.equals( state )) {
                newState = Intents.NETWORK_STATE_RECONNECTING;
                info0ExtraName = Intents.EXTRA_NETWORK_CAUSE;
                info0ExtraValue = stateFields[STATE_FIELD_INFO0];
            } else if (STATE_RESOLVE.equals( state )) {
                newState = Intents.NETWORK_STATE_RESOLVE;
            } else if (STATE_WAIT.equals( state )) {
                newState = Intents.NETWORK_STATE_WAIT;
            } else if (STATE_AUTH.equals( state )) {
                newState = Intents.NETWORK_STATE_AUTH;
            } else if (STATE_GET_CONFIG.equals( state )) {
                newState = Intents.NETWORK_STATE_GET_CONFIG;
            } else if (STATE_CONNECTED.equals( state )) {
                newState = Intents.NETWORK_STATE_CONNECTED;
                info1ExtraName = Intents.EXTRA_NETWORK_LOCALIP;
                info1ExtraValue = stateFields[STATE_FIELD_INFO1];
                info2ExtraName = Intents.EXTRA_NETWORK_REMOTEIP;
                info2ExtraValue = stateFields[STATE_FIELD_INFO2];
            } else if (STATE_ASSIGN_IP.equals( state )) {
                newState = Intents.NETWORK_STATE_ASSIGN_IP;
                info1ExtraName = Intents.EXTRA_NETWORK_LOCALIP;
                info1ExtraValue = stateFields[STATE_FIELD_INFO1];
            } else if (STATE_ADD_ROUTES.equals( state )) {
                newState = Intents.NETWORK_STATE_ADD_ROUTES;
            } else if (STATE_EXITING.equals( state )) {
                newState = Intents.NETWORK_STATE_EXITING;
                info0ExtraName = Intents.EXTRA_NETWORK_CAUSE;
                info0ExtraValue = stateFields[STATE_FIELD_INFO0];
            } else {
                Log.d( mTAG_MT, "unknown state: " + state );
                newState = Intents.NETWORK_STATE_UNKNOWN;
            }

            final long time = Long.parseLong( stateFields[STATE_FIELD_TIME] ) * 1000;

            mNotification2.networkStateChanged(
                    mCurrentState, newState, time,
                    info0ExtraName, info0ExtraValue,
                    info1ExtraName, info1ExtraValue,
                    info2ExtraName, info2ExtraValue
            );
        }

        mCurrentState = newState;
		
		// notification
		if ( mWaitingForPassphrase || mWaitingForUserPassword ) {
			// noop,there is already a notification out there
		} else if (STATE_CONNECTED.equals(state)) {
            mNotification2.notifyConnected();
        } else if (STATE_EXITING.equals(state)) {
            mNotification2.cancel();
        } else {
            mNotification2.notifyDisconnected();
        }
	}

	// invoked through onState
	private void onConnected()
	{
		// activate traffic statistics  TODO chri - option in settings to activate/deactivate traffic stats 
		sendCommandImmediately( new SimpleCommand( String.format("bytecount %d", TrafficStats.mPollInterval )) );
		
		// change the DNS server if necessary
        String vpnDns = mPreferences2.getVpnDns();
        boolean enabled = mPreferences2.getVpnDnsEnabled();

		if ( enabled && TextUtils.isEmpty( vpnDns ) )
		{
			Log.i(mTAG_MT, "Can not set VPN DNS: No DNS Server configured!" );
		}
		else if ( enabled && !TextUtils.isEmpty( vpnDns ) )
		{
			HashMap<String, String> properties = SystemPropertyUtil.getProperties();

			Integer systemDnsChange = SystemPropertyUtil.getIntProperty( SystemPropertyUtil.NET_DNSCHANGE );
            int myDnsChange = mPreferences2.getDnsChange();
			Log.d(mTAG_MT, "=============> " + myDnsChange  + " == " + systemDnsChange );
			if ( systemDnsChange == null )
			{
				// DNS is not yet setup, leave it alone
				Log.d(mTAG_MT, "=============> applying new dns server, dns subsystem was not yet activated" );
			}
			else if ( systemDnsChange.intValue() == myDnsChange  )
			{
				// Our DNS is already active, leave it alone
				Log.d(mTAG_MT, "=============> applying new dns server, already set" );
			}
			else
			{
				// Apply our DNS settings
				Log.d(mTAG_MT, "=============> applying new dns server" );
				Integer newDnsChange = DnsUtil.setDns1( vpnDns );
                mPreferences2.setDns1( newDnsChange, properties.get( SystemPropertyUtil.NET_DNS1 ) );
            }
		}
		//Fix HTC routes if needed
        if( mPreferences2.getFixHtcRoutes() )
			updateHTCRoutes();
		
	}

    // invoked through onState
	private void onDisconnected()
	{
		// stop traffic statistics  TODO chri - option in settings to activate/deactivate traffic stats 
		sendCommandImmediately( new SimpleCommand( "bytecount 0") );
		
		Integer systemDnsChange = SystemPropertyUtil.getIntProperty( SystemPropertyUtil.NET_DNSCHANGE );
        int myDnsChange = mPreferences2.getDnsChange();
		if ( systemDnsChange == null )
		{
			// DNS is not yet setup, leave it alone
			Log.d(mTAG_MT, "=============> applying new dns server, dns subsystem was not yet activated" );
		}
		else if ( systemDnsChange.intValue() == myDnsChange )
		{
			// This is our change, revert it.
			Log.d(mTAG_MT, "=============> " + myDnsChange  + " == " + systemDnsChange + " resetting dns" );
            DnsUtil.setDns1( mPreferences2.getDns1() );
		}
		else
		{
			// Someone else has already changed the DNS, leave it alone.
			Log.d(mTAG_MT, "=============> " + myDnsChange  + " == " + systemDnsChange + " resetting dns, leaving dns alone" );
		}
	}

    TrafficStats mTrafficStats;
	private void onByteCount(String line) {
		int startOfOut = line.indexOf(',');
		int startOfIn = RTMSG_BYTECOUNT.length();
		long in = Long.parseLong( line.substring( startOfIn, startOfOut) ); // in the tunnel
		long out = Long.parseLong( line.substring( startOfOut+1) ); // out of the tunnel
		if (mTrafficStats==null) mTrafficStats = new TrafficStats();
		mTrafficStats.setStats(out, in);
		String msg = mTrafficStats.toSmallInOutPerSecString();
		Log.d(mTAG_MT, msg );
        mNotification2.notifyBytes( msg, mTrafficStats.getTuntapReadBytes(), mTrafficStats.getTuntapWriteBytes() );
    }

    private void onNeedOk(String line)
    {
        if (line.equals( ">NEED-OK:Need 'PROTECTFD' confirmation MSG:protect_fd_nonlocal" ))
        {
            FileDescriptor[] fds;
            try
            {
                fds = mSocket.getAncillaryFileDescriptors();
            }
            catch (IOException e)
            {
                Log.e( mTAG_MT, "Error reading fds from socket", e );
                //TODO: notify user, explain cause
                sendSignal( SIGTERM );
                fds = null;
            }
            if (fds == null)
            {
                Log.e( mTAG_MT, "No fds received from socket" );
                //TODO: notify user, explain cause
                sendSignal( SIGTERM );
                return;
            }

            for (FileDescriptor fd : fds)
            {
                ifConfig.protect( fd );
                JniUtil.closeQuietly( fd );
            }

            sendCommand( new SimpleCommand( "needok 'PROTECTFD' ok" ) );
        }
        // NEED-OK:Need 'IFCONFIG' confirmation MSG:10.0.0.1 255.255.255.0 1500 subnet
        else if (line.startsWith( ">NEED-OK:Need 'IFCONFIG' confirmation MSG:" ))
        {
            String msg = line.substring( ">NEED-OK:Need 'IFCONFIG' confirmation MSG:".length() );
            ifConfig.setIfconfig( msg );

            sendCommand( new SimpleCommand( "needok 'IFCONFIG' ok" ) );
        }
        //>NEED-OK:Need 'ROUTE' confirmation MSG:10.0.0.0 255.0.0.0
        else if (line.startsWith( ">NEED-OK:Need 'ROUTE' confirmation MSG:" ))
        {
            String msg = line.substring( ">NEED-OK:Need 'ROUTE' confirmation MSG:".length() );
            ifConfig.setRoute( msg );

            sendCommand( new SimpleCommand( "needok 'ROUTE' ok" ) );
        }
        //>NEED-OK:Need 'DNSSERVER' confirmation MSG:8.8.8.8
        else if (line.startsWith( ">NEED-OK:Need 'DNSSERVER' confirmation MSG:" ))
        {
            String msg = line.substring( ">NEED-OK:Need 'DNSSERVER' confirmation MSG:".length() );
            ifConfig.setDnsServer( msg );

            sendCommand( new SimpleCommand( "needok 'DNSSERVER' ok" ) );
        }
        //>NEED-OK:Need 'OPENTUN' confirmation MSG:tun0
        else if (line.startsWith( ">NEED-OK:Need 'OPENTUN' confirmation MSG:" ))
        {
            //TODO: ignore tun device name
            //TODO: assert not TAP
            ParcelFileDescriptor pfd = ifConfig.establish();
            if (pfd == null)
            {
                sendCommand( new SimpleCommand( "needok 'OPENTUN' cancel" ) );
            }
            else
            {
                mSocket.setFileDescriptorsForSend( new FileDescriptor[]{
                        pfd.getFileDescriptor()
                } );

                sendCommand( new SimpleCommand( "needok 'OPENTUN' ok" ) );

                // The API documentation fails to mention that it will not reset the file descriptor to
                // be send and will happily send the file descriptor on every write ...
                mSocket.setFileDescriptorsForSend( null );

                try
                {
                    pfd.close();
                }
                catch (IOException e)
                {
                    Log.e( mTAG_MT, "Failed to close our side of tun fd" );
                }
            }
        }
        else
        {
            Log.w( mTAG_MT, "unexpected 'NEED-OK:' notification: " + line );
        }
    }


    /*
	 * broadcasting intents 
	 */
	
	private void networkStateChanged(int newState)
	{
        int oldState = mCurrentState;
        mCurrentState = newState;
        mNotification2.networkStateChanged( oldState, newState );
    }

    // see issue #35: http://code.google.com/p/android-openvpn-settings/issues/detail?id=35
	protected void updateHTCRoutes()
	{
		new Shell( 
				"OpenVPN-Settings-ip-route",
				"ip ru del table wifi",
				Shell.SU
		).run();
		
		new Shell( 
				"OpenVPN-Settings-ip-route",
				"ip ru del table gprs",
				Shell.SU
		).run();
	}

    OpenVpnPasswordRequest getPasswordRequest()
    {
        if ( mWaitingForPassphrase )
            return OpenVpnPasswordRequest.PASSPHRASE;
        if ( mWaitingForUserPassword )
            return  OpenVpnPasswordRequest.CREDENTIALS;
        return OpenVpnPasswordRequest.NONE;
    }

}
