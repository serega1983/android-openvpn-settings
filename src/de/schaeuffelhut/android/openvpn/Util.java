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
package de.schaeuffelhut.android.openvpn;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;

public class Util 
{
	final static class IsFileFilter implements FileFilter {
		public boolean accept(File pathname) {
			return pathname.isFile();
		}
	}

	final static class FileExtensionFilter implements FileFilter {
		final String suffix;
		public FileExtensionFilter(String suffix) {
			this.suffix = suffix;
		}
		public boolean accept(File pathname) {
			return pathname.getPath().endsWith(suffix);
		}
	}

	private Util(){}
	
	static boolean isShellFriendly(CharSequence s)
	{
		String str = s instanceof String ? (String)s : s.toString();
		return str.matches( "^[a-zA-Z0-9_]+$" );
	}

	static boolean isShellFriendlyPath(CharSequence s)
	{
		String str = s instanceof String ? (String)s : s.toString();
		return str.matches( "^[/a-zA-Z0-9_.]+$" );
	}
	
	static void copy(File source, File target)
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

	static void closeQuietly(InputStream is)
	{
		try{
			if ( is !=null )
				is.close();
		}catch (Exception e) {
			Log.e( "OpenVPN", "closing InputStream", e );
		}
	}

	static void closeQuietly(AssetFileDescriptor afd)
	{
		try{
			if ( afd != null)
				afd.close();
		}catch (Exception e) {
			Log.e( "OpenVPN", "closing AssetFileDescriptor", e );
		}
	}

	static void closeQuietly(Reader r)
	{
		try{
			if ( r != null)
				r.close();
		}catch (Exception e) {
			Log.e( "OpenVPN", "closing InputStream", e );
		}
	}

	static void closeQuietly(OutputStream os)
	{
		try{
			if ( os != null )
				os.close();
		}catch (Exception e) {
			Log.e( "OpenVPN", "closing OutputStream", e );
		}
	}

	static String join(List<String> command, char c) {
		StringBuilder sb = new StringBuilder();
		for(Iterator<String> it=command.iterator(); it.hasNext();)
		{
			sb.append( it.next() );
			if ( it.hasNext() )
				sb.append( c );
		}
		return sb.toString();
	}

	static String makeShellCompatible(String name)
	{
		return name.replaceAll( "[^a-zA-Z0-9_]", "_" ); 
	}

	public static int waitForQuietly(Process p)
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

	public static void joinQuietly(Thread t)
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

	static void copyAsset(String tag, AssetManager assets, String assetName,
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

	public static void waitQuietly(Thread t)
	{
		wait: do {
			try {
				t.wait();
			} catch (InterruptedException e) {
				continue wait;
			}
		} while (false); 
	}

}
