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

package de.schaeuffelhut.android.openvpn.service.api;

import android.content.*;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Wraps a remote {@code IOpenVpnService} obtained with {@code bindService()}.
 * Takes care of binding and unbinding to the service and deals with a vanished
 * Service. Override the call back methods defined in {@code ServiceConnection} to
 * act when the service becomes bound or unbound. This class can also issue
 * {@code startService()} and {@code stopService()} messages.
 *
 * @author Friedrich Schäuffelhut
 * @since 2012-11-13
 */
public class OpenVpnServiceWrapper implements ServiceConnection
{
    static final ComponentName COMPONENT_NAME = new ComponentName(
            "de.schaeuffelhut.android.openvpn",
            "de.schaeuffelhut.android.openvpn.services.OpenVpnService"
    );

    private final Context context;
    private final ComponentName serviceName;
    private IOpenVpnService openVpnService;
    private final ArrayList<IOpenVpnStateListener> listeners = new ArrayList<IOpenVpnStateListener>();

    /**
     * Creates an {@code OpenVpnServiceWrapper} using the specified context as
     * the target for {@code bindService()}, {@code unbindService()} as well as
     * {@code startService()} and {@code stopService()}. Keeps a reference to
     * the {@code Context} object. Connects to the default OpenVpnService
     * published by OpenVpnSettings.
     *
     * @param context The {@code Context} to call {@code bindService()}, {@code unbindService()} as well as
     * {@code startService()} and {@code stopService()} upon. A reference to this {@code Context} is kept.
     */
    public OpenVpnServiceWrapper(Context context)
    {
        this( context, COMPONENT_NAME );
    }

    /**
     * Creates an {@code OpenVpnServiceWrapper} using the specified context as
     * the target for {@code bindService()}, {@code unbindService()} as well as
     * {@code startService()} and {@code stopService()}. Keeps a reference to
     * the {@code Context} object.
     *
     * @param context The {@code Context} to call {@code bindService()}, {@code unbindService()} as well as
     * {@code startService()} and {@code stopService()} upon. A reference to this {@code Context} is kept.
     * @param serviceName The service to connect to.
     */
    public OpenVpnServiceWrapper(Context context, ComponentName serviceName)
    {
        this.context = context;
        this.serviceName = serviceName;
        invalidateRemoteInterface();
    }

    private Intent createIntentAddressingOpenVpnService()
    {
        return new Intent().setComponent( serviceName );
    }

    public static Intent createDefaultIntentAddressingOpenVpnService()
    {
        return new Intent().setComponent( COMPONENT_NAME );
    }

    /*
     * start/stop service
     */

    /**
     * Start the {@code IOpenVpnService}.
     * @return false if the service could not be found, e.g. OpenVpnSettings is not installed and
     *         the service is not available. Otherwise returns {@code true}.
     */
    public boolean startService()
    {
        ComponentName componentName = context.startService( createIntentAddressingOpenVpnService() );
        // Since we already address the service by its component name we only need to know if we could reach it.
        return null != componentName;
    }

    public void stopService()
    {
        context.stopService( createIntentAddressingOpenVpnService() );
    }

    /*
     * ServiceConnection
     */

    /**
     * Bind to {@code IOpenVpnService}. Also installs a {@code BroadcastReceiver} listening for
     * the OPENVPN_STATE_CHANGED intent. If the service is started, {@code OpenVpnServiceWrapper}
     * will try to bind again to the service.
     * @return false if the service could not be bound to, e.g. OpenVpnSettings is not installed and
     *         the service is not available. Otherwise returns {@code true}.
     */
    public boolean bindService()
    {
        context.registerReceiver( broadcastReceiver, new IntentFilter( Intents.OPENVPN_STATE_CHANGED.getAction() ) );
        return doBindService();
    }

    private boolean doBindService()
    {
        return context.bindService( createIntentAddressingOpenVpnService(), this, 0 );
    }

    public void unbindService()
    {
        context.unregisterReceiver( broadcastReceiver );
        context.unbindService( this );
    }

    public final void onServiceConnected(ComponentName componentName, IBinder iBinder)
    {
        openVpnService = IOpenVpnService.Stub.asInterface( iBinder );
        if ( isPaused )
            return;
        addRememberedListenersToRemoteService();
        onServiceConnectedHook( componentName, iBinder );
    }

    /**
     * Called when ServiceConnection.onServiceConnected() is called and listeners are not paused.
     * @param componentName @see ServiceConnection.onServiceConnected()
     * @param iBinder       @see ServiceConnection.onServiceConnected()
     */
    protected void onServiceConnectedHook(ComponentName componentName, IBinder iBinder)
    {
        // overwrite hook if necessary
        //TODO: Replace this hook by delegating to a instance of ServiceConnection
    }

    public final void onServiceDisconnected(ComponentName componentName)
    {
        invalidateRemoteInterface();
        if ( isPaused )
            return;
        removeRememberedListenersFromRemoteService(); // call after invalidateRemoteInterface() so we talk to NullOpenVpnService.
        onServiceDisconnectedHook( componentName );
    }

    /**
     * Called when ServiceConnection.onServiceDisconnected() is called and listeners are not paused.
     * @param componentName @see ServiceConnection.onServiceDisconnected()
     */
    protected void onServiceDisconnectedHook(ComponentName componentName)
    {
        // overwrite hook if necessary
        //TODO: Replace this hook by delegating to a instance of ServiceConnection
    }

