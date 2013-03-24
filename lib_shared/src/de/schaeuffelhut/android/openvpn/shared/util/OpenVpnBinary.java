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

package de.schaeuffelhut.android.openvpn.shared.util;

import android.content.pm.ApplicationInfo;

import java.io.File;
import java.util.List;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-01-25
 */
public class OpenVpnBinary
{
    private final File path;
    private final List<String> usage;
    private final String version;
    private final boolean hasIpRoute;

    public OpenVpnBinary(File path)
    {
        this( path, queryUsage( path, null ) );
    }

    public OpenVpnBinary(File path, ApplicationInfo applicationInfo)
    {
        this( path, queryUsage( path, applicationInfo ) );
    }

    private static List<String> queryUsage(File path, ApplicationInfo applicationInfo)
    {
        ShellWithCollectedOutput shell = new ShellWithCollectedOutput( "OpenVPN", path.getAbsolutePath(), applicationInfo );
        shell.run();
        return shell.getStdout();
    }


    /**
     * Test only constructor.
     * @param path     path to openvpn binary.
     * @param usage    usage output of {@code openvpn --help}
     */
    OpenVpnBinary(File path, List<String> usage)
    {
        this.path = path;
        this.usage = usage;
        this.version = detectVersion();
        this.hasIpRoute = detectFeatureIpRoute();
    }

    private String detectVersion()
    {
        for(String line : usage)
        {
            if ( line.startsWith( "OpenVPN" ) )
            {
                int versionStart = line.indexOf( ' ' ) + 1;
                int versionEnd = line.indexOf( ' ', versionStart );
                return line.substring( versionStart, versionEnd );
            }
        }
        return "unknown";
    }

    private boolean detectFeatureIpRoute()
    {
        for(String line : usage)
            if ( line.startsWith( "--iproute cmd" ) )
                return true;
        return false;
    }


    public String getVersion()
    {
        return version;
    }

    public boolean hasIpRoute()
    {
        return hasIpRoute;
    }
}
