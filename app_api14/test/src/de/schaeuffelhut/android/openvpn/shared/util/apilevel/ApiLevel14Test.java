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

import android.content.pm.ApplicationInfo;
import android.test.InstrumentationTestCase;
import junit.framework.TestCase;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-03-10
 */
public class ApiLevel14Test extends InstrumentationTestCase
{
    private final ApiLevel14 apiLevel = new ApiLevel14();

    public void testHasVpnService() throws Exception
    {
        assertTrue( apiLevel.hasVpnService() );
    }

//    public void testIsVpnServicePrepared() throws Exception
//    {
//        assertTrue( apiLevel.isVpnServicePrepared( getInstrumentation().getContext() ) );
//    }

//    public void testPrepareVpnService() throws Exception
//    {
//        assertTrue( apiLevel.prepareVpnService( DUMMY_ACTIVITY, DUMMY_REQUEST_CODE ) );
//    }

    public void testAddNativeLibDirToLdLibraryPath_with_empty_LD_LIBRARY_PATH() throws Exception
    {
        ProcessBuilder processBuilder = new ProcessBuilder( "" );
        processBuilder.environment().remove( "LD_LIBRARY_PATH" );
        ApplicationInfo info = new ApplicationInfo();
        info.nativeLibraryDir = "/full/path/to/shared/libraries";

        apiLevel.addNativeLibDirToLdLibraryPath( processBuilder, info );

        assertEquals( "/full/path/to/shared/libraries", processBuilder.environment().get( "LD_LIBRARY_PATH" ) );
    }

    public void testAddNativeLibDirToLdLibraryPath_with_nonempty_LD_LIBRARY_PATH() throws Exception
    {
        ProcessBuilder processBuilder = new ProcessBuilder( "" );
        processBuilder.environment().put( "LD_LIBRARY_PATH", "/some/path" );
        ApplicationInfo info = new ApplicationInfo();
        info.nativeLibraryDir = "/full/path/to/shared/libraries";

        apiLevel.addNativeLibDirToLdLibraryPath( processBuilder, info );

        assertEquals( "/some/path:/full/path/to/shared/libraries", processBuilder.environment().get( "LD_LIBRARY_PATH" ) );
    }
}
