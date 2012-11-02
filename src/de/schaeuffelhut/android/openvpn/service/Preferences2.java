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

package de.schaeuffelhut.android.openvpn.service;

import de.schaeuffelhut.android.openvpn.Preferences;

import java.io.File;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-11-02
 */
public class Preferences2
{
    private final DaemonMonitor mDaemonMonitor;

    public Preferences2(DaemonMonitor daemonMonitor)
    {
        this.mDaemonMonitor = daemonMonitor;
    }

    @Deprecated
    OpenVpnService x_getContext()
    {
        return mDaemonMonitor.mContext;
    }

    @Deprecated
    File x_getConfigFile()
    {
        return mDaemonMonitor.mConfigFile;
    }

    int getMgmtPort()
    {
        return Preferences.getMgmtPort( x_getContext(), x_getConfigFile() );
    }

    String getVpnDns()
    {
        return Preferences.getVpnDns( x_getContext(), x_getConfigFile() );
    }

    boolean getVpnDnsEnabled()
    {
        return Preferences.getVpnDnsEnabled( x_getContext(), x_getConfigFile() );
    }

    int getDnsChange()
    {
        return Preferences.getDnsChange( x_getContext(), x_getConfigFile() );
    }

    void setDns1(Integer newDnsChange, String dns1)
    {
        Preferences.setDns1(
                x_getContext(),
                x_getConfigFile(),
                newDnsChange,
                dns1
        );
    }

    boolean getFixHtcRoutes()
    {
        return Preferences.getFixHtcRoutes( x_getContext() );
    }

    String getDns1()
    {
        return Preferences.getDns1( x_getContext(), x_getConfigFile() );
    }
}
