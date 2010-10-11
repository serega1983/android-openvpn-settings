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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import de.schaeuffelhut.android.openvpn.util.AdUtil;

public class AdvancedSettings extends PreferenceActivity
{
	static final String HAS_DAEMONS_STARTED = "hasDaemonsStarted";
	protected static final int INFO_DIALOG_ISSUE_35 = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

    	setContentView( AdUtil.getAdSupportedListView( getApplicationContext() ) );
		addPreferencesFromResource( R.xml.advanced_settings );

		//		{
		//        CheckBoxPreference pref = (CheckBoxPreference) findPreference( Preferences.KEY_OPENVPN_USE_INTERNAL_STORAGE );
		//		pref.setOnPreferenceChangeListener(
		//				new Preference.OnPreferenceChangeListener() {
		//					public boolean onPreferenceChange(
		//							Preference pref, Object newValue) {
		//						if ( (Boolean) newValue )
		//						{
		//							// when turning on internal storage, defer to ImportFiles
		//							Intent intent = new Intent( getApplicationContext(), ImportFiles.class );
		//							startActivityForResult(intent, REQUEST_CODE_IMPORT_FILES);
		//							return false; // let ImportFiles decide if option was turned on
		//						}
		//						else
		//						{
		//							return true;
		//						}
		//					}
		//				});
		//	}

		{
			CheckBoxPreference pref = (CheckBoxPreference) findPreference( Preferences.KEY_OPENVPN_DO_MODPROBE_TUN );
			pref.setSummary( getString( R.string.advanced_settings_do_modprobe_tun, Preferences.getLoadTunModuleCommand( pref.getSharedPreferences()) ) );
		}
		
		{
			ListPreference pref = (ListPreference) findPreference( Preferences.KEY_OPENVPN_MODPROBE_ALTERNATIVE );
			pref.setOnPreferenceChangeListener(
					new Preference.OnPreferenceChangeListener() {
						public boolean onPreferenceChange( Preference pref, Object newValue ) {
							pref.setSummary( (String)newValue );
							updateSummary( newValue + " " + Preferences.getPathToTun(pref.getSharedPreferences()) );
							return true;
						}
					});
			pref.setSummary( Preferences.getModprobeAlternative( pref.getSharedPreferences() ) );
		}

		{
			EditTextPreference pref = (EditTextPreference) findPreference( Preferences.KEY_OPENVPN_PATH_TO_TUN );
			pref.setOnPreferenceChangeListener(
					new Preference.OnPreferenceChangeListener() {
						public boolean onPreferenceChange( Preference pref, Object newValue ) {
//							pref.setSummary( ( TextUtils.isEmpty( (String)newValue ) ? "tun" : (String)newValue  ) );
							pref.setSummary( (String)newValue );
							updateSummary( (Preferences.getModprobeAlternative(pref.getSharedPreferences()) + " " + newValue ) );
							return true;
						}
					});
			pref.setSummary( Preferences.getPathToTun( pref.getSharedPreferences() ) );
		}


		{
			EditTextPreference pref = (EditTextPreference) findPreference( Preferences.KEY_OPENVPN_EXTERNAL_STORAGE );
			pref.setOnPreferenceChangeListener(
					new Preference.OnPreferenceChangeListener() {
						public boolean onPreferenceChange(
								Preference pref, Object newValue) {
							File path = new File( (String)newValue );
							pref.setSummary( ( !path.exists() ? "Not found: " : "" ) + path.getAbsolutePath() );
							//						initToggles( path );
							return true;
						}
					});
			File path = Preferences.getExternalStorageAsFile( pref.getSharedPreferences() );
			String summary = ( !path.exists() ? "Not found: " : "" ) + path.getAbsolutePath();

			if ( getIntent().getBooleanExtra( HAS_DAEMONS_STARTED, true ) )
			{
				summary += "\nStop tunnels to change this setting.";
				pref.setEnabled( false );
			}
			pref.setSummary( summary );
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

		{
			CheckBoxPreference pref = (CheckBoxPreference) findPreference( Preferences.KEY_FIX_HTC_ROUTES );
			pref.setOnPreferenceChangeListener(
					new Preference.OnPreferenceChangeListener() {
						public boolean onPreferenceChange(Preference pref, Object newValue) {
							if ( (Boolean)newValue )
							{
								showDialog( INFO_DIALOG_ISSUE_35 );
							}
							return true;
						}
					});
		}

		{
			CheckBoxPreference pref = (CheckBoxPreference) findPreference( Preferences.KEY_OPENVPN_SHOW_ADS );
			pref.setOnPreferenceChangeListener(
					new Preference.OnPreferenceChangeListener() {
						public boolean onPreferenceChange(Preference pref, Object newValue) {
							if ( (Boolean)newValue )
								pref.setSummary( "Thank you for your support!" );
							else
								pref.setSummary( "Please consider supporting development." );
							return true;
						}
					});
			if ( Preferences.getShowAds(this) )
				pref.setSummary( "Thank you for your support!" );
			else
				pref.setSummary( "Please consider your support." );
			if ( !AdUtil.hasAdSupport() )
			{
				pref.setSummary( "AdMob library is missing!" );
				pref.setEnabled( false );
			}
		}

		//	{
		//		EditTextPreference pref = (EditTextPreference) findPreference( Preferences.KEY_OPENVPN_PATH_TO_SU );
		//		pref.setOnPreferenceChangeListener(
		//				new Preference.OnPreferenceChangeListener() {
		//					public boolean onPreferenceChange(
		//							Preference pref, Object newValue) {
		//						File path = new File( (String)newValue );
		//						pref.setSummary( ( !path.exists() ? "Not found: " : "" ) + path.getAbsolutePath() );
		//						return true;
		//					}
		//				});
		//		File path = Preferences.getPathToSuAsFile( pref.getSharedPreferences() );
		//		pref.setSummary( ( !path.exists() ? "Not found: " : "" ) + path.getAbsolutePath() );
		//	}

	}

	private void updateSummary(String cmd) {
		CheckBoxPreference pref = (CheckBoxPreference) findPreference( Preferences.KEY_OPENVPN_DO_MODPROBE_TUN );
		pref.setSummary( getString( R.string.advanced_settings_do_modprobe_tun, cmd ) );
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch ( id ) {
		case INFO_DIALOG_ISSUE_35:
			return new AlertDialog.Builder( this )
			.setTitle("Attention")
			.setIcon( android.R.drawable.ic_dialog_info )
			.setMessage( "Please make sure you understand issue 35: http://code.google.com/p/android-openvpn-settings/issues/detail?id=35" )
			.setPositiveButton( "View", new AlertDialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://code.google.com/p/android-openvpn-settings/issues/detail?id=35") ));
				}
			})
			.setNegativeButton( "Dismiss", null )
			.create();

		default:
			return super.onCreateDialog(id);
		}
	}
}
