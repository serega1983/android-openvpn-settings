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
package de.schaeuffelhut.android.openvpn;

import java.io.File;
import java.util.HashMap;

import android.app.Service;
import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * @author M.Sc. Friedrich Schäuffelhut
 *
 */
public final class ControlShell extends Service
{
	final static String TAG = "OpenVPN-ControlShell";
	
	/*
	 * Service API
	 */
	
	public ControlShell() {
	}
	
	final class ServiceBinder extends Binder {
		ControlShell getService() {
            return ControlShell.this;
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
	private File mBinDir;
	
	private File binOpenvpn;
	private File binLibcrypto;
	private File binLiblzo;

	private final HashMap<String, DaemonMonitor> registry = new HashMap<String, DaemonMonitor>(4);
	
	private synchronized void startup()
	{
		Log.i(TAG, "starting");

		mConnectivity = new NetworkConnectivityListener();
		mConnectivity.registerHandler(new Handler(){
			boolean isFirstMessage = true;
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if ( !isFirstMessage ) // last message sticks around and causes an unnecessary restart
					daemonRestart();
				isFirstMessage = false;
			}
		}, 0);
		//mConnectivity.startListening() is called after installer is done.
		
		mConfigDir = new File( getApplicationContext().getFilesDir(), "config.d" );
		Log.d( TAG, "mConfigDir=" + mConfigDir );
		 
		mComDir = new File( getApplicationContext().getFilesDir(), "com.d" );
		if ( !mComDir.exists() )
			mComDir.mkdirs();
		Log.d( TAG, "mComDir=" + mComDir );

		mBinDir = new File( "/system/bin" );
//		mBinDir = new File( getApplicationContext().getFilesDir(), "bin" );
//		if ( !mBinDir.exists() )
//			mBinDir.mkdirs();
//		Log.d( TAG, "mBinDir=" + mBinDir );

		Log.d( TAG, "installing binaries" );
		binOpenvpn = new File( mBinDir, "openvpn");
		binLiblzo = new File( mBinDir, "liblzo.so");
		binLibcrypto = new File( mBinDir, "libcrypto.so");

		new HandlerThread( "OpenVPN-Installer" ) {
			@Override
			protected void onLooperPrepared()
			{
//				new Installer( getAssets(), binOpenvpn, binLibcrypto, binLiblzo ).installOpenVPN();
				
				daemonAttach();

				mConnectivity.startListening( getApplicationContext() );

				Toast.makeText(getApplicationContext(), "Installer finished", Toast.LENGTH_SHORT).show();
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

	final String[] configs()
	{
		File[] configFiles = new File(
				getApplicationContext().getFilesDir(),
				"config.d"
		).listFiles( new Util.FileExtensionFilter(".conf") );
		
		final int length = configFiles == null ? 0 : configFiles.length;
		
		String[] configFileName = new String[length];
		for (int i = 0; configFiles != null && i < length; i++)
			configFileName[i] = configFiles[i].getName();
		
		return configFileName;
	}
	
	synchronized void daemonAttach(String config, boolean start)
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
					binOpenvpn,
					new File( mConfigDir, config ),
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
	void daemonAttach()
	{
		Log.d( TAG, "trying to attach to already running daemons" );
		for (String config : configs() )
			daemonAttach(
					config,
					PreferenceManager.getDefaultSharedPreferences( getApplicationContext() ).getBoolean(
							OpenVPNSettings.KEY_CONFIG_ENABLED(config), false
					)
			);
	}

	synchronized void daemonStart(String config)
	{
		if ( isDaemonStarted(config) )
		{
			Log.i( TAG, config + " is already running" );
		}
		else
		{
			DaemonMonitor daemonMonitor = new DaemonMonitor(
					getApplicationContext(),
					binOpenvpn,
					new File( mConfigDir, config ),
					mComDir
			);
			daemonMonitor.start();
			registry.put( config, daemonMonitor );
		}
	}

	synchronized void daemonRestart(String config)
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

	synchronized void daemonRestart()
	{
		for (String config : configs() )
			if ( isDaemonStarted( config ) )
				daemonRestart( config );
	}
	
	synchronized void daemonStop(String config)
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

	synchronized boolean isDaemonStarted(String config)
	{
		return registry.containsKey( config ) && registry.get( config ).isAlive();
	}
	
	synchronized boolean hasDaemonsStarted()
	{
		for( DaemonMonitor monitor : registry.values() )
			if ( monitor.isAlive() )
				return true;
		return false;
	}
}