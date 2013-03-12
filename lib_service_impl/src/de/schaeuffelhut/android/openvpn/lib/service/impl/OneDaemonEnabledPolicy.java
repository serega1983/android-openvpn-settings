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

import android.content.SharedPreferences;
import de.schaeuffelhut.android.openvpn.Preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Enforces the 'One OpenVPN daemon only policy'.
 * Checks the {@code Preferences.KEY_CONFIG_INTENDED_STATE()} for each configuration file.
 * If more than one config is enabled, all configs will be set to disabled.
 * <p/>
 * If only one config is enabled a call to {@code OneDaemonEnabledPolicy.hasEnabledConfig())}
 * will return {@code true} and a call to {@code OneDaemonEnabledPolicy.getCurrent()}
 * will return it, otherwise {@code OneDaemonEnabledPolicy.hasEnabledConfig())} will
 * return {@code false} and {@code OneDaemonEnabledPolicy.getCurrent()} will
 * throw an {@code IllegalStateException}.
 * <p/>
 * This code exists to ease the transition from multiple allowed daemons to
 * one daemon only. The update might hit an installation where multiple
 * daemons are enabled. If those are not properly disabled the app might get
 * into an invalid state which might not be recoverable.
 * <p/>
 * First release with 'One OpenVPN daemon only policy' has version code 34 and version 0.4.13.
 *
 * @author Friedrich Schäuffelhut
 * @since 2012-11-03
 */
class OneDaemonEnabledPolicy
{
    private final SharedPreferences sharedPreferences;
    private final List<File> configFiles;

    public OneDaemonEnabledPolicy(SharedPreferences sharedPreferences, List<File> configFiles)
    {
        this.sharedPreferences = sharedPreferences;
        this.configFiles = configFiles;
    }

    public void initialize()
    {
        disableVanishedConfigs();

        if (hasMoreThanOneEnabledConfigs())
            disableConfigs();
    }

    private void disableVanishedConfigs()
    {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        for (File config : collectEnabledConfigs())
            if ( !config.exists() )
                edit.putBoolean( intendedStateOf( config ), false );
        edit.commit();
    }

    public boolean hasEnabledConfig()
    {
        return collectEnabledConfigs().size() == 1;
    }

    public File getCurrent()
    {
        if (!hasEnabledConfig())
            throw new IllegalStateException( "no config available" );
        return collectEnabledConfigs().get( 0 );
    }

    private boolean hasMoreThanOneEnabledConfigs()
    {
        return collectEnabledConfigs().size() > 1;
    }

    private ArrayList<File> collectEnabledConfigs()
    {
        ArrayList<File> enabledConfigs = new ArrayList<File>();
        for (File configFile : configFiles)
            if (sharedPreferences.getBoolean( intendedStateOf( configFile ), false ))
                enabledConfigs.add( configFile );
        return enabledConfigs;
    }

    private void disableConfigs()
    {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        for (File enabledConfig : configFiles)
            edit.putBoolean( intendedStateOf( enabledConfig ), false );
        edit.commit();
    }

    private String intendedStateOf(File configFile)
    {
        return Preferences.KEY_CONFIG_INTENDED_STATE( configFile );
    }
}
