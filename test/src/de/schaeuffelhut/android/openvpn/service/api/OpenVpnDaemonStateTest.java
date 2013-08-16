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
public class OpenVpnDaemonStateTest extends TestCase
{
     /**
     * Test parcel write/read round trip for all values.
     */
   public void test_writeToParcel_all_values() throws Exception
    {
        for (OpenVpnDaemonState state : OpenVpnDaemonState.values())
            test_writeToParcel( state );
    }

    private void test_writeToParcel(OpenVpnDaemonState state)
    {
        Parcel parcel = Parcel.obtain();
        state.writeToParcel( parcel, 0 );
        parcel.setDataPosition( 0 );
        OpenVpnDaemonState copy = OpenVpnDaemonState.CREATOR.createFromParcel( parcel );

        assertEquals( state, copy );
    }

    /** Do NOT change this test, it defines external API. */
    public void test_ordinal_0_maps_to_UNKNOWN() throws Exception
    {
            assert_mapping_between_ordinal_and_enum( 0, OpenVpnDaemonState.UNKNOWN );
    }

    /** Do NOT change this test, it defines external API. */
    public void test_ordinal_1_maps_to_STARTUP() throws Exception
    {
            assert_mapping_between_ordinal_and_enum( 1, OpenVpnDaemonState.STARTUP );
    }

    /** Do NOT change this test, it defines external API. */
    public void test_ordinal_2_maps_to_ENABLED() throws Exception
    {
            assert_mapping_between_ordinal_and_enum( 2, OpenVpnDaemonState.ENABLED );
    }

    /** Do NOT change this test, it defines external API. */
    public void test_ordinal_3_maps_to_DISABLED() throws Exception
    {
            assert_mapping_between_ordinal_and_enum( 3, OpenVpnDaemonState.DISABLED );
    }

    private void assert_mapping_between_ordinal_and_enum(int ordinal, OpenVpnDaemonState expectedState)
    {
        assert_ordinal_maps_to_enum( (byte) ordinal, expectedState );
        assert_enum_maps_to_ordinal( (byte) ordinal, expectedState );
    }

    private void assert_ordinal_maps_to_enum(byte ordinal, OpenVpnDaemonState expectedState)
    {
        Parcel parcel = Parcel.obtain();
        parcel.writeByte( (byte) ordinal );

        parcel.setDataPosition( 0 );
        OpenVpnDaemonState copy = OpenVpnDaemonState.CREATOR.createFromParcel( parcel );

        assertEquals( expectedState, copy );
    }

    private void assert_enum_maps_to_ordinal(byte expectedOrdinal, OpenVpnDaemonState state)
    {
        Parcel parcel = Parcel.obtain();
        state.writeToParcel( parcel, 0 );

        parcel.setDataPosition( 0 );
        int ordinal = parcel.readByte();

        assertEquals( expectedOrdinal, ordinal );
    }




    public void test_read_undefined_ordinal()
    {
        byte undefinedOrdinal = (byte) 4;

        Parcel parcel = Parcel.obtain();
        parcel.writeByte( undefinedOrdinal );

        parcel.setDataPosition( 0 );
        try
        {
            OpenVpnDaemonState.CREATOR.createFromParcel( parcel );
            fail( "RuntimeException expected" );
        }
        catch (RuntimeException e)
        {
            assertEquals( "Unexpected ordinal: " + undefinedOrdinal, e.getMessage() );
        }
    }


    public void test_isStarted()
    {
        assertFalse( OpenVpnDaemonState.UNKNOWN.isStarted() );
        assertTrue( OpenVpnDaemonState.STARTUP.isStarted() );
        assertTrue( OpenVpnDaemonState.ENABLED.isStarted() );
        assertFalse( OpenVpnDaemonState.DISABLED.isStarted() );
    }

    public void test_isStopped()
    {
        assertTrue( OpenVpnDaemonState.UNKNOWN.isStopped() );
        assertFalse( OpenVpnDaemonState.STARTUP.isStopped() );
        assertFalse( OpenVpnDaemonState.ENABLED.isStopped() );
        assertTrue( OpenVpnDaemonState.DISABLED.isStopped() );
    }
}