    private void invalidateRemoteInterface()
    {
        openVpnService = NullOpenVpnService.getInstance();
    }

    public boolean isBound()
    {
        return !(openVpnService instanceof NullOpenVpnService);
    }

    /*
     * BroadcastReceiver
     */

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            doBindService();
        }
    };

    /*
     * Delegate to IOpenVpnService
     */

    public void connect(OpenVpnConfig config)
    {
        try
        {
            openVpnService.connect( config );
        }
        catch (RemoteException e)
        {
            invalidateRemoteInterface();
            //TODO: bind to the interface again?
        }
    }

    public void supplyCredentials(OpenVpnCredentials credentials)
    {
        try
        {
            openVpnService.supplyCredentials( credentials );
        }
        catch (RemoteException e)
        {
            invalidateRemoteInterface();
            //TODO: bind to the interface again?
        }
    }

    public void supplyPassphrase(OpenVpnPassphrase passphrase)
    {
        try
        {
            openVpnService.supplyPassphrase( passphrase );
        }
        catch (RemoteException e)
        {
            invalidateRemoteInterface();
            //TODO: bind to the interface again?
        }
    }

    public OpenVpnState getStatus()
    {
        try
        {
            return openVpnService.getStatus();
        }
        catch (RemoteException e)
        {
            invalidateRemoteInterface();
            //TODO: bind to the interface again?
        }
        return OpenVpnState.stopped();
    }

    public OpenVpnState getStatusFor(OpenVpnConfig config)
    {
        try
        {
            return openVpnService.getStatusFor( config );
        }
        catch (RemoteException e)
        {
            invalidateRemoteInterface();
            //TODO: bind to the interface again?
        }
        return OpenVpnState.stopped();
    }

    public void disconnect()
    {
        try
        {
            openVpnService.disconnect();
        }
        catch (RemoteException e)
        {
            invalidateRemoteInterface();
            //TODO: bind to the interface again?
        }
    }

    public void addOpenVpnStateListener(IOpenVpnStateListener listener)
    {
        if ( listener == null )
            throw new NullPointerException( "listener is null in addOpenVpnStateListener" );

        remember( listener );

        try
        {
            openVpnService.addOpenVpnStateListener( listener );
        }
        catch (RemoteException e)
        {
            invalidateRemoteInterface();
            //TODO: bind to the interface again?
        }
    }

    public void removeOpenVpnStateListener(IOpenVpnStateListener listener)
    {
        if ( listener == null )
            throw new NullPointerException( "listener is null in removeOpenVpnStateListener" );

        forget( listener );

        try
        {
            openVpnService.removeOpenVpnStateListener( listener );
        }
        catch (RemoteException e)
        {
            invalidateRemoteInterface();
            //TODO: bind to the interface again?
        }
    }


    private void remember(IOpenVpnStateListener listener)
    {
        synchronized ( listeners ) {
            for( IOpenVpnStateListener l : listeners )
                if ( l == listener ) // identity wanted here
                    return;
            listeners.add( listener );
        }
    }

    private void forget(IOpenVpnStateListener listener)
    {
        synchronized ( listeners ) {
            for( Iterator<IOpenVpnStateListener> it = listeners.iterator(); it.hasNext(); )
            {
                if ( it.next() == listener ) // identity wanted here
                {
                    it.remove();
                    // bail out, listener will be in the list only once
                    break;
                }
            }
        }
    }

    private boolean listenersRegisteredWithRemoteService = false;

    private void addRememberedListenersToRemoteService()
    {
        synchronized (listeners)
        {
            if ( !isBound() )
                return;
            if (listenersRegisteredWithRemoteService)
                return;
            try
            {
                for (IOpenVpnStateListener l : listeners)
                    openVpnService.addOpenVpnStateListener( l );
                listenersRegisteredWithRemoteService = true;
            }
            catch (RemoteException e)
            {
                invalidateRemoteInterface(); //TODO: put under test
                //TODO: bind to the interface again?
            }
        }
    }

    private void removeRememberedListenersFromRemoteService()
    {
        synchronized (listeners)
        {
            listenersRegisteredWithRemoteService = false;
            if ( !isBound() )
                return;
            try
            {
                for (IOpenVpnStateListener l : listeners)
                    openVpnService.removeOpenVpnStateListener( l );
            }
            catch (RemoteException e)
            {
                invalidateRemoteInterface(); //TODO: put under test
                //TODO: bind to the interface again?
            }
        }
    }

    private boolean isPaused = false;

    /**
     * Call {@code resumeListeners()} in {@code onResume()} or {@code onStart()} to add all listeners
     * registered with this {@code OpenVpnServiceWrapper} to remote service.
     */
    public void resumeListeners()
    {
        addRememberedListenersToRemoteService();
        isPaused = false;
    }

    /**
     * Call {@code pauseListeners()} in {@code onPause()} or {@code onStop()} to remove all listeners
     * registered with this {@code OpenVpnServiceWrapper} from remote service.
     */
    public void pauseListeners()
    {
        removeRememberedListenersFromRemoteService();
        isPaused = true;
    }
}
