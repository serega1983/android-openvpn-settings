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

import android.os.Build;
import android.util.Log;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-03-10
 */
final class ApiLevelLoader
{
    private static final String TAG = "OpenVpnSettings";

    private ApiLevelLoader()
    {
    }

    /**
     * Find api level abstraction closest to the SDK version of the execution environment.
     *
     * @return the api level abstraction closest to the SDK version of execution environment.
     */
    static ApiLevel loadAbstractionForExecutionEnvironment()
    {
        ApiLevel apiLevel = loadAbstractionFor( getSdkVersion() );
        Log.d( TAG, "Successfully loaded api abstraction layer " + apiLevel.getClass().getSimpleName() );
        return apiLevel;
    }

    /**
     * Get the SDK version in an API level 3 compatible way.
     *
     * @return the SDK version
     */
    private static int getSdkVersion()
    {
        //TODO: once support for API level 3 is dropped, use Build.VERSION.SDK_INT
        return Integer.parseInt( Build.VERSION.SDK );
    }

    /**
     * Find api level abstraction closest to the specified SDK version.
     *
     * @param sdkVersion
     * @return the api level abstraction closest to the SDK version of execution environment.
     */
    static ApiLevel loadAbstractionFor(int sdkVersion)
    {
        final int knownApiLevels[] = new int[]{
                3, 14
        };

        for (int i = knownApiLevels.length - 1; i >= 0; i--)
        {
            final int apiLevel = knownApiLevels[i];
            if (sdkVersion >= apiLevel)
                return createAbstractionFor( apiLevel );
        }

        throw new RuntimeException( String.format( "No ApiLevel implementation for SDK version %d found" ) );
    }

    /**
     * Instantiates the appropriate implementation for the requested api level.
     * Uses Class.forName() to avoid linking of incompatible classes.
     *
     * @param apiLevel The api level of the abstraction to be loaded.
     * @return an instance of the appropriate implementation for the requested api level.
     */
    private static ApiLevel createAbstractionFor(int apiLevel)
    {
        final Class<?> apiLevelClass;
        try
        {
            apiLevelClass = ApiLevel.class.forName( ApiLevel.class.getName() + apiLevel );
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException( "Failed to load abstraction layet for api level " + apiLevel, e );
        }

        try
        {
            return (ApiLevel) apiLevelClass.newInstance();
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException( "Failed to load abstraction layet for api level " + apiLevel, e );
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException( "Failed to load abstraction layet for api level " + apiLevel, e );
        }
    }
}
