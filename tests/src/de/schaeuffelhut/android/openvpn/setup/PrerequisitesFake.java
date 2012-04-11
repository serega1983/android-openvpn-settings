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

/**
 * Created with IntelliJ IDEA.
 * User: fries
 * Date: 4/11/12
 * Time: 6:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class PrerequisitesFake implements Prerequisites
{
    public static final int FLAG_NONE = 0;
    public static final int FLAG_HAS_ROOT_SHELL = 1;
    public static final int FLAG_HAS_TUN_DEVICE = 2;
    public static final int FLAG_HAS_INSMOD = 4;
    public static final int FLAG_HAS_TUN_KERNEL_MODULE = 8;
    public static final int FLAG_HAS_OPENVPN = 16;
    public static final int FLAG_HAS_BUSYBOX = 32;
    private int flags;

    public boolean hasRootShell()
    {
        return checkFlag( FLAG_HAS_ROOT_SHELL );
    }

    public boolean hasTunDevice()
    {
        return checkFlag( FLAG_HAS_TUN_DEVICE );
    }

    public boolean hasInsmod()
    {
        return checkFlag( FLAG_HAS_INSMOD );
    }

    public boolean hasTunKernelModule()
    {
        return checkFlag( FLAG_HAS_TUN_KERNEL_MODULE );
    }

    public boolean hasOpenVPN()
    {
        return checkFlag( FLAG_HAS_OPENVPN );
    }

    public boolean hasBusyBox()
    {
        return checkFlag( FLAG_HAS_BUSYBOX );
    }


    void set(int flags)
    {
        this.flags = flags;
    }

    private boolean checkFlag(int flag)
    {
        return ( flags & flag) == flag;
    }

}
