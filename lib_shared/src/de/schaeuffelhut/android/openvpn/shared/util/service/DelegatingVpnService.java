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

package de.schaeuffelhut.android.openvpn.shared.util.service;

import android.content.Intent;
import android.net.VpnService;
import android.os.IBinder;

/**
 * A type of {@code VpnService} delegating method calls to an instance of {@code ServiceDelegate}.
 * This allows sharing a single implementation of a service between different
 * service types, e.g. between {@code Service} and {@code VpnService}.
 *
 * @author Friedrich Schäuffelhut
 * @since 2013-03-06
 */
public class DelegatingVpnService<T extends ServiceDelegate> extends VpnService implements IDelegatingService<T>
{
    private final T serviceDelegate;

    public DelegatingVpnService(T serviceDelegate)
    {
        this.serviceDelegate = serviceDelegate;//TODO: guava checkNotNull()
        this.serviceDelegate.setService( this );
    }

    public T getServiceDelegate()
    {
        return serviceDelegate;
    }

    @Override
    public void onCreate()
    {
        serviceDelegate.onCreate();
    }

    @Deprecated
    @Override
    public void onStart(Intent intent, int startId)
    {
        serviceDelegate.onStart( intent, startId );
    }

    @Override
    public void onDestroy()
    {
        serviceDelegate.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return  serviceDelegate.onBind( intent );
    }

    /*  API not delegated
    public int onStartCommand(android.content.Intent intent, int flags, int startId) {  }
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {  }
    public void onLowMemory() {  }
    public void onTrimMemory(int level) {  }
    public boolean onUnbind(android.content.Intent intent) {  }
    public void onRebind(android.content.Intent intent) {  }
    public void onTaskRemoved(android.content.Intent rootIntent) {  }
    protected void dump(java.io.FileDescriptor fd, java.io.PrintWriter writer, java.lang.String[] args) {  }
*/
}
