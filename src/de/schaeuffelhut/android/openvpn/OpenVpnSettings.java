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
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import de.schaeuffelhut.android.openvpn.service.OpenVpnService;
import de.schaeuffelhut.android.openvpn.util.UnexpectedSwitchValueException;
import de.schaeuffelhut.android.openvpn.util.Util;

public class OpenVpnSettings extends PreferenceActivity implements ServiceConnection
{
	final static String TAG = "OpenVPN-Settings";
	
	private static final int REQUEST_CODE_IMPORT_FILES = 1;
	private static final int REQUEST_CODE_EDIT_CONFIG = 2;

	private static final int DIALOG_HELP = 1;
	private static final int DIALOG_PLEASE_RESTART = 2;
	
	ArrayList<DaemonEnabler> mDaemonEnablers = new ArrayList<DaemonEnabler>(4);
	OpenVpnService mOpenVpnService = null;

	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource( R.xml.preferences );

        //TODO: write OpenVpnEnabled, see WifiEnabler => start stop OpenVpnService
        {
        	CheckBoxPreference pref = (CheckBoxPreference) findPreference( Preferences.KEY_OPENVPN_ENABLED );
        	pref.setSummary( "" );
        	pref.setChecked( mOpenVpnService != null );
        }

//		{
//	        CheckBoxPreference pref = (CheckBoxPreference) findPreference( Preferences.KEY_OPENVPN_USE_INTERNAL_STORAGE );
//			pref.setOnPreferenceChangeListener(
//					new Preference.OnPreferenceChangeListener() {
//						public boolean onPreferenceChange(
//								Preference pref, Object newValue) {
//							if ( (Boolean) newValue )
//							{
//								// when turning on internal storage, defer to ImportFiles
//								Intent intent = new Intent( getApplicationContext(), ImportFiles.class );
//								startActivityForResult(intent, REQUEST_CODE_IMPORT_FILES);
//								return false; // let ImportFiles decide if option was turned on
//							}
//							else
//							{
//								return true;
//							}
//						}
//					});
//		}

		{
			EditTextPreference pref = (EditTextPreference) findPreference( Preferences.KEY_OPENVPN_EXTERNAL_STORAGE );
			pref.setOnPreferenceChangeListener(
					new Preference.OnPreferenceChangeListener() {
						public boolean onPreferenceChange(
								Preference pref, Object newValue) {
							File path = new File( (String)newValue );
							pref.setSummary( ( !path.exists() ? "Not found: " : "" ) + path.getAbsolutePath() );
							initToggles( path );
							return true;
						}
					});
			File path = Preferences.getExternalStorageAsFile( pref.getSharedPreferences() );
			pref.setSummary( ( !path.exists() ? "Not found: " : "" ) + path.getAbsolutePath() );
		}

		{
			EditTextPreference pref = (EditTextPreference) findPreference( Preferences.KEY_OPENVPN_PATH_TO_BINARY );
			pref.setOnPreferenceChangeListener(
					new Preference.OnPreferenceChangeListener() {
						public boolean onPreferenceChange(
								Preference pref, Object newValue) {
							File path = new File( (String)newValue );
							pref.setSummary( ( !path.exists() ? "Not found: " : "" ) + path.getAbsolutePath() );
							return true;
						}
					});
			File path = Preferences.getPathToBinaryAsFile( pref.getSharedPreferences() );
			if ( path == null )
				pref.setSummary( "Please set path to openvpn binary." );
			else
				pref.setSummary( ( !path.exists() ? "Not found: " : "" ) + path.getAbsolutePath() );
		}

//		{
//			EditTextPreference pref = (EditTextPreference) findPreference( Preferences.KEY_OPENVPN_PATH_TO_SU );
//			pref.setOnPreferenceChangeListener(
//					new Preference.OnPreferenceChangeListener() {
//						public boolean onPreferenceChange(
//								Preference pref, Object newValue) {
//							File path = new File( (String)newValue );
//							pref.setSummary( ( !path.exists() ? "Not found: " : "" ) + path.getAbsolutePath() );
//							return true;
//						}
//					});
//			File path = Preferences.getPathToSuAsFile( pref.getSharedPreferences() );
//			pref.setSummary( ( !path.exists() ? "Not found: " : "" ) + path.getAbsolutePath() );
//		}

		registerForContextMenu( getListView() );
		initToggles();

