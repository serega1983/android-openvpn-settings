package de.schaeuffelhut.android.openvpn;

import java.io.File;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class AdvancedSettings extends PreferenceActivity
{
	static final String HAS_DAEMONS_STARTED = "hasDaemonsStarted";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
}
