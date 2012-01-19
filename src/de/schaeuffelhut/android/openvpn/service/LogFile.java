package de.schaeuffelhut.android.openvpn.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import android.util.Log;

public class LogFile {
	
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
			fileOutputStream = new FileOutputStream( logFile, true );
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
