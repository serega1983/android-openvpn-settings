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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import de.schaeuffelhut.android.openvpn.Intents;
import de.schaeuffelhut.android.openvpn.Notifications;
import de.schaeuffelhut.android.openvpn.Preferences;
import de.schaeuffelhut.android.openvpn.util.DnsUtil;
import de.schaeuffelhut.android.openvpn.util.Preconditions;
import de.schaeuffelhut.android.openvpn.util.Shell;
import de.schaeuffelhut.android.openvpn.util.SystemPropertyUtil;
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

	final OpenVpnService mContext;
	final NotificationManager mNotificationManager;
		
	final File mConfigFile;
	final File mPidFile;
	final int mNotificationId;
	final LogFile mLog;
	
	Shell mDaemonProcess;
	private ManagementThread mManagementThread;


	
	public DaemonMonitor(OpenVpnService context, File configFile, File comDir )
	{
		mContext = context;
		mConfigFile = configFile;
		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mLog = new LogFile( Preferences.logFileFor( configFile ) );
		 
		//TODO: need a unique config identifie, or remove pid writing fetaure
		mPidFile = new File( comDir, configFile.getAbsolutePath().replace( "_", "__").replace( '/', '_') + "-pid" );
		mTagDaemonMonitor = String.format("OpenVPN-DaemonMonitor[%s]", mConfigFile);
			
		mNotificationId = Preferences.getNotificationId( mContext, mConfigFile );
		
		reattach();
	}
	
	private boolean reattach()
	{
		mDaemonProcess = null;

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

		if ( !Preconditions.check( mContext ) ){
			mContext.sendStickyBroadcast( 
					Intents.daemonStateChanged(
							mConfigFile.getAbsolutePath(),
							Intents.DAEMON_STATE_DISABLED
					)
			);
			return;
		}
		
		final File openvpnBinary = Preferences.getPathToBinaryAsFile( PreferenceManager.getDefaultSharedPreferences( mContext ) );
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
		Preferences.setDns1( mContext, mConfigFile, 0, "" );

		final int mgmtPort = 10000 + (int)(Math.random() * 50000); 
		Log.w( mTagDaemonMonitor, "start(): choosing random port for management interface: " + mgmtPort );
		Preferences.setMgmtPort( mContext, mConfigFile, mgmtPort );
		
		if ( mPidFile.exists() )      mPidFile.delete();

		mContext.sendStickyBroadcast( 
				Intents.daemonStateChanged(
						mConfigFile.getAbsolutePath(),
						Intents.DAEMON_STATE_STARTUP
				)
		);
		
		if( !(Util.hasTunSupport()) ) // only load the driver if it's not yet available
		{
			if (Preferences.getDoModprobeTun( PreferenceManager.getDefaultSharedPreferences(mContext) ) )  // LATER remove the preferences setting
			{
				Shell insmod = new Shell( 
						mTagDaemonMonitor + "-daemon",
						Preferences.getLoadTunModuleCommand( PreferenceManager.getDefaultSharedPreferences(mContext) ),
						Shell.SU
				);
				insmod.start();
				try {
					insmod.join();
				} catch (InterruptedException e) {
					throw new RuntimeException( "waiting for insmod to finish", e );
				}
				
				if ( Util.hasTunSupport() )
				{
					shareTunModule();
				}
				else
				{
					Toast.makeText(mContext, "Failed to load tun module. Device node /dev/tun or dev/net/tun did not show up.", Toast.LENGTH_LONG).show();
					//TODO: bail out, dont start openvpn as tun support is not given
				}
			}
		}
		
		mDaemonProcess = new Shell( 
				mTagDaemonMonitor + "-daemon",
				String.format( 
						"%s --cd %s --config %s --writepid %s --script-security %d --management 127.0.0.1 %d --management-query-passwords --verb 3",
						openvpnBinary.getAbsolutePath(),				
						Util.shellEscape(mConfigFile.getParentFile().getAbsolutePath()),
						Util.shellEscape(mConfigFile.getName()),
						Util.shellEscape(mPidFile.getAbsolutePath()),
						Preferences.getScriptSecurityLevel( mContext, mConfigFile ),
						mgmtPort
				),
				Shell.SU
		){
			boolean waitForMgmt = true;

			protected void onBeforeExecute()
			{
				if ( Preferences.getLogStdoutEnable( mContext, mConfigFile ) )
					mLog.open();
			}

			@Override
			protected void onStdout(String line)
			{
				log( line );
				
				if ( waitForMgmt && line.indexOf( "MANAGEMENT: TCP Socket listening on" ) != -1 )
				{
					waitForMgmt = false;
					mManagementThread = new ManagementThread( DaemonMonitor.this );
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
			protected void onCmdTerminated()
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
				mDaemonProcess = null;
				mLog.close();
			}
		};
		mDaemonProcess.start();
	}

	private void shareTunModule()
	{
		if ( Preferences.isTunSharingExpired()  )
			return;
		
		if ( Preferences.getSendDeviceDetailWasSuccessfull( mContext ) )
			return;
		
		Notifications.sendShareTunModule(mContext, mNotificationManager);
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

}
