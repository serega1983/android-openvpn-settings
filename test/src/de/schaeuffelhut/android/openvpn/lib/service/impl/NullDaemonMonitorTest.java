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

import de.schaeuffelhut.android.openvpn.lib.service.impl.NullDaemonMonitor;
import de.schaeuffelhut.android.openvpn.service.api.OpenVpnPasswordRequest;
import junit.framework.TestCase;

import java.io.File;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-11-03
 */
public class NullDaemonMonitorTest extends TestCase
{
    public void test_isAlive() throws Exception
    {
        assertFalse( NullDaemonMonitor.getInstance().isAlive() );
    }

    public void test_getConfigFile()
    {
        assertEquals( new File( "/dev/null" ), NullDaemonMonitor.getInstance().getConfigFile() );
    }

    public void test_getPasswordRequest()
    {
        assertEquals( OpenVpnPasswordRequest.NONE, NullDaemonMonitor.getInstance().getPasswordRequest() );
    }
}
