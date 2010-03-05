package de.schaeuffelhut.android.openvpn.util;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class SystemPropertyUtil
{
	public final static String NET_DNS1 = "net.dns1";
	public final static String NET_DNS2 = "net.dns2";
	public final static String NET_DNSCHANGE = "net.dnschange";
	
	private final static class GetProp extends Shell {
		private final String key;
		String value;

		private GetProp(String key) {
			super( "OpenVPN-Settings-getprop" );
			this.key = key;
		}

		@Override
		protected void onShellPrepared() {
			exec( "getprop " + key );
		}

		@Override
		protected void onStdout(String line) {
			super.onStdout(line);
			value = line;
		}
		
		@Override
		protected void onShellTerminated() {
			try { joinLoggers(); } catch (InterruptedException e) {};
			waitForQuietly();
		}
	}

	private final static class GetAllProp extends Shell {
		HashMap<String, String> properties = new HashMap<String, String>();
		Pattern p = Pattern.compile("^\\[(.*?)\\]: \\[(.*?)\\]$");
		Matcher m = p.matcher("");
		
		private GetAllProp() {
			super( "OpenVPN-Settings-getprop" );
		}

		@Override
		protected void onShellPrepared() {
			exec( "getprop"  );
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
		
		@Override
		protected void onShellTerminated() {
			try { joinLoggers(); } catch (InterruptedException e) {}
			waitForQuietly();
		}
	}

	private final static class SetProp extends Shell {
		private final String key;
		private final String value;

		private SetProp(String key, String value) {
			super( "OpenVPN-Settings-setprop" );
			this.key = key;
			this.value = value;
		}

		@Override
		protected void onShellPrepared() {
			su();
			//TODO: understand this necessary delay
			try { sleep(500); } catch (InterruptedException e) {}
			exec( "setprop " + key + " " + value );
		}

		@Override
		protected void onShellTerminated() {
			try { joinLoggers(); } catch (InterruptedException e) {}
			waitForQuietly();
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

	public final static int getIntProperty(String key)
	{
		final int result;
		String value = getProperty(key);
		if ( value == null )
			result = 0;
		else
			result = Integer.parseInt( value );
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
