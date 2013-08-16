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

package de.schaeuffelhut.android.openvpn.shared.util.apilevel;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import de.schaeuffelhut.android.openvpn.shared.BuildConfig;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-03-09
 */
public abstract class ApiLevel
{
    private static ApiLevel API_LEVEL = ApiLevelLoader.loadAbstractionForExecutionEnvironment();

    public final static ApiLevel get()
    {
        return API_LEVEL;
    }

    /**
     * This setter is used only by ApiLeveltestSupport for setting
     * a mock object.
     * @param apiLevel the mocked ApiLevel object.
     */
    final static void set(ApiLevel apiLevel)
    {
        if (!BuildConfig.DEBUG )
            throw new IllegalStateException( "ApiLevel may only be set by debug builds." );
        API_LEVEL = apiLevel;
    }

    /*
     * API starts here
     */

    /**
     * Returns true if the execution environment supports the VpnService
     * introduced in API level 14 (ICS), false otherwise.
     *
     * @return true if the execution environment supports the VpnService
     *         introduced in API level 14 (ICS), false otherwise.
     */
    public boolean hasVpnService()
    {
        return false;
    }

    /**
     * Prepares the android VpnService to be used by the application.
     * For API level 14 (ICS) and higher returns true if the VpnService has already been prepared, false otherwise.
     * For API levels below 14 (pre ICS) returns always true.
     *
     * @param activity
     * @param requestCode
     * @return
     */
    public boolean prepareVpnService(Activity activity, int requestCode)
    {
        // Implemented in ApiLevel14
        // Earlier SDKs ignore this call
        return true;
    }

    /**
     * Returns true if the android VpnService has already been prepared, false otherwise.
     * For API level 14 (ICS) and higher returns true if the VpnService has already been prepared, false otherwise.
     * For API levels below 14 (pre ICS) returns always true.
     *
     * @return
     */
    public boolean isVpnServicePrepared(Context context)
    {
        return true;
    }

    public void addNativeLibDirToLdLibraryPath(ProcessBuilder pb, ApplicationInfo info)
    {
        // earlier version of android can't modify the process builder environment
    }
}
