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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Enforces the 'One OpenVPN daemon only policy'.
 * Creates a {@code DaemonMonitor} for each configuration file.
 * If more than one alive daemon is found all daemons are stopped.
 * If only one daemon is alive a call to {@code OneDaemonPolicy.getCurrent()}
 * will return it, otherwise {@code OneDaemonPolicy.getCurrent()} returns
 * the {@code NullDaemonMonitor).
 *
 * This code exists to ease the transition from multiple allowed daemons to
 * one daemon only. The update might hit an installation where multiple
 * daemons are enabled. If those are not properly stopped they will be orphaned
 * processes which can only be stopped by a device reboot.
 *
 * First release with 'One OpenVPN daemon only policy' has version code 34 and version 0.4.13.
 *
 * @author Friedrich Schäuffelhut
 * @since 2012-11-03
 */
class OneDaemonRunningPolicy
{
    private final DaemonMonitorFactory daemonMonitorFactory;
    private final List<File> configFiles;
    private List<DaemonMonitor> daemonMonitors;

    public OneDaemonRunningPolicy(DaemonMonitorFactory daemonMonitorFactory, List<File> configFiles)
    {
        this.daemonMonitorFactory = daemonMonitorFactory;
        this.configFiles = configFiles;
    }

    public void initialize()
    {
        attachToAllConfigFiles();
        stopDaemonsIfThereIsMoreThanOne();
    }

    private void attachToAllConfigFiles()
    {
        daemonMonitors = new ArrayList<DaemonMonitor>( configFiles.size() );
        for (File configFile : configFiles)
            daemonMonitors.add( newDaemonMonitor( configFile ) );
    }

    private void stopDaemonsIfThereIsMoreThanOne()
    {
        List<DaemonMonitor> alive = collectDaemonMonitorsAlive();
        if (alive.size() > 1)
            for (DaemonMonitor daemonMonitor : alive)
                daemonMonitor.stop();
    }

    private DaemonMonitor newDaemonMonitor(File configFile)
    {
        return daemonMonitorFactory.createDaemonMonitorFor( configFile );
    }

    public DaemonMonitor getCurrent()
    {
        final List<DaemonMonitor> alive = collectDaemonMonitorsAlive();

        if (alive.isEmpty())
            return NullDaemonMonitor.getInstance();

        if (alive.size() == 1)
            return alive.get( 0 );

        return NullDaemonMonitor.getInstance();
    }

    private List<DaemonMonitor> collectDaemonMonitorsAlive()
    {
        List<DaemonMonitor> alive = new ArrayList<DaemonMonitor>();
        for (DaemonMonitor daemonMonitor : daemonMonitors)
            if (daemonMonitor.isAlive())
                alive.add( daemonMonitor );
        return alive;
    }
}
