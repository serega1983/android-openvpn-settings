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
package de.schaeuffelhut.android.openvpn;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import de.schaeuffelhut.android.openvpn.lib.app.R;
import de.schaeuffelhut.android.openvpn.shared.util.Util;

public class ViewLogFile extends Activity
{
	private static final String TAG = "OpenVPN-EditConfig";
	
	public static String EXTRA_FILENAME = "extra_filename";
	private TextView mLogFileName;
	private EditText mContent;

	private String logPathName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView( R.layout.view_logfile );

		mLogFileName = (TextView) findViewById(R.id.view_logfile_name);
		mLogFileName.setEnabled(false);
		
		mContent = (EditText) findViewById(R.id.view_logfile_content);
		mContent.requestFocus();
		
		logPathName = recoverLogFileName(savedInstanceState);
		mLogFileName.setText( logPathName );
		refresh();

		{
			Button refreshButton = (Button) findViewById(R.id.view_logfile_refresh);
			refreshButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					refresh();
				}
			});
		}
		
		{
			Button cancelButton = (Button) findViewById(R.id.view_logfile_cancel);
			cancelButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					finish();
				}
			});
		}
	}

	private void refresh()
	{
		mContent.setText( Util.getFileAsString( new File( logPathName ) ) );
		mContent.setSelection(mContent.getText().length());
	}

	private String recoverLogFileName(Bundle savedInstanceState) {
		String filename = savedInstanceState != null ? savedInstanceState.getString( EXTRA_FILENAME ) : null;
		if (filename == null ) {
			Bundle extras = getIntent().getExtras();
			if(extras !=null) filename = extras.getString( EXTRA_FILENAME );
		}
		if (filename == null )
			filename = "new.conf";
		
		return Preferences.logFileFor( new File( filename ) ).getAbsolutePath();
	}
}
