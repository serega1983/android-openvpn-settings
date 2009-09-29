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
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class ImportFiles extends Activity
{
	private final static int FIND_IMPORT_DIR = 1;
	private static final int SAVE_PROGRESS_DIALOG = 1;
	private ProgressDialog saveProgressDialog;
	
	private Button findImportDir_bt;
	private Button ok_bt;
	private Button cancel_bt;
	private EditText importDir_et;
	private RadioGroup importType_rg;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView( R.layout.import_files );

		SharedPreferences preferences = getPreferences( 0 );
		
		importDir_et = (EditText)findViewById(R.id.import_files_import_dir );
		importDir_et.setText( preferences.getString( "import-dir", "/sdcard") );
		importDir_et.addTextChangedListener(new TextWatcher(){
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void afterTextChanged(Editable s) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				validateSave();
			}
		});
		
		findImportDir_bt = (Button)findViewById( R.id.import_files_find_import_dir );
		findImportDir_bt.setOnClickListener(
				new OnClickListener(){
					public void onClick(View v) {
						try
						{
							Intent intent = new Intent("org.openintents.action.PICK_DIRECTORY");
							//intent.setData(Uri.parse("file://" + fileName));
							intent.putExtra(
									"org.openintents.extra.TITLE",
									"Import OpenVPN config"
							);
							startActivityForResult(intent, FIND_IMPORT_DIR);
						}
						catch (ActivityNotFoundException e)
						{
							Toast.makeText(
									getApplicationContext(),
									"You need to install \"OI File Manager\" from the market!",
									Toast.LENGTH_LONG
							).show();
						}
					}
				});

		importType_rg = (RadioGroup)findViewById( R.id.import_files_replace_or_merge );
		String importType = preferences.getString( "import-type", "replace" );
		if ( "replace".equals( importType ) )
			importType_rg.check( R.id.import_files_replace );
		else if ( "merge".equals( importType ) )
			importType_rg.check( R.id.import_files_merge );
		else if ( "add".equals( importType ) )
			importType_rg.check( R.id.import_files_add );
		
		ok_bt = (Button)findViewById( R.id.import_files_ok );
		ok_bt.setOnClickListener( new OnClickListener(){
			public void onClick(View v) {
				save();
			}
		});

		cancel_bt = (Button)findViewById( R.id.import_files_cancel );
		cancel_bt.setOnClickListener( new OnClickListener(){
			public void onClick(View v) {
				setResult( RESULT_CANCELED );
				finish();
			}
		});

		validateSave();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch ( requestCode ) {
		case FIND_IMPORT_DIR:
			Log.i( "OpenVPN", String.format("onActivityResult( FIND_IMPORT_DIR, %d, <data> )", resultCode ) );
			
			final File selectedDir = data.getData() == null ? null : new File( data.getData().getPath() );
			Log.i( "OpenVPN", "data.getData().getPath(): " + selectedDir );

			if ( selectedDir == null )
			{
				Toast.makeText(
						getApplicationContext(),
						"No directory selected!",
						Toast.LENGTH_SHORT
				).show();
			}
			else if ( !selectedDir.exists()|| !selectedDir.isDirectory() )
			{
				Toast.makeText(
						getApplicationContext(),
						"Selected directory does not exists!",
						Toast.LENGTH_SHORT
				).show();
			}
			else if ( !selectedDir.isDirectory() )
			{
				Toast.makeText(
						getApplicationContext(),
						"Selected file must be a  directory!",
						Toast.LENGTH_SHORT
				).show();
			}
			else
			{
				importDir_et.setText( selectedDir.getPath() );
			}
			validateSave();
			break;
		}
	}
	
	void validateSave()
	{
		boolean isValid = true;

		isValid &= new File( importDir_et.getText().toString() ).isDirectory();

		ok_bt.setEnabled( isValid );
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case SAVE_PROGRESS_DIALOG: {
			saveProgressDialog = new ProgressDialog(this);
            saveProgressDialog.setTitle("Importing");
            saveProgressDialog.setMessage("Please wait while loading...");
            saveProgressDialog.setIndeterminate(true);
            saveProgressDialog.setCancelable(false);
            return saveProgressDialog;
		}
		default:
			return super.onCreateDialog(id);
		}
	}
	
	void save()
	{
		Log.v("OpenVPN", "save: importing configuration" );
		try
		{
			showDialog( SAVE_PROGRESS_DIALOG );
			int progressMax = 0;
			
			final boolean replace;
			final boolean merge;
			final boolean add;
			switch (importType_rg.getCheckedRadioButtonId()) {
			case R.id.import_files_replace:
				replace = true;
				merge = false;
				add = false;
				break;
			case R.id.import_files_merge:
				replace = false;
				merge = true;
				add = false;
				break;
			case R.id.import_files_add:
				replace = false;
				merge = false;
				add = true;
				break;
			default:
				throw new UnexpectedSwitchValueException( importType_rg.getCheckedRadioButtonId() );
			}
			Log.v("OpenVPN", "replace="+replace );
			Log.v("OpenVPN", "merge="+merge );
			Log.v("OpenVPN", "add="+add );
			
			File configDir = new File( getApplicationContext().getFilesDir(), "config.d" );
			boolean created = configDir.mkdirs();
			
			File[] deleteFiles = replace ? configDir.listFiles( new Util.IsFileFilter() ) : new File[0];
			progressMax += deleteFiles.length;
			
			File importDir = new File( importDir_et.getText().toString() );
			File[] importFiles = importDir.listFiles( new Util.IsFileFilter() );
			progressMax += importFiles.length;
			
			/*
			 * do the work
			 */
			saveProgressDialog.setMax( progressMax );
			saveProgressDialog.setProgress( 0 );
			saveProgressDialog.setIndeterminate( false );
			int progress = 0;

			for(File f : deleteFiles )
			{
				Log.v("OpenVPN", "delete " + f.getAbsolutePath() );
				saveProgressDialog.setMessage( String.format("Removing old files (%s)", f.getName() ) );
				f.delete();
				saveProgressDialog.setProgress( progress++ );
			}
			
			for(int i=0; i<importFiles.length; i++)
			{
				File source = importFiles[i];
				String sourceShort = source.getPath().substring( importDir.getPath().length() + 1 );
				File target = new File( configDir, sourceShort );
				saveProgressDialog.setMessage( String.format("Merging new files (%s)", target.getName() ) );

				if ( add && target.exists() )
				{
					Log.v("OpenVPN", "skipping existing file " + target.getAbsolutePath() );
					Toast.makeText(
							getApplicationContext(),
							String.format( "File %s already exists. Skipping", sourceShort ),
							Toast.LENGTH_LONG
					).show();
				}
				else
				{
					Log.v( "OpenVPN", String.format("copy from '%s' to '%s'", source, target ) );
					Util.copy(source, target);
				}
				saveProgressDialog.setProgress( progress++ );
			}

			Editor edit = getPreferences( 0 ).edit();
			edit.putString( "import-dir", importDir_et.getText().toString() );
			edit.putString( "import-type", replace ? "replace" : merge ? "merge" : add ? "add" : null );
			edit.commit();
			
			Log.v( "OpenVPN", "done" );
			setResult( RESULT_OK );
			finish();
		}
		catch (Exception e) {
			Toast.makeText(
					getApplicationContext(),
					e.getMessage(),
					Toast.LENGTH_SHORT
			).show();
		}
		finally
		{
			saveProgressDialog.dismiss();
		}
	}
}
