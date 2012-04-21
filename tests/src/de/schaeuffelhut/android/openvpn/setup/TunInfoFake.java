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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
    private List<File> tunModules;
    private boolean deviceNodeAvailable;
    private int tryToLoadTunModuleCount = 0;
    public Collection<TryToLoadTunModuleStrategy> tryToLoadTunModuleParameters = Collections.emptyList();
    private TunLoader onCallToTryToLoadTunModuleSetTunLoaderTo;
    private boolean onCallToTryToLoadTunModuleSetDeviceNodeAvailableTo;

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

    public List<File> listTunModules()
    {
        if ( tunModules == null )
            return Collections.emptyList();
        return tunModules;
    }

    public void tryToLoadTunModule(Collection<TryToLoadTunModuleStrategy> strategy) throws IllegalStateException
    {
        tryToLoadTunModuleCount++;
        tryToLoadTunModuleParameters = new ArrayList<TryToLoadTunModuleStrategy>( strategy );
        setTunLoader( onCallToTryToLoadTunModuleSetTunLoaderTo );
        setDeviceNodeAvailable( onCallToTryToLoadTunModuleSetDeviceNodeAvailableTo );
        //TODO: implement fake method tryToLoadTunModule().
    }

    public void onCallToTryToLoadTunModuleSetTunLoaderTo(TunLoader tunLoader)
    {
        onCallToTryToLoadTunModuleSetTunLoaderTo = tunLoader;
    }


    public void onCallToTryToLoadTunModuleSetDeviceNodeAvailableTo(boolean b)
    {
        onCallToTryToLoadTunModuleSetDeviceNodeAvailableTo = b;
    }

    public void setTunLoader(TunLoader tunLoader)
    {
        this.tunLoader = tunLoader;
    }

    public void setListTunModules(List<File> tunModules)
    {
        this.tunModules = tunModules;
    }

    public void setDeviceNodeAvailable(boolean deviceNodeAvailable)
    {
        this.deviceNodeAvailable = deviceNodeAvailable;
    }

    public int tryToLoadTunModuleCount()
    {
        return tryToLoadTunModuleCount;
    }

}
