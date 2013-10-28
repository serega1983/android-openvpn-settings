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

import de.schaeuffelhut.android.util.MockitoSupport;
import junit.framework.TestCase;

import java.io.File;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-11-03
 */
public class DaemonMonitorMockFactoryTest extends TestCase
{
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        MockitoSupport.workaroundMockitoClassloaderIssue();
    }

    public void test_newDaemonMonitor_isAlive()
    {
        DaemonMonitorMockFactory daemonMonitorMockFactory = new DaemonMonitorMockFactory();

        assertTrue( daemonMonitorMockFactory.createDaemonMonitorFor( new File( "/sdcard/openvpn/test1-ALIVE.conf" ) ).isAlive() );
        assertFalse( daemonMonitorMockFactory.createDaemonMonitorFor( new File( "/sdcard/openvpn/test1-DEAD.conf" ) ).isAlive() );
        assertTrue( daemonMonitorMockFactory.createDaemonMonitorFor( new File( "/sdcard/openvpn/test2-ALIVE.conf" ) ).isAlive() );
        assertFalse( daemonMonitorMockFactory.createDaemonMonitorFor( new File( "/sdcard/openvpn/test2-DEAD.conf" ) ).isAlive() );
    }

    public void test_newDaemonMonitor_getConfigFile()
    {
        DaemonMonitorMockFactory daemonMonitorMockFactory = new DaemonMonitorMockFactory();
        assertEquals( new File( "/sdcard/openvpn/test1-DEAD.conf" ), daemonMonitorMockFactory.createDaemonMonitorFor( new File( "/sdcard/openvpn/test1-DEAD.conf" ) ).getConfigFile() );
        assertEquals( new File( "/sdcard/openvpn/test2-ALIVE.conf" ), daemonMonitorMockFactory.createDaemonMonitorFor( new File( "/sdcard/openvpn/test2-ALIVE.conf" ) ).getConfigFile() );
    }

    public void test_newDaemonMonitor_stop_cancels_isAlive()
    {
        DaemonMonitorMockFactory daemonMonitorMockFactory = new DaemonMonitorMockFactory();
        DaemonMonitor daemonMonitor = daemonMonitorMockFactory.createDaemonMonitorFor( new File( "/sdcard/openvpn/test1-ALIVE.conf" ) );
        assertTrue( daemonMonitor.isAlive() );
        daemonMonitor.stop();
        assertFalse( daemonMonitor.isAlive() );
    }

    public void test_newDaemonMonitor_start_sets_isAlive()
    {
        DaemonMonitorMockFactory daemonMonitorMockFactory = new DaemonMonitorMockFactory();
        DaemonMonitor daemonMonitor = daemonMonitorMockFactory.createDaemonMonitorFor( new File( "/sdcard/openvpn/test1-DEAD.conf" ) );
        assertFalse( daemonMonitor.isAlive() );
        daemonMonitor.start();
        assertTrue( daemonMonitor.isAlive() );
    }

    public void test_getLastMockDaemonMonitorCreated()
    {
        DaemonMonitorMockFactory daemonMonitorMockFactory = new DaemonMonitorMockFactory();
        daemonMonitorMockFactory.createDaemonMonitorFor( new File("A") );
        DaemonMonitor expected = daemonMonitorMockFactory.createDaemonMonitorFor( new File( "B" ) );

        assertSame( expected, daemonMonitorMockFactory.getLastMockDaemonMonitorCreated() );
    }
}
