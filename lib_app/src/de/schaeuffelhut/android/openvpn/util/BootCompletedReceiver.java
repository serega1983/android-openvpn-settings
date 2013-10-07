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
package de.schaeuffelhut.android.openvpn.util;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import de.schaeuffelhut.android.openvpn.Preferences;
import de.schaeuffelhut.android.openvpn.service.api.OpenVpnServiceWrapper;

public class BootCompletedReceiver extends BroadcastReceiver
{
    public static final String TAG = "OpenVPN";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        // just make sure we are getting the right intent (better safe than sorry)
        if (Intent.ACTION_BOOT_COMPLETED.equals( intent.getAction() ))
        {
            if (Preferences.getOpenVpnEnabled( context ))
            {
                Log.d( TAG, "OpenVPN-Service enabled in preferences, starting!" );

                ComponentName service = context.startService( OpenVpnServiceWrapper.createDefaultIntentAddressingOpenVpnService() );

                if (service == null)
                {
                    // something really wrong here
                    Log.e( TAG, "Could not start service. Service object NULL " );
                }
                else
                {
                    Log.i( TAG, service.toString() + "started" );
                }
            }
            else
            {
                Log.d( TAG, "OpenVPN-Service disabled in preferences, not starting!" );
            }
        }
        else
        {
            Log.e( TAG, "Received unexpected intent " + intent.toString() );
        }
    }
}
