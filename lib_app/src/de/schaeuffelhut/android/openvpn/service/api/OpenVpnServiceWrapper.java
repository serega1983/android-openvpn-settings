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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

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
    private IOpenVpnService openVpnService;

    /**
     * Creates an {@code OpenVpnServiceWrapper} using the specified context as
     * the target for {@code bindService()}, {@code unbindService()} as well as
     * {@code startService()} and {@code stopService()}. Keeps a reference to
     * the {@code Context} object.
     *
     * @param context The {@code Context} to call {@code bindService()}, {@code unbindService()} as well as
     * {@code startService()} and {@code stopService()} upon. A reference to this {@code Context} is kept.
     */
    public OpenVpnServiceWrapper(Context context)
    {
        this.context = context;
        invalidateRemoteInterface();
    }

    public static Intent createIntentAddressingOpenVpnService()
    {
        return new Intent().setComponent( COMPONENT_NAME );
    }

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

    /**
     * Bind to {@code IOpenVpnService}.
     * @return false if the service could not be bound to, e.g. OpenVpnSettings is not installed and
     *         the service is not available. Otherwise returns {@code true}.
     */
    public boolean bindService()
    {
        boolean success = context.bindService( createIntentAddressingOpenVpnService(), this, 0 );
        return success;
    }

    public void unbindService()
    {
        context.unbindService( this );
    }

    public void onServiceConnected(ComponentName componentName, IBinder iBinder)
    {
        openVpnService = IOpenVpnService.Stub.asInterface( iBinder );
    }

    public void onServiceDisconnected(ComponentName componentName)
    {
        invalidateRemoteInterface();
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
        return new OpenVpnState.Stopped();
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
        return new OpenVpnState.Stopped();
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
}
