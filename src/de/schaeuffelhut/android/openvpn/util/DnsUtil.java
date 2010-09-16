package de.schaeuffelhut.android.openvpn.util;

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
