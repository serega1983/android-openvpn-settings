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
 * @since 2012-08-29
 */
public class OpenVpnCredentials implements Parcelable
{
    private static final byte PROTOCOL_VERSION_1 = 1;

    private final String username;
    private final String password;

    public OpenVpnCredentials(String username, String password)
    {
        this.username = username;
        this.password = password;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public int describeContents()
    {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags)
    {
        parcel.writeByte( PROTOCOL_VERSION_1 );
        parcel.writeString( username );
        parcel.writeString( password );
    }

    public static final Creator<OpenVpnCredentials> CREATOR = new Creator<OpenVpnCredentials>()
    {
        public OpenVpnCredentials createFromParcel(Parcel in)
        {
            final byte protocolVersion = in.readByte();
            switch (protocolVersion)
            {
                case 1:
                    return new OpenVpnCredentials( in.readString(), in.readString() );
                default:
                    throw new RuntimeException( "Unexpected protocol version: " + protocolVersion ); // should be UnexpectedSwitchValueException
            }
        }

        public OpenVpnCredentials[] newArray(int size)
        {
            return new OpenVpnCredentials[size];
        }
    };
}
