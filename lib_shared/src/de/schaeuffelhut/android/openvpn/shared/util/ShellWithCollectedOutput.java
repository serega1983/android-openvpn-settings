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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-01-25
 */
public class ShellWithCollectedOutput extends Shell
{
    private final ArrayList<String> stdout = new ArrayList<String>();
    private final ArrayList<String> stderr = new ArrayList<String>();

    public ShellWithCollectedOutput(String tag, String command)
    {
        super( tag, command, SH );
    }

    public ShellWithCollectedOutput(String tag, String command, ApplicationInfo applicationInfo)
    {
        super( tag, command, applicationInfo, SH );
    }

    @Override
    protected void onStdout(String line)
    {
        stdout.add( line );
    }

    @Override
    protected void onStderr(String line)
    {
        stderr.add( line );
    }

    public List<String> getStdout()
    {
        return Collections.unmodifiableList( stdout );
    }

    public List<String> getStderr()
    {
        return Collections.unmodifiableList( stderr );
    }
}
