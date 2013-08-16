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
import android.preference.PreferenceManager;
import de.schaeuffelhut.android.openvpn.Preferences;

import java.io.File;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-11-02
 */
class Preferences2
{
    public final Context mContext;
    public final File mConfigFile;

    public Preferences2(Context context, File configFile)
    {
        this.mContext = context;
        this.mConfigFile = configFile;
    }

    int getMgmtPort()
    {
        return Preferences.getMgmtPort( mContext, mConfigFile );
    }

    String getVpnDns()
    {
        return Preferences.getVpnDns( mContext, mConfigFile );
    }

    boolean getVpnDnsEnabled()
    {
        return Preferences.getVpnDnsEnabled( mContext, mConfigFile );
    }

    int getDnsChange()
    {
        return Preferences.getDnsChange( mContext, mConfigFile );
    }

    void setDns1(Integer newDnsChange, String dns1)
    {
        Preferences.setDns1(
                mContext,
                mConfigFile,
                newDnsChange,
                dns1
        );
    }

    String getDns1()
    {
        return Preferences.getDns1( mContext, mConfigFile );
    }

    void setMgmtPort(int mgmtPort)
    {
        Preferences.setMgmtPort( mContext, mConfigFile, mgmtPort );
    }

    int getScriptSecurityLevel()
    {
        return Preferences.getScriptSecurityLevel( mContext, mConfigFile );
    }

    boolean getLogStdoutEnable()
    {
        return Preferences.getLogStdoutEnable( mContext, mConfigFile );
    }

    File logFileFor()
    {
        return Preferences.logFileFor( mConfigFile );
    }




    boolean getFixHtcRoutes()
    {
        return Preferences.getFixHtcRoutes( mContext );
    }

    File getPathToBinaryAsFile()
    {
        return Preferences.getPathToBinaryAsFile( PreferenceManager.getDefaultSharedPreferences( mContext ) );
    }

    boolean getIntendedState()
    {
        return Preferences.getIntendedState( mContext, mConfigFile );
    }

    public boolean hasPassphrase()
    {
        return Preferences.hasPassphrase( mContext, mConfigFile );
    }

    public String getPassphrase()
    {
        return Preferences.getPassphrase( mContext, mConfigFile );
    }

    public boolean hasCredentials()
    {
        return Preferences.hasCredentials( mContext, mConfigFile );
    }

    public String getUsername()
    {
        return Preferences.getUsername( mContext, mConfigFile );
    }

    public String getPassword()
    {
        return Preferences.getPassword( mContext, mConfigFile );
    }

    int getNotificationId()
    {
        return Preferences.getNotificationId( mContext, mConfigFile );
    }
}
