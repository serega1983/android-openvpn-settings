///*
// * This file is part of OpenVPN-Settings.
// *
// * Copyright © 2009-2012  Friedrich Schäuffelhut
// *
// * OpenVPN-Settings is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * OpenVPN-Settings is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with OpenVPN-Settings.  If not, see <http://www.gnu.org/licenses/>.
// *
// * Report bugs or new features at: http://code.google.com/p/android-openvpn-settings/
// * Contact the author at:          android.openvpn@schaeuffelhut.de
// */
//
//package de.schaeuffelhut.android.openvpn.lib.service.impl;
//
//import android.content.Intent;
//import android.test.ServiceTestCase;
//import de.schaeuffelhut.android.openvpn.Intents;
//import de.schaeuffelhut.android.openvpn.services.OpenVpnService;
//import de.schaeuffelhut.android.util.MockitoSupport;
//
//import java.io.File;
//
///**
// * @author Friedrich Schäuffelhut
// * @since 2012-11-05
// */
//public class OpenVpnServiceIntentApiTest extends ServiceTestCase<OpenVpnService>
//{
//    private static DaemonMonitorMockFactory daemonMonitorFactory; //Ugly hack: Store in static field so createServiceDelegate() can access it.
//
//    private static class OpenVpnServiceSut extends OpenVpnService {
//        @Override
//        protected OpenVpnServiceImpl createServiceDelegate()
//        {
//            OpenVpnServiceImpl serviceDelegate = super.createServiceDelegate();
//            serviceDelegate.setDaemonMonitorFactory( daemonMonitorFactory );
//            return serviceDelegate;
//        }
//    }
//
//    public OpenVpnServiceIntentApiTest()
//    {
//        super( OpenVpnService.class );
//    }
//
//    private OpenVpnServiceImpl getServiceDelegate()
//    {
//        return super.getService().getServiceDelegate();
//    }
//
//    @Override
//    public void setUp() throws Exception
//    {
//        super.setUp();
//        daemonMonitorFactory = new DaemonMonitorMockFactory();
//        MockitoSupport.workaroundMockitoClassloaderIssue();
//        setupService();
////        getServiceDelegate().setDaemonMonitorFactory( daemonMonitorFactory );
//    }
//
//    public void test_startService_daemon_with_null_intent()
//    {
//        try
//        {
//            startService( null );
//            assertFalse( getServiceDelegate().getCurrent().isAlive() );
//            // no action expected
//        }
//        catch (NullPointerException e)
//        {
//            fail( "Should avoid NullPointerException" );
//        }
//    }
//
//    public void test_startService_daemonStart()
//    {
//        File configFile = new File( "/sdcard/openvpn/test-" + System.currentTimeMillis() + ".conf" );
//        Intent intent = new Intent( Intents.START_DAEMON );
//        intent.putExtra( Intents.EXTRA_CONFIG, configFile.getAbsolutePath() );
//        startService( intent );
//
//        assertTrue( getServiceDelegate().getCurrent().isAlive() );
//        assertEquals( configFile, getServiceDelegate().getCurrent().getConfigFile() );
//    }
//
//    public void test_startService_daemon_start_without_EXTRA_CONFIG()
//    {
//        try
//        {
//            startService( new Intent( Intents.START_DAEMON ) );
//            assertFalse( getServiceDelegate().getCurrent().isAlive() );
//            // no action expected
//        }
//        catch (NullPointerException e)
//        {
//            fail( "Should avoid NullPointerException" );
//        }
//    }
//
//    public void test_startService_daemonStop()
//    {
//        File configFile = new File( "/sdcard/openvpn/test-" + System.currentTimeMillis() + ".conf" );
//
//        startService( null );
//        {
//            Intent intent = new Intent( Intents.START_DAEMON );
//            intent.putExtra( Intents.EXTRA_CONFIG, configFile.getAbsolutePath() );
//            getServiceDelegate().onStart( intent, 0 );
//        }
//        assertTrue( getServiceDelegate().getCurrent().isAlive() );
//        assertEquals( configFile, getServiceDelegate().getCurrent().getConfigFile() );
//
//        {
//            Intent intent = new Intent( Intents.STOP_DAEMON );
//            intent.putExtra( Intents.EXTRA_CONFIG, configFile.getAbsolutePath() );
//            getServiceDelegate().onStart( intent, 0 );
//        }
//        assertFalse( getServiceDelegate().getCurrent().isAlive() );
//        assertEquals( configFile, getServiceDelegate().getCurrent().getConfigFile() );
//    }
//
//}
