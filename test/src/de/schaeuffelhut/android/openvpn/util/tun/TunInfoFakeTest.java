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

package de.schaeuffelhut.android.openvpn.util.tun;

import android.test.AndroidTestCase;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: fries
 * Date: 4/14/12
 * Time: 7:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class TunInfoFakeTest extends AndroidTestCase
{
    private TunInfoFake tunInfo = new TunInfoFake();

    public void testIsDeviceNodeAvailable()
    {
        tunInfo.setDeviceNodeAvailable( true );
        assertTrue( tunInfo.isDeviceNodeAvailable() );
    }


    public void testGetDeviceFile_with_device_node_unavailable()
    {
        try
        {
            tunInfo.getDeviceFile();
            fail( "Expected IllegalStateException" );
        }
        catch (IllegalStateException e)
        {
            // expected
        }
    }

    public void testGetDeviceFile_with_device_node_available()
    {
        tunInfo.setDeviceNodeAvailable( true );
        assertEquals( new File( "/dev/tun" ), tunInfo.getDeviceFile() );
    }

    public void testHasTunLoader()
    {
        tunInfo.setTunLoader( new DummyTunLoader() );
        assertTrue( tunInfo.hasTunLoader() );
    }

    public void testGetTunLoader_with_none_defined()
    {
        try
        {
            tunInfo.getTunLoader();
            fail( "Expected IllegalStateException" );
        }
        catch (IllegalStateException e)
        {
            // expected
        }
    }

    public void testGetTunLoader()
    {
        DummyTunLoader tunLoader = new DummyTunLoader();
        tunInfo.setTunLoader( tunLoader );
        assertSame( tunLoader, tunInfo.getTunLoader() );
    }
}
