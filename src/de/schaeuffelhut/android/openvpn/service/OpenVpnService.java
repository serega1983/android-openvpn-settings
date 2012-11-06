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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import de.schaeuffelhut.android.openvpn.Intents;
import de.schaeuffelhut.android.openvpn.Preferences;
import de.schaeuffelhut.android.openvpn.util.NetworkConnectivityListener;

/**
 * @author M.Sc. Friedrich Schäuffelhut
 *
 */
//TODO: expose interface through aidl
public class OpenVpnService extends Service
{
	private final static String TAG = "OpenVPN-ControlShell";
	
	/* This is a hack
	 * see http://www.mail-archive.com/android-developers@googlegroups.com/msg18298.html
	 * we are not really able to decide if the service was started.
	 * So we remember a week reference to it. We set it if we are running and clear it
	 * if we are stopped. If anything goes wrong, the reference will hopefully vanish
	 */	
	private static WeakReference<OpenVpnService> sRunningInstance = null;
	public final static boolean isServiceStarted()
	{
		final boolean isServiceStarted;
		if ( sRunningInstance == null )
		{
			isServiceStarted = false;
		}
		else if ( sRunningInstance.get() == null )
		{
			isServiceStarted = false;
			sRunningInstance = null;
		}
		else
		{
			isServiceStarted = true;
		}
		return isServiceStarted;
	}
	private void markServiceStarted(){
		sRunningInstance = new WeakReference<OpenVpnService>( this );
	}
	private void markServiceStopped(){
		sRunningInstance = null;
	}

	/*
	 * Service API
	 */

	private final OnSharedPreferenceChangeListenerImplementation onSharedPreferenceChangeListener = new OnSharedPreferenceChangeListenerImplementation();
	private final class OnSharedPreferenceChangeListenerImplementation implements SharedPreferences.OnSharedPreferenceChangeListener {
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
		{
			if ( Preferences.isConfigKey( key ) )
				handleConfigKey( key );
		}

		private void handleConfigKey(String key)
		{
			final File config = Preferences.configOf(key);
			if ( key.equals( Preferences.KEY_CONFIG_LOG_STDOUT_ENABLE( config ) ) )
				handleWriteLogFile( config, key );
		}

		private void handleWriteLogFile(File config, String key)
		{
			DaemonMonitor daemonMonitor = mRegistry.get( config );
			
			if ( daemonMonitor == null )
				return;
			if ( !daemonMonitor.isAlive() )
				return;
			
			if ( Preferences.getLogStdoutEnable( OpenVpnService.this, config ) )
				daemonMonitor.startLogging();
			else
				daemonMonitor.stopLogging();
		}
	}

	
	@Deprecated //TODO: aidl?
	public final class ServiceBinder extends Binder {
		public final OpenVpnService getService() {
            return OpenVpnService.this;
        }
	}

	private final IBinder mBinder = new ServiceBinder();
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		startup();

		PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(
				Preferences.KEY_OPENVPN_ENABLED, true
		).commit();
		
