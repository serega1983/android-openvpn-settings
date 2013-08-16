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

package de.schaeuffelhut.android.openvpn;

import android.content.Context;
import de.schaeuffelhut.android.openvpn.setup.prerequisites.ProbePrerequisites;
import de.schaeuffelhut.android.openvpn.util.tun.TunInfo;
import de.schaeuffelhut.android.openvpn.util.tun.TunInfoImpl;

/**
 * Created with IntelliJ IDEA.
 * User: fries
 * Date: 4/10/12
 * Time: 9:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class IocContext
{
    static IocContext iocContext = new IocContext();

    private boolean fulfilsPrerequisites = false;

    public final static IocContext get()
    {
        return iocContext;
    }

    public ProbePrerequisites probePrerequisites(Context context)
    {
        ProbePrerequisites probePrerequisites = new ProbePrerequisites();
        probePrerequisites.probe( context );
        fulfilsPrerequisites = probePrerequisites.isSuccess();
        return probePrerequisites;
    }

    public boolean fulfilsPrerequisites()
    {
        return fulfilsPrerequisites;
    }
}
