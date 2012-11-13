package de.schaeuffelhut.android.openvpn.service.api;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-10-27
 */
public class OpenVpnServiceService extends Service
{
    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    private final IfcOpenVpnService.Stub mBinder = new IfcOpenVpnService.Stub()
    {
        OpenVpnState[] states = new OpenVpnState[]{
                new OpenVpnState.Started( "CONNECTING", "USA3", "", 0, 0, 0 ),
                new OpenVpnState.Started( "WAIT", "USA3", "", 0, 0, 0 ),
                new OpenVpnState.Started( "AUTH", "USA3", "", 0, 0, 0 ),
                new OpenVpnState.Started( "GET_CONFIG", "USA3", "", 0, 0, 0 ),
                new OpenVpnState.Started( "ASSIGN_IP", "USA3", "192.168.1.1", 0, 0, 0 ),
                new OpenVpnState.Started( "ADD_ROUTES", "USA3", "192.168.1.1", 0, 0, 0 ),
                new OpenVpnState.Started( "CONNECTED", "USA3", "192.168.1.1", 10, 0, 1 ),
                new OpenVpnState.Started( "CONNECTED", "USA3", "192.168.1.1", 20, 10248, 2 ),
                new OpenVpnState.Started( "CONNECTED", "USA3", "192.168.1.1", 1050, 29452, 3 ),
                new OpenVpnState.Started( "RECONNECTING", "USA3", "", 1050, 29452, 0 ),
                new OpenVpnState.Started( "EXITING", "USA3", "", 1050, 29452, 0 ),
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

        public void connectTo(OpenVpnConfig config) throws RemoteException
        {
            if (t == null || !t.isAlive())
            {
                t = new Thread( r );
                t.start();
            }

        }

        public void authenticate(String username, String password) throws RemoteException
        {
            //TODO: implement method stub

        }

        public OpenVpnState getStatus() throws RemoteException
        {
            return t != null && t.isAlive() ? states[i % states.length] : new OpenVpnState.Stopped();
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
    };
}