		if ( !bindService(
        		new Intent( this, OpenVpnService.class ),
        		this,
        		Context.BIND_AUTO_CREATE
        ) )
        {
			Log.w(TAG, "Could not bind to ControlShell" );
        }
    }


	private void initToggles() {
		initToggles( Preferences.getConfigDir( this, PreferenceManager.getDefaultSharedPreferences(this) ) );
	}

    static class ConfigFilePreference extends CheckBoxPreference
    {
    	final File mConfig;
    	final DaemonEnabler mDaemonEnabler;
		public ConfigFilePreference(Context context, OpenVpnService openVpnService, File config) {
			super(context);
			setKey( Preferences.KEY_CONFIG_ENABLED( config ) );
			setTitle( config.getName() );
			setSummary( "Select to turn on OpenVPN tunel");
			mConfig = config;
			mDaemonEnabler = new DaemonEnabler( context, openVpnService, this, config );
		}
    	
    }
    private void initToggles(File configDir)
	{
		PreferenceCategory configurations = (PreferenceCategory) findPreference(Preferences.KEY_OPENVPN_CONFIGURATIONS);
		configurations.removeAll();
		mDaemonEnablers.clear();
		
		for ( File config : configs(configDir) )
		{
//			CheckBoxPreference pref = new CheckBoxPreference( getApplicationContext() );
//			pref.setKey( Preferences.KEY_CONFIG_ENABLED( config ) );
//			pref.setTitle( config.getName() );
//			pref.setSummary( "Select to turn on OpenVPN tunel");
//
//			configurations.addPreference(pref);
//			mDaemonEnablers.add( new DaemonEnabler( 
//					getApplicationContext(), mOpenVpnService, pref, config ) );			

			ConfigFilePreference pref = new ConfigFilePreference( this, mOpenVpnService, config );
			configurations.addPreference(pref);
			mDaemonEnablers.add( pref.mDaemonEnabler );			
		}
		
		if ( configurations.getPreferenceCount() == 0 ){
			EditTextPreference pref = new EditTextPreference(this);
			pref.setEnabled(false);
			pref.setPersistent(false);
			pref.setTitle( "No configuration found." );
			pref.setSummary( "Please copy your *.config, certificates, etc to\n" + Preferences.getExternalStorage(PreferenceManager.getDefaultSharedPreferences(this)) );
			configurations.addPreference( pref );
		}
	}

	final File[] configs(File configDir)
	{
		File[] configFiles;
		if ( configDir == null )
			configFiles = new File[0];
		else
			configFiles = configDir.listFiles( new Util.FileExtensionFilter(".conf") );
	
		return configFiles == null ? new File[0] : configFiles;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch ( requestCode ) {
		case REQUEST_CODE_IMPORT_FILES: {
			CheckBoxPreference pref = (CheckBoxPreference)findPreference( Preferences.KEY_OPENVPN_USE_INTERNAL_STORAGE );
			switch ( resultCode ) {
			case ImportFiles.RESULT_OK:
				pref.setChecked( true );
				break;
			case ImportFiles.RESULT_CANCELED:
				pref.setChecked( false );
				break;
			default:
				throw new UnexpectedSwitchValueException( resultCode );
			}
		} break;

		case REQUEST_CODE_EDIT_CONFIG: {
			String filename = data == null ? null : data.getStringExtra( EditConfig.EXTRA_FILENAME );
			if ( filename != null && mOpenVpnService.isDaemonStarted( new File(filename)) )
				showDialog( DIALOG_PLEASE_RESTART );
				
		} break;
		
		default:
			Log.w( TAG, String.format( "unexpected onActivityResult(%d, %d, %s) ", requestCode, resultCode, data ) );
			break;
		}
	}

    @Override
    protected void onResume() {
		super.onResume();
		
		for(DaemonEnabler daemonEnabler : mDaemonEnablers )
			daemonEnabler.resume();
	}
	
    @Override
    protected void onPause() {
    	super.onPause();
    	
		for(DaemonEnabler daemonEnabler : mDaemonEnablers )
			daemonEnabler.pause();
    }

	/*
	 * Menu
	 */
	
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.settings_menu, menu);
	    return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
