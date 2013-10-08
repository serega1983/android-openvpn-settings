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

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.*;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import de.schaeuffelhut.android.openvpn.Intents;
import de.schaeuffelhut.android.openvpn.Preferences;
import de.schaeuffelhut.android.openvpn.service.api.*;
import de.schaeuffelhut.android.openvpn.shared.util.NetworkConnectivityListener;
import de.schaeuffelhut.android.openvpn.shared.util.apilevel.ApiLevel;
import de.schaeuffelhut.android.openvpn.shared.util.service.ServiceDelegate;

/**
 * @author M.Sc. Friedrich Schäuffelhut
 *
 */
//TODO: expose interface through aidl
public class OpenVpnServiceImpl implements ServiceDelegate
{
    /*
     Delegate to Service object.
     */
    private final Service mService;

    public OpenVpnServiceImpl(Service service, IfConfigFactory ifConfigFactory, CmdLineBuilder cmdLineBuilder)
    {
        this.mService = service;
        this.daemonMonitorFactory = new DaemonMonitorImplFactory( getContext(), listenerDispatcher, ifConfigFactory, cmdLineBuilder );
    }

    private Service getService()
    {
        return mService;
    }

    private Context getContext()
    {
        return getService();
    }

    private Context getApplicationContext2()
    {
        return getService().getApplicationContext();
    }




	private final static String TAG = "OpenVPN-ControlShell";
	
	/* This is a hack
	 * see http://www.mail-archive.com/android-developers@googlegroups.com/msg18298.html
	 * we are not really able to decide if the service was started.
	 * So we remember a week reference to it. We set it if we are running and clear it
	 * if we are stopped. If anything goes wrong, the reference will hopefully vanish
	 */	
	private static WeakReference<OpenVpnServiceImpl> sRunningInstance = null;
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
		sRunningInstance = new WeakReference<OpenVpnServiceImpl>( this );
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
			
