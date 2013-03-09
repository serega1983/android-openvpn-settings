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

import java.util.Locale;

/**
 * {@code CidrInetAddress} represents an IP address and netmask.
 * It can be normalized to turn an IP address into a route.
 */
public class CidrInetAddress
{
    private String mIp;
    private int len;

    public CidrInetAddress(String ip, String mask)
    {
        mIp = ip;
        long netmask = asLong( mask );

        // Add 33. bit to ensure the loop terminates
        netmask += 1l << 32;

        int lenZeros = 0;
        while ((netmask & 0x1) == 0)
        {
            lenZeros++;
            netmask = netmask >> 1;
        }
        // Check if rest of netmask is only 1s
        if (netmask != (0x1ffffffffl >> lenZeros))
        {
            // Asume no CIDR, set /32
            len = 32;
        }
        else
        {
            len = 32 - lenZeros;
        }
    }

    public CidrInetAddress(String ip, String mask, String mode)
    {
        this( ip, mask );

        if (len == 32 && !mask.equals( "255.255.255.255" ))
        {
            // get the netmask as IP
            long netint = CidrInetAddress.asLong( mask );
            if (Math.abs( netint - getInt() ) == 1)
            {
                if (mode.equals( "net30" ))
                    len = 30;
                else
                    len = 31;
            }
            else
            {
                //TODO: OpenVPN.logMessage(0, "", getString(R.string.ip_not_cidr, local,netmask,mode));
            }
        }
    }

    @Override
    public String toString()
    {
        return String.format( Locale.ENGLISH, "%s/%d", mIp, len );
    }


    public boolean normalise() // TODO: replace with CidrRoute
    {
        long ip = asLong( mIp );

        long newip = ip & (0xffffffffl << (32 - len));
        if (newip != ip)
        {
            mIp = String.format( "%d.%d.%d.%d", (newip & 0xff000000) >> 24, (newip & 0xff0000) >> 16, (newip & 0xff00) >> 8, newip & 0xff );
            return true;
        }
        else
        {
            return false;
        }
    }

    static long asLong(String ipaddr)
    {
        String[] ipt = ipaddr.split( "\\." );
        long ip = 0;

        ip += Long.parseLong( ipt[0] ) << 24;
        ip += Integer.parseInt( ipt[1] ) << 16;
        ip += Integer.parseInt( ipt[2] ) << 8;
        ip += Integer.parseInt( ipt[3] );

        return ip;
    }

    public long getInt()
    {
        return asLong( mIp );
    }

    public String getIp()
    {
        return mIp;
    }

    public int getPrefixLength(){
        return len;
    }
}
