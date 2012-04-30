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

package de.schaeuffelhut.android.openvpn.setup;

import de.schaeuffelhut.android.openvpn.util.tun.TunLoader;
import de.schaeuffelhut.android.openvpn.util.tun.TunLoaderPreferences;
import de.schaeuffelhut.android.openvpn.util.tun.TunInfoFake;

/**
* Created with IntelliJ IDEA.
* User: fries
* Date: 4/24/12
* Time: 11:13 AM
* To change this template use File | Settings | File Templates.
*/
class TunLoaderProbeFake implements TunLoaderProbe
{
    int tryCurrentTunLoaderCallCount = 0;
    int makeSuccessfullyProbedTunLoaderTheDefaultCallCount = 0;
    int scanDeviceForTunCallCount = 0;
    int trySdCardCallCount = 0;

    private final TunInfoFake tunInfo;
    private TunLoader onCallToTryToLoadTunModuleSetTunLoaderTo;
    private boolean onCallToTryToLoadTunModuleSetDeviceNodeAvailableTo;

    public TunLoaderProbeFake(TunInfoFake tunInfo)
    {
        this.tunInfo = tunInfo;
    }

    public void tryCurrentTunLoader()
    {
        tryCurrentTunLoaderCallCount++;
    }

    public void scanDeviceForTun()
    {
        scanDeviceForTunCallCount++;
    }

    public void trySdCard()
    {
        trySdCardCallCount++;
    }

    public void makeSuccessfullyProbedTunLoaderTheDefault(TunLoaderPreferences preferences)
    {
        if ( preferences == null )
            throw new NullPointerException( "Parameter preferences may not be null" );
        makeSuccessfullyProbedTunLoaderTheDefaultCallCount++;
        tunInfo.setTunLoader( onCallToTryToLoadTunModuleSetTunLoaderTo );
        tunInfo.setDeviceNodeAvailable( onCallToTryToLoadTunModuleSetDeviceNodeAvailableTo );
    }

    public void onCallToTryToLoadTunModuleSetDeviceNodeAvailableTo(boolean b)
    {
        onCallToTryToLoadTunModuleSetDeviceNodeAvailableTo = b;
    }
}
