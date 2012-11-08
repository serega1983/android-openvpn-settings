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
import java.util.Arrays;

import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.text.TextUtils;
import de.schaeuffelhut.android.openvpn.lib.app.R;
import de.schaeuffelhut.android.openvpn.util.AdUtil;

public class EditConfigPreferences extends PreferenceActivity
{
	public static String EXTRA_FILENAME = "extra_filename";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// always return config name to caller
		setResult( 0, new Intent().putExtra(EXTRA_FILENAME, getIntent().getStringExtra( EXTRA_FILENAME ) ) );
		
		File config = new File( getIntent().getStringExtra( EXTRA_FILENAME ) );
		
    	setContentView( AdUtil.getAdSupportedListView( getApplicationContext() ) );
		addPreferencesFromResource( R.xml.config_settings );
		
		renamePreference("openvpn_config_name", Preferences.KEY_CONFIG_NAME(config));
		{
			EditTextPreference pref = (EditTextPreference)findPreference( Preferences.KEY_CONFIG_NAME(config) );
			pref.setOnPreferenceChangeListener( new Preference.OnPreferenceChangeListener() {
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					if ( TextUtils.isEmpty( (CharSequence)newValue ) )
						preference.setSummary( "Enter custom name" );
					else
						preference.setSummary( (CharSequence)newValue );
					return true;
				}
			});
			String value = pref.getText();
			if ( TextUtils.isEmpty( (value ) ) )
				pref.setSummary( "Enter custom name" );
			else
				pref.setSummary( value );
		}

		renamePreference("openvpn_config_use_vpn_dns", Preferences.KEY_VPN_DNS_ENABLE(config));
		
		renamePreference("openvpn_config_dns1", Preferences.KEY_VPN_DNS(config));
		{
			EditTextPreference pref = (EditTextPreference)findPreference( Preferences.KEY_VPN_DNS(config) );
			pref.setOnPreferenceChangeListener( new Preference.OnPreferenceChangeListener() {
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					if ( TextUtils.isEmpty( (CharSequence)newValue ) )
						preference.setSummary( "Enter VPN DNS server" );
					else
						preference.setSummary( (CharSequence)newValue );
					return true;
				}
			});
			String value = pref.getText();
			if ( TextUtils.isEmpty( (value ) ) )
				pref.setSummary( "Enter VPN DNS server" );
			else
				pref.setSummary( value );
		}
		
		renamePreference("openvpn_config_script_security_level", Preferences.KEY_SCRIPT_SECURITY_LEVEL(config));
		{
			ListPreference pref = (ListPreference)findPreference( Preferences.KEY_SCRIPT_SECURITY_LEVEL(config) );
			pref.setOnPreferenceChangeListener( new Preference.OnPreferenceChangeListener() {
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					ListPreference pref = (ListPreference)preference;
					int index = Arrays.binarySearch( pref.getEntryValues(), newValue );
					pref.setSummary( pref.getEntries()[index] );
					return true;
				}
			});
			pref.setSummary( pref.getEntry() );
		}

		renamePreference("openvpn_config_log_stdout_enable", Preferences.KEY_CONFIG_LOG_STDOUT_ENABLE(config));
		{
			Preference pref = (Preference)findPreference( Preferences.KEY_CONFIG_LOG_STDOUT_ENABLE(config) );
			pref.setSummary( Preferences.logFileFor(config).getAbsolutePath() );
		}
	}

	private void renamePreference(String oldKey, String newKey) {
		Preference pref = findPreference( oldKey );
		pref.setKey( newKey );
		getPreferenceScreen().removePreference( pref );
		getPreferenceScreen().addPreference( pref );
	}
}