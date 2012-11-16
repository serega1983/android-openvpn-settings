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
public enum OpenVpnPasswordRequest implements Parcelable
{
    NONE( false, false ),
    PASSPHRASE( true, false ),
    CREDENTIALS( false, true );

    private final boolean needsPassphrase;
    private final boolean needsCredentials;

    private OpenVpnPasswordRequest(boolean needsPassphrase, boolean needsCredentials)
    {
        this.needsPassphrase = needsPassphrase;
        this.needsCredentials = needsCredentials;
    }

    public int describeContents()
    {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i)
    {
        parcel.writeByte( (byte) ordinal() );
    }

    public static final Creator<OpenVpnPasswordRequest> CREATOR = new Creator<OpenVpnPasswordRequest>()
    {
        public OpenVpnPasswordRequest createFromParcel(Parcel in)
        {
            final byte ordinal = in.readByte();
            switch ( ordinal )
            {
                case 0: return OpenVpnPasswordRequest.NONE;
                case 1: return OpenVpnPasswordRequest.PASSPHRASE;
                case 2: return OpenVpnPasswordRequest.CREDENTIALS;
                default:
                    throw new RuntimeException( "Unexpected ordinal: " + ordinal ); // should be UnexpectedSwitchValueException
            }
        }

        public OpenVpnPasswordRequest[] newArray(int size)
        {
            return new OpenVpnPasswordRequest[size];
        }
    };

    public boolean requiresPassphrase()
    {
        return needsPassphrase;
    }

    public boolean requiresCredentials()
    {
        return needsCredentials;
    }

}
