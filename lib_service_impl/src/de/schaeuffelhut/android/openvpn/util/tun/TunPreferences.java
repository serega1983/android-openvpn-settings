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
import de.schaeuffelhut.android.openvpn.shared.util.Util;

import java.io.File;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-11-07
 */
public class TunPreferences
{
    private static final String KEY_SEND_DEVICE_DETAIL_WAS_SUCCESSFULL = "send_device_detail_was_successfull";
    public static final String KEY_OPENVPN_TUN_SETTINGS = "openvpn_tun_settings";
    public static final String KEY_OPENVPN_PATH_TO_TUN = "openvpn_path_to_tun";
    public static final String KEY_OPENVPN_DO_MODPROBE_TUN = "openvpn_do_modprobe_tun";
    public static final String KEY_OPENVPN_MODPROBE_ALTERNATIVE = "openvpn_modprobe_alternative";

    private TunPreferences()
    {
    }

    public static void setSendDeviceDetailWasSuccessfull(Context context, boolean success)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );
        sharedPreferences.edit().putBoolean( KEY_SEND_DEVICE_DETAIL_WAS_SUCCESSFULL, success ).commit();
    }

    public static boolean getSendDeviceDetailWasSuccessfull(Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );
        return sharedPreferences.getBoolean( KEY_SEND_DEVICE_DETAIL_WAS_SUCCESSFULL, false );
    }

    public static boolean isTunSharingExpired()
    {
//		final long T_2012_01_01 = 1325372400000L;
//		System.err.println( new GregorianCalendar( 2012, Calendar.APRIL, 1).getTimeInMillis() );
        final long T_2012_04_01 = 1333231200000L;
        return System.currentTimeMillis() >= T_2012_04_01;
    }

    public static boolean isTunSharingEnabled(Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );
        return !isTunSharingExpired() && getDoModprobeTun( sharedPreferences ) && Util.hasTunSupport();
    }

    public final static void setDoModprobeTun(Context context, boolean value)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );
        sharedPreferences.edit().putBoolean( KEY_OPENVPN_DO_MODPROBE_TUN, value ).commit();
    }

    public final static boolean getDoModprobeTun(SharedPreferences sharedPreferences)
    {
        return sharedPreferences.getBoolean( KEY_OPENVPN_DO_MODPROBE_TUN, false );
    }

    public final static String getLoadTunModuleCommand(SharedPreferences sharedPreferences)
    {
        return getModprobeAlternative( sharedPreferences ) + " " + Util.shellEscape( getPathToTun( sharedPreferences ) );
    }

    public final static void setPathToTun(SharedPreferences sharedPreferences, File path)
    {
        sharedPreferences.edit().putString( KEY_OPENVPN_PATH_TO_TUN, path.getPath() ).commit();
    }

    public final static String getPathToTun(SharedPreferences sharedPreferences)
    {
        return sharedPreferences.getString( KEY_OPENVPN_PATH_TO_TUN, "tun" );
    }

    public final static String getModprobeAlternative(SharedPreferences sharedPreferences)
    {
        return sharedPreferences.getString( KEY_OPENVPN_MODPROBE_ALTERNATIVE, "modprobe" );
    }

    public final static void setModprobeAlternativeToInsmod(SharedPreferences sharedPreferences)
    {
        sharedPreferences.edit().putString( KEY_OPENVPN_MODPROBE_ALTERNATIVE, "insmod" ).commit();
    }

    public final static void setModprobeAlternativeToModprobe(SharedPreferences sharedPreferences)
    {
        sharedPreferences.edit().putString( KEY_OPENVPN_MODPROBE_ALTERNATIVE, "modprobe" ).commit();
    }
}
