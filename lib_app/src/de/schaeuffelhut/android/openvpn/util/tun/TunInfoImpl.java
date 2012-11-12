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

package de.schaeuffelhut.android.openvpn.util.tun;

import android.content.Context;

import java.io.File;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: fries
 * Date: 4/11/12
 * Time: 9:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class TunInfoImpl implements TunInfo
{
    private static final File DEV_TUN = new File( "/dev/tun" );
    private static final File DEV_NET_TUN = new File( "/dev/net/tun" );
    private final Context context;

    public TunInfoImpl(Context context)
    {
        this.context = context;
    }

    public boolean isDeviceNodeAvailable()
    {
        return DEV_TUN.exists() || DEV_NET_TUN.exists();
    }

    public File getDeviceFile()
    {
        if (DEV_TUN.exists())
            return DEV_TUN;
        if (new File( "/dev/net/tun" ).exists())
            return new File( "/dev/net/tun" );
        throw new IllegalMonitorStateException( "tun device node not found" );
    }

    public boolean hasTunLoader()
    {
        return new TunLoaderPreferences( context ).getType().canLoadTun;
    }

    public TunLoader getTunLoader()
    {
        return new TunLoaderPreferences( context ).createTunLoader();
    }
}
