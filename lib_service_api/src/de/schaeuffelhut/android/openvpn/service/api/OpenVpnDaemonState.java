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
public enum OpenVpnDaemonState implements Parcelable
{
    UNKNOWN( false ), //TODO: eliminate OpenVpnDaemonState.UNKNOWN
    STARTUP( true ),
    ENABLED( true ),
    DISABLED( false );

    private final boolean isStarted;

    private OpenVpnDaemonState(boolean started)
    {
        isStarted = started;
    }

    public int describeContents()
    {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i)
    {
        parcel.writeByte( (byte) ordinal() );
    }

    public static final Parcelable.Creator<OpenVpnDaemonState> CREATOR = new Parcelable.Creator<OpenVpnDaemonState>()
    {
        public OpenVpnDaemonState createFromParcel(Parcel in)
        {
            final byte ordinal = in.readByte();
            switch ( ordinal )
            {
                case 0: return OpenVpnDaemonState.UNKNOWN;
                case 1: return OpenVpnDaemonState.STARTUP;
                case 2: return OpenVpnDaemonState.ENABLED;
                case 3: return OpenVpnDaemonState.DISABLED;
                default:
                    throw new RuntimeException( "Unexpected ordinal: " + ordinal ); // should be UnexpectedSwitchValueException
            }
        }

        public OpenVpnDaemonState[] newArray(int size)
        {
            return new OpenVpnDaemonState[size];
        }
    };

    public boolean isStarted()
    {
        return isStarted;
    }

    public boolean isStopped()
    {
        return !isStarted;
    }
}
