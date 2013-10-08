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

package de.schaeuffelhut.android.openvpn.shared.util.apilevel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.VpnService;
import de.schaeuffelhut.android.openvpn.shared.util.Util;

import java.util.Map;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-03-10
 */
public class ApiLevel14 extends ApiLevel3
{
    @Override
    public boolean hasVpnService()
    {
        return true;
    }

    @Override
    public boolean prepareVpnService(Activity activity, int requestCode)
    {
        Intent prepare = VpnService.prepare( activity.getApplicationContext() );
        if ( prepare != null )
        {
            activity.startActivityForResult( prepare, requestCode );
            return false; // VpnService is not prepared and the user will receive a dialog.
        }
        else
        {
            return true; // VpnService has already been prepared, user not asked again.
        }
    }

    @Override
    public boolean isVpnServicePrepared(Context context)
    {
        return VpnService.prepare( context ) == null;
    }

    public void addNativeLibDirToLdLibraryPath(ProcessBuilder pb, ApplicationInfo info)
    {
        final Map<String, String> env = pb.environment();
        final String currentLdLibraryPath = env.get( "LD_LIBRARY_PATH" );
        if (Util.isBlank( currentLdLibraryPath ))
            env.put( "LD_LIBRARY_PATH", info.nativeLibraryDir );
        else
            env.put( "LD_LIBRARY_PATH", currentLdLibraryPath + ":" + info.nativeLibraryDir );
    }
}
