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
package de.schaeuffelhut.android.openvpn.service;

import java.io.File;
import java.io.IOException;

import android.util.Log;
import android.widget.Toast;
import de.schaeuffelhut.android.openvpn.IocContext;
import de.schaeuffelhut.android.openvpn.Preferences;
import de.schaeuffelhut.android.openvpn.util.Preconditions;
import de.schaeuffelhut.android.openvpn.util.Shell;
import de.schaeuffelhut.android.openvpn.util.Util;
import de.schaeuffelhut.android.openvpn.util.tun.TunInfo;

/**
 * Starts an OpenVPN process and monitors it until its death;
 * s
 * @author fries
 *
 */
public final class DaemonMonitor
{
	private final OpenVpnService mContext;
	private final File mConfigFile;
    private final Notification2 mNotification2;
    private final Preferences2 mPreferences2;

	private final LogFile mLog;
	final String mTagDaemonMonitor;

	private Shell mDaemonProcess;
	private ManagementThread mManagementThread;


    public DaemonMonitor(OpenVpnService context, File configFile, Notification2 notification2)
	{
		mContext = context;
		mConfigFile = configFile;
        mNotification2 = notification2;
        mPreferences2 = new Preferences2( this.mContext, this.mConfigFile );

        mLog = new LogFile( mPreferences2.logFileFor() );
		mTagDaemonMonitor = String.format("OpenVPN-DaemonMonitor[%s]", mConfigFile);

        reattach();
    }

    private boolean reattach()
	{
		mDaemonProcess = null;

        mManagementThread = new ManagementThread( DaemonMonitor.this, mNotification2, mPreferences2 );
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

		if ( !Preconditions.check( mContext ) ){
            mNotification2.daemonStateChangedToDisabled();
			return;
		}

        final File openvpnBinary = mPreferences2.getPathToBinaryAsFile();
		if ( openvpnBinary == null ) {
			Log.w( mTagDaemonMonitor, "start(): openvpn binary not found" );
			return;//TODO: send Intents.DAEMON_STATE_DISABLED
		}
		if ( !openvpnBinary.exists() ) {
			Log.w( mTagDaemonMonitor, "start(): file not found: " + openvpnBinary );
			return; //TODO: send Intents.DAEMON_STATE_DISABLED 
		}
		
		if ( !mConfigFile.exists() ) {
			Log.w( mTagDaemonMonitor, "start(): file not found: " + mConfigFile );
			return; //TODO: send Intents.DAEMON_STATE_DISABLED
		}

		// reset saved dns state
        mPreferences2.setDns1( 0, "" );

		final int mgmtPort = 10000 + (int)(Math.random() * 50000); 
		Log.w( mTagDaemonMonitor, "start(): choosing random port for management interface: " + mgmtPort );
        mPreferences2.setMgmtPort( mgmtPort );

        mNotification2.daemonStateChangedToStartUp();

        TunInfo tunInfo = IocContext.get().getTunInfo( mContext );
        if (!tunInfo.isDeviceNodeAvailable()) // only load the driver if it's not yet available
        {
            if (tunInfo.hasTunLoader())
            {
                tunInfo.getTunLoader().loadModule();

                if (tunInfo.isDeviceNodeAvailable())
                {
                    shareTunModule();
                }
                else
                {
                    Toast.makeText( mContext, "Failed to load tun module. Device node /dev/tun or /dev/net/tun did not show up.", Toast.LENGTH_LONG ).show();
                    //TODO: bail out, don't start openvpn as tun support is not given
                }
            }
            else
            {
                Toast.makeText( mContext, "No TUN loader defined.", Toast.LENGTH_LONG ).show();
                //TODO: bail out, don't start openvpn as tun support is not given
            }
        }

        mDaemonProcess = new Shell(
				mTagDaemonMonitor + "-daemon",
				String.format( 
						"%s --cd %s --config %s --script-security %d --management 127.0.0.1 %d --management-query-passwords --verb 3",
						openvpnBinary.getAbsolutePath(),				
						Util.shellEscape(mConfigFile.getParentFile().getAbsolutePath()),
						Util.shellEscape(mConfigFile.getName()),
                        mPreferences2.getScriptSecurityLevel(),
						mgmtPort
				),
				Shell.SU
		){
			boolean waitForMgmt = true;

			protected void onBeforeExecute()
			{
				if ( mPreferences2.getLogStdoutEnable() )
					mLog.open();
			}

			@Override
			protected void onStdout(String line)
			{
				log( line );
				
				if ( waitForMgmt && line.indexOf( "MANAGEMENT: TCP Socket listening on" ) != -1 )
				{
					waitForMgmt = false;
                    mManagementThread = new ManagementThread( DaemonMonitor.this, mNotification2, mPreferences2 );
					mManagementThread.start();
				}
			}

			private void log(String line)
			{
				/*
				 * This is the quick and dirty 80% logging solution. If for some
				 * reason OpenVPN Settings dies but the daemon continues to run,
				 * the ManagamentThread will attach to the already running
				 * openvpn daemon upon next start. But the log output can not be
				 * read because this Shell thread does not exist anymore.
				 * 
				 * This short coming could be overcome using the management
				 * interface. The disadvantage is that one has to deal with two
				 * log sources during the startup time. Before the management
				 * interface is ready STDOUT has to be read, later on the log
				 * source has to be switched. The STDOUT logger could listen to
				 * 'SUCCESS: real-time log notification set to ON' message and
				 * disable itself.
				 */
				mLog.append( line );
			}

			@Override
			protected void onExecuteFailed(IOException e)
			{
				onExit();
			}
			
			@Override
			protected void onCmdTerminated(int exitCode)
			{
				onExit();
			}

			private void onExit()
			{
				// while mManagementThread == null, system is in startup phase
				// and a DAEMON_STATE_DISABLED message is expected
				if ( mManagementThread == null )
                    mNotification2.daemonStateChangedToDisabled();
				mDaemonProcess = null;
				mLog.close();
			}
		};
		mDaemonProcess.start();
	}

