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

package de.schaeuffelhut.android.openvpn.shared.util.apilevel;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import junit.framework.TestCase;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-03-10
 */
public class ApiLevel3Test extends TestCase
{
    private static final Context DUMMY_CONTEXT = null;
    private static final Activity DUMMY_ACTIVITY = null;
    private static final int DUMMY_REQUEST_CODE = 0;

    private final ApiLevel3 apiLevel = new ApiLevel3();

    public void testHasVpnService() throws Exception
    {
        assertFalse( apiLevel.hasVpnService() );
    }

    public void testIsVpnServicePrepared() throws Exception
    {
        assertTrue( apiLevel.isVpnServicePrepared( DUMMY_CONTEXT ) );
    }

    public void testPrepareVpnService() throws Exception
    {
        assertTrue( apiLevel.prepareVpnService( DUMMY_ACTIVITY, DUMMY_REQUEST_CODE ) );
    }

    public void testAddNativeLibDirToLdLibraryPath() throws Exception
    {
        ProcessBuilder processBuilder = new ProcessBuilder( "" );
        String expectedValue = processBuilder.environment().get( "LD_LIBRARY_PATH" );

        apiLevel.addNativeLibDirToLdLibraryPath( processBuilder, new ApplicationInfo() );

        assertEquals( expectedValue, processBuilder.environment().get( "LD_LIBRARY_PATH" ) );
    }
}
