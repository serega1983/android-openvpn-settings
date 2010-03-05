package de.schaeuffelhut.android.openvpn;

import java.io.File;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.text.TextUtils;

public class EditConfigPreferences extends PreferenceActivity
{
	public static String EXTRA_FILENAME = "extra_filename";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		File config = new File( getIntent().getStringExtra( EXTRA_FILENAME ) );
		
		addPreferencesFromResource( R.xml.config_settings );
		
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
	}

	private void renamePreference(String oldKey, String newKey) {
		Preference pref = findPreference( oldKey );
		pref.setKey( newKey );
		getPreferenceScreen().removePreference( pref );
		getPreferenceScreen().addPreference( pref );
	}
}