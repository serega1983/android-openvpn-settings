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
import android.os.Parcelable;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-11-14
 */
public enum OpenVpnNetworkState implements Parcelable
{
    UNKNOWN, //TODO: eliminate OpenVpnNetworkState.UNKNOWN
    CONNECTING,
    RECONNECTING,
    RESOLVE,
    WAIT,
    AUTH,
    GET_CONFIG,
    CONNECTED,
    ASSIGN_IP,
    ADD_ROUTES,
    EXITING
    ;

    public int describeContents()
    {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i)
    {
        parcel.writeByte( (byte) ordinal() );
    }

    public static final Creator<OpenVpnNetworkState> CREATOR = new Creator<OpenVpnNetworkState>()
    {
        public OpenVpnNetworkState createFromParcel(Parcel in)
        {
            final byte ordinal = in.readByte();
            switch (ordinal)
            {
                case 0:
                    return OpenVpnNetworkState.UNKNOWN;
                case 1:
                    return OpenVpnNetworkState.CONNECTING;
                case 2:
                    return OpenVpnNetworkState.RECONNECTING;
                case 3:
                    return OpenVpnNetworkState.RESOLVE;
                case 4:
                    return OpenVpnNetworkState.WAIT;
                case 5:
                    return OpenVpnNetworkState.AUTH;
                case 6:
                    return OpenVpnNetworkState.GET_CONFIG;
                case 7:
                    return OpenVpnNetworkState.CONNECTED;
                case 8:
                    return OpenVpnNetworkState.ASSIGN_IP;
                case 9:
                    return OpenVpnNetworkState.ADD_ROUTES;
                case 10:
                    return OpenVpnNetworkState.EXITING;
                default:
                    throw new RuntimeException( "Unexpected ordinal: " + ordinal ); // should be UnexpectedSwitchValueException
            }
        }

        public OpenVpnNetworkState[] newArray(int size)
        {
            return new OpenVpnNetworkState[size];
        }
    };

}
