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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import android.util.Log;
import de.schaeuffelhut.android.openvpn.shared.util.Util;

public class LoggerThread extends Thread
{
	final String tag;
	final InputStream is;
	final boolean closeInput;

	public LoggerThread(String tag, InputStream is, boolean closeInput)
	{
		this.tag = tag;
		this.is = is;
		this.closeInput = closeInput;
		setName( tag );
	}

	@Override
	public final void run()
	{
		try
		{
			LineNumberReader lnr = new LineNumberReader(
					new InputStreamReader( is ),
					256
			);
			String line;
			while( null != ( line = lnr.readLine() ) )
			{
				Log.d( tag, line );
				onLogLine( line );
			}
		}
		catch (Exception e)
		{
			Log.e( tag, "error", e );
		}
		finally
		{
			if ( closeInput )
				Util.closeQuietly( is );
			Log.i( tag, "terminated" );
			onTerminate();
		}
	}

	protected void onLogLine(String line) {
		//overwrite if desired
	}

	protected void onTerminate() {
		//overwrite if desired
	}
}