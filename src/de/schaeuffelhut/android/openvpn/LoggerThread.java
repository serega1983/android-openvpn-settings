/**
 * 
 */
package de.schaeuffelhut.android.openvpn;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import android.util.Log;

class LoggerThread extends Thread
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
	public void run()
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