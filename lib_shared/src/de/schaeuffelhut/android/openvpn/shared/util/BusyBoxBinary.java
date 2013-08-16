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

import java.io.File;
import java.util.List;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-01-25
 */
public class BusyBoxBinary
{
    private final File path;
    private final List<String> usage;
    private final List<String> applets;
    private final String version;
    private final boolean hasIpApplet;

    public BusyBoxBinary(File path)
    {
        this( path, queryUsage( path ), queryApplets( path ) );
    }

    private static List<String> queryUsage(File path)
    {
        ShellWithCollectedOutput shell = new ShellWithCollectedOutput( "OpenVPN", path.getAbsolutePath() );
        shell.run();
        return shell.getStdout();
    }

    private static List<String> queryApplets(File path)
    {
        ShellWithCollectedOutput shell = new ShellWithCollectedOutput( "OpenVPN", path.getAbsolutePath() + " --list" );
        shell.run();
        return shell.getStdout();
    }

    public BusyBoxBinary(File path, List<String> usage, List<String> applets)
    {
        this.path = path;
        this.usage = usage;
        this.applets = applets;
        this.version = detectVersion();
        this.hasIpApplet = applets.contains( "ip" );
    }
    private String detectVersion()
    {
        for(String line : usage)
        {
            if ( line.startsWith( "BusyBox" ) )
            {
                int versionStart = line.indexOf( ' ' ) + 1;
                int versionEnd = line.indexOf( ' ', versionStart );
                return line.substring( versionStart, versionEnd );
            }
        }
        return "unknown";
    }

    public String getVersion()
    {
        return version;
    }

    public boolean hasIpApplet()
    {
        return hasIpApplet;
    }
}
