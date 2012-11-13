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
import android.nfc.Tag;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * Wraps a remote {@code IOpenVpnService} obtained with {@code bindService()}.
 * Takes care of
 *
 * @author Friedrich Schäuffelhut
 * @since 2012-11-13
 */
public class OpenVpnServiceWrapper implements ServiceConnection
{
    private IOpenVpnService openVpnService;

    public OpenVpnServiceWrapper()
    {
        invalidateRemoteInterface();
    }

    /**
     * Bind to {@code IOpenVpnService}.
     * @param context The context to use for binding.
     * @return false if the service could not be bound, e.g. OpenVpnSettings is not installed and the service is not available
     */
    public boolean bindService(Context context)
    {
        boolean success = context.bindService( new Intent( "de.schaeuffelhut.android.openvpn.services.OpenVpnService" ), this, 0 );
        return success;
    }

    public void unbindService(Context context)
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
