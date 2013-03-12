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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.net.LocalSocket;
import android.util.Log;

public class Util 
{
	public final static class IsFileFilter implements FileFilter {
		public boolean accept(File pathname) {
			return pathname.isFile();
		}
	}

	public final static class IsDirectoryFilter implements FileFilter {
		public boolean accept(File pathname) {
			return pathname.isDirectory();
		}
	}

	public final static class FileExtensionFilter implements FileFilter {
		final String[] suffix;
		public FileExtensionFilter(String... suffix) {
			this.suffix = suffix;
		}
		public boolean accept(File pathname) {
			for(String s : suffix)
				if ( pathname.getPath().endsWith(s) )
					return true;
			return false;
		}
	}

	private Util(){}
	
	public final static boolean isShellFriendly(CharSequence s)
	{
		String str = s instanceof String ? (String)s : s.toString();
		return str.matches( "^[a-zA-Z0-9_]+$" );
	}

	public final static boolean isShellFriendlyPath(CharSequence s)
	{
		String str = s instanceof String ? (String)s : s.toString();
		return str.matches( "^[/a-zA-Z0-9_.]+$" );
	}
	
	public final static void copy(File source, File target)
	{
		InputStream is = null;
		OutputStream os = null;
		try
		{
			is = new FileInputStream( source );
			os = new FileOutputStream( target );
			byte[] buffer = new byte[1024];
			int count;
			while( ( count = is.read( buffer ) ) != -1 )
				os.write( buffer, 0, count );
		}
		catch (IOException e)
		{
			Log.e( "OpenVPN", "copy", e );
		}
		finally
		{
			closeQuietly( is );
			closeQuietly( os );
		}
	}

	public final static void closeQuietly(InputStream is)
	{
		try{
			if ( is !=null )
				is.close();
		}catch (Exception e) {
			Log.e( "OpenVPN", "closing InputStream", e );
		}
	}

	public final static void closeQuietly(AssetFileDescriptor afd)
	{
		try{
			if ( afd != null)
				afd.close();
		}catch (Exception e) {
			Log.e( "OpenVPN", "closing AssetFileDescriptor", e );
		}
	}

	public final static void closeQuietly(Reader r)
	{
		try{
			if ( r != null)
				r.close();
		}catch (Exception e) {
			Log.e( "OpenVPN", "closing InputStream", e );
		}
	}

	public final static void closeQuietly(OutputStream os)
	{
		try{
			if ( os != null )
				os.close();
		}catch (Exception e) {
			Log.e( "OpenVPN", "closing OutputStream", e );
		}
	}

	public final static void closeQuietly(Writer writer)
	{
		try{
			if ( writer != null )
				writer.close();
		}catch (Exception e) {
			Log.e( "OpenVPN", "closing Writer", e );
		}
	}

	public final static void closeQuietly(Socket s)
	{
		try{
			if ( s != null )
				s.close();
		}catch (Exception e) {
			Log.e( "OpenVPN", "closing Socket", e );
		}
	}

	public final static void closeQuietly(LocalSocket s)
	{
		try{
			if ( s != null )
				s.close();
		}catch (Exception e) {
			Log.e( "OpenVPN", "closing LocalSocket", e );
		}
	}

	public final static String join(List<String> command, char c) {
		StringBuilder sb = new StringBuilder();
		for(Iterator<String> it=command.iterator(); it.hasNext();)
		{
			sb.append( it.next() );
			if ( it.hasNext() )
				sb.append( c );
		}
		return sb.toString();
	}

	public final static String makeShellCompatible(String name)
	{
		return name.replaceAll( "[^a-zA-Z0-9_]", "_" ); 
	}

	public final static int waitForQuietly(Process p)
	{
		int exitCode = Integer.MAX_VALUE;
		if ( p != null )
		{
			waitFor: do
			{
				try {
					exitCode = p.waitFor();
				}
				catch (InterruptedException e)
				{
					continue waitFor;
				}
			} while ( false );
		}
		return exitCode;
	}

	public final static void joinQuietly(Thread t)
	{
		join: do
		{
			try {
				t.join();
			}
			catch (InterruptedException e)
			{
				continue join;
			}
		} while ( false );
	}

	public final static void copyAsset(String tag, AssetManager assets, String assetName,
			File target) {
		if ( target.exists() )
		{
			Log.d( tag, String.format( "asset %s already installed at %s", assetName, target ) );
		}
		else
		{
			Log.d( tag, String.format( "copying asset %s to %s", assetName, target ) );
			InputStream is = null;
			OutputStream os = null;
			try
			{
				is = assets.open( assetName );
				os = new FileOutputStream( target );
				byte[] buffer = new byte[1024];
				int count;
				while( ( count = is.read( buffer ) ) != -1 )
					os.write( buffer, 0, count );
			}
			catch (IOException e)
			{
				Log.e( tag, "copy", e );
			}
			finally
			{
				closeQuietly( is );
				closeQuietly( os );
			}
		}
	}

