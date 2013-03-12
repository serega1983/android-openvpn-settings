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

import android.os.IBinder;
import android.os.RemoteException;
import de.schaeuffelhut.android.openvpn.lib.service.impl.OpenVpnStateListenerDispatcher;
import de.schaeuffelhut.android.openvpn.service.api.IOpenVpnStateListener;
import de.schaeuffelhut.android.openvpn.service.api.OpenVpnDaemonState;
import de.schaeuffelhut.android.openvpn.service.api.OpenVpnNetworkState;
import de.schaeuffelhut.android.openvpn.service.api.OpenVpnState;
import junit.framework.TestCase;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-11-14
 */
public class OpenVpnStateListenerDispatcherTest extends TestCase
{
    private final OpenVpnStateListenerDispatcher listenerDispatcher = new OpenVpnStateListenerDispatcher();
    private final IOpenVpnStateListener listener1 = Mockito.mock( IOpenVpnStateListener.Stub.class );
    private final IOpenVpnStateListener listener2 = Mockito.mock( IOpenVpnStateListener.Stub.class );

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        Mockito.when( listener1.asBinder() ).thenCallRealMethod();
        Mockito.when( listener2.asBinder() ).thenCallRealMethod();
    }

    public void test_addListener_two_times() throws RemoteException
    {
        listenerDispatcher.addOpenVpnStateListener( listener1 );
        listenerDispatcher.addOpenVpnStateListener( listener1 );

        listenerDispatcher.onDaemonStateChanged( OpenVpnDaemonState.ENABLED );
        verify( listener1, times( 1 ) ).onDaemonStateChanged( OpenVpnDaemonState.ENABLED );
    }

    public void test_removeListener() throws RemoteException
    {
        listenerDispatcher.addOpenVpnStateListener( listener1 );
        listenerDispatcher.removeOpenVpnStateListener( listener1 );

        listenerDispatcher.onDaemonStateChanged( OpenVpnDaemonState.ENABLED );
        verify( listener1, never() ).onDaemonStateChanged( OpenVpnDaemonState.ENABLED );
    }

    public void test_linkToDeath() throws RemoteException
    {
        IBinder binder = mock( IBinder.class );
        when( listener1.asBinder() ).thenReturn( binder );

        listenerDispatcher.addOpenVpnStateListener( listener1 );

        // capture the DeathRecipient
        ArgumentCaptor<IBinder.DeathRecipient> deathRecipientArgumentCaptor = ArgumentCaptor.forClass( IBinder.DeathRecipient.class );
        verify( binder ).linkToDeath( deathRecipientArgumentCaptor.capture(), anyInt() );

        // and call it
        deathRecipientArgumentCaptor.getValue().binderDied();

        // verify delegation doesn't happen, which means listener was removed.
        listenerDispatcher.onDaemonStateChanged( OpenVpnDaemonState.ENABLED );
        verify( listener1, never() ).onDaemonStateChanged( OpenVpnDaemonState.ENABLED );

        // verify unlinkToDeath was called
        verify( binder ).unlinkToDeath( same( deathRecipientArgumentCaptor.getValue() ), anyInt() );
    }


    public void test_delegation_of_all_methods_to_one_listener() throws RemoteException
    {
        listenerDispatcher.addOpenVpnStateListener( listener1 );

        listenerDispatcher.onDaemonStateChanged( OpenVpnDaemonState.ENABLED );
        verify( listener1 ).onDaemonStateChanged( OpenVpnDaemonState.ENABLED );

        listenerDispatcher.onRequestPassphrase();
        verify( listener1 ).onRequestPassphrase();

        listenerDispatcher.onRequestCredentials();
        verify( listener1 ).onRequestCredentials();

        listenerDispatcher.onNetworkStateChanged( OpenVpnNetworkState.UNKNOWN, OpenVpnNetworkState.CONNECTED, 100, "A", "B", "C" );
        verify( listener1 ).onNetworkStateChanged( OpenVpnNetworkState.UNKNOWN, OpenVpnNetworkState.CONNECTED, 100, "A", "B", "C" );

        listenerDispatcher.onByteCountChanged( 100, 200 );
        verify( listener1 ).onByteCountChanged( 100, 200 );
    }

    public void test_delegation_of_all_methods_to_two_listeners() throws RemoteException
    {
        listenerDispatcher.addOpenVpnStateListener( listener1 );
        listenerDispatcher.addOpenVpnStateListener( listener2 );

        listenerDispatcher.onDaemonStateChanged( OpenVpnDaemonState.ENABLED );
        verify( listener1 ).onDaemonStateChanged( OpenVpnDaemonState.ENABLED );
        verify( listener2 ).onDaemonStateChanged( OpenVpnDaemonState.ENABLED );

        listenerDispatcher.onRequestPassphrase();
        verify( listener1 ).onRequestPassphrase();
        verify( listener2 ).onRequestPassphrase();

        listenerDispatcher.onRequestCredentials();
        verify( listener1 ).onRequestCredentials();
        verify( listener2 ).onRequestCredentials();

        listenerDispatcher.onNetworkStateChanged( OpenVpnNetworkState.UNKNOWN, OpenVpnNetworkState.CONNECTED, 100, "A", "B", "C" );
        verify( listener1 ).onNetworkStateChanged( OpenVpnNetworkState.UNKNOWN, OpenVpnNetworkState.CONNECTED, 100, "A", "B", "C" );
        verify( listener2 ).onNetworkStateChanged( OpenVpnNetworkState.UNKNOWN, OpenVpnNetworkState.CONNECTED, 100, "A", "B", "C" );

        listenerDispatcher.onByteCountChanged( 100, 200);
        verify( listener1 ).onByteCountChanged( 100, 200 );
        verify( listener2 ).onByteCountChanged( 100, 200 );
    }


    /**
     * Assert removed listener will not be called.
     * @throws RemoteException
     */
    public void test_delegation_of_all_methods_to_one_listener_with_one_listener_added_and_removed() throws RemoteException
    {
        listenerDispatcher.addOpenVpnStateListener( listener1 );
        listenerDispatcher.addOpenVpnStateListener( listener2 );
        listenerDispatcher.removeOpenVpnStateListener( listener1 );

        listenerDispatcher.onDaemonStateChanged( OpenVpnDaemonState.ENABLED );
        verify( listener1, never() ).onDaemonStateChanged( OpenVpnDaemonState.ENABLED );
        verify( listener2 ).onDaemonStateChanged( OpenVpnDaemonState.ENABLED );

        listenerDispatcher.onRequestPassphrase();
        verify( listener1, never() ).onRequestPassphrase();
        verify( listener2 ).onRequestPassphrase();

        listenerDispatcher.onRequestCredentials();
        verify( listener1, never() ).onRequestCredentials();
        verify( listener2 ).onRequestCredentials();

        listenerDispatcher.onNetworkStateChanged( OpenVpnNetworkState.UNKNOWN, OpenVpnNetworkState.CONNECTED, 100, "A", "B", "C" );
        verify( listener1, never() ).onNetworkStateChanged( OpenVpnNetworkState.UNKNOWN, OpenVpnNetworkState.CONNECTED, 100, "A", "B", "C" );
        verify( listener2 ).onNetworkStateChanged( OpenVpnNetworkState.UNKNOWN, OpenVpnNetworkState.CONNECTED, 100, "A", "B", "C" );

        listenerDispatcher.onByteCountChanged( 100, 200);
        verify( listener1, never() ).onByteCountChanged( 100, 200 );
        verify( listener2 ).onByteCountChanged( 100, 200 );
    }

    /*
     * Assert RemoteException in any delegated method will remove the listener
     */

    public void test_RemoteException_in__onDaemonStateChanged__removes_listener() throws RemoteException
    {
        doThrow( new RemoteException() ).when( listener1 ).onDaemonStateChanged( any( OpenVpnDaemonState.class ) );
        listenerDispatcher.addOpenVpnStateListener( listener1 );
        listenerDispatcher.onDaemonStateChanged( OpenVpnDaemonState.ENABLED );
        verify( listener1 ).onDaemonStateChanged( OpenVpnDaemonState.ENABLED );
        // IOpenVpnStateListener was removed due to RemoteException, so expect method not being called again
        listenerDispatcher.onDaemonStateChanged( OpenVpnDaemonState.ENABLED );
        verify( listener1, times(1) ).onDaemonStateChanged( OpenVpnDaemonState.ENABLED );
    }

    public void test_RemoteException_in__onRequestPassphras__removes_listener() throws RemoteException
    {
        doThrow( new RemoteException() ).when( listener1 ).onRequestPassphrase();
        listenerDispatcher.addOpenVpnStateListener( listener1 );
        listenerDispatcher.onRequestPassphrase();
        verify( listener1 ).onRequestPassphrase();
        // IOpenVpnStateListener was removed due to RemoteException, so expect method not being called again
        listenerDispatcher.onRequestPassphrase();
        verify( listener1 ).onRequestPassphrase();
    }

    public void test_RemoteException_in__onRequestCredentials__removes_listener() throws RemoteException
    {
        doThrow( new RemoteException() ).when( listener1 ).onRequestCredentials();
        listenerDispatcher.addOpenVpnStateListener( listener1 );
        listenerDispatcher.onRequestCredentials();
        verify( listener1 ).onRequestCredentials();
        // IOpenVpnStateListener was removed due to RemoteException, so expect method not being called again
        listenerDispatcher.onRequestCredentials();
        verify( listener1 ).onRequestCredentials();
    }

    public void test_RemoteException_in__onNetworkStateChanged__removes_listener() throws RemoteException
    {
        doThrow( new RemoteException() ).when( listener1 ).onNetworkStateChanged(
                any( OpenVpnNetworkState.class ), any( OpenVpnNetworkState.class ), anyLong(), anyString(), anyString(), anyString()
        );
        listenerDispatcher.addOpenVpnStateListener( listener1 );
        listenerDispatcher.onNetworkStateChanged( OpenVpnNetworkState.UNKNOWN, OpenVpnNetworkState.CONNECTED, 100, "A", "B", "C" );
        verify( listener1 ).onNetworkStateChanged( OpenVpnNetworkState.UNKNOWN, OpenVpnNetworkState.CONNECTED, 100, "A", "B", "C" );
        // IOpenVpnStateListener was removed due to RemoteException, so expect method not being called again
        listenerDispatcher.onNetworkStateChanged( OpenVpnNetworkState.UNKNOWN, OpenVpnNetworkState.CONNECTED, 100, "A", "B", "C" );
        verify( listener1 ).onNetworkStateChanged( OpenVpnNetworkState.UNKNOWN, OpenVpnNetworkState.CONNECTED, 100, "A", "B", "C" );
    }

    public void test_RemoteException_in__onByteCountChanged__removes_listener() throws RemoteException
    {
        doThrow( new RemoteException() ).when( listener1 ).onByteCountChanged( anyInt(), anyInt() );
        listenerDispatcher.addOpenVpnStateListener( listener1 );
        listenerDispatcher.onByteCountChanged( 100, 200 );
        verify( listener1 ).onByteCountChanged( 100, 200 );
        // IOpenVpnStateListener was removed due to RemoteException, so expect method not being called again
        listenerDispatcher.onByteCountChanged( 100, 200 );
        verify( listener1 ).onByteCountChanged( 100, 200 );
    }
}
