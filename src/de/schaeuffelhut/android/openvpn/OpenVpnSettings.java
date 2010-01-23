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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import de.schaeuffelhut.android.openvpn.service.OpenVpnService;
import de.schaeuffelhut.android.openvpn.util.UnexpectedSwitchValueException;
import de.schaeuffelhut.android.openvpn.util.Util;

public class OpenVpnSettings extends PreferenceActivity implements ServiceConnection
{
	final static String TAG = "OpenVPN-Settings";
	
	private static final int REQUEST_CODE_IMPORT_FILES = 1;
	private static final int DIALOG_HELP = 1;
	
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

    
    private void initToggles(File configDir)
	{
		PreferenceCategory configurations = (PreferenceCategory) findPreference(Preferences.KEY_OPENVPN_CONFIGURATIONS);
		configurations.removeAll();
		mDaemonEnablers.clear();
		
		for ( File config : configs(configDir) )
		{
			CheckBoxPreference pref = new CheckBoxPreference( getApplicationContext() );
			pref.setKey( Preferences.KEY_CONFIG_ENABLED( config ) );
			pref.setTitle( config.getName() );
			pref.setSummary( "Select to turn on OpenVPN tunel");
			
			configurations.addPreference(pref);
			mDaemonEnablers.add( new DaemonEnabler( 
					getApplicationContext(), mOpenVpnService, pref, config ) );			
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
	 * Dialogs
	 */

	@Override
	protected Dialog onCreateDialog(int id) {
		final Dialog dialog;
		switch(id) {
		case DIALOG_HELP:
			dialog = HelpDialog.makeDialog(this);
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
