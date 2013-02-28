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

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;
import de.schaeuffelhut.android.openvpn.shared.util.Shell;

public class SystemPropertyUtil
{
	public final static String NET_DNS1 = "net.dns1";
	public final static String NET_DNS2 = "net.dns2";
	public final static String NET_DNSCHANGE = "net.dnschange";
	
	private final static class GetProp extends Shell {
		String value;

		private GetProp(String key) {
			super( "OpenVPN-Settings-getprop", "getprop " + key, Shell.SH );
		}

		@Override
		protected void onStdout(String line) {
			super.onStdout(line);
			value = line;
		}
	}

	private final static class GetAllProp extends Shell {
		HashMap<String, String> properties = new HashMap<String, String>();
		Pattern p = Pattern.compile("^\\[(.*?)\\]: \\[(.*?)\\]$");
		Matcher m = p.matcher("");
		
		private GetAllProp() {
			super( "OpenVPN-Settings-getallprop", "getprop", Shell.SH );
		}

		@Override
		protected void onStdout(String line) {
			// no logging
			//super.onStdout(line);
			m.reset( line );
			if ( m.matches() )
				properties.put( m.group( 1 ), m.group( 2 ) );
			else
				Log.e("OpenVPN-Settings-getprop", "unmatched: "+line);
		}
	}

	private final static class SetProp extends Shell {

		private SetProp(String key, String value) {
			super( "OpenVPN-Settings-setprop", "setprop " + key + " " + value, Shell.SU );
		}
	}

	public final static void setProperty(final String key, String value)
	{
		SetProp setProp = new SetProp(key, value);
		setProp.run();
	}

	public final static String getProperty(final String key)
	{
		GetProp getProp = new GetProp(key);
		getProp.run();
		return getProp.value;
	}

	public final static Integer getIntProperty(String key)
	{
		Integer result;
		String value = getProperty(key);
		if ( value == null )
			result = null;
		else if ( "".equals( value ) )
			result = null;
		else
		{
			try {
				result = Integer.parseInt( value );
			} catch (NumberFormatException e) {
				Log.e( "OpenVPN-Settings", String.format( "getIntProperty(\"%s\")", value ), e );
				result = null;
			}
		}
		return result;
	}
	
	public final static HashMap<String, String> getProperties()
	{
		GetAllProp getProp = new GetAllProp();
		getProp.run();
		return getProp.properties;
	}

	public final static int getInt(HashMap<String, String> properties, String key)
	{
		final int result;
		String value = properties.get( key );
		if ( value == null )
			result = 0;
		else
			result = Integer.parseInt( value );
		return result;
	}
}
