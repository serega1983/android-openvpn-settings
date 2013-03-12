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

package de.schaeuffelhut.android.openvpn.util;

import de.schaeuffelhut.android.openvpn.shared.util.SystemPropertyUtil;

public class DnsUtil {
	private DnsUtil(){}

	public static Integer bumpDns() {
	    /*
	     * Bump the property that tells the name resolver library to reread
	     * the DNS server list from the properties.
	     */
		Integer n = SystemPropertyUtil.getIntProperty( SystemPropertyUtil.NET_DNSCHANGE );
		if ( n != null ) {
			n++;
		    SystemPropertyUtil.setProperty( SystemPropertyUtil.NET_DNSCHANGE, Integer.toString( n ) );			
		}
	    return n;
	}

	public static Integer setDns1(String dns1) {
		SystemPropertyUtil.setProperty( SystemPropertyUtil.NET_DNS1, dns1 );
		return bumpDns();
	}

	public static String getDns1() {
		return SystemPropertyUtil.getProperty( SystemPropertyUtil.NET_DNS1 );
	}
	
}
