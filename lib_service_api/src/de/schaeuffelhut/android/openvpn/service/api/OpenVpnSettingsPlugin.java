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

package de.schaeuffelhut.android.openvpn.service.api;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.widget.Toast;

/**
 * Support plugin registration with OpenVPN Settings.
 *
 * @author Friedrich Schäuffelhut
 * @since 2012-11-20
 */
public class OpenVpnSettingsPlugin
{
    static final ComponentName COMPONENT_NAME = new ComponentName(
            "de.schaeuffelhut.android.openvpn",
            "de.schaeuffelhut.android.openvpn.services.PluginRegistry"
    );

    private final Context context;
    private final PackageInfo openVpnSettings;

    public OpenVpnSettingsPlugin(Context context)
    {
        this.context = context;
        openVpnSettings = findOpenVpnSettingsPackage();
//        Log.d( "OpenVPNSettings", "openVpnSettings.versionCode: " + openVpnSettings.versionCode );
//        Log.d( "OpenVPNSettings", "openVpnSettings.versionName: " + openVpnSettings.versionName );
    }

    public boolean isOpenVpnSettingsInstalled()
    {
        return openVpnSettings != null && openVpnSettings.versionCode >= 37;
    }

    private PackageInfo findOpenVpnSettingsPackage()
    {
        for (PackageInfo p : context.getPackageManager().getInstalledPackages( 0 ))
            if ("de.schaeuffelhut.android.openvpn".equals( p.packageName ))
                return p;
        return null;
    }

    public void showOpenVpnSettingsInMarket()
    {
        try
        {
            tryToOpenMarket();
            return;
        }
        catch (ActivityNotFoundException e)
        {

        }

        try
        {
            tryToOpenBrowser();
            return;
        }
        catch (ActivityNotFoundException e)
        {

        }

        Toast.makeText( context, "Neither market nor the browser could be launched! Please get OpenVPN Settings from the internet: code.google.com/p/android-openvpn-settings", Toast.LENGTH_LONG ).show();
    }

    private void tryToOpenMarket() throws ActivityNotFoundException
    {
        Intent intent = new Intent( Intent.ACTION_VIEW );
        intent.setData( Uri.parse( "market://details?id=de.schaeuffelhut.android.openvpn" ) );
        context.startActivity( intent );
    }

    private void tryToOpenBrowser() throws ActivityNotFoundException
    {
        Intent intent = new Intent( Intent.ACTION_VIEW );
        intent.setData( Uri.parse( "https://code.google.com/p/android-openvpn-settings/" ) );
        context.startActivity( intent );
    }
}
