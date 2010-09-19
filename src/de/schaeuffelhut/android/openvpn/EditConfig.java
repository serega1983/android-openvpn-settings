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
import de.schaeuffelhut.android.openvpn.util.Util;

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

		setContentView(R.layout.edit_config);

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
