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
 * @since 2012-11-14
 */
public class OpenVpnPasswordRequestTest extends TestCase
{
     /**
     * Test parcel write/read round trip for all values.
     */
   public void test_writeToParcel_all_values() throws Exception
    {
        for (OpenVpnPasswordRequest state : OpenVpnPasswordRequest.values())
            test_writeToParcel( state );
    }

    private void test_writeToParcel(OpenVpnPasswordRequest state)
    {
        Parcel parcel = Parcel.obtain();
        state.writeToParcel( parcel, 0 );
        parcel.setDataPosition( 0 );
        OpenVpnPasswordRequest copy = OpenVpnPasswordRequest.CREATOR.createFromParcel( parcel );

        assertEquals( state, copy );
    }

    /** Do NOT change this test, it defines external API. */
    public void test_ordinal_0_maps_to_NONE() throws Exception
    {
            assert_mapping_between_ordinal_and_enum( 0, OpenVpnPasswordRequest.NONE );
    }

    /** Do NOT change this test, it defines external API. */
    public void test_ordinal_1_maps_to_PASSPHRASE() throws Exception
    {
            assert_mapping_between_ordinal_and_enum( 1, OpenVpnPasswordRequest.PASSPHRASE );
    }

    /** Do NOT change this test, it defines external API. */
    public void test_ordinal_2_maps_to_CREDENTIALS() throws Exception
    {
            assert_mapping_between_ordinal_and_enum( 2, OpenVpnPasswordRequest.CREDENTIALS );
    }

    private void assert_mapping_between_ordinal_and_enum(int ordinal, OpenVpnPasswordRequest expectedState)
    {
        assert_ordinal_maps_to_enum( (byte) ordinal, expectedState );
        assert_enum_maps_to_ordinal( (byte) ordinal, expectedState );
    }

    private void assert_ordinal_maps_to_enum(byte ordinal, OpenVpnPasswordRequest expectedState)
    {
        Parcel parcel = Parcel.obtain();
        parcel.writeByte( (byte) ordinal );

        parcel.setDataPosition( 0 );
        OpenVpnPasswordRequest copy = OpenVpnPasswordRequest.CREATOR.createFromParcel( parcel );

        assertEquals( expectedState, copy );
    }

    private void assert_enum_maps_to_ordinal(byte expectedOrdinal, OpenVpnPasswordRequest state)
    {
        Parcel parcel = Parcel.obtain();
        state.writeToParcel( parcel, 0 );

        parcel.setDataPosition( 0 );
        int ordinal = parcel.readByte();

        assertEquals( expectedOrdinal, ordinal );
    }




    public void test_read_undefined_ordinal()
    {
        byte undefinedOrdinal = (byte) 3;

        Parcel parcel = Parcel.obtain();
        parcel.writeByte( undefinedOrdinal );

        parcel.setDataPosition( 0 );
        try
        {
            OpenVpnPasswordRequest.CREATOR.createFromParcel( parcel );
            fail( "RuntimeException expected" );
        }
        catch (RuntimeException e)
        {
            assertEquals( "Unexpected ordinal: " + undefinedOrdinal, e.getMessage() );
        }
    }


    public void test_NONE_requiresPassphrase()
    {
        assertFalse( OpenVpnPasswordRequest.NONE.requiresPassphrase() );
    }

    public void test_PASSPHRASE_needPassphrase()
    {
        assertTrue( OpenVpnPasswordRequest.PASSPHRASE.requiresPassphrase() );
    }

    public void test_CREDENTIALS_needPassphrase()
    {
        assertFalse( OpenVpnPasswordRequest.CREDENTIALS.requiresPassphrase() );
    }

    public void test_NONE_requiresCredentials()
    {
        assertFalse( OpenVpnPasswordRequest.NONE.requiresCredentials() );
    }

    public void test_PASSPHRASE_needCredentials()
    {
        assertFalse( OpenVpnPasswordRequest.PASSPHRASE.requiresCredentials() );
    }

    public void test_CREDENTIALS_needCredentials()
    {
        assertTrue( OpenVpnPasswordRequest.CREDENTIALS.requiresCredentials() );
    }
}
