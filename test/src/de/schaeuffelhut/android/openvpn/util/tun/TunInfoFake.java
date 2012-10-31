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

import de.schaeuffelhut.android.openvpn.util.tun.TunInfo;
import de.schaeuffelhut.android.openvpn.util.tun.TunLoader;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: fries
 * Date: 4/14/12
 * Time: 7:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class TunInfoFake implements TunInfo
{
    private int flags;
    private TunLoader tunLoader;
    private boolean deviceNodeAvailable;

    public boolean isDeviceNodeAvailable()
    {
        return deviceNodeAvailable;
    }

    public File getDeviceFile()
    {
        if ( !isDeviceNodeAvailable() )
            throw new IllegalStateException( "Device node is not available" );
        return new File( "/dev/tun" );
    }

    public boolean hasTunLoader()
    {
        return tunLoader != null;
    }

    public TunLoader getTunLoader()
    {
        if ( !hasTunLoader() )
            throw new IllegalStateException( "TunLoader has not been defined" );
        return tunLoader;
    }

    public void setTunLoader(TunLoader tunLoader)
    {
        this.tunLoader = tunLoader;
    }

    public void setDeviceNodeAvailable(boolean deviceNodeAvailable)
    {
        this.deviceNodeAvailable = deviceNodeAvailable;
    }
}
