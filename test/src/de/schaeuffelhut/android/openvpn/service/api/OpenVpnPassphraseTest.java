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

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-10-28
 */
public class OpenVpnPassphraseTest extends TestCase
{
    private final String uniquePassphrase = "abc" + System.currentTimeMillis();

    public void test_getPassphrase() throws Exception
    {
        assertEquals( uniquePassphrase, new OpenVpnPassphrase( uniquePassphrase ).getPassphrase() );
    }

    public void test_writeToParcel_1() throws Exception
    {
        test_writeToParcel( uniquePassphrase );
    }

    public void test_writeToParcel_2() throws Exception
    {
        test_writeToParcel( uniquePassphrase +"A" );
    }

    private void test_writeToParcel(String passphrase)
    {
        Parcel parcel = Parcel.obtain();
        new OpenVpnPassphrase( passphrase ).writeToParcel( parcel, 0 );
        parcel.setDataPosition(0);
        OpenVpnPassphrase copy = OpenVpnPassphrase.CREATOR.createFromParcel( parcel );

        assertEquals( passphrase, copy.getPassphrase() );
    }

    /**
     * Do NOT change this test, it ensures compatibility with older clients.
     */
    public void test_read_protocol_version_1()
    {
        Parcel parcel = Parcel.obtain();
        parcel.writeByte( (byte)1 );
        parcel.writeString( uniquePassphrase );

        parcel.setDataPosition(0);
        OpenVpnPassphrase copy = OpenVpnPassphrase.CREATOR.createFromParcel( parcel );

        assertEquals( uniquePassphrase, copy.getPassphrase() );
    }

    public void test_write_protocol_version_1()
    {
        Parcel parcel = Parcel.obtain();
        new OpenVpnPassphrase( uniquePassphrase ).writeToParcel( parcel, 0 );

        parcel.setDataPosition(0);

        assertEquals( 1, parcel.readByte() );
        assertEquals( uniquePassphrase, parcel.readString() );
    }

    public void test_read_protocol_version_unexpected()
    {
        byte unexpectedProtocolVersion = (byte) 2;

        Parcel parcel = Parcel.obtain();
        parcel.writeByte( unexpectedProtocolVersion );

        parcel.setDataPosition( 0 );
        try
        {
            OpenVpnPassphrase.CREATOR.createFromParcel( parcel );
            fail( "RuntimeException expected" );
        }
        catch (RuntimeException e)
        {
            assertEquals( "Unexpected protocol version: " + unexpectedProtocolVersion, e.getMessage() );
        }
    }
}
