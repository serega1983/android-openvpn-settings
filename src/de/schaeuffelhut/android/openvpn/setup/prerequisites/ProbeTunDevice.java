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

package de.schaeuffelhut.android.openvpn.setup.prerequisites;

import android.content.Context;
import android.text.TextUtils;
import de.schaeuffelhut.android.openvpn.IocContext;
import de.schaeuffelhut.android.openvpn.setup.TunInfo;
import de.schaeuffelhut.android.openvpn.setup.TunLoader;
import de.schaeuffelhut.android.openvpn.setup.TunLoaderFactory;
import de.schaeuffelhut.android.openvpn.setup.TunLoaderFactoryImpl;
import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: fries
 * Date: 4/26/12
 * Time: 10:29 AM
 * To change this template use File | Settings | File Templates.
 */
class ProbeTunDevice
{
    private TunInfo tunInfo;
    TunLoaderFactory tunLoaderFactory = new TunLoaderFactoryImpl();

    List<String> messages = new ArrayList<String>();

    ProbeTunDevice(Context context)
    {
        tunInfo = IocContext.get().getTunInfo( context );
    }

    public ProbeResult probe()
    {
        //1
        if (checkForTunDevice())
            return successProbeResult();

        //2
        if (tryDefaultTunLoader())
            return successProbeResult();

        //3
        if (tryStandardLocations())
            return successProbeResult();

        //4 suggest TUN Installer
        message( "Could not load the tun module. Please try the TUN Installer from the market." );

        return failedProbeResult();
    }

    private boolean tryStandardLocations()
    {
        for (File tun : new File[]{new File( "/system/lib/modules/tun.ko" ), new File( "/lib/modules/tun.ko" )})
            if (tryInsmod( tun ))
                return true;
        return false;
    }

    private boolean tryInsmod(File tun)
    {
        message( "Executing insmod " + tun.getPath() );
        return tryTunLoader( tunLoaderFactory.createInsmod( tun ) );
    }

    private boolean tryDefaultTunLoader()
    {
        boolean success;
        if (tunInfo.hasTunLoader())
        {
            message( "A TUN loader is defined, executing." );
            success = tryTunLoader( tunInfo.getTunLoader() );
        }
        else
        {
            message( "No TUN loader defined." );
            success = false;
        }
        return success;
    }

    private boolean tryTunLoader(TunLoader tunLoader)
    {
        tunLoader.loadModule();
        return checkForTunDevice();
    }

    private ProbeResult successProbeResult()
    {
        return newProbeResult( PrerequisitesActivity.Status.SUCCESS );
    }

    private ProbeResult failedProbeResult()
    {
        return newProbeResult( PrerequisitesActivity.Status.FAILED );
    }

    private ProbeResult newProbeResult(PrerequisitesActivity.Status status)
    {
        return new ProbeResult(
                status,
                "TUN Device Driver",
                "",
                TextUtils.join( "\n", messages )
        );
    }

    boolean checkForTunDevice()
    {
        boolean deviceNodeAvailable = tunInfo.isDeviceNodeAvailable();
        if (deviceNodeAvailable)
            message( "TUN device is at " + tunInfo.getDeviceFile() + "." );
        else
            message( "TUN device node not found." );
        return deviceNodeAvailable;
    }

    void message(String msg)
    {
        messages.add( msg );
    }
}
