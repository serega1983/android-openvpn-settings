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

package de.schaeuffelhut.android.openvpn.shared.util;

import android.app.Service;

/**
* @author Friedrich Schäuffelhut
* @since 2013-03-06
*/
public interface ServiceDelegate
{
    /**
     * Called by a delegating service to supply its reference to an instance of ServiceDelegate.
     * @param service the service object delegating to this ServiceDelegate.
     */
    public void setService(Service service);

    public void onCreate();

    @Deprecated
    public void onStart(android.content.Intent intent, int startId);

    public void onDestroy();

    public abstract android.os.IBinder onBind(android.content.Intent intent);
}
