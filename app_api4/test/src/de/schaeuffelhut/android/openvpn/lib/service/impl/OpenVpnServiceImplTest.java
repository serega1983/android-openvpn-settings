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
//import android.preference.PreferenceManager;
//import android.test.ServiceTestCase;
//import de.schaeuffelhut.android.openvpn.Preferences;
//import de.schaeuffelhut.android.openvpn.service.api.OpenVpnConfig;
//import de.schaeuffelhut.android.openvpn.shared.util.service.DelegatingService;
//import junit.framework.Assert;
//import org.mockito.Mockito;
//import org.mockito.invocation.InvocationOnMock;
//import org.mockito.stubbing.Answer;
//
//import java.io.File;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
///**
// * @author Friedrich Schäuffelhut
// * @since 2012-11-02
// */
//public class OpenVpnServiceImplTest extends ServiceTestCase<OpenVpnServiceImplTest.MockOpenVpnServiceImpl>
//{
//    private static DaemonMonitorMockFactory daemonMonitorFactory; //Ugly hack: Store in static field so createServiceDelegate() can access it.
//    private static List<File> configs;
//
//    public static class MockOpenVpnServiceImpl extends DelegatingService<OpenVpnServiceImpl>
//    {
//        public MockOpenVpnServiceImpl()
//        {
//            super();
//        }
//
//        @Override
//        protected OpenVpnServiceImpl createServiceDelegate()
//        {
//            OpenVpnServiceImpl openVpnService = new OpenVpnServiceImpl(
//                    this,
//                    new IfConfigFactoryImpl(),
//                    new CmdLineBuilder4( getApplicationContext() )
//            ) {
//                @Override
//                protected List<File> listConfigs()
//                {
//                    return configs;
//                }
//            };
//            openVpnService.setDaemonMonitorFactory( daemonMonitorFactory );
//            return openVpnService;
//        }
//    }
//
//    public OpenVpnServiceImplTest()
//    {
//        super( MockOpenVpnServiceImpl.class );
//    }
//
//    @Override
//    protected void setUp() throws Exception
//    {
//        super.setUp();
//        daemonMonitorFactory = new DaemonMonitorMockFactory();
//        configs = Collections.emptyList();
//        setupService();
//    }
//
//    public OpenVpnServiceImpl getServiceDelegate()
//    {
//        return super.getService().getServiceDelegate();
//    }
//
//    private boolean shutdownServiceCalled = false;
//
//    /**
//     * Prevent shutdownService() being called a second time
//     * by tearDown() when it was called before in the test method.
//     */
//    @Override
//    protected void shutdownService()
//    {
//        if (!shutdownServiceCalled)
//        {
//            shutdownServiceCalled = true;
//            super.shutdownService();
//        }
//    }
//
//    public void test_onCreate_sets_isServiceStarted() throws InterruptedException
//    {
//        assertFalse( OpenVpnServiceImpl.isServiceStarted() );
//
//        startService( new Intent( getContext(), MockOpenVpnServiceImpl.class ) );
//        assertTrue( OpenVpnServiceImpl.isServiceStarted() );
//    }
//
//    public void test_onDestroy_clears_isServiceStarted() throws InterruptedException
//    {
//        startService( new Intent( getContext(), MockOpenVpnServiceImpl.class ) );
//        shutdownService();
//
//        assertFalse( OpenVpnServiceImpl.isServiceStarted() );
//    }
//
//    public void test_onCreate_sets_Preference_KEY_OPENVPN_ENABLED() throws InterruptedException
//    {
//        PreferenceManager.getDefaultSharedPreferences( getContext() ).edit().putBoolean(
//                Preferences.KEY_OPENVPN_ENABLED, false
//        ).commit();
//
//        startService( new Intent( getContext(), MockOpenVpnServiceImpl.class ) );
//        assertTrue( PreferenceManager.getDefaultSharedPreferences( getContext() ).getBoolean( Preferences.KEY_OPENVPN_ENABLED, false ) );
//    }
//
//    public void test_onDestroy_clears_Preference_KEY_OPENVPN_ENABLED() throws InterruptedException
//    {
//        PreferenceManager.getDefaultSharedPreferences( getContext() ).edit().putBoolean(
//                Preferences.KEY_OPENVPN_ENABLED, false
//        ).commit();
//
//        startService( new Intent( getContext(), MockOpenVpnServiceImpl.class ) );
//        shutdownService();
//
//        assertFalse( PreferenceManager.getDefaultSharedPreferences( getContext() ).getBoolean( Preferences.KEY_OPENVPN_ENABLED, true ) );
//    }
//
//    /**
//     * Assert service attached to the only one running daemon which is enabled.
//     */
//    public void test_onCreate_attaches_with_dead_alive_configs()
//    {
//
//        File configFile0 = new File( "/sdcard/openvpn/test0-DEAD.conf" );
//        File configFile1 = new File( "/sdcard/openvpn/test1-ALIVE.conf" );
//        configs = Arrays.asList( new File[]{
//                configFile1,
//        } );
//        PreferenceManager.getDefaultSharedPreferences( getContext() )
//                .edit()
//                .putBoolean( Preferences.KEY_CONFIG_INTENDED_STATE( configFile0 ), false )
//                .putBoolean( Preferences.KEY_CONFIG_INTENDED_STATE( configFile1 ), true )
//                .commit();
//
//        startService( new Intent( getContext(), MockOpenVpnServiceImpl.class ) );
//
//        assertTrue( daemonMonitorFactory.getLastMockDaemonMonitorCreated().isAlive() );
//        assertEquals( configFile1, daemonMonitorFactory.getLastMockDaemonMonitorCreated().getConfigFile() );
//    }
//
//    /**
//     * Assert multiple running daemons with multiple enabled configs will be disabled.
//     */
//    public void test_onCreate_attaches_with_dead_alive_alive_configs()
//    {
//        File configFile0 = new File( "/sdcard/openvpn/test0-DEAD.conf" );
//        File configFile1 = new File( "/sdcard/openvpn/test1-ALIVE.conf" );
//        final File configFile2 = new File( "/sdcard/openvpn/test2-ALIVE.conf" );
//        configs = Arrays.asList( new File[]{
//                configFile0,
//                configFile1,
//                configFile2,
//        } );
//        PreferenceManager.getDefaultSharedPreferences( getContext() )
//                .edit()
//                .putBoolean( Preferences.KEY_CONFIG_INTENDED_STATE( configFile0 ), false )
//                .putBoolean( Preferences.KEY_CONFIG_INTENDED_STATE( configFile1 ), true )
//                .putBoolean( Preferences.KEY_CONFIG_INTENDED_STATE( configFile2 ), true )
//                .commit();
//
//        DaemonMonitorMockFactory daemonMonitorFactory = new DaemonMonitorMockFactory(){
//            @Override
//            public DaemonMonitor createDaemonMonitorFor(final File configFile)
//            {
//                final DaemonMonitor daemonMonitor = super.createDaemonMonitorFor( configFile );
//                Mockito.doAnswer( new Answer()
//                {
//                    public Object answer(InvocationOnMock invocation) throws Throwable
//                    {
//                        if ( PreferenceManager.getDefaultSharedPreferences( getContext() ).getBoolean(
//                             Preferences.KEY_CONFIG_INTENDED_STATE( configFile ), false
//                        ) )
//                            daemonMonitor.start();
//                        else
//                            daemonMonitor.stop();
//                        return null;
//                    }
//                }).when( daemonMonitor ).switchToIntendedState();
//                return daemonMonitor;
//            }
//        };
//        //getServiceDelegate().setDaemonMonitorFactory( daemonMonitorFactory );
//
//        startService( new Intent( getContext(), MockOpenVpnServiceImpl.class ) );
//
//        assertFalse( daemonMonitorFactory.getLastMockDaemonMonitorCreated().isAlive() );
//        assertEquals( configFile2, daemonMonitorFactory.getLastMockDaemonMonitorCreated().getConfigFile() );
//    }
//
//    public void test_daemonStart() throws InterruptedException
//    {
//        OpenVpnConfig configFile = new OpenVpnConfig( new File( "/sdcard/openvpn/test-" + System.currentTimeMillis() + ".conf" ) );
//        startService( new Intent( getContext(), MockOpenVpnServiceImpl.class ) );
//
//        getServiceDelegate().daemonStart( configFile );
//
//        assertTrue( getServiceDelegate().getCurrent().isAlive() );
//        Assert.assertEquals( configFile.getFile(), getServiceDelegate().getCurrent().getConfigFile() );
//    }
//
//    public void test_daemonStart_A_then_daemonStart_B() throws InterruptedException
//    {
//        OpenVpnConfig configFileA = new OpenVpnConfig( new File( "/sdcard/openvpn/test-A" + System.currentTimeMillis() + ".conf" ) );
//        OpenVpnConfig configFileB = new OpenVpnConfig( new File( "/sdcard/openvpn/test-B" + System.currentTimeMillis() + ".conf" ) );
//        startService( new Intent( getContext(), MockOpenVpnServiceImpl.class ) );
//
//        getServiceDelegate().daemonStart( configFileA );
//        DaemonMonitor daemonMonitorA = getServiceDelegate().getCurrent();
//        getServiceDelegate().daemonStart( configFileB );
//        DaemonMonitor daemonMonitorB = getServiceDelegate().getCurrent();
//
//        assertFalse( daemonMonitorA.isAlive() );
//        assertEquals( configFileA.getFile(), daemonMonitorA.getConfigFile() );
//        assertTrue( daemonMonitorB.isAlive() );
//        assertEquals( configFileB.getFile(), daemonMonitorB.getConfigFile() );
//    }
//
//    public void test_daemonStop() throws InterruptedException
//    {
//        OpenVpnConfig configFile = new OpenVpnConfig( new File( "/sdcard/openvpn/test-" + System.currentTimeMillis() + ".conf" ) );
//        startService( new Intent( getContext(), MockOpenVpnServiceImpl.class ) );
//        getServiceDelegate().daemonStart( configFile );
//
//        getServiceDelegate().daemonStop( configFile.getFile() );
//
//        assertFalse( getServiceDelegate().getCurrent().isAlive() );
//    }
//
//    public void test_daemonPassphrase() throws InterruptedException
//    {
//        OpenVpnConfig configFile = new OpenVpnConfig( new File( "/sdcard/openvpn/test-" + System.currentTimeMillis() + ".conf" ) );
//        String passphrase = "passphrase" + System.currentTimeMillis();
//        startService( new Intent( getContext(), MockOpenVpnServiceImpl.class ) );
//        getServiceDelegate().daemonStart( configFile );
//
//        getServiceDelegate().daemonPassphrase( configFile.getFile(), passphrase );
//
//        Mockito.verify( getServiceDelegate().getCurrent() ).supplyPassphrase( passphrase );
//    }
//
//    public void test_daemonUsernamePassword() throws InterruptedException
//    {
//        OpenVpnConfig configFile = new OpenVpnConfig( new File( "/sdcard/openvpn/test-" + System.currentTimeMillis() + ".conf" ) );
//        String username = "username" + System.currentTimeMillis();
//        String passphrase = "passphrase" + System.currentTimeMillis();
//        startService( new Intent( getContext(), MockOpenVpnServiceImpl.class ) );
//        getServiceDelegate().daemonStart( configFile );
//
//        getServiceDelegate().daemonUsernamePassword( configFile.getFile(), username, passphrase );
//
//        Mockito.verify( getServiceDelegate().getCurrent() ).supplyUsernamePassword( username, passphrase );
//    }
//
//    public void test_isDaemonStarted() throws InterruptedException
//    {
//        OpenVpnConfig configFile = new OpenVpnConfig( new File( "/sdcard/openvpn/test-" + System.currentTimeMillis() + ".conf" ) );
//        startService( new Intent( getContext(), MockOpenVpnServiceImpl.class ) );
//
//        assertFalse( getServiceDelegate().isDaemonStarted( configFile.getFile() ) );
//
//        getServiceDelegate().daemonStart( configFile );
//        assertTrue( getServiceDelegate().isDaemonStarted( configFile.getFile() ) );
//
//        getServiceDelegate().daemonStop( configFile.getFile() );
//        assertFalse( getServiceDelegate().isDaemonStarted( configFile.getFile() ) );
//    }
//
//    public void test_hasDaemonsStarted() throws InterruptedException
//    {
//        OpenVpnConfig configFile = new OpenVpnConfig( new File( "/sdcard/openvpn/test-" + System.currentTimeMillis() + ".conf" ) );
//        startService( new Intent( getContext(), MockOpenVpnServiceImpl.class ) );
//
//        assertFalse( getServiceDelegate().hasDaemonsStarted() );
//
//        getServiceDelegate().daemonStart( configFile );
//        assertTrue( getServiceDelegate().hasDaemonsStarted() );
//
//        getServiceDelegate().daemonStop( configFile.getFile() );
//        assertFalse( getServiceDelegate().hasDaemonsStarted() );
//    }
//}
