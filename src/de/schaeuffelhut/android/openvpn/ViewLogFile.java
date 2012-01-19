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

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import de.schaeuffelhut.android.openvpn.util.Util;

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
		
		setContentView(R.layout.view_logfile );

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