		markServiceStarted();

		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener( onSharedPreferenceChangeListener );
	}

    @Override
    //TODO: implement onStartCommand instead, when updated to API level 5 or higher.
    public void onStart(Intent intent, int startId)
    {
        super.onStart( intent, startId );
        if ( intent == null )
            return;
        Log.d(TAG, "onStart: " + intent.getAction() );
        if ( !intent.hasExtra( Intents.EXTRA_CONFIG ) )
            return;
        if ( Intents.START_DAEMON.equals( intent.getAction() ) )
            daemonStart( new File( intent.getStringExtra( Intents.EXTRA_CONFIG ) ) );
        else if ( Intents.STOP_DAEMON.equals( intent.getAction() ) )
            daemonStop( new File( intent.getStringExtra( Intents.EXTRA_CONFIG ) ) );
    }

    @Override
	public void onDestroy()
    {
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener( onSharedPreferenceChangeListener );

		markServiceStopped();

		PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(
				Preferences.KEY_OPENVPN_ENABLED, false
		).commit();

        shutdown();
	}

	
	/*
	 * ControlShell
	 */

	private NetworkConnectivityListener mConnectivity;
	
	private File mConfigDir;

	private final HashMap<File, DaemonMonitor> mRegistry = new HashMap<File, DaemonMonitor>(4);

    private DaemonMonitorFactory daemonMonitorFactory = new DaemonMonitorImplFactory( this );

    private synchronized void startup()
	{
		Log.i(TAG, "starting");

		daemonAttach();

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

		//TODO: introduce preference setting
		mConnectivity.startListening( getApplicationContext() );
	}
	
	private synchronized void shutdown()
	{
		Log.i(TAG, "shutting down");
				
		final ArrayList<DaemonMonitor> daemonMonitors = new ArrayList<DaemonMonitor>( mRegistry.values() );
		
		// sending shutdown signal to all running daemons
		for( DaemonMonitor daemonMonitor : daemonMonitors )
		{
			if( daemonMonitor.isAlive() )
			{
				daemonMonitor.stop();
//				try {
//					daemonMonitor.stopAndWaitForTermination();
//				} catch (InterruptedException e) {
//					Log.e(TAG, "shutdown", e);
//				}
			}
		}

		// wait for shutdown to finish
		for( DaemonMonitor daemonMonitor : daemonMonitors ){
			try {
				daemonMonitor.waitForTermination();
			} catch (InterruptedException e) {
				Log.e(TAG, "shutdown", e);
			}
		}

		mConnectivity.stopListening();
		mConnectivity = null;
	}

