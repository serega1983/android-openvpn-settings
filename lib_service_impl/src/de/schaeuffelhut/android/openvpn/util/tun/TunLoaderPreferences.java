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

package de.schaeuffelhut.android.openvpn.util.tun;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.schaeuffelhut.android.openvpn.util.tun.TunPreferences;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: fries
 * Date: 4/21/12
 * Time: 8:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class TunLoaderPreferences
{
    // ATTENTION: These keys and values are stored on user devices. Do not change them!
    private static final String KEY_TYPE = "tunloader.type";
    private static final String VALUE_TYPE_NONE = "NONE";
    private static final String VALUE_TYPE_LEGACY = "LEGACY";
    private static final String VALUE_TYPE_MODPROBE = "MODPROBE";
    private static final String VALUE_TYPE_INSMOD = "INSMOD";

    private static final String KEY_PATH_TO_MODULE = "tunloader.path_to_module";

    private final SharedPreferences preferences;

    public TunLoaderPreferences(Context context)
    {
        preferences = PreferenceManager.getDefaultSharedPreferences( context );
    }

    public void setTypeToNone()
    {
        setTypeTo( VALUE_TYPE_NONE );
    }

    public void setTypeToLegacy()
    {
        setTypeTo( VALUE_TYPE_LEGACY );
    }

    public void setTypeToModprobe()
    {
        setTypeTo( VALUE_TYPE_MODPROBE );
    }

    private void setTypeTo(String value)
    {
        preferences.edit().putString( KEY_TYPE, value ).commit();
    }

    public void setTypeToInsmod(File pathToModule)
    {
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString( KEY_TYPE, VALUE_TYPE_INSMOD );
        edit.putString( KEY_PATH_TO_MODULE, pathToModule.getPath() );
        edit.commit();
    }

    /**
     * Removes key {@code KEY_TUNLOADER_TYPE}. Used in unit tests.
     */
    void removeType()
    {
        preferences.edit().remove( KEY_TYPE ).commit();
    }

    public TunLoaderFactoryImpl.Types getType()
    {
        return TunLoaderFactoryImpl.Types.valueOf( preferences.getString( KEY_TYPE, getDefaultType() ) );
    }

    /**
     * Returns the default type, if no type has been set yet. Defaults to {@code LEGACY} if
     * the legacy key 'openvpn_do_modprobe_tun' (pre version 0.4.11) is true, otherwise defaults to {@code NONE}.
     * @return the default type.
     */
    private String getDefaultType()
    {
        final String defaultType;
        if (TunPreferences.getDoModprobeTun( preferences ))
            defaultType = VALUE_TYPE_LEGACY;
        else
            defaultType = VALUE_TYPE_NONE;
        return defaultType;
    }

    public void setPathToModule(File file)
    {
        preferences.edit().putString( KEY_PATH_TO_MODULE, file.getPath() ).commit();
    }

    public void removePathToModule()
    {
        preferences.edit().remove( KEY_PATH_TO_MODULE ).commit();
    }

    public File getPathToModule()
    {
        return new File( preferences.getString( KEY_PATH_TO_MODULE, null ) );
    }

    public TunLoader createTunLoader()
    {
        return getType().createTunLoader( this, preferences );
    }

}
