package de.schaeuffelhut.android.openvpn;

import java.io.File;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class XXEditConfig extends Activity
{
	private final static int FIND_IMPORT_DIR = 1;
	private static final int SAVE_PROGRESS_DIALOG = 1;
	private ProgressDialog saveProgressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView( R.layout.edit_config );

		((EditText)findViewById( R.id.edit_config_name )).addTextChangedListener( new TextWatcher(){
			boolean wasShellFriendly;
			public void afterTextChanged(Editable s) {}
			public void beforeTextChanged(CharSequence s, int start, int count,  int after) {
				wasShellFriendly = Util.isShellFriendly( s );
			}
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if ( s.length() == 0 )
				{
					Toast.makeText(
							getApplicationContext(),
							"Name is mandatory!",
							Toast.LENGTH_SHORT
					).show();
				}
				else if ( !wasShellFriendly )
				{
					// no futher warnings
				}
				else if ( !Util.isShellFriendly( s ) )
				{
					Toast.makeText(
							getApplicationContext(),
							"Name may only contain the characters A-Z, a-z, 0-9 and _ ",
							Toast.LENGTH_SHORT
					).show();
				}
				validateSave();
			}
		});

		((Button)findViewById( R.id.edit_config_find_import_dir )).setOnClickListener(
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

		((Spinner)findViewById( R.id.edit_config_config_file )).setOnItemSelectedListener(
				new OnItemSelectedListener(){
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
						File f = (File)parent.getItemAtPosition( position );
						if ( !Util.isShellFriendly( f.getPath() ) )
						{
							Toast.makeText(
									getApplicationContext(),
									"Name of configuration file may only contain characters a-z, A-Z, 0-9 and _",
									Toast.LENGTH_SHORT
							).show();
						}
						validateSave();
					}
					public void onNothingSelected(AdapterView<?> arg0) {
						validateSave();
					}
				});

		((Button)findViewById( R.id.edit_config_save )).setOnClickListener( new OnClickListener(){
			public void onClick(View v) {
				save();
			}
		});

		((Button)findViewById( R.id.edit_config_cancel )).setOnClickListener( new OnClickListener(){
			public void onClick(View v) {
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
			Log.i( "OpenVPN", data.getData().getPath() );
			
			final File selectedDir = data.getData() == null ? null : new File( data.getData().getPath() );

			File importDir;
			File[] importFiles;
			if ( selectedDir == null )
			{
				Toast.makeText(
						getApplicationContext(),
						"No directory selected!",
						Toast.LENGTH_SHORT
				).show();
				importDir = null;
				importFiles = null;
			}
			else if ( !selectedDir.exists()|| !selectedDir.isDirectory() )
			{
				Toast.makeText(
						getApplicationContext(),
						"Selected directory does not exists!",
						Toast.LENGTH_SHORT
				).show();
				importDir = selectedDir;
				importFiles = new File[0];
			}
			else if ( !selectedDir.isDirectory() )
			{
				Toast.makeText(
						getApplicationContext(),
						"Selected file must be a  directory!",
						Toast.LENGTH_SHORT
				).show();
				importDir = selectedDir;
				importFiles = new File[0];
			}
			else
			{
				importDir = selectedDir;
				File[] sourceFiles = importDir.listFiles( new Util.IsFileFilter());
				importFiles = new File[ sourceFiles.length ];
				for(int i=0; i<sourceFiles.length; i++)
					importFiles[i] = new File( sourceFiles[i].getAbsolutePath().substring(
							importDir.getAbsolutePath().length() + 1 /* the trailing slash */
					));
			}
			
			EditText et = (EditText)findViewById( R.id.edit_config_import_dir );
			et.setText( importDir == null ? "" : importDir.getPath() );
			
			Spinner sp = (Spinner)findViewById( R.id.edit_config_config_file );
			ArrayAdapter<File> arrayAdapter = new ArrayAdapter<File>(
					this,
					android.R.layout.simple_spinner_item,
					importFiles
			);
			sp.setAdapter( arrayAdapter);
			for(int i = 0; i<arrayAdapter.getCount(); i++)
			{
				if ( arrayAdapter.getItem(i).getName().endsWith( ".conf" ) )
				{
					sp.setSelection(i, false);
					break;
				}
			}
			
			validateSave();
			break;
		}
	}
	
	void validateSave(){
		boolean isValid = true;
		
		isValid &= Util.isShellFriendly( ((EditText)findViewById( R.id.edit_config_name )).getText() );
		
		String importDir = ((EditText)findViewById( R.id.edit_config_import_dir )).getText().toString();
		Spinner configFileSpinner = (Spinner)findViewById( R.id.edit_config_config_file );
		File configFile = (File)configFileSpinner.getSelectedItem();
		isValid &= configFile != null && Util.isShellFriendlyPath( configFile.getPath() ) && new File(
				new File( importDir ),
				configFile.getPath()
		).isFile();
		
		configFileSpinner.setEnabled( configFileSpinner.getAdapter() == null ? false : !configFileSpinner.getAdapter().isEmpty() );
		
		((Button)findViewById( R.id.edit_config_save )).setEnabled( isValid );
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
			
			/*
			 * prepare and count progress max
			 */
			boolean replace = ((RadioButton)findViewById( R.id.edit_config_replace )).isSelected();
			Log.v("OpenVPN", "replace="+replace );
			
			File configDir = new File( getApplicationContext().getFilesDir(), "config.d/" + ((EditText)findViewById( R.id.edit_config_name )).getText() );
			boolean created = configDir.mkdirs();
			File[] deleteFiles;
			if ( !created && replace )
				deleteFiles = configDir.listFiles( new Util.IsFileFilter() );
			else
				deleteFiles = new File[0];
			progressMax += deleteFiles.length;
			
			String importDir = ((EditText)findViewById( R.id.edit_config_import_dir )).getText().toString();
			ArrayAdapter<File> files = ((ArrayAdapter<File>)((Spinner)findViewById( R.id.edit_config_config_file )).getAdapter());
			progressMax += files.getCount();
			
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
			
			for(int i=0; i<files.getCount(); i++)
			{
				File source = new File( importDir, files.getItem( i ).getPath() );
				File target = new File( configDir, files.getItem( i ).getPath() );
				saveProgressDialog.setMessage( String.format("Merging new files (%s)", target.getName() ) );
				if ( target.exists() )
				{
					Log.v("OpenVPN", "delete " + target.getAbsolutePath() );
					target.delete();
				}
				Log.v( "OpenVPN", String.format("copy from '%s' to '%s'", source, target ) );
				Util.copy(source, target);
				saveProgressDialog.setProgress( progress++ );
			}

			Log.v( "OpenVPN", "done" );
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
