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

import java.io.File;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-11-03
 */
class DaemonMonitorImplFactory implements DaemonMonitorFactory
{
    private final Context context;
    private final OpenVpnStateListenerDispatcher listenerDispatcher;
    private final IfConfigFactory ifConfigFactory;
    private final CmdLineBuilder cmdLineBuilder;

    public DaemonMonitorImplFactory(Context context, OpenVpnStateListenerDispatcher listenerDispatcher, IfConfigFactory ifConfigFactory, CmdLineBuilder cmdLineBuilder)
    {
        this.context = context;
        this.listenerDispatcher = listenerDispatcher;
        this.ifConfigFactory = ifConfigFactory;
        this.cmdLineBuilder = cmdLineBuilder;
    }

    public DaemonMonitor createDaemonMonitorFor(File configFile)
    {
        Preferences2 preferences2 = new Preferences2( context, configFile );
        Notification notification2 = new Notification(
                context,
                configFile,
                preferences2.getNotificationId(),
                listenerDispatcher
        );
        return new DaemonMonitorImpl( context, configFile, notification2, preferences2, cmdLineBuilder, ifConfigFactory );
    }

}
