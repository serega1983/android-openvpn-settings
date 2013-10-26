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

package de.schaeuffelhut.android.openvpn.services;

import de.schaeuffelhut.android.openvpn.lib.openvpn.Installer;
import de.schaeuffelhut.android.openvpn.lib.service.impl.*;
import de.schaeuffelhut.android.openvpn.shared.util.service.DelegatingService;

import java.io.File;

/**
 * This class provides a unique and persistent name for the OpenVpnService implemented else where.
 * Other APPS may use this name to lookup and find the OpenVpnService.
 * @author Friedrich Schäuffelhut
 * @since 2012-11-13
 */
public class OpenVpnService extends DelegatingService<OpenVpnServiceImpl>
{
    public static final String NAME = "de.schaeuffelhut.android.openvpn.services.OpenVpnService";

    public OpenVpnService()
    {
        super();

        try
        {
            Installer installer = new Installer( this );
            installer.installOpenVpn();
            installer.installBusyBox();
        }
        catch (de.schaeuffelhut.android.openvpn.lib.openvpn.InstallFailed installFailed)
        {
            throw new RuntimeException( installFailed ); //TODO: handle exception
        }
    }

    @Override
    protected OpenVpnServiceImpl createServiceDelegate()
    {
        return new OpenVpnServiceImpl( this, new IfConfigFactoryImpl(), new CmdLineBuilder4( getApplicationContext() ) );
    }

    /*
     * Keep implementation outside this class and package.
     */
}
