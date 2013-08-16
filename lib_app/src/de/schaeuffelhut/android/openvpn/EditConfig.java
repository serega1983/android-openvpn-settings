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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import de.schaeuffelhut.android.openvpn.lib.app.R;
import de.schaeuffelhut.android.openvpn.shared.util.Util;

public class EditConfig extends Activity
{
	private static final String TAG = "OpenVPN-EditConfig";
	
	public static String EXTRA_FILENAME = "extra_filename";
	private EditText mConfigName;
	private EditText mContent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// always return config name to caller
		setResult( 0, new Intent().putExtra(EXTRA_FILENAME, getIntent().getStringExtra( EXTRA_FILENAME ) ) );

		setContentView( R.layout.edit_config);

		mConfigName = (EditText) findViewById(R.id.edit_config_name);
		mConfigName.setEnabled(false);
		
		mContent = (EditText) findViewById(R.id.edit_config_content);
		
		String configPathName = recoverConfigName(savedInstanceState);
		mConfigName.setText( configPathName );
		mContent.setText( Util.getFileAsString( new File( configPathName ) ) );
		
//
//		Log.i("Edit" , "Populating fields for file: " + filename);
//		populateFields();

		{
			Button saveButton = (Button) findViewById(R.id.edit_config_save);
			saveButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					save();
					Intent intent = new Intent();
					intent.putExtra( EXTRA_FILENAME, mConfigName.getText().toString() );
					setResult(RESULT_OK, intent);
					finish();
				}
			});
		}
		
		{
			Button cancelButton = (Button) findViewById(R.id.edit_config_cancel);
			cancelButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					setResult(RESULT_CANCELED);
					finish();
				}
			});
		}
	}

	private String recoverConfigName(Bundle savedInstanceState) {
		String filename = savedInstanceState != null ? savedInstanceState.getString( EXTRA_FILENAME ) : null;
		if (filename == null ) {
			Bundle extras = getIntent().getExtras();
			if(extras !=null) filename = extras.getString( EXTRA_FILENAME );
		}
		if (filename == null )
			filename = "new.conf";
		return filename;
	}
	
	private void save() {
		File file = new File( mConfigName.getText().toString() );

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream(file), Charset.forName("ISO-8859-1" ) ) );
			writer.write( mContent.getText().toString() );
			writer.flush();
		} catch (IOException e) {
			new AlertDialog.Builder( this ).setIcon(android.R.drawable.ic_dialog_info).setTitle("Save faild").setMessage( e.getMessage() ).create().show();
			Log.e(TAG, "save config faild", e);
		} finally {
			Util.closeQuietly( writer );
		}
	}
}