	public final static void waitQuietly(Thread t)
	{
		wait: do {
			try {
				t.wait();
			} catch (InterruptedException e) {
				continue wait;
			}
		} while (false); 
	}

	public final static String applicationVersionName(Context context) {
		String versionName = "unknown";
		try {
			versionName = context.getPackageManager().getPackageInfo( context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionName;
	}

	public final static int applicationVersionCode(Context context) {
		int versionCode = 0;
		try {
			versionCode = context.getPackageManager().getPackageInfo( context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}

    public final static String getAssetAsString(Context context, String asset) {
		Reader reader = null;
		StringBuilder sb = new StringBuilder(1024);
		try {
			reader = new InputStreamReader( context.getAssets().open(asset) );
			char[] buf = new char[1024];
			int length;
			while( ( length = reader.read(buf) ) >= 0 )
				sb.append( buf, 0, length );
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeQuietly(reader);
		}
		String string = sb.toString();
		return string;
	}

	public static CharSequence getFileAsString(File file) {
		Reader reader = null;
		StringBuilder sb = new StringBuilder(1024);
		try {
			reader = new InputStreamReader( new FileInputStream(file), Charset.forName( "ISO-8859-1" )  );
			char[] buf = new char[1024];
			int length;
			while( ( length = reader.read(buf) ) >= 0 )
				sb.append( buf, 0, length );
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeQuietly(reader);
		}
		String string = sb.toString();
		return string;
	}
	
	/**
	 * @author chri
	 */
	public static String roundDecimalsToString(double d) {
		// using DecimalFormat we get i18n issues.
		if ( d < 10 )
	         return String.valueOf(Math.ceil(d * 100) / 100);
		if ( d < 100 )
	         return String.valueOf(Math.ceil(d * 10) / 10);
		else
	         return String.valueOf(Math.round(d));
	}
	
	/**
	 * Prepare and sanitize a string to be used as parameter for a command
	 * @param s
	 * @return A string safe to use as parameter for a command
	 */
	public static String shellEscape(String s) {
		StringBuilder sb = new StringBuilder( s.length()+200);
		sb.append("'");
		sb.append(s.replace("'", "\\'").replace("\n", "\\\n"));
		sb.append("'");
		return sb.toString();
	}

	/**
	 * Prepare and sanitize a string to be used as parameter for a command
	 * @param s
	 * @return A string safe to use as parameter for a command
	 */
	public static String optionalShellEscape(String s) {
        if ( s.matches( "[a-zA-Z0-9/._-]+" ) )
            return s;

		return shellEscape( s );
	}

	/**
	 * Locte the tun.ko driver on the filesystem  
	 * @return a String with the path of the tun.ko driver
	 */
	public static String findTunDriverPath() {
		String tunDriverPath = "";
		String find_str; 
		try {
			Process find_proc = Runtime.getRuntime().exec("find /sdcard /system /data -name tun.ko");
			DataInputStream find_in = new DataInputStream(find_proc.getInputStream());
			try {
				while ((find_str = find_in.readLine()) != null) {
					tunDriverPath = find_str;
				}
			} catch (IOException e) {
				System.exit(0);
			}
		} catch (IOException e1) {
			Log.e("OpenVpnSettings_Util",e1.toString());
		}
		
//		if (new File(tunDriverPath).exists())
//			System.out.println(tunDriverPath);
//		else System.out.println("ERROR file does not exist: " + tunDriverPath);
		return tunDriverPath;
	}

	/**
	 * List files in a null pointer safe way.
	 * @param configDir the directory to list, may be null
	 * @param filter the filter to match names against, may be null.
	 * @return an array of files if no files match the filters the empty array is returned.
	 */
	public static File[] listFiles(File configDir, FileFilter filter) {
		final File[] files;
		if ( configDir == null )
			files = null;
		else
			files = configDir.listFiles( filter );
		return files == null ? new File[0] : files;
	}

	public static boolean hasTunSupport()
	{
		// TODO: is /dev/tun a reliable indicator for tun capability being installed
		try
		{
			return new File("/dev/tun").exists() || new File("/dev/net/tun").exists();
		}
		catch (Exception e) {
			return false;
		}
	}

    public static boolean isBlank(String string)
    {
        if ( string == null )
            return true;
        return string.trim().length() == 0;
    }
}
