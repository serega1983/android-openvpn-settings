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
import junit.framework.TestCase;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.schaeuffelhut.android.openvpn.service.api.OpenVpnServiceWrapper.COMPONENT_NAME;
import static org.mockito.Mockito.*;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-11-13
 */
public class OpenVpnServiceWrapperTest extends TestCase
{
    private static interface Stub extends IOpenVpnService, IBinder {}

    private Context context = mock( Context.class );
    private OpenVpnServiceWrapper wrapper = new OpenVpnServiceWrapper( context ) {
        @Override
        protected void onServiceConnectedHook(ComponentName componentName, IBinder iBinder)
        {
            onServiceConnectedCalled.set( true );
        }

        @Override
        protected void onServiceDisconnectedHook(ComponentName componentName)
        {
            onServiceDisconnectedCalled.set( true );
        }
    };
    private Stub openVpnServiceStub = mock( Stub.class );

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        resetOpenVpnServiceStub();
    }

    private void resetOpenVpnServiceStub()
    {
        reset( openVpnServiceStub );
        Mockito.when( openVpnServiceStub.queryLocalInterface( Mockito.anyString() ) ).thenReturn( openVpnServiceStub );
    }

    // ============================================================
    // Test constants
    // ============================================================

    /**
     * Verifies the component name of the {@code IOpenVpnService}.
     * Do NOT change the component name. Clients connect to the {@code IOpenVpnService} using this name.
     */
    public void test_COMPONENT_NAME()
    {
        assertEquals( "de.schaeuffelhut.android.openvpn", COMPONENT_NAME.getPackageName() );
        assertEquals( "de.schaeuffelhut.android.openvpn.services.OpenVpnService", COMPONENT_NAME.getClassName() );
    }

    public void test_createIntentAddressingOpenVpnService_has_no_action()
    {
        assertEquals( null, OpenVpnServiceWrapper.createDefaultIntentAddressingOpenVpnService().getAction() );
    }

    public void test_createIntentAddressingOpenVpnService_verify_componentName()
    {
        assertEquals( COMPONENT_NAME, OpenVpnServiceWrapper.createDefaultIntentAddressingOpenVpnService().getComponent() );
    }

    private void assert_intentAddressesOpenVpnService_with_no_action(Intent intent)
    {
        assertEquals( null, intent.getAction() );
        assertEquals( COMPONENT_NAME, intent.getComponent() );
    }

    // ============================================================
    // Test wrapper status
    // ============================================================

    public void test_isBound_after_init()
    {
        assertFalse( wrapper.isBound() );
    }

    public void test_isBound_after_onServiceConnected()
    {
        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );

        assertTrue( wrapper.isBound() );
    }

    public void test_isBound_after_onServiceDisconnected()
    {
        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );
        wrapper.onServiceDisconnected( COMPONENT_NAME );

        assertFalse( wrapper.isBound() );
    }

    // ============================================================
    // Test startService(), stopService()
    // ============================================================

    public void test_startService() throws RemoteException
    {
        wrapper.startService();

        ArgumentCaptor<Intent> intentCaptor = new ArgumentCaptor<Intent>();
        Mockito.verify( context ).startService( intentCaptor.capture() );

        assert_intentAddressesOpenVpnService_with_no_action( intentCaptor.getValue() );
    }

    public void test_startService_fails() throws RemoteException
    {
        Mockito.when( context.startService(  Mockito.any( Intent.class ) ) ).thenReturn( null );

        boolean success = wrapper.startService();

        assertFalse( success );
    }

    public void test_startService_succeeds() throws RemoteException
    {
        Mockito.when( context.startService(  Mockito.any( Intent.class ) ) ).thenReturn( COMPONENT_NAME );

        boolean success = wrapper.startService();

        assertTrue( success );
    }

    public void test_stopService() throws RemoteException
    {
        wrapper.stopService();

        ArgumentCaptor<Intent> intentCaptor = new ArgumentCaptor<Intent>();
        Mockito.verify( context ).stopService( intentCaptor.capture() );

        assert_intentAddressesOpenVpnService_with_no_action( intentCaptor.getValue() );
    }

    // ============================================================
    // Test bindService(), unbindService()
    // ============================================================

    public void test_bindService() throws RemoteException
    {
        wrapper.bindService();

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass( Intent.class );
        Mockito.verify( context ).bindService( intentCaptor.capture(), Mockito.same( wrapper ), Mockito.eq( 0 ) );
        assert_intentAddressesOpenVpnService_with_no_action( intentCaptor.getValue() );
    }

    public void test_bindService_fails() throws RemoteException
    {
        Mockito.when( context.bindService(  Mockito.any( Intent.class ), Mockito.same( wrapper ), Mockito.eq( 0 )  ) ).thenReturn( false );

        boolean success = wrapper.bindService();

        assertFalse( success );
    }

    public void test_bindService_succeeds() throws RemoteException
    {
        Mockito.when( context.bindService(  Mockito.any( Intent.class ), Mockito.same( wrapper ), Mockito.eq( 0 )  ) ).thenReturn( true );

        boolean success = wrapper.bindService();

        assertTrue( success );
    }

    public void test_unbindService() throws RemoteException
    {
        wrapper.unbindService();

        Mockito.verify( context ).unbindService( Mockito.same( wrapper ) );
    }

    // ============================================================
    // Test delegation to bound service
    // ============================================================

    public void test_connect_delegates_to_stub() throws RemoteException
    {
        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );
        OpenVpnConfig param = new OpenVpnConfig( new File( "test" ) );

        wrapper.connect( param );

        Mockito.verify( openVpnServiceStub ).connect( param );
    }

    public void test_connect_RemoteException_disables_binding() throws RemoteException
    {
        Mockito.doThrow( new RemoteException() ).when( openVpnServiceStub ).connect( Mockito.any( OpenVpnConfig.class ) );
        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );

        wrapper.connect( new OpenVpnConfig( new File( "test" ) ) );

        assertFalse( wrapper.isBound() );
    }


    public void test_supplyCredentials_delegates_to_stub() throws RemoteException
    {
        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );
        OpenVpnCredentials param = new OpenVpnCredentials( "u", "p" );

        wrapper.supplyCredentials( param );

        Mockito.verify( openVpnServiceStub ).supplyCredentials( param );
    }

    public void test_supplyCredentials_RemoteException_disables_binding() throws RemoteException
    {
        Mockito.doThrow( new RemoteException() ).when( openVpnServiceStub ).supplyCredentials( Mockito.any( OpenVpnCredentials.class ) );
        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );

        wrapper.supplyCredentials( new OpenVpnCredentials( "u", "p" ) );

        assertFalse( wrapper.isBound() );
    }


    public void test_supplyPassphrase_delegates_to_stub() throws RemoteException
    {
        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );
        OpenVpnPassphrase param = new OpenVpnPassphrase( "p" );

        wrapper.supplyPassphrase( param );

        Mockito.verify( openVpnServiceStub ).supplyPassphrase( param );
    }

    public void test_supplyPassphrase_RemoteException_disables_binding() throws RemoteException
    {
        Mockito.doThrow( new RemoteException() ).when( openVpnServiceStub ).supplyPassphrase( Mockito.any( OpenVpnPassphrase.class ) );
        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );

        wrapper.supplyPassphrase( new OpenVpnPassphrase( "p" ) );

        assertFalse( wrapper.isBound() );
    }


    public void test_getStatus_delegates_to_stub() throws RemoteException
    {
        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );

        wrapper.getStatus();

        Mockito.verify( openVpnServiceStub ).getStatus();
    }

    public void test_getStatus_RemoteException_disables_binding() throws RemoteException
    {
        Mockito.when( openVpnServiceStub.getStatus() ).thenThrow( new RemoteException() );
        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );

        wrapper.getStatus();

        assertFalse( wrapper.isBound() );
    }

    public void test_getStatus_RemoteException_returns_stopped() throws RemoteException
    {
        Mockito.when( openVpnServiceStub.getStatus() ).thenThrow( new RemoteException() );
        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );

        OpenVpnState status = wrapper.getStatus();

        assertFalse( status.isStarted() );
    }


    public void test_getStatusFor_delegates_to_stub() throws RemoteException
    {
        OpenVpnConfig config = new OpenVpnConfig( new File( "/dev/null" ) );
        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );

        wrapper.getStatusFor( config );

        Mockito.verify( openVpnServiceStub ).getStatusFor( Mockito.same( config ) );
    }

    public void test_getStatusFor_RemoteException_disables_binding() throws RemoteException
    {
        OpenVpnConfig config = new OpenVpnConfig( new File( "/dev/null" ) );
        Mockito.when( openVpnServiceStub.getStatusFor( config ) ).thenThrow( new RemoteException() );
        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );

        wrapper.getStatusFor( config );

        assertFalse( wrapper.isBound() );
    }

    public void test_getStatusFor_RemoteException_returns_stopped() throws RemoteException
    {
        OpenVpnConfig config = new OpenVpnConfig( new File( "/dev/null" ) );
        Mockito.when( openVpnServiceStub.getStatusFor( config ) ).thenThrow( new RemoteException() );
        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );

        OpenVpnState status = wrapper.getStatusFor( config );

        assertFalse( status.isStarted() );
    }


    public void test_disconnect_delegates_to_stub() throws RemoteException
    {
        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );

        wrapper.disconnect();

        Mockito.verify( openVpnServiceStub ).disconnect();
    }

    public void test_disconnect_RemoteException_disables_binding() throws RemoteException
    {
        Mockito.doThrow( new RemoteException() ).when( openVpnServiceStub ).disconnect();
        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );

        wrapper.disconnect();

        assertFalse( wrapper.isBound() );
    }

    public void test_addOpenVpnStateListener_does_not_accept_null() throws RemoteException
    {
        try
        {
            wrapper.addOpenVpnStateListener( null );
            fail( "NullPointerException expected" );
        }
        catch (NullPointerException e)
        {
            assertEquals( "listener is null in addOpenVpnStateListener", e.getMessage() );
        }
    }

    public void test_addOpenVpnStateListener_delegates_to_stub() throws RemoteException
    {
        IOpenVpnStateListener param = mock( IOpenVpnStateListener.class );
        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );

        wrapper.addOpenVpnStateListener( param );

        Mockito.verify( openVpnServiceStub ).addOpenVpnStateListener( param );
    }

    public void test_addOpenVpnStateListener_RemoteException_disables_binding() throws RemoteException
    {
        IOpenVpnStateListener param = mock( IOpenVpnStateListener.class );
        Mockito.doThrow( new RemoteException() ).when( openVpnServiceStub ).addOpenVpnStateListener( param );
        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );

        wrapper.addOpenVpnStateListener( param );

        assertFalse( wrapper.isBound() );
    }


    public void test_removeOpenVpnStateListener_delegates_to_stub() throws RemoteException
    {
        IOpenVpnStateListener param = mock( IOpenVpnStateListener.class );
        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );

        wrapper.removeOpenVpnStateListener( param );

        Mockito.verify( openVpnServiceStub ).removeOpenVpnStateListener( param );
    }

    public void test_removeOpenVpnStateListener_RemoteException_disables_binding() throws RemoteException
    {
        IOpenVpnStateListener param = mock( IOpenVpnStateListener.class );
        Mockito.doThrow( new RemoteException() ).when( openVpnServiceStub ).removeOpenVpnStateListener( param );
        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );

        wrapper.removeOpenVpnStateListener( param );

        assertFalse( wrapper.isBound() );
    }


    public void test_removeOpenVpnStateListener_does_not_accept_null() throws RemoteException
    {
        try
        {
            wrapper.removeOpenVpnStateListener( null );
            fail( "NullPointerException expected" );
        }
        catch (NullPointerException e)
        {
            assertEquals( "listener is null in removeOpenVpnStateListener", e.getMessage() );
        }
    }



    public void test_onServiceConnected_registers_listeners() throws RemoteException
    {
        IOpenVpnStateListener param = mock( IOpenVpnStateListener.class );
        wrapper.addOpenVpnStateListener( param );

        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );

        Mockito.verify( openVpnServiceStub ).addOpenVpnStateListener( param );
    }

    public void test_onServiceConnected_registers_identical_listeners_only_once() throws RemoteException
    {
        IOpenVpnStateListener param = mock( IOpenVpnStateListener.class );
        wrapper.addOpenVpnStateListener( param );
        wrapper.addOpenVpnStateListener( param );

        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );

        Mockito.verify( openVpnServiceStub ).addOpenVpnStateListener( param );
    }

    public void test_onServiceConnected_registers_several_listeners() throws RemoteException
    {
        IOpenVpnStateListener param1 = mock( IOpenVpnStateListener.class );
        IOpenVpnStateListener param2 = mock( IOpenVpnStateListener.class );
        IOpenVpnStateListener param3 = mock( IOpenVpnStateListener.class );
        wrapper.addOpenVpnStateListener( param1 );
        wrapper.addOpenVpnStateListener( param2 );
        wrapper.addOpenVpnStateListener( param3 );

        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );

        Mockito.verify( openVpnServiceStub ).addOpenVpnStateListener( param1 );
        Mockito.verify( openVpnServiceStub ).addOpenVpnStateListener( param2 );
        Mockito.verify( openVpnServiceStub ).addOpenVpnStateListener( param3 );
    }

    public void test_onServiceConnected_registers_only_listeners_which_where_not_removed() throws RemoteException
    {
        IOpenVpnStateListener param1 = mock( IOpenVpnStateListener.class );
        IOpenVpnStateListener param2 = mock( IOpenVpnStateListener.class );
        IOpenVpnStateListener param3 = mock( IOpenVpnStateListener.class );
        wrapper.addOpenVpnStateListener( param1 );
        wrapper.addOpenVpnStateListener( param2 );
        wrapper.addOpenVpnStateListener( param3 );
        wrapper.removeOpenVpnStateListener( param2 );

        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );

        Mockito.verify( openVpnServiceStub ).addOpenVpnStateListener( param1 );
        Mockito.verify( openVpnServiceStub, never() ).addOpenVpnStateListener( param2 );
        Mockito.verify( openVpnServiceStub ).addOpenVpnStateListener( param3 );
    }


    public void test_pauseListeners() throws RemoteException
    {
        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );

        IOpenVpnStateListener param1 = mock( IOpenVpnStateListener.class );
        IOpenVpnStateListener param2 = mock( IOpenVpnStateListener.class );
        IOpenVpnStateListener param3 = mock( IOpenVpnStateListener.class );
        wrapper.addOpenVpnStateListener( param1 );
        wrapper.addOpenVpnStateListener( param2 );
        wrapper.addOpenVpnStateListener( param3 );

        wrapper.pauseListeners();

        Mockito.verify( openVpnServiceStub ).removeOpenVpnStateListener( param1 );
        Mockito.verify( openVpnServiceStub ).removeOpenVpnStateListener( param2 );
        Mockito.verify( openVpnServiceStub ).removeOpenVpnStateListener( param3 );
    }

    public void test_resumeListeners() throws RemoteException
    {
        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );

        IOpenVpnStateListener param1 = mock( IOpenVpnStateListener.class );
        IOpenVpnStateListener param2 = mock( IOpenVpnStateListener.class );
        IOpenVpnStateListener param3 = mock( IOpenVpnStateListener.class );
        wrapper.addOpenVpnStateListener( param1 );
        wrapper.addOpenVpnStateListener( param2 );
        wrapper.addOpenVpnStateListener( param3 );
        resetOpenVpnServiceStub();
        wrapper.pauseListeners();

        wrapper.resumeListeners();

        Mockito.verify( openVpnServiceStub ).addOpenVpnStateListener( param1 );
        Mockito.verify( openVpnServiceStub ).addOpenVpnStateListener( param2 );
        Mockito.verify( openVpnServiceStub ).addOpenVpnStateListener( param3 );
    }

    public void test_resumeListeners_before_onServiceConnected() throws RemoteException
    {
        IOpenVpnStateListener param1 = mock( IOpenVpnStateListener.class );
        IOpenVpnStateListener param2 = mock( IOpenVpnStateListener.class );
        IOpenVpnStateListener param3 = mock( IOpenVpnStateListener.class );
        wrapper.addOpenVpnStateListener( param1 );
        wrapper.addOpenVpnStateListener( param2 );
        wrapper.addOpenVpnStateListener( param3 );

        wrapper.resumeListeners();
        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );

        Mockito.verify( openVpnServiceStub ).addOpenVpnStateListener( param1 );
        Mockito.verify( openVpnServiceStub ).addOpenVpnStateListener( param2 );
        Mockito.verify( openVpnServiceStub ).addOpenVpnStateListener( param3 );
    }

    public void test_resumeListeners_after_onServiceConnected() throws RemoteException
    {
        IOpenVpnStateListener param1 = mock( IOpenVpnStateListener.class );
        IOpenVpnStateListener param2 = mock( IOpenVpnStateListener.class );
        IOpenVpnStateListener param3 = mock( IOpenVpnStateListener.class );
        wrapper.addOpenVpnStateListener( param1 );
        wrapper.addOpenVpnStateListener( param2 );
        wrapper.addOpenVpnStateListener( param3 );

        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );
        wrapper.resumeListeners();

        Mockito.verify( openVpnServiceStub ).addOpenVpnStateListener( param1 );
        Mockito.verify( openVpnServiceStub ).addOpenVpnStateListener( param2 );
        Mockito.verify( openVpnServiceStub ).addOpenVpnStateListener( param3 );
    }

    public void test_resumeListeners_after_onServiceDisconnected() throws RemoteException
    {
        IOpenVpnStateListener param1 = mock( IOpenVpnStateListener.class );
        IOpenVpnStateListener param2 = mock( IOpenVpnStateListener.class );
        IOpenVpnStateListener param3 = mock( IOpenVpnStateListener.class );
        wrapper.addOpenVpnStateListener( param1 );
        wrapper.addOpenVpnStateListener( param2 );
        wrapper.addOpenVpnStateListener( param3 );
        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );
        wrapper.resumeListeners();
        resetOpenVpnServiceStub();

        wrapper.onServiceDisconnected( COMPONENT_NAME );
        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );

        Mockito.verify( openVpnServiceStub ).addOpenVpnStateListener( param1 );
        Mockito.verify( openVpnServiceStub ).addOpenVpnStateListener( param2 );
        Mockito.verify( openVpnServiceStub ).addOpenVpnStateListener( param3 );
    }

    /**
     * This tests also covers installment of the {@code BroadcastReceiver}.
     */
    public void test_bindService_installs_BroadcastReceiver()
    {
        wrapper.bindService();
        ArgumentCaptor<BroadcastReceiver> broadcastReceiverArgumentCaptor = ArgumentCaptor.forClass( BroadcastReceiver.class );
        ArgumentCaptor<IntentFilter> intentFilterArgumentCaptor = ArgumentCaptor.forClass( IntentFilter.class );

        verify( context ).registerReceiver( broadcastReceiverArgumentCaptor.capture(), intentFilterArgumentCaptor.capture() );

        BroadcastReceiver broadcastReceiver = broadcastReceiverArgumentCaptor.getValue();
        assertNotNull( broadcastReceiver );

        IntentFilter intentFilter = intentFilterArgumentCaptor.getValue();
        assertTrue( intentFilter.hasAction( Intents.OPENVPN_STATE_CHANGED.getAction() ) );
    }

    public void test_binds_when_OPENVPN_STATE_CHANGED_broadcast_is_received()
    {
        wrapper.bindService();
        ArgumentCaptor<BroadcastReceiver> broadcastReceiverArgumentCaptor = ArgumentCaptor.forClass( BroadcastReceiver.class );
        verify( context ).registerReceiver( broadcastReceiverArgumentCaptor.capture(), any(IntentFilter.class) );
        BroadcastReceiver broadcastReceiver = broadcastReceiverArgumentCaptor.getValue();
        reset( context );

        broadcastReceiver.onReceive( context, new Intent( Intents.OPENVPN_STATE_CHANGED.getAction() ) );

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass( Intent.class );
        Mockito.verify( context ).bindService( intentCaptor.capture(), Mockito.same( wrapper ), Mockito.eq( 0 ) );
        assert_intentAddressesOpenVpnService_with_no_action( intentCaptor.getValue() );
    }

    public void test_unbindService_removes_BroadcastReceiver()
    {
        wrapper.bindService();
        ArgumentCaptor<BroadcastReceiver> broadcastReceiverArgumentCaptor = ArgumentCaptor.forClass( BroadcastReceiver.class );
        verify( context ).registerReceiver( broadcastReceiverArgumentCaptor.capture(), any( IntentFilter.class ) );
        BroadcastReceiver broadcastReceiver = broadcastReceiverArgumentCaptor.getValue();

        wrapper.unbindService();

        verify( context ).unregisterReceiver( broadcastReceiver );
    }

    /*
     * Test if ServiceConnection hooks
     */
    private final AtomicBoolean onServiceConnectedCalled = new AtomicBoolean( false );
    private final AtomicBoolean onServiceDisconnectedCalled = new AtomicBoolean( false );
    public void test_onServiceConnectedHook_is_called_when_listeners_are_not_paused()
    {
        assertFalse( onServiceConnectedCalled.get() );
        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );
        assertTrue( onServiceConnectedCalled.get() );
    }
    public void test_onServiceConnectedHook_is_not_called_when_listeners_are_paused()
    {
        assertFalse( onServiceConnectedCalled.get() );
        wrapper.pauseListeners();
        wrapper.onServiceConnected( COMPONENT_NAME, openVpnServiceStub );
        assertFalse( onServiceConnectedCalled.get() );
    }
    public void test_onServiceDisconnectedHook_is_called_when_listeners_are_not_paused()
    {
        assertFalse( onServiceDisconnectedCalled.get() );
        wrapper.onServiceDisconnected( COMPONENT_NAME );
        assertTrue( onServiceDisconnectedCalled.get() );
    }
    public void test_onServiceDisconnectedHook_is_not_called_when_listeners_are_paused()
    {
        assertFalse( onServiceDisconnectedCalled.get() );
        wrapper.pauseListeners();
        wrapper.onServiceDisconnected( COMPONENT_NAME );
        assertFalse( onServiceDisconnectedCalled.get() );
    }
}