			if ( Preferences.getLogStdoutEnable( getContext(), config ) )
				daemonMonitor.startLogging();
			else
				daemonMonitor.stopLogging();
		}
	}


    public final class ServiceBinder extends IOpenVpnService.Stub
    {
        private Handler handler = new Handler( Looper.getMainLooper() );


//        public final OpenVpnServiceImpl getService()
//        {
//            return OpenVpnServiceImpl.this;
//        }

        public void connect(final OpenVpnConfig config) throws RemoteException
        {
            handler.post( new Runnable()
            {
                public void run()
                {
                    daemonStart( config );
                }
            } );
        }

        public void supplyCredentials(final OpenVpnCredentials credentials) throws RemoteException
        {
            handler.post( new Runnable()
            {
                public void run()
                {
                    daemonUsernamePassword( getCurrent().getConfigFile(), credentials.getUsername(), credentials.getPassword() );
                }
            } );
        }

        public void supplyPassphrase(final OpenVpnPassphrase passphrase) throws RemoteException
        {
            handler.post( new Runnable()
            {
                public void run()
                {
                    daemonPassphrase( getCurrent().getConfigFile(), passphrase.getPassphrase() );
                }
            } );
        }

        public OpenVpnState getStatus() throws RemoteException
        {
            return OpenVpnState.fromStickyBroadcast( getContext(), getCurrent().getPasswordRequest() );
        }

        public OpenVpnState getStatusFor(OpenVpnConfig config) throws RemoteException
        {
            if ( getCurrent().getConfigFile().equals( config.getFile() ) )
                return OpenVpnState.fromStickyBroadcast( getContext(), getCurrent().getPasswordRequest() );
            else
                return OpenVpnState.stopped();
        }

        public void disconnect() throws RemoteException
        {
            handler.post( new Runnable()
            {
                public void run()
                {
                    daemonStop( getCurrent().getConfigFile() );
                }
            } );
        }

        public void addOpenVpnStateListener(IOpenVpnStateListener listener) throws RemoteException
        {
            listenerDispatcher.addOpenVpnStateListener( listener );
        }

        public void removeOpenVpnStateListener(IOpenVpnStateListener listener)
        {
            listenerDispatcher.removeOpenVpnStateListener( listener );
        }

        /**
         * Intercept remote method calls and check for "onRevoke" code which
         * is represented by IBinder.LAST_CALL_TRANSACTION. If onRevoke message
         * was received, call onRevoke() otherwise delegate to super implementation.
         */
        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException
        {
            if (ApiLevel.get().hasVpnService() && isVpnServiceOnRevoke( code ))
            {
                //TODO: only allow this method call when this is a pure android VpnService
                //TODO: When this service runs on API14 in rooted mode, do not call onRevoke()
                onRevoke();
                return true;
            }
            return super.onTransact( code, data, reply, flags );
        }

        private void onRevoke()
        {
            getCurrent().stop();
        }

        private boolean isVpnServiceOnRevoke(int code)
        {
            // see Implementation of android.net.VpnService.Callback.onTransact()
            // http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/4.2.2_r1/android/net/VpnService.java/#VpnService.onRevoke%28%29
            return code == IBinder.LAST_CALL_TRANSACTION;
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
//		super.onCreate();

		startup();

		PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(
				Preferences.KEY_OPENVPN_ENABLED, true
		).commit();

        getService().sendBroadcast( new Intent( de.schaeuffelhut.android.openvpn.service.api.Intents.OPENVPN_STATE_CHANGED.getAction() ) );

		markServiceStarted();

		PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener( onSharedPreferenceChangeListener );
	}

    @Override
    //TODO: implement onStartCommand instead, when updated to API level 5 or higher.
    public void onStart(Intent intent, int startId)
    {
//        super.onStart( intent, startId );
        if ( intent == null )
            return;
        Log.d(TAG, "onStart: " + intent.getAction() );
        if ( !intent.hasExtra( Intents.EXTRA_CONFIG ) )
            return;
        if ( Intents.START_DAEMON.equals( intent.getAction() ) )
            daemonStart( new OpenVpnConfig( new File( intent.getStringExtra( Intents.EXTRA_CONFIG ) ) ) );
        else if ( Intents.STOP_DAEMON.equals( intent.getAction() ) )
            daemonStop( new File( intent.getStringExtra( Intents.EXTRA_CONFIG ) ) );
    }

    @Override
	public void onDestroy()
    {
		PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener( onSharedPreferenceChangeListener );

		markServiceStopped();

		PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(
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

    private final OpenVpnStateListenerDispatcher listenerDispatcher = new OpenVpnStateListenerDispatcher();

    private DaemonMonitorFactory daemonMonitorFactory;

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
		mConnectivity.startListening( getApplicationContext2() );
	}
	
	private synchronized void shutdown()
	{
		Log.i(TAG, "shutting down");

        if ( getCurrent().isAlive() ) {
            getCurrent().stop();
        }

		mConnectivity.stopListening();
		mConnectivity = null;
	}


    /**
	 * Try to attach to already running OpenVPN daemons, starting them if they
	 * are enabled.
	 */
	private final void daemonAttach()
	{
		Log.d( TAG, "trying to attach to already running daemons" );

        mRegistry.clear();
        DaemonMonitor daemonMonitor = new FindCurrentDaemon( getContext(), daemonMonitorFactory, listConfigs() ).getTheOneRunningDaemonOrTheNullDaemonMonitor();
        setCurrent( daemonMonitor );

        // clean up sticky broadcasts
        //TODO: review this code it might not be correct
        Intent intent = getService().registerReceiver( null, new IntentFilter( Intents.DAEMON_STATE_CHANGED ) );
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

    private Notification newNotification2(File config)
    {
        return new Notification( getContext(), config, new Preferences2( getContext(), config ).getNotificationId(), listenerDispatcher );
    }


    private DaemonMonitor newDaemonMonitor(File config)
    {
        return daemonMonitorFactory.createDaemonMonitorFor( config );
    }

    // hook to be overwritten in unit test
    protected List<File> listConfigs()
    {
        return Preferences.listKnownConfigs( getContext() );
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

	final synchronized void daemonStart(OpenVpnConfig config)
	{
        if ( getCurrent().isAlive() && !getCurrent().getConfigFile().equals( config.getFile() ) )
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

        //TODO: When running on rooted Android phone do not check if VpnService was prepared.
        if (!ApiLevel.get().isVpnServicePrepared( getContext() ) )
        {
            Toast.makeText( getContext(), "VPN service must be prepared before daemonStart() may be called!", Toast.LENGTH_LONG ).show();
            return;
        }

        if ( isDaemonStarted(config.getFile()) )
		{
			Log.i( TAG, config + " is already running" );
		}
		else if ( Preferences.getVpnDnsEnabled(getContext(), config.getFile()) && isVpnDnsActive() )
		{
			Log.i( TAG, config + " only one VPN DNS may be active at a time, aborting" );
			Toast.makeText( getContext() , "VPN DNS is only supported in one tunnel!", Toast.LENGTH_LONG).show();
            newNotification2( config.getFile() ).daemonStateChangedToDisabled();
		}
		else
		{
			DaemonMonitor daemonMonitor = newDaemonMonitor( config.getFile() );
            setCurrent( daemonMonitor );
            daemonMonitor.start();
		}
	}

	final synchronized void daemonStop(File config)
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

	void daemonPassphrase(File config, String passphrase)
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

	void daemonUsernamePassword(File config, String username, String password)
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

    @Deprecated //TODO: use Intents.DAEMON_STATE_CHANGED instead
    final synchronized boolean isDaemonStarted(File config)
	{
        return getCurrent().isAlive() && config.equals( getCurrent().getConfigFile() );
	}

    @Deprecated //TODO: use Intents.DAEMON_STATE_CHANGED instead
    final synchronized boolean hasDaemonsStarted()
	{
        return getCurrent().isAlive();
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