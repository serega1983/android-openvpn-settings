package de.schaeuffelhut.android.openvpn.util;

public class DnsUtil {
	private DnsUtil(){}

	public static int bumpDns() {
	    /*
	     * Bump the property that tells the name resolver library to reread
	     * the DNS server list from the properties.
	     */
	    String propVal = SystemPropertyUtil.getProperty( SystemPropertyUtil.NET_DNSCHANGE );
	    int n = 0;
	    if (propVal.length() != 0) {
	        try {
	            n = Integer.parseInt(propVal);
	        } catch (NumberFormatException e) {}
	    }
	    n++;
	    SystemPropertyUtil.setProperty( SystemPropertyUtil.NET_DNSCHANGE, "" + n );
	    return n;
	}

	public static int setDns1(String dns1) {
		SystemPropertyUtil.setProperty( SystemPropertyUtil.NET_DNS1, dns1 );
		return bumpDns();
	}

	public static String getDns1() {
		return SystemPropertyUtil.getProperty( SystemPropertyUtil.NET_DNS1 );
	}
	
}
