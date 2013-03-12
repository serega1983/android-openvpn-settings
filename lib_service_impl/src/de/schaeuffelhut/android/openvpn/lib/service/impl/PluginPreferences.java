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

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.File;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-11-24
 */
public class PluginPreferences
{
    private final static String KEY_ACTIVITY_HANDLING_PASSPHRASE_REQUEST = "activity_handling_passphrase_request";
    private final static String DEFAULT_ACTIVITY_HANDLING_PASSPHRASE_REQUEST = "de.schaeuffelhut.android.openvpn/de.schaeuffelhut.android.openvpn.EnterPassphrase";

    private final static String KEY_ACTIVITY_HANDLING_CREDENTIALS_REQUEST = "activity_handling_credentials_request";
    private final static String DEFAULT_ACTIVITY_HANDLING_CREDENTIALS_REQUEST = "de.schaeuffelhut.android.openvpn/de.schaeuffelhut.android.openvpn.EnterUserPassword";

    private final static String KEY_ACTIVITY_HANDLING_ONGOING_NOTIFICATION = "activity_handling_ongoing_notification";
    private final static String DEFAULT_ACTIVITY_HANDLING_ONGOING_NOTIFICATION = "de.schaeuffelhut.android.openvpn/de.schaeuffelhut.android.openvpn.OpenVpnSettings";

    private final static String KEY_CONFIG_DIR = "config_dir";

    private final Context context;
    private final SharedPreferences preferences;
    private ComponentName activityHandlingOngoingNotification;

    PluginPreferences(Context context, String pluginPackageName)
    {
        this.context = context;
        preferences = context.getSharedPreferences( "plugin_" + pluginPackageName + "_preferences.xml", Context.MODE_PRIVATE );
    }

    public ComponentName getActivityHandlingPassphraseRequest()
    {
        return ComponentName.unflattenFromString(
                preferences.getString(
                        KEY_ACTIVITY_HANDLING_PASSPHRASE_REQUEST,
                        DEFAULT_ACTIVITY_HANDLING_PASSPHRASE_REQUEST
                )
        );
    }

    public ComponentName getActivityHandlingCredentialsRequest()
    {
        return ComponentName.unflattenFromString(
                preferences.getString(
                        KEY_ACTIVITY_HANDLING_CREDENTIALS_REQUEST,
                        DEFAULT_ACTIVITY_HANDLING_CREDENTIALS_REQUEST
                )
        );
    }


    public ComponentName getActivityHandlingOngoingNotification()
    {
        return ComponentName.unflattenFromString(
                preferences.getString(
                        KEY_ACTIVITY_HANDLING_ONGOING_NOTIFICATION,
                        DEFAULT_ACTIVITY_HANDLING_ONGOING_NOTIFICATION
                )
        );
    }

    public File getConfigDir()
    {
        return new File( preferences.getString( KEY_CONFIG_DIR, new File( Environment.getExternalStorageDirectory(), "openvpn" ).getAbsolutePath() ) );
    }

    class Editor
    {
        SharedPreferences.Editor editor = preferences.edit();

        public Editor setActivityHandlingPassphraseRequest(ComponentName componentName)
        {
            String flattenedName = componentName.flattenToString();
            if (flattenedName.equals( DEFAULT_ACTIVITY_HANDLING_PASSPHRASE_REQUEST ))
                editor.remove( KEY_ACTIVITY_HANDLING_PASSPHRASE_REQUEST );
            else
                editor.putString( KEY_ACTIVITY_HANDLING_PASSPHRASE_REQUEST, flattenedName );
            return this;
        }

        public Editor setActivityHandlingCredentialsRequest(ComponentName componentName)
        {
            String flattenedName = componentName.flattenToString();
            if (flattenedName.equals( DEFAULT_ACTIVITY_HANDLING_CREDENTIALS_REQUEST ))
                editor.remove( KEY_ACTIVITY_HANDLING_CREDENTIALS_REQUEST );
            else
                editor.putString( KEY_ACTIVITY_HANDLING_CREDENTIALS_REQUEST, flattenedName );
            return this;
        }

        public Editor setActivityHandlingOngoingNotification(ComponentName componentName)
        {
            String flattenedName = componentName.flattenToString();
            if (flattenedName.equals( DEFAULT_ACTIVITY_HANDLING_ONGOING_NOTIFICATION ))
                editor.remove( KEY_ACTIVITY_HANDLING_ONGOING_NOTIFICATION );
            else
                editor.putString( KEY_ACTIVITY_HANDLING_ONGOING_NOTIFICATION, flattenedName );
            return this;
        }

        public Editor setConfigDir(File configDir)
        {
            editor.putString( KEY_CONFIG_DIR, configDir.getAbsolutePath() );
            return this;
        }

        public void commit()
        {
            editor.commit();
        }
    }

    public Editor edit()
    {
        return new Editor();
    }
}
