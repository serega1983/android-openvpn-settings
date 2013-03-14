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

    public DaemonMonitorImplFactory(Context context, OpenVpnStateListenerDispatcher listenerDispatcher, IfConfigFactory ifConfigFactory)
    {
        this.context = context;
        this.listenerDispatcher = listenerDispatcher;
        this.ifConfigFactory = ifConfigFactory;
    }

    public DaemonMonitor createDaemonMonitorFor(File configFile)
    {
        Preferences2 preferences2 = new Preferences2( context, configFile );
        Notification2 notification2 = new Notification2(
                context,
                configFile,
                preferences2.getNotificationId(),
                listenerDispatcher
        );
        CmdLineBuilder cmdLineBuilder = (CmdLineBuilder)new CmdLineBuilder4(
                new File( "/data/data/de.schaeuffelhut.android.openvpn/app_bin/openvpn" ),
                new File( "/data/data/de.schaeuffelhut.android.openvpn/app_bin/ip" )
        );
        return new DaemonMonitorImpl( context, configFile, notification2, preferences2, cmdLineBuilder, ifConfigFactory );
    }

}
