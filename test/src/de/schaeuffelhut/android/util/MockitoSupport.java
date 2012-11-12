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

package de.schaeuffelhut.android.util;

import org.mockito.Mockito;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-11-06
 */
public class MockitoSupport
{
    private MockitoSupport()
    {
    }

    /**
     * When sharedUserid is specified in AndroidManifest.xml Mockito can not find the
     * DexMockMaker because the ContextClassLoader set can not see the library.
     * Call this method from TestCase.setUp() to set Mockitos class loader as the ContextClassLoader.
     */
    //TODO: Remove this method when Mockito has been fixed.
    public static void workaroundMockitoClassloaderIssue()
    {
        // Workaround for Mockito Classloader issue, when using sharedUserId in AndroidManifest.xml
        // see: https://groups.google.com/forum/?fromgroups=#!topic/mockito/Z2c71TqrdyA
        Thread.currentThread().setContextClassLoader( Mockito.class.getClassLoader() );
    }
}
