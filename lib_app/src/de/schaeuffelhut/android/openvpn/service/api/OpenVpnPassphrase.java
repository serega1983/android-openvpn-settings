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
public class OpenVpnPassphrase implements Parcelable
{
    private static final byte PROTOCOL_VERSION_1 = (byte) 1;
    private final String passphrase;

    public OpenVpnPassphrase(String passphrase)
    {
        this.passphrase = passphrase;
    }

    public String getPassphrase()
    {
        return passphrase;
    }

    public int describeContents()
    {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags)
    {
        parcel.writeByte( PROTOCOL_VERSION_1 );
        parcel.writeString( passphrase );
    }

    public static final Creator<OpenVpnPassphrase> CREATOR = new Creator<OpenVpnPassphrase>()
    {
        public OpenVpnPassphrase createFromParcel(Parcel in)
        {
            final byte protocolVersion = in.readByte();
            switch (protocolVersion)
            {
                case 1:
                    return new OpenVpnPassphrase( in.readString() );
                default:
                    throw new RuntimeException( "Unexpected protocol version: " + protocolVersion ); // should be UnexpectedSwitchValueException
            }
        }

        public OpenVpnPassphrase[] newArray(int size)
        {
            return new OpenVpnPassphrase[size];
        }
    };
}
