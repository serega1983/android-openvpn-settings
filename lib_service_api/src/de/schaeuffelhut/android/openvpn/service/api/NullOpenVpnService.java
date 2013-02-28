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

import android.os.IBinder;
import android.os.RemoteException;

/**
 * {@code NullOpenVpnService} is used when no {@code IOpenVpnService} is available.
 *
 * @author Friedrich Schäuffelhut
 * @since 2012-11-13
 */
public class NullOpenVpnService implements IOpenVpnService
{
    public static NullOpenVpnService getInstance()
    {
        return INSTANCE;
    }

    private final static NullOpenVpnService INSTANCE = new NullOpenVpnService();

    private NullOpenVpnService(){}

    public void connect(OpenVpnConfig config) throws RemoteException
    {
    }

    public void supplyCredentials(OpenVpnCredentials credentials) throws RemoteException
    {
    }

    public void supplyPassphrase(OpenVpnPassphrase passphrase) throws RemoteException
    {
    }

    public OpenVpnState getStatus() throws RemoteException
    {
        return OpenVpnState.stopped();
    }

    public OpenVpnState getStatusFor(OpenVpnConfig config) throws RemoteException
    {
        return OpenVpnState.stopped();
    }

    public void disconnect() throws RemoteException
    {
    }


    public void addOpenVpnStateListener(IOpenVpnStateListener listener)
    {
    }

    public void removeOpenVpnStateListener(IOpenVpnStateListener listener)
    {
    }

    public IBinder asBinder()
    {
        throw new IllegalStateException( "NullOpenVpnService has no binder" );
    }
}
