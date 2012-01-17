package de.schaeuffelhut.android.openvpn.tun;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import de.schaeuffelhut.android.openvpn.Preferences;
import de.schaeuffelhut.android.openvpn.util.SystemPropertyUtil;

public class DeviceDetails
{
	final String kernelVersion;
	final String pathToTun;
	final String deviceDetails;
	
	public DeviceDetails(SharedPreferences pref)
	{
		kernelVersion = readKernelVersion();
		pathToTun = findPathToTun( pref );
		deviceDetails = collectDetails();
	}

	
	private static String readKernelVersion()
	{
		final String procVersion;
		try {
			procVersion = FileUtils.readFileToString( new File( "/proc/version" ) );
		} catch (IOException e) {
			// LOG via ACRA
			Log.e( "OpenVPN-Settings", "reading /proc/version", e );
			return "unknown";
		}
		
		if ( procVersion.startsWith( "Linux version " ) )
			return procVersion.substring( "Linux version ".length(), procVersion.indexOf( " ", "Linux version ".length() ) );

		return procVersion;
	}

	private static String findPathToTun(SharedPreferences pref) {
		String pathToTun = Preferences.getPathToTun( pref );
		if ( !TextUtils.isEmpty( pathToTun ) && new File( pathToTun ).exists() )
			return pathToTun;
		
		if ( new File( "/system/lib/modules/tun.ko" ).exists() )
			return "/system/lib/modules/tun.ko";
			
		return "unknown";
	}

	private static String collectDetails() {
		
		StringBuilder sb = new StringBuilder();
		
		detectFiles(sb);
		readSystemProperties(sb);

		return sb.toString(); 
	}

	private static void detectFiles(StringBuilder sb) {
		sb.append( "Detecting files in default locations:\n" );
		detectFile(sb, "/system/xbin/openvpn");
		detectFile(sb, "/system/bin/openvpn");
		detectFile(sb, "/system/xbin/busybox");
		detectFile(sb, "/system/sbin/busybox");
		detectFile(sb, "/system/bin/busybox");
		detectFile(sb, "/system/lib/modules/tun.ko");
		detectFile(sb, "/system/bin/su");
		detectFile(sb, "/system/xbin/su");
		detectFile(sb, "/system/sbin/su");
		sb.append( "\n" );
	}

	private static void detectFile(StringBuilder sb, String file) {
		sb.append( file );
		sb.append( ' ' );
		if ( new File( file ).exists() )
			sb.append( "exists" );
		else
			sb.append( "not found" );
		sb.append( '\n' );
	}

	private static void readSystemProperties(StringBuilder sb) {
		sb.append( "System Properties (ro.build*, ro.product*, ro.revision) :\n" );
		HashMap<String, String> properties = SystemPropertyUtil.getProperties();
		ArrayList<Entry<String, String>> entrySet = new ArrayList<Entry<String, String>>( properties.entrySet());
		Collections.sort( entrySet, new Comparator<Entry<String, String>>() {
			public int compare(Entry<String, String> lhs, Entry<String, String> rhs) {
				return lhs.getKey().compareTo(rhs.getKey());
			}
		});
		for(Map.Entry<String, String> e : entrySet )
		{
			String key = e.getKey();
			if ( key.startsWith( "ro.build" ) || key.startsWith( "ro.product" ) || key.startsWith( "ro.revision" ) )
			{
				sb.append( key );
				sb.append( ": " );
				sb.append( e.getValue() );
				sb.append( '\n' );
			}
		}
		sb.append( "\n" );
	}
}

