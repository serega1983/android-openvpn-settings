/**
 * Copyright 2009 Friedrich Schäuffelhut
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package de.schaeuffelhut.android.openvpn.util;

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
import android.util.Log;

public class Util 
{
	public final static class IsFileFilter implements FileFilter {
		public boolean accept(File pathname) {
			return pathname.isFile();
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

}
