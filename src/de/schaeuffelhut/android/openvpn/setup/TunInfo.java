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

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: fries
 * Date: 4/11/12
 * Time: 9:17 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TunInfo
{
    /**
     * Returns {@code true} if the device node in /dev/tun or /dev/net/tun is available.
     *
     * @return {@code true} if the device node in /dev/tun or /dev/net/tun is available, {@code false} otherwise.
     */
    boolean isDeviceNodeAvailable();

    /**
     * Returns path to device file, which is either /dev/tun or /dev/net/tun.
     *
     * @return path to device file.
     * @throws IllegalStateException if device file is not available.
     */
    File getDeviceFile();

    /**
     * Returns {@code true} if a method to load the tun module is defined, {@code false} otherwise.
     *
     * @return {@code true} if a method to load the tun module is defined, {@code false} otherwise.
     */
    boolean hasTunLoader();

    /**
     * Returns an instance of {@link TunLoader}, capable of loading the tun module.
     *
     * @return an instance of {@link TunLoader}, capable of loading the tun module.
     */
    TunLoader getTunLoader();

    /**
     * Returns a list of all tun.ko modules found on this system.
     *
     * @return a list of all tun.ko modules found on this system.
     */
    //TODO: remove
    List<File> listTunModules();
}
