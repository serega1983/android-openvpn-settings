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

package de.schaeuffelhut.android.openvpn.lib.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import android.util.Log;

class LogFile {
	
	private final File logFile;
	private FileOutputStream fileOutputStream;

	public LogFile(File logFile)
	{
		this.logFile = logFile;
	}

	void open()
	{
		if ( fileOutputStream != null )
			return;
		
		Log.d( "OpenVPn Settings", "Opening log file " + logFile );
		try {
			fileOutputStream = new FileOutputStream( logFile, false );
		} catch (FileNotFoundException e) {
			Log.e( "OpenVPN Settings", "Failed to open log file: " + logFile, e );
		}
	}
	
	void append(String log)
	{
		if ( fileOutputStream == null )
			return;
		
		try {
			fileOutputStream.write( log.getBytes() );
			fileOutputStream.write( '\r' );
			fileOutputStream.write( '\n' );
		} catch (IOException e) {
			Log.e( "OpenVPN Settings", "Failed writing to log file: " + log, e );
			IOUtils.closeQuietly( fileOutputStream );
			fileOutputStream = null;
		}
	}
	
	void close()
	{
		Log.d( "OpenVPn Settings", "Closing log file " + logFile );
		IOUtils.closeQuietly( fileOutputStream );
		fileOutputStream = null;
	}
}
