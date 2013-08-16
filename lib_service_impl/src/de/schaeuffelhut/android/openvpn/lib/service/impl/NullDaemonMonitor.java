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

import de.schaeuffelhut.android.openvpn.service.api.OpenVpnPasswordRequest;

import java.io.File;

/**
 * Instance of DaemonMonitor used when no DaemonMonitor is available.
 * Use this instead of a null value.
 *
 * @author Friedrich Schäuffelhut
 * @since 2012-11-03
 */
class NullDaemonMonitor implements DaemonMonitor
{
    private static final NullDaemonMonitor INSTANCE = new NullDaemonMonitor();

    static DaemonMonitor getInstance()
    {
        return INSTANCE;
    }

    public void start()
    {
        //TODO: implement method stub

    }

    public void restart()
    {
        //TODO: implement method stub

    }

    public void stop()
    {
        //TODO: implement method stub

    }

    public void waitForTermination() throws InterruptedException
    {
        //TODO: implement method stub

    }

    public void queryState()
    {
        //TODO: implement method stub

    }

    public void supplyPassphrase(String passphrase)
    {
        //TODO: implement method stub

    }

    public void supplyUsernamePassword(String username, String password)
    {
        //TODO: implement method stub

    }

    public boolean isAlive()
    {
        //TODO: implement method stub
        return false;
    }

    public void startLogging()
    {
        //TODO: implement method stub

    }

    public void stopLogging()
    {
        //TODO: implement method stub

    }

    public boolean isDaemonProcessAlive()
    {
        //TODO: implement method stub
        return false;
    }

    public boolean isVpnDnsActive()
    {
        //TODO: implement method stub
        return false;
    }

    public File getConfigFile()
    {
        return new File( "/dev/null" );
    }

    public void switchToIntendedState()
    {
        //TODO: implement method stub
    }

    public OpenVpnPasswordRequest getPasswordRequest()
    {
        return OpenVpnPasswordRequest.NONE;
    }
}
