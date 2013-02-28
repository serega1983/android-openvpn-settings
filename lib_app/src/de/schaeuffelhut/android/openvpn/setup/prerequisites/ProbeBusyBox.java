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
import android.net.Uri;
import android.util.Log;
import de.schaeuffelhut.android.openvpn.lib.app.R;
import de.schaeuffelhut.android.openvpn.lib.openvpn.InstallFailed;
import de.schaeuffelhut.android.openvpn.lib.openvpn.Installer;
import de.schaeuffelhut.android.openvpn.shared.util.Shell;

import java.io.File;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-04-30
 */
class ProbeBusyBox extends ProbeExecutable
{
    ProbeBusyBox(Context context)
    {
        super( "BusyBox binary",
                "Used to configure the network interface.",
                R.string.prerequisites_item_title_getBusyBox,
                Uri.parse( "market://details?id=stericson.busybox" ),
                new File( "/system/xbin/busybox" ), new File( "/system/bin/busybox" ), new File( "/sbin/busybox" )
                //TODO:, install( context )
        );
    }

    private static File install(Context context)
    {
        try
        {
            File file = new Installer( context ).installBusyBox();
            Shell shell = new Shell( "OpenVPN-Settings", file.getAbsolutePath(), false );
            shell.run();
            return file;
        }
        catch (InstallFailed installFailed)
        {
            Log.e( "OpenVPN-Settings", "installing busybox", installFailed );
        }
        return null;
    }
}
