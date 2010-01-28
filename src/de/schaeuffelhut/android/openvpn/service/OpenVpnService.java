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
import java.util.HashMap;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import de.schaeuffelhut.android.openvpn.Preferences;
import de.schaeuffelhut.android.openvpn.util.NetworkConnectivityListener;

/**
 * @author M.Sc. Friedrich Schäuffelhut
 *
 */
//TODO: expose interface through aidl
public final class OpenVpnService extends Service
{
	final static String TAG = "OpenVPN-ControlShell";
	
	/*
	 * Service API
	 */
	
	public OpenVpnService() {
	}
	
	@Deprecated //TODO: aidl?
	public final class ServiceBinder extends Binder {
		public final OpenVpnService getService() {
            return OpenVpnService.this;
        }
	}

	final IBinder mBinder = new ServiceBinder();
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		startup();
	}

	
	@Override
	public void onDestroy() {
		super.onDestroy();
		shutdown();
	}

	
	/*
	 * ControlShell
	 */

	private NetworkConnectivityListener mConnectivity;
	
	private File mConfigDir;
	private File mComDir;	

	private final HashMap<File, DaemonMonitor> registry = new HashMap<File, DaemonMonitor>(4);
	
	private synchronized void startup()
	{
		Log.i(TAG, "starting");

		{
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

			mConfigDir = Preferences.getConfigDir(this, sharedPreferences);
			Log.d( TAG, "mConfigDir=" + mConfigDir );
			if ( mConfigDir == null )
				Log.w( TAG, "Missing path to configuration directory!" );
			if ( !mConfigDir.exists() )
				Log.w( TAG, "configuration directory not found: " + mConfigDir );
		}

		mComDir = new File( getFilesDir(), "com.d" );
		if ( !mComDir.exists() )
			mComDir.mkdirs();
		Log.d( TAG, "mComDir=" + mComDir );
		
		mConnectivity = new NetworkConnectivityListener();
		mConnectivity.registerHandler(new Handler(){
			boolean isFirstMessage = true;
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				//TODO: only restart if IP-address changed
				//TODO: optional, put on hold when on slow network, e.g. GPRs, make it a config option
				if ( !isFirstMessage ) // last message sticks around and causes an unnecessary restart
					daemonRestart();
				isFirstMessage = false;
			}
		}, 0);
		//mConnectivity.startListening() is called after installer is done.

		new HandlerThread( "OpenVPN-Attach" ) {
			@Override
			protected void onLooperPrepared()
			{
				daemonAttach();
				mConnectivity.startListening( getApplicationContext() );
			}
		}.start();
	}
	
	private synchronized void shutdown()
	{
		Log.i(TAG, "shuting down");
		
		mConnectivity.stopListening();
		mConnectivity = null;
		
//		// stop running configs
//		File[] configFiles = new File(
//				getApplicationContext().getFilesDir(),
//				"config.d"
//		).listFiles(new Util.FileExtensionFilter(".conf"));
//		for (int i = 0; configFiles != null && i < configFiles.length; i++)
//		{
//			final String configFileName = configFiles[i].getName();
//			if ( isDaemonStarted( configFileName ) )
//				daemonStop( configFileName );
//		}

//		wait for proceses we are still parents of 
	}

	synchronized void daemonAttach(File config, boolean start)
	{
		if ( isDaemonStarted(config) )
		{
			Log.v( TAG, config + ": is running and already attached" );
		}
		else
		{
			Log.v(TAG, config +": trying to attach");

			DaemonMonitor daemonMonitor = new DaemonMonitor(
					getApplicationContext(),
					config,
					mComDir
			);
			
			if ( daemonMonitor.isAlive() ) // daemon was already running
			{
				Log.v(TAG, config +": successfully attached");
				registry.put( config, daemonMonitor );
				if ( !start )
				{
					Log.v(TAG, config +": daemon is disabled in settings, stopping");
					daemonStop( config );
				}
			}
			else if ( start ) // daemon is not running, but should be started
			{
				Log.v(TAG, config +": not attached");
				Log.v(TAG, config +": daemon is enabled in settings, starting");
				daemonMonitor.start();
				// ManagementThread might not be alive yet,
				// so daemonMonitor.isAlive() might return false here.
				registry.put( config, daemonMonitor );
			}
			else
			{
				Log.v(TAG, config +": not attached");
				Log.v(TAG, config +": daemon is disabled in settings, not starting");
			}
		}
	}

	/**
	 * Try to attach to already running OpenVPN daemons, starting them if they
	 * are enabled.
	 */
	private final void daemonAttach()
	{
		Log.d( TAG, "trying to attach to already running daemons" );
		for ( File config : Preferences.configs(this) )
			daemonAttach(
					config,
					PreferenceManager.getDefaultSharedPreferences( getApplicationContext() ).getBoolean(
							Preferences.KEY_CONFIG_INTENDED_STATE( config ), false
					)
			);
	}

	public final synchronized void daemonStart(File config)
	{
		if ( isDaemonStarted(config) )
		{
			Log.i( TAG, config + " is already running" );
		}
		else
		{
			DaemonMonitor daemonMonitor = new DaemonMonitor(
					getApplicationContext(),
					config,
					mComDir
			);
			daemonMonitor.start();
			registry.put( config, daemonMonitor );
		}
	}

	public final synchronized void daemonRestart(File config)
	{
		if ( !isDaemonStarted(config) )
		{
			Log.i( TAG, config + " is not running" );
		}
		else 
		{
			Log.i( TAG, config + " restarting" );
			DaemonMonitor monitor = registry.get( config );
			monitor.restart();
		}
	}

	public final synchronized void daemonRestart()
	{
		for ( File config : Preferences.configs(this) )
			if ( isDaemonStarted( config ) )
				daemonRestart( config );
	}
	
	public final synchronized void daemonStop(File config)
	{
		if ( !isDaemonStarted(config) )
		{
			Log.i( TAG, config + " is not running" );
		}
		else 
		{
			DaemonMonitor monitor = registry.get( config );
			monitor.stop();
		}
	}

	public final synchronized void daemonQueryState(File config)
	{
		if ( !isDaemonStarted(config) )
		{
			Log.i( TAG, config + " is not running" );
		}
		else 
		{
			DaemonMonitor monitor = registry.get( config );
			monitor.queryState();
		}
	}

	public void daemonPassphrase(File config, String passphrase)
	{
		if ( !isDaemonStarted(config) )
		{
			Log.i( TAG, config + " is not running" );
		}
		else 
		{
			DaemonMonitor monitor = registry.get( config );
			monitor.supplyPassphrase( passphrase );
		}
	}

	public void daemonUsernamePassword(File config, String username, String password)
	{
		if ( !isDaemonStarted(config) )
		{
			Log.i( TAG, config + " is not running" );
		}
		else 
		{
			DaemonMonitor monitor = registry.get( config );
			monitor.supplyUsernamePassword( username, password );
		}
	}

	public final synchronized boolean isDaemonStarted(File config)
	{
		return registry.containsKey( config ) && registry.get( config ).isAlive();
	}
	
	public final synchronized boolean hasDaemonsStarted()
	{
		for( DaemonMonitor monitor : registry.values() )
			if ( monitor.isAlive() )
				return true;
		return false;
	}

}