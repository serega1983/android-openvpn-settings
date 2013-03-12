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

package de.schaeuffelhut.android.openvpn.lib.service.impl;

import android.os.ParcelFileDescriptor;
import de.schaeuffelhut.android.openvpn.shared.util.CidrInetAddress;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;

/**
 * Configures and creates a tun device for an Android 4 (ICS) vpn.
* @author Friedrich Schäuffelhut
* @since 2013-03-10
*/
public class IfConfig
{
    private CidrInetAddress localIp;
    private int mtu;
    private String mode;
    private List<CidrInetAddress> routes = new ArrayList<CidrInetAddress>( 4 );
    private List<String> dnssevers = new ArrayList<String>( 4 );

    void setIfconfig(String msg)
    {
        String[] fields = msg.split( " " );
        //TODO: assert field length
        String ip = fields[0];
        String netmask = fields[1];
        mtu = Integer.parseInt( fields[2] );
        mode = fields[3];

        localIp = new CidrInetAddress( ip, netmask, mode );
    }

    public void setRoute(String msg)
    {
        String[] fields = msg.split( " " );
        //TODO: assert field length
        String dest = fields[0];
        String mask = fields[1];

        //TODO: use CidrRoute here
        CidrInetAddress route = new CidrInetAddress(dest, mask);
        if(route.getPrefixLength() == 32 && !mask.equals("255.255.255.255")) {
            //TODO: OpenVPN.logMessage(0, "", getString(R.string.route_not_cidr,dest,mask));
        }

        if(route.normalise())
            //TODO: OpenVPN.logMessage(0, "", getString(R.string.route_not_netip,dest,route.len,route.mIp));

        routes.add(route);
    }

    public void setDnsServer(String msg)
    {
        dnssevers.add(msg);
    }

    void protect(FileDescriptor fd){
        throw new RuntimeException( "not implemented" );
        //VpnService.this.protect( fdInt );
    }

    protected ParcelFileDescriptor establish(){
        throw new RuntimeException( "not implemented" );
//        VpnService.Builder builder = new VpnService.Builder();
//        configure( builder );
//        return builder.establish();
    }

//    private void configure(VpnService.Builder builder)
//    {
//        builder.setSession( "TODO: TestSession" );
//        builder.addAddress( localIp.getIp(), localIp.getPrefixLength() ); //TODO: make localIp optional, handle error
//        builder.setMtu( mtu );
//        for (CidrInetAddress route : routes)
//            builder.addRoute( route.getIp(), route.getPrefixLength() );
//        for (String dnsserver : dnssevers)
//            builder.addDnsServer( dnsserver );
////            builder.addSearchDomain(  );
//        builder.addRoute( "0.0.0.0", 0 );
//    }

}
