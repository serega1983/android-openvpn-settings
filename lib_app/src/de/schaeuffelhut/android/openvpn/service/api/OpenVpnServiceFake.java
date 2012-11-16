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

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-10-27
 */
public class OpenVpnServiceFake extends Service
{
    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    private final IOpenVpnService.Stub mBinder = new IOpenVpnService.Stub()
    {
        OpenVpnState[] states = new OpenVpnState[]{
                new OpenVpnState.Started( OpenVpnDaemonState.ENABLED, OpenVpnNetworkState.CONNECTING, "USA3", "", 0, 0, 0 ),
                new OpenVpnState.Started( OpenVpnDaemonState.ENABLED, OpenVpnNetworkState.WAIT, "USA3", "", 0, 0, 0 ),
                new OpenVpnState.Started( OpenVpnDaemonState.ENABLED, OpenVpnNetworkState.AUTH, "USA3", "", 0, 0, 0 ),
                new OpenVpnState.Started( OpenVpnDaemonState.ENABLED, OpenVpnNetworkState.GET_CONFIG, "USA3", "", 0, 0, 0 ),
                new OpenVpnState.Started( OpenVpnDaemonState.ENABLED, OpenVpnNetworkState.ASSIGN_IP, "USA3", "192.168.1.1", 0, 0, 0 ),
                new OpenVpnState.Started( OpenVpnDaemonState.ENABLED, OpenVpnNetworkState.ADD_ROUTES, "USA3", "192.168.1.1", 0, 0, 0 ),
                new OpenVpnState.Started( OpenVpnDaemonState.ENABLED, OpenVpnNetworkState.CONNECTED, "USA3", "192.168.1.1", 10, 0, 1 ),
                new OpenVpnState.Started( OpenVpnDaemonState.ENABLED, OpenVpnNetworkState.CONNECTED, "USA3", "192.168.1.1", 20, 10248, 2 ),
                new OpenVpnState.Started( OpenVpnDaemonState.ENABLED, OpenVpnNetworkState.CONNECTED, "USA3", "192.168.1.1", 1050, 29452, 3 ),
                new OpenVpnState.Started( OpenVpnDaemonState.ENABLED, OpenVpnNetworkState.RECONNECTING, "USA3", "", 1050, 29452, 0 ),
                new OpenVpnState.Started( OpenVpnDaemonState.ENABLED, OpenVpnNetworkState.EXITING, "USA3", "", 1050, 29452, 0 ),
        };
        int i = 0;
        Runnable r = new Runnable()
        {
            public void run()
            {
                for (; ; )
                {
                    try
                    {
                        Thread.sleep( 1000 );
                    }
                    catch (InterruptedException e)
                    {
                        break;
                    }
                    i++;
                    sendUpdate();
                }
                sendUpdate();
            }

            private void sendUpdate()
            {
                sendBroadcast( Intents.OPENVPN_STATE_CHANGED.createLocalAppIntent() );
                if (i % states.length == 2)
                    sendBroadcast( Intents.OPENVPN_NEEDS_USERNAME_PASSWORD.createLocalAppIntent() );

            }
        };
        Thread t = null;

        public void connect(OpenVpnConfig config) throws RemoteException
        {
            if (t == null || !t.isAlive())
            {
                t = new Thread( r );
                t.start();
            }

        }

        public void supplyPassphrase(OpenVpnPassphrase passphrase) throws RemoteException
        {
            //TODO: implement method stub

        }

        public void supplyCredentials(OpenVpnCredentials credentials) throws RemoteException
        {
            //TODO: implement method stub

        }

        public OpenVpnState getStatus() throws RemoteException
        {
            return t != null && t.isAlive() ? states[i % states.length] : OpenVpnState.stopped();
        }

        public OpenVpnState getStatusFor(OpenVpnConfig config) throws RemoteException
        {
            //TODO: compare with config supplied to connect()
            return t != null && t.isAlive() ? states[i % states.length] : OpenVpnState.stopped();
        }

        public void disconnect() throws RemoteException
        {
            t.interrupt();
            try
            {
                t.join();
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException( e ); //TODO: handle exception
            }
        }


        public void addOpenVpnStateListener(IOpenVpnStateListener listener)
        {
        }

        public void removeOpenVpnStateListener(IOpenVpnStateListener listener)
        {
        }

    };
}
