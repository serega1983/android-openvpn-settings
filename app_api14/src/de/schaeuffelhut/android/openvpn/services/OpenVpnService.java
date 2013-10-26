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

import android.os.ParcelFileDescriptor;
import de.schaeuffelhut.android.openvpn.lib.openvpn4.InstallFailed;
import de.schaeuffelhut.android.openvpn.lib.service.impl.CmdLineBuilder14;
import de.schaeuffelhut.android.openvpn.lib.service.impl.IfConfig;
import de.schaeuffelhut.android.openvpn.lib.service.impl.IfConfigFactory;
import de.schaeuffelhut.android.openvpn.lib.service.impl.OpenVpnServiceImpl;
import de.schaeuffelhut.android.openvpn.shared.util.CidrInetAddress;
import de.schaeuffelhut.android.openvpn.shared.util.JniUtil;
import de.schaeuffelhut.android.openvpn.shared.util.service.DelegatingVpnService;

import java.io.FileDescriptor;
import java.util.List;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-03-13
 */
public class OpenVpnService extends DelegatingVpnService<OpenVpnServiceImpl>
{
    OpenVpnService()
    {
        try
        {
            de.schaeuffelhut.android.openvpn.lib.openvpn4.Installer.install( this );
        }
        catch (InstallFailed installFailed)
        {
            throw new RuntimeException( installFailed ); //TODO: handle exception
        }
    }

    @Override
    protected OpenVpnServiceImpl createServiceDelegate()
    {
        return new OpenVpnServiceImpl(
                this,
                new MyIfConfigFactory(),
                new CmdLineBuilder14( getApplicationContext() )
        );
    }

    private class MyIfConfigFactory implements IfConfigFactory
    {
        public IfConfig createIfConfig()
        {
            return new MyIfConfig();
        }

    }

    private class MyIfConfig extends IfConfig
    {
        protected void protect(FileDescriptor fd)
        {
            OpenVpnService.this.protect( JniUtil.asInt( fd ) );
        }

        @Override
        protected ParcelFileDescriptor establish(CidrInetAddress localIp, int mtu, String mode, List<CidrInetAddress> routes, List<String> dnsServers)
        {
            Builder builder = new Builder();

            builder.setSession( "Unknown" );

            builder.addAddress( localIp.getIp(), localIp.getPrefixLength() ); //TODO: make localIp optional, handle error
            builder.setMtu( mtu );

            for (CidrInetAddress route : routes)
                builder.addRoute( route.getIp(), route.getPrefixLength() );

            for (String dnsServer : dnsServers)
                builder.addDnsServer( dnsServer );

//            builder.addSearchDomain(  );

            return builder.establish();
        }
    }
}
