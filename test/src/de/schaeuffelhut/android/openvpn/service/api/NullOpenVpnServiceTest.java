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

import junit.framework.TestCase;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-11-13
 */
class NullOpenVpnServiceTest extends TestCase
{
    public void testGetInstance() throws Exception
    {
        assertNotNull( NullOpenVpnService.getInstance() );
    }

    public void testGetStatus() throws Exception
    {
        assertFalse( NullOpenVpnService.getInstance().getStatus().isStarted() );
    }

    public void testGetStatusFor() throws Exception
    {
        assertFalse( NullOpenVpnService.getInstance().getStatusFor( null ).isStarted() );
    }

    public void testAsBinder() throws Exception
    {
        try
        {
            NullOpenVpnService.getInstance().asBinder();
            fail( "IllegalStateException expected" );
        }
        catch (IllegalStateException e)
        {
            // expected
        }
    }
}
