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
public class OpenVpnNetworkStateTest extends TestCase
{
    /**
     * Test parcel write/read round trip for all values.
     */
    public void test_writeToParcel_all_values()
    {
        for (OpenVpnNetworkState state : OpenVpnNetworkState.values())
            test_writeToParcel( state );
    }

    private void test_writeToParcel(OpenVpnNetworkState state)
    {
        Parcel parcel = Parcel.obtain();
        state.writeToParcel( parcel, 0 );
        parcel.setDataPosition( 0 );
        OpenVpnNetworkState copy = OpenVpnNetworkState.CREATOR.createFromParcel( parcel );

        assertEquals( state, copy );
    }

    /** Do NOT change this test, it defines external API. */
    public void test_ordinal_0_maps_to_UNKNOWN() throws Exception
    {
            assert_mapping_between_ordinal_and_enum( 0, OpenVpnNetworkState.UNKNOWN );
    }

    /** Do NOT change this test, it defines external API. */
    public void test_ordinal_1_maps_to_CONNECTING() throws Exception
    {
            assert_mapping_between_ordinal_and_enum( 1, OpenVpnNetworkState.CONNECTING );
    }

    /** Do NOT change this test, it defines external API. */
    public void test_ordinal_2_maps_to_RECONNECTING() throws Exception
    {
            assert_mapping_between_ordinal_and_enum( 2, OpenVpnNetworkState.RECONNECTING );
    }

    /** Do NOT change this test, it defines external API. */
    public void test_ordinal_3_maps_to_RESOLVE() throws Exception
    {
            assert_mapping_between_ordinal_and_enum( 3, OpenVpnNetworkState.RESOLVE );
    }

    /** Do NOT change this test, it defines external API. */
    public void test_ordinal_4_maps_to_WAIT() throws Exception
    {
            assert_mapping_between_ordinal_and_enum( 4, OpenVpnNetworkState.WAIT );
    }

    /** Do NOT change this test, it defines external API. */
    public void test_ordinal_5_maps_to_AUTH() throws Exception
    {
            assert_mapping_between_ordinal_and_enum( 5, OpenVpnNetworkState.AUTH );
    }

    /** Do NOT change this test, it defines external API. */
    public void test_ordinal_6_maps_to_GET_CONFIG() throws Exception
    {
            assert_mapping_between_ordinal_and_enum( 6, OpenVpnNetworkState.GET_CONFIG );
    }

    /** Do NOT change this test, it defines external API. */
    public void test_ordinal_7_maps_to_CONNECTED() throws Exception
    {
            assert_mapping_between_ordinal_and_enum( 7, OpenVpnNetworkState.CONNECTED );
    }

    /** Do NOT change this test, it defines external API. */
    public void test_ordinal_8_maps_to_ASSIGN_IP() throws Exception
    {
            assert_mapping_between_ordinal_and_enum( 8, OpenVpnNetworkState.ASSIGN_IP );
    }

    /** Do NOT change this test, it defines external API. */
    public void test_ordinal_9_maps_to_ROUTES() throws Exception
    {
            assert_mapping_between_ordinal_and_enum( 9, OpenVpnNetworkState.ADD_ROUTES );
    }

    /** Do NOT change this test, it defines external API. */
    public void test_ordinal_10_maps_to_EXITING() throws Exception
    {
            assert_mapping_between_ordinal_and_enum( 10, OpenVpnNetworkState.EXITING );
    }

    private void assert_mapping_between_ordinal_and_enum(int ordinal, OpenVpnNetworkState expectedState)
    {
        assert_ordinal_maps_to_enum( (byte) ordinal, expectedState );
        assert_enum_maps_to_ordinal( (byte) ordinal, expectedState );
    }

    private void assert_ordinal_maps_to_enum(byte ordinal, OpenVpnNetworkState expectedState)
    {
        Parcel parcel = Parcel.obtain();
        parcel.writeByte( (byte) ordinal );

        parcel.setDataPosition( 0 );
        OpenVpnNetworkState copy = OpenVpnNetworkState.CREATOR.createFromParcel( parcel );

        assertEquals( expectedState, copy );
    }

    private void assert_enum_maps_to_ordinal(byte expectedOrdinal, OpenVpnNetworkState state)
    {
        Parcel parcel = Parcel.obtain();
        state.writeToParcel( parcel, 0 );

        parcel.setDataPosition( 0 );
        int ordinal = parcel.readByte();

        assertEquals( expectedOrdinal, ordinal );
    }


    public void test_read_undefined_ordinal()
    {
        byte undefinedOrdinal = (byte) 11;

        Parcel parcel = Parcel.obtain();
        parcel.writeByte( undefinedOrdinal );

        parcel.setDataPosition( 0 );
        try
        {
            OpenVpnNetworkState.CREATOR.createFromParcel( parcel );
            fail( "RuntimeException expected" );
        }
        catch (RuntimeException e)
        {
            assertEquals( "Unexpected ordinal: " + undefinedOrdinal, e.getMessage() );
        }
    }
}
