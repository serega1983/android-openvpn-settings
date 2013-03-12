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
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import de.schaeuffelhut.android.openvpn.IocContext;
import de.schaeuffelhut.android.openvpn.lib.app.R;
import de.schaeuffelhut.android.openvpn.util.tun.*;

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
    private final TunInfo tunInfo;
    private final TunLoaderPreferences tunLoaderPreferences;
    private final SharedPreferences sharedPreferences;
    private TunLoaderFactory tunLoaderFactory = new TunLoaderFactoryImpl();
    List<String> messages = new ArrayList<String>();
    private List<ListViewItem> childItems = new ArrayList<ListViewItem>();

    ProbeTunDevice(Context context)
    {
        tunInfo = TunInfoSingleton.get().getTunInfo( context );
        tunLoaderPreferences = new TunLoaderPreferences( context );
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );
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

        //4 try legacy tun loader only if defined, otherwise silently ignore
        if ( TunLoaderFactoryImpl.hasLegacyDefinition( sharedPreferences ))
            if (tryLegacyLoader())
                return successProbeResult();

        //5 suggest TUN Installer
        message( "Could not load the tun module. Please try the TUN Installer from the market." );

        childItems.add( new LinkListViewItem(
                R.string.prerequisites_item_title_getTunInstaller,
                Uri.parse( "market://details?id=com.aed.tun.installer" ) )
        );

        return failedProbeResult();
    }

    private boolean tryLegacyLoader()
    {
        if (!TunLoaderFactoryImpl.hasLegacyDefinition( sharedPreferences ))
            return false;

        message( "Executing legacy tun loader (defined before version 0.4.11)." );
        TunLoader tunLoader = TunLoaderFactoryImpl.createFromLegacyDefinition( sharedPreferences );
        return makeTunLoaderDefaultIfLoadAttemptSucceeds( tunLoader );
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
        TunLoader insmod = tunLoaderFactory.createInsmod( tun );
        return makeTunLoaderDefaultIfLoadAttemptSucceeds( insmod );
    }

    private boolean makeTunLoaderDefaultIfLoadAttemptSucceeds(TunLoader tunLoader)
    {
        boolean success = tryTunLoader( tunLoader );
        if (success)
        {
            message( "Setting the default TUN loader." );
            tunLoader.makeDefault( tunLoaderPreferences );
        }
        return success;
    }

    private boolean tryDefaultTunLoader()
    {
        boolean success;
        if (tunInfo.hasTunLoader())
        {
            message( "Executing default TUN loader." );
            success = tryTunLoader( tunInfo.getTunLoader() );
        }
        else
        {
            message( "No default TUN loader defined." );
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
                "Exchange network packets with kernel.",
                TextUtils.join( "\n", messages ),
                childItems
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
