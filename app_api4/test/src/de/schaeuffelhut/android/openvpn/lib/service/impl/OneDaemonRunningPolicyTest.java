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

import junit.framework.TestCase;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-11-03
 */
public class OneDaemonRunningPolicyTest extends TestCase
{
    public void test_getCurrent__with_no_configs()
    {
        OneDaemonRunningPolicy oneDaemonRunningPolicy = createOneDaemonRunningPolicy( Collections.<File>emptyList() );
        oneDaemonRunningPolicy.initialize();

        assertTrue( oneDaemonRunningPolicy.getCurrent() instanceof NullDaemonMonitor );
        assertFalse( oneDaemonRunningPolicy.getCurrent().isAlive() );
    }

    public void test_getCurrent__with_one_alive_config()
    {
        OneDaemonRunningPolicy oneDaemonRunningPolicy = createOneDaemonRunningPolicy( Arrays.asList( new File( "/sdcard/openvpn/test1-ALIVE.conf" ) ) );
        oneDaemonRunningPolicy.initialize();

        assertFalse( oneDaemonRunningPolicy.getCurrent() instanceof NullDaemonMonitor );
        assertTrue( oneDaemonRunningPolicy.getCurrent().isAlive() );
        assertEquals( new File( "/sdcard/openvpn/test1-ALIVE.conf" ), oneDaemonRunningPolicy.getCurrent().getConfigFile() );
    }

    public void test_getCurrent__with_one_dead_config()
    {
        OneDaemonRunningPolicy oneDaemonRunningPolicy = createOneDaemonRunningPolicy( Arrays.asList( new File( "/sdcard/openvpn/test1-DEAD.conf" ) ) );
        oneDaemonRunningPolicy.initialize();

        assertTrue( oneDaemonRunningPolicy.getCurrent() instanceof NullDaemonMonitor );
        assertFalse( oneDaemonRunningPolicy.getCurrent().isAlive() );
    }

    public void test_getCurrent__with_one_dead_and_one_alive_config()
    {
        OneDaemonRunningPolicy findDaemons = createOneDaemonRunningPolicy( Arrays.asList(
                new File( "/sdcard/openvpn/test1-DEAD.conf" ),
                new File( "/sdcard/openvpn/test2-ALIVE.conf" )
        ) );
        findDaemons.initialize();

        assertFalse( findDaemons.getCurrent() instanceof NullDaemonMonitor );
        assertTrue( findDaemons.getCurrent().isAlive() );
        assertEquals( new File( "/sdcard/openvpn/test2-ALIVE.conf" ), findDaemons.getCurrent().getConfigFile() );
    }

    public void test_getCurrent__with_two_alive_configs()
    {
        OneDaemonRunningPolicy findDaemons = createOneDaemonRunningPolicy( Arrays.asList(
                new File( "/sdcard/openvpn/test1-ALIVE.conf" ),
                new File( "/sdcard/openvpn/test2-ALIVE.conf" )
        ) );
        findDaemons.initialize();

        assertTrue( findDaemons.getCurrent() instanceof NullDaemonMonitor );
        assertFalse( findDaemons.getCurrent().isAlive() );
    }


    public void test_getCurrent__with_two_dead_configs()
    {
        OneDaemonRunningPolicy findDaemons = createOneDaemonRunningPolicy( Arrays.asList(
                new File( "/sdcard/openvpn/test1-DEAD.conf" ),
                new File( "/sdcard/openvpn/test2-DEAD.conf" )
        ) );
        findDaemons.initialize();

        assertTrue( findDaemons.getCurrent() instanceof NullDaemonMonitor );
        assertFalse( findDaemons.getCurrent().isAlive() );
    }

    public void test_init__with_two_alive_configs_stops_daemons()
    {
        OneDaemonRunningPolicy findDaemons = createOneDaemonRunningPolicy( Arrays.asList(
                new File( "/sdcard/openvpn/test1-ALIVE.conf" ),
                new File( "/sdcard/openvpn/test2-ALIVE.conf" )
        ) );
        findDaemons.initialize();

        verify( getMockDaemonMonitors().get( 0 ) ).stop();
        verify( getMockDaemonMonitors().get( 1 ) ).stop();
    }

    public void test_init__with_alive_dead_alive_configs_stops_daemons()
    {
        OneDaemonRunningPolicy findDaemons = createOneDaemonRunningPolicy( Arrays.asList(
                new File( "/sdcard/openvpn/test1-ALIVE.conf" ),
                new File( "/sdcard/openvpn/test2-DEAD.conf" ),
                new File( "/sdcard/openvpn/test3-ALIVE.conf" )
        ) );
        findDaemons.initialize();

        verify( getMockDaemonMonitors().get( 0 ) ).stop();
        verify( getMockDaemonMonitors().get( 1 ), never() ).stop();
        verify( getMockDaemonMonitors().get( 2 ) ).stop();
    }


    private final DaemonMonitorMockFactory daemonMonitorFactory = new DaemonMonitorMockFactory();

    private OneDaemonRunningPolicy createOneDaemonRunningPolicy(List<File> configFiles)
    {
        return new OneDaemonRunningPolicy( daemonMonitorFactory, configFiles );
    }

    private List<DaemonMonitor> getMockDaemonMonitors()
    {
        return daemonMonitorFactory.mockDaemonMonitors;
    }
}
