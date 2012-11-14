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

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import junit.framework.TestCase;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.File;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-11-13
 */
public class OpenVpnServiceWrapperTest extends TestCase
{
    private static interface Stub extends IOpenVpnService, IBinder {}

    private Context context = Mockito.mock( Context.class );
    private OpenVpnServiceWrapper wrapper = new OpenVpnServiceWrapper( context );
    private Stub  stub = Mockito.mock( Stub.class );

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        Mockito.when( stub.queryLocalInterface( Mockito.anyString() ) ).thenReturn( stub );
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
        assertEquals( "de.schaeuffelhut.android.openvpn", OpenVpnServiceWrapper.COMPONENT_NAME.getPackageName() );
        assertEquals( "de.schaeuffelhut.android.openvpn.services.OpenVpnService", OpenVpnServiceWrapper.COMPONENT_NAME.getClassName() );
    }

    public void test_createIntentAddressingOpenVpnService_has_no_action()
    {
        assertEquals( null, OpenVpnServiceWrapper.createIntentAddressingOpenVpnService().getAction() );
    }

    public void test_createIntentAddressingOpenVpnService_verify_componentName()
    {
        assertEquals( OpenVpnServiceWrapper.COMPONENT_NAME, OpenVpnServiceWrapper.createIntentAddressingOpenVpnService().getComponent() );
    }

    private void assert_intentAddressesOpenVpnService_with_no_action(Intent intent)
    {
        assertEquals( null, intent.getAction() );
        assertEquals( OpenVpnServiceWrapper.COMPONENT_NAME, intent.getComponent() );
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
        wrapper.onServiceConnected( null, stub );

        assertTrue( wrapper.isBound() );
    }

    public void test_isBound_after_onServiceDisconnected()
    {
        wrapper.onServiceConnected( null, stub );
        wrapper.onServiceDisconnected( null );

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
        Mockito.when( context.startService(  Mockito.any( Intent.class ) ) ).thenReturn( OpenVpnServiceWrapper.COMPONENT_NAME );

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

        ArgumentCaptor<Intent> intentCaptor = new ArgumentCaptor<Intent>();
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
        wrapper.onServiceConnected( null, stub );
        OpenVpnConfig param = new OpenVpnConfig( new File( "test" ) );

        wrapper.connect( param );

        Mockito.verify( stub ).connect( param );
    }

    public void test_connect_RemoteException_disables_binding() throws RemoteException
    {
        Mockito.doThrow( new RemoteException() ).when( stub ).connect( Mockito.any( OpenVpnConfig.class ) );
        wrapper.onServiceConnected( null, stub );

        wrapper.connect( new OpenVpnConfig( new File( "test" ) ) );

        assertFalse( wrapper.isBound() );
    }


    public void test_supplyCredentials_delegates_to_stub() throws RemoteException
    {
        wrapper.onServiceConnected( null, stub );
        OpenVpnCredentials param = new OpenVpnCredentials( "u", "p" );

        wrapper.supplyCredentials( param );

        Mockito.verify( stub ).supplyCredentials( param );
    }

    public void test_supplyCredentials_RemoteException_disables_binding() throws RemoteException
    {
        Mockito.doThrow( new RemoteException() ).when( stub ).supplyCredentials( Mockito.any( OpenVpnCredentials.class ) );
        wrapper.onServiceConnected( null, stub );

        wrapper.supplyCredentials( new OpenVpnCredentials( "u", "p" ) );

        assertFalse( wrapper.isBound() );
    }


    public void test_supplyPassphrase_delegates_to_stub() throws RemoteException
    {
        wrapper.onServiceConnected( null, stub );
        OpenVpnPassphrase param = new OpenVpnPassphrase( "p" );

        wrapper.supplyPassphrase( param );

        Mockito.verify( stub ).supplyPassphrase( param );
    }

    public void test_supplyPassphrase_RemoteException_disables_binding() throws RemoteException
    {
        Mockito.doThrow( new RemoteException() ).when( stub ).supplyPassphrase( Mockito.any( OpenVpnPassphrase.class ) );
        wrapper.onServiceConnected( null, stub );

        wrapper.supplyPassphrase( new OpenVpnPassphrase( "p" ) );

        assertFalse( wrapper.isBound() );
    }


    public void test_getStatus_delegates_to_stub() throws RemoteException
    {
        wrapper.onServiceConnected( null, stub );

        wrapper.getStatus();

        Mockito.verify( stub ).getStatus();
    }

    public void test_getStatus_RemoteException_disables_binding() throws RemoteException
    {
        Mockito.doThrow( new RemoteException() ).when( stub ).getStatus();
        wrapper.onServiceConnected( null, stub );

        wrapper.getStatus();

        assertFalse( wrapper.isBound() );
    }

    public void test_getStatus_RemoteException_returns_stopped() throws RemoteException
    {
        Mockito.doThrow( new RemoteException() ).when( stub ).getStatus();
        wrapper.onServiceConnected( null, stub );

        OpenVpnState status = wrapper.getStatus();

        assertFalse( status.isStarted() );
    }


    public void test_disconnect_delegates_to_stub() throws RemoteException
    {
        wrapper.onServiceConnected( null, stub );

        wrapper.disconnect();

        Mockito.verify( stub ).disconnect();
    }

    public void test_disconnect_RemoteException_disables_binding() throws RemoteException
    {
        Mockito.doThrow( new RemoteException() ).when( stub ).disconnect();
        wrapper.onServiceConnected( null, stub );

        wrapper.disconnect();

        assertFalse( wrapper.isBound() );
    }

}
