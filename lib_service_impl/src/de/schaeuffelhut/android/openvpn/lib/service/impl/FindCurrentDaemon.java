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

package de.schaeuffelhut.android.openvpn.lib.service.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;
import java.util.List;

/**
 * Finds and returns the one OpenVPN daemon that is intended to run.
 * Stops all daemons if more than one running is found.
 * Marks all configs as disabled if more than one is marked as enabled.
 * Ensures the one running daemon is intended to run.
 * Starts a new daemon if it is intended to is currently was not running.
 *
 * @author Friedrich Schäuffelhut
 * @since 2012-11-03
 */
class FindCurrentDaemon
{
    private final Context context;
    private final DaemonMonitorFactory daemonMonitorFactory;
    private final List<File> configFiles;

    FindCurrentDaemon(Context context, DaemonMonitorFactory daemonMonitorFactory, List<File> configFiles)
    {
        this.context = context;
        this.daemonMonitorFactory = daemonMonitorFactory;
        this.configFiles = configFiles;
    }

    private SharedPreferences getSharedPreferences()
    {
        return PreferenceManager.getDefaultSharedPreferences( context );
    }

    private DaemonMonitor newDaemonMonitor(File configFile)
    {
        return daemonMonitorFactory.createDaemonMonitorFor( configFile );
    }

    //TODO: write unit tests for FindCurrentDaemon.getTheOneRunningDaemonOrTheNullDaemonMonitor()
    DaemonMonitor getTheOneRunningDaemonOrTheNullDaemonMonitor()
    {
        // If more than one daemon is running, stop all
        OneDaemonRunningPolicy oneDaemonRunningPolicy = new OneDaemonRunningPolicy( daemonMonitorFactory, configFiles );
        oneDaemonRunningPolicy.initialize();

        // If more than one config has intended_state=true, disable all
        OneDaemonEnabledPolicy oneDaemonEnabledPolicy = new OneDaemonEnabledPolicy( getSharedPreferences(), configFiles );
        oneDaemonEnabledPolicy.initialize();

        // from here on only one config should be in intended_state = true

        // If the remaining daemon is not intended to run, stop it
        oneDaemonRunningPolicy.getCurrent().switchToIntendedState();

        // If the remaining daemon is still alive, it should be the intended one
        if (oneDaemonRunningPolicy.getCurrent().isAlive())
            return oneDaemonRunningPolicy.getCurrent();

        // if it is not alive but there is a config that is intended to run, start it.
        if (oneDaemonEnabledPolicy.hasEnabledConfig())
        {
            DaemonMonitor current = newDaemonMonitor( oneDaemonEnabledPolicy.getCurrent() );
            current.start();
            return current;
        }

        // otherwise there is no daemon around
        return NullDaemonMonitor.getInstance();
    }
}
