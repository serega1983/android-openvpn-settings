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

import android.os.Parcel;
import junit.framework.TestCase;

import java.io.File;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-10-28
 */
public class OpenVpnCredentialsTest extends TestCase
{
    private final String uniqueUsername = "abc" + System.currentTimeMillis();
    private final String uniquePassword = "def" + System.currentTimeMillis();

    public void test_getUsername() throws Exception
    {
        assertEquals( uniqueUsername, new OpenVpnCredentials( uniqueUsername, uniquePassword ).getUsername() );
    }

    public void test_getPassword() throws Exception
    {
        assertEquals( uniquePassword, new OpenVpnCredentials( uniqueUsername, uniquePassword ).getPassword() );
    }

    public void test_writeToParcel_1() throws Exception
    {
        test_writeToParcel( uniqueUsername, uniquePassword );
    }

    public void test_writeToParcel_2() throws Exception
    {
        test_writeToParcel( uniqueUsername+"A", uniquePassword+"B" );
    }

    private void test_writeToParcel(String username, String password)
    {
        Parcel parcel = Parcel.obtain();
        new OpenVpnCredentials( username, password ).writeToParcel( parcel, 0 );
        parcel.setDataPosition(0);
        OpenVpnCredentials copy = OpenVpnCredentials.CREATOR.createFromParcel( parcel );

        assertEquals( username, copy.getUsername() );
        assertEquals( password, copy.getPassword() );
    }

    /**
     * Do NOT change this test, it ensures compatibility with older clients.
     */
    public void test_read_protocol_version_1()
    {
        Parcel parcel = Parcel.obtain();
        parcel.writeByte( (byte)1 );
        parcel.writeString( uniqueUsername );
        parcel.writeString( uniquePassword );

        parcel.setDataPosition(0);
        OpenVpnCredentials copy = OpenVpnCredentials.CREATOR.createFromParcel( parcel );

        assertEquals( uniqueUsername, copy.getUsername() );
        assertEquals( uniquePassword, copy.getPassword() );
    }

    public void test_write_protocol_version_1()
    {
        Parcel parcel = Parcel.obtain();
        new OpenVpnCredentials( uniqueUsername, uniquePassword ).writeToParcel( parcel, 0 );

        parcel.setDataPosition(0);

        assertEquals( 1, parcel.readByte() );
        assertEquals( uniqueUsername, parcel.readString() );
        assertEquals( uniquePassword, parcel.readString() );
    }

    public void test_read_protocol_version_unexpected()
    {
        byte unexpectedProtocolVersion = (byte) 2;

        Parcel parcel = Parcel.obtain();
        parcel.writeByte( unexpectedProtocolVersion );

        parcel.setDataPosition( 0 );
        try
        {
            OpenVpnCredentials.CREATOR.createFromParcel( parcel );
            fail( "RuntimeException expected" );
        }
        catch (RuntimeException e)
        {
            assertEquals( "Unexpected protocol version: " + unexpectedProtocolVersion, e.getMessage() );
        }
    }

}
