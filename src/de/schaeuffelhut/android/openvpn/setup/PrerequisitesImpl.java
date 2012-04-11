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

import de.schaeuffelhut.android.openvpn.util.Shell;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: fries
 * Date: 4/6/12
 * Time: 11:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class PrerequisitesImpl implements Prerequisites
{
    private boolean hasRootShell = false;

    public PrerequisitesImpl()
    {
        new Shell("check-for-su", "id", true){
            @Override
            protected void onCmdTerminated(int exitCode)
            {
                super.onCmdTerminated( exitCode );
                hasRootShell = exitCode == 0 ;
            }
        }.run();
    }

    public boolean hasRootShell()
    {
        return hasRootShell;
    }

    public boolean hasTunDevice()
    {
        return new File( "/dev/tun" ).exists() || new File( "/dev/net/tun" ).exists();
    }

    public boolean hasInsmod()
    {
        return new File( "/system/bin/insmod" ).exists();
    }

    public boolean hasTunKernelModule()
    {
        return new File( "/system/lib/modules/tun.ko" ).exists();
    }

    public boolean hasOpenVPN()
    {
        return new File( "/system/bin/openvpn" ).exists() || new File( "/system/xbin/openvpn" ).exists();
    }

    public boolean hasBusyBox()
    {
        return new File( "/system/bin/busybox" ).exists() || new File( "/system/xbin/busybox" ).exists();
    }
}