    private void shareTunModule()
	{
		if (isTunSharingExpired())
			return;

        if ( mPreferences2.getSendDeviceDetailWasSuccessfull() )
			return;

        mNotification2.sendShareTunModule();
    }

    @Deprecated//TODO: move some where in TunSharing code
    private boolean isTunSharingExpired()
    {
        return Preferences.isTunSharingExpired();
    }

    void restart()
	{
		if ( !isAlive() )
		{
			Log.w( mTagDaemonMonitor, "Can't restart, daemon is not running!" );
		}
		else
		{
			mManagementThread.sendSignal( ManagementThread.SIGUSR1 );
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
			mManagementThread.sendSignal( ManagementThread.SIGTERM );
		}
	}

//	void stopAndWaitForTermination() throws InterruptedException
//	{
//		if ( !isAlive() )
//		{
//			Log.w( mTagDaemonMonitor, "Can't stop, daemon is not running!" );
//		}
//		else
//		{
//			mManagementThread.stopAndWaitForTermination();
//		}
//	}

	void waitForTermination() throws InterruptedException {
		if ( !isAlive() )
		{
			Log.w( mTagDaemonMonitor, "Can't wait for termination, daemon is already dead!" );
		}
		else
		{
			mManagementThread.mTerminated.await();
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
			mManagementThread.sendState();
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

	void startLogging() {
		Log.d( mTagDaemonMonitor, "Start logging" );
		mLog.open();
	}

	void stopLogging() {
		Log.d( mTagDaemonMonitor, "Stop logging" );
		mLog.close();
	}

    boolean isDaemonProcessAlive()
    {
        return mDaemonProcess != null && mDaemonProcess.isAlive();
    }

    public boolean getVpnDnsEnabled()
    {
        return mPreferences2.getVpnDnsEnabled();
    }
}
