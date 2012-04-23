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

package de.schaeuffelhut.android.openvpn.setup;

import java.io.File;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: fries
 * Date: 4/23/12
 * Time: 7:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class TunLoaderProbe
{
    private final ITunLoaderFactory tunLoaderFactory;
    private ArrayList<TunLoader> tunLoaders = new ArrayList<TunLoader>();

    public TunLoaderProbe(ITunLoaderFactory tunLoaderFactory)
    {
        this.tunLoaderFactory = tunLoaderFactory;
    }

    public void tryCurrentTunLoader()
    {
        tunLoaders.add( tunLoaderFactory.createCurrent() );
    }

    public void scanDeviceForTun()
    {
        tunLoaders.add( tunLoaderFactory.createModprobe() );
        tunLoaders.add( tunLoaderFactory.createInsmod( new File( "/system/lib/modules/tun.ko" ) ) );
        tunLoaders.add( tunLoaderFactory.createInsmod( new File( "/lib/modules/tun.ko" ) ) );
    }

    public void tryToLoadModule()
    {
        for (TunLoader tunLoader : tunLoaders)
            tunLoader.loadModule();
    }
}