//	    menu.findItem( R.id.configs_options_startall ).setVisible( configs.length > 0 );
//	    menu.findItem( R.id.configs_options_restartall ).setVisible( mControlShell.hasDaemonsStarted() );
//	    menu.findItem( R.id.configs_options_stopall ).setVisible( mOpenVpnService != null && mOpenVpnService.hasDaemonsStarted() );
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch ( item.getItemId() ) {
		case R.id.settings_menu_refresh:
			initToggles();
			return true;
		case R.id.settings_menu_help:
			showDialog( DIALOG_HELP );
			return true;
//		case R.id.configs_options_import:
//			Intent intent = new Intent( getApplicationContext(), ImportFiles.class );
//			startActivityForResult(intent, REQUEST_CODE_IMPORT_FILES);
//			return true;
//		case R.id.configs_options_refresh:
//			initToggles();
//			return true;
//		case R.id.configs_options_startall:
//			return true;
//		case R.id.configs_options_restartall:
//			return true;
//		case R.id.configs_options_stopall:
//			return true;
//		case R.id.configs_options_settings:
//			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * Context Menu
	 */
	
	final static int CONTEXT_CONFIG_DISABLE = 1;
	final static int CONTEXT_CONFIG_ENABLE = 2;
	final static int CONTEXT_CONFIG_EDIT = 3;

	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		ConfigFilePreference configFilePref = getConfigPreferenceFromMenuInfo(menuInfo);
		if ( configFilePref != null )
		{
//			//Connect/Disconnect
//			if ( mOpenVpnService.isDaemonStarted( configFilePref.mConfig ) ){
//				menu.add( ContextMenu.NONE, CONTEXT_CONFIG_DISABLE, 1, "Disable" );
//			} else {
//				menu.add( ContextMenu.NONE, CONTEXT_CONFIG_ENABLE, 1, "Enable" );
//			}
			
			//Edit
			menu.add( ContextMenu.NONE, CONTEXT_CONFIG_EDIT, 2, "Edit" );
			
			//Delete
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		ConfigFilePreference configFilePref = getConfigPreferenceFromMenuInfo(item.getMenuInfo());
		
		switch ( item.getItemId() ) {
		//TODO: think about it
//		case CONTEXT_CONFIG_ENABLE:
//			configFilePref.setEnabled(false);
//			mOpenVpnService.daemonStart( configFilePref.mConfig );
//			return true;
//		case CONTEXT_CONFIG_DISABLE:
//			configFilePref.setEnabled(false);
//			mOpenVpnService.daemonStop( configFilePref.mConfig );
//			return true;
		case CONTEXT_CONFIG_EDIT: {
			Intent i = new Intent(this, EditConfig.class);
			i.putExtra( EditConfig.EXTRA_FILENAME, configFilePref.mConfig.getAbsolutePath() );
	        startActivityForResult(i, REQUEST_CODE_EDIT_CONFIG ); 
		} return true;
		
		default:
			return false;
//			throw new UnexpectedSwitchValueException( item.getItemId() );
		}
	}
	
    private ConfigFilePreference getConfigPreferenceFromMenuInfo(ContextMenuInfo menuInfo) {
        if ((menuInfo == null) || !(menuInfo instanceof AdapterContextMenuInfo)) {
            return null;
        }

        AdapterContextMenuInfo adapterMenuInfo = (AdapterContextMenuInfo) menuInfo;
        Object item = getPreferenceScreen().getRootAdapter().getItem( adapterMenuInfo.position );
        if ( item instanceof ConfigFilePreference )
        	return (ConfigFilePreference)item;
        else
        	return null;
    }


	/*
	 * Dialogs
	 */

	@Override
	protected Dialog onCreateDialog(int id) {
		final Dialog dialog;
		switch(id) {
		case DIALOG_HELP:
			dialog = HelpDialog.makeDialog(this);
			break;
		case DIALOG_PLEASE_RESTART:
			dialog = new AlertDialog.Builder( this ).setIcon(android.R.drawable.ic_dialog_info).
			setTitle( "Restart Required" ).setMessage( "The tunnel is currently active. For changes to take effect you must disable and then reenable the tunnel." ).setNeutralButton("OK", null).create();
			break;
		default:
			throw new UnexpectedSwitchValueException(id);
		}
		return dialog;
	}


	public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
		mOpenVpnService = ((OpenVpnService.ServiceBinder)serviceBinder).getService();
		Log.d( TAG, "Connected to OpenVpnService" );

		for(DaemonEnabler daemonEnabler : mDaemonEnablers )
			daemonEnabler.setOpenVpnService( mOpenVpnService );
		
		CheckBoxPreference pref = (CheckBoxPreference) findPreference( Preferences.KEY_OPENVPN_ENABLED );
		pref.setSummary( "OpenVpn service is running" );
		pref.setChecked( mOpenVpnService != null );
	}
	
	public void onServiceDisconnected(ComponentName name) {
		mOpenVpnService = null;
		Log.d( TAG, "Disconnected from OpenVpnService" );

		for(DaemonEnabler daemonEnabler : mDaemonEnablers )
			daemonEnabler.setOpenVpnService( null );
		
		CheckBoxPreference pref = (CheckBoxPreference) findPreference( Preferences.KEY_OPENVPN_ENABLED );
		pref.setSummary( "OpenVpn service encountered a problem and is not running" );
		pref.setChecked( mOpenVpnService != null );
	}
}