//	private synchronized void daemonAttach(File config, boolean start)
//	{
//		if ( isDaemonStarted(config) )
//		{
//			Log.v( TAG, config + ": is running and already attached" );
//		}
//		else if ( start && Preferences.getVpnDnsEnabled(this, config) && isVpnDnsActive() )
//		{
//			Log.i( TAG, config + " only one VPN DNS may be active at a time, aborting" );
//		}
//		else
//		{
//			Log.v(TAG, config +": trying to attach");
//
//            DaemonMonitor daemonMonitor = newDaemonMonitor( config );
//
//            if ( daemonMonitor.isAlive() ) // daemon was already running
//			{
//				Log.v(TAG, config +": successfully attached");
//				mRegistry.put( config, daemonMonitor );
//				if ( !start )
//				{
//					Log.v(TAG, config +": daemon is disabled in settings, stopping");
//					daemonStop( config );
//				}
//			}
//			else if ( start ) // daemon is not running, but should be started
//			{
//				Log.v(TAG, config +": not attached");
//				Log.v(TAG, config +": daemon is enabled in settings, starting");
//				daemonMonitor.start();
//				// ManagementThread might not be alive yet,
//				// so daemonMonitor.isAlive() might return false here.
//				mRegistry.put( config, daemonMonitor );
//			}
//			else
//			{
//				Log.v(TAG, config +": not attached");
//				Log.v(TAG, config +": daemon is disabled in settings, not starting");
//			}
//		}
//	}

    /**
	 * Try to attach to already running OpenVPN daemons, starting them if they
	 * are enabled.
	 */
	private final void daemonAttach()
	{
		Log.d( TAG, "trying to attach to already running daemons" );
//		for ( File config : listConfigs())
//			daemonAttach(
//					config,
//					PreferenceManager.getDefaultSharedPreferences( getApplicationContext() ).getBoolean(
//							Preferences.KEY_CONFIG_INTENDED_STATE( config ), false
//					)
//			);

        mRegistry.clear();
        DaemonMonitor daemonMonitor = new FindCurrentDaemon( this, daemonMonitorFactory, listConfigs() ).getTheOneRunningDaemonOrTheNullDaemonMonitor();
        setCurrent( daemonMonitor );

        // clean up sticky broadcasts
        //TODO: review this code it might not be correct
        Intent intent = registerReceiver( null, new IntentFilter( Intents.DAEMON_STATE_CHANGED ) );
        if (intent != null && !isDaemonStarted( new File( intent.getStringExtra( Intents.EXTRA_CONFIG ) ) ))
            newNotification2(
                    new File( intent.getStringExtra( Intents.EXTRA_CONFIG ) )
            ).daemonStateChangedToDisabled();
    }

    private void setCurrent(DaemonMonitor daemonMonitor)
    {
        for(Iterator<DaemonMonitor> it = mRegistry.values().iterator(); it.hasNext(); )
            if ( !it.next().isAlive() )
                it.remove();

        if ( !mRegistry.isEmpty() )
            throw new IllegalStateException( "Trying to register a second daemon!" );

        mRegistry.put( daemonMonitor.getConfigFile(), daemonMonitor );
    }

    DaemonMonitor getCurrent()
    {
        Iterator<DaemonMonitor> iterator = mRegistry.values().iterator();
        if ( iterator.hasNext() )
            return iterator.next();
        return NullDaemonMonitor.getInstance();
    }

    private Notification2 newNotification2(File config)
    {
        return new Notification2( this, config, Preferences.getNotificationId( this, config ) );
    }


    private DaemonMonitor newDaemonMonitor(File config)
    {
        return daemonMonitorFactory.createDaemonMonitorFor( config );
    }

    // hook to be overwritten in unit test
    protected List<File> listConfigs()
    {
        return Preferences.configs( this );
    }


    private final synchronized void daemonRestart()
    {
        for ( File config : listConfigs())
            if ( isDaemonStarted( config ) )
                daemonRestart( config );
    }

    private final synchronized void daemonRestart(File config)
    {
        if ( !isDaemonStarted(config) )
        {
            Log.i( TAG, config + " is not running" );
        }
        else
        {
            Log.i( TAG, config + " restarting" );
            DaemonMonitor monitor = mRegistry.get( config );
            monitor.restart();
        }
    }

    private final synchronized boolean isVpnDnsActive()
    {
        for( DaemonMonitor monitor : mRegistry.values() )
            if ( monitor.isVpnDnsActive() )
                return true;
        return false;
    }

    /* ========================================
     *          public API starts here
     * ========================================
     */

	public final synchronized void daemonStart(File config)
	{
        if ( getCurrent().isAlive() && !getCurrent().getConfigFile().equals( config ) )
        {
            Log.i( TAG, "Stopping current daemon " + getCurrent().getConfigFile() );
            getCurrent().stop();
            try
            {
                getCurrent().waitForTermination();
            }
            catch (InterruptedException e)
            {
            }
        }

		if ( isDaemonStarted(config) )
		{
			Log.i( TAG, config + " is already running" );
		}
		else if ( Preferences.getVpnDnsEnabled(this, config) && isVpnDnsActive() )
		{
			Log.i( TAG, config + " only one VPN DNS may be active at a time, aborting" );
			Toast.makeText( this , "VPN DNS is only supported in one tunnel!", Toast.LENGTH_LONG).show();
            newNotification2( config ).daemonStateChangedToDisabled();
		}
		else
		{
			DaemonMonitor daemonMonitor = newDaemonMonitor( config );
            setCurrent( daemonMonitor );
            daemonMonitor.start();
		}
	}

	public final synchronized void daemonStop(File config)
	{
		if ( !isDaemonStarted(config) )
		{
			Log.i( TAG, config + " is not running" );
		}
		else 
		{
			DaemonMonitor monitor = mRegistry.get( config );
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
			DaemonMonitor monitor = mRegistry.get( config );
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
			DaemonMonitor monitor = mRegistry.get( config );
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
			DaemonMonitor monitor = mRegistry.get( config );
			monitor.supplyUsernamePassword( username, password );
		}
	}

	public final synchronized boolean isDaemonStarted(File config)
	{
		return mRegistry.containsKey( config ) && mRegistry.get( config ).isAlive();
	}
	
	public final synchronized boolean hasDaemonsStarted()
	{
		for( DaemonMonitor monitor : mRegistry.values() )
			if ( monitor.isAlive() )
				return true;
		return false;
	}

    /**
     * Unit tests use this method to inject a different {@code DaemonMonitorFactory}.
     *
     * @param daemonMonitorFactory the {@code DaemonMonitorFactory} to be used.
     */
    void setDaemonMonitorFactory(DaemonMonitorFactory daemonMonitorFactory)
    {
        this.daemonMonitorFactory = daemonMonitorFactory;
    }

}