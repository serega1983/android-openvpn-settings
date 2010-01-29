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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import de.schaeuffelhut.android.openvpn.service.OpenVpnService;

public class DaemonEnabler implements Preference.OnPreferenceChangeListener
{
	private static final boolean LOCAL_LOGD = true;
	private static final String TAG = "OpenVPNDaemonEnabler";

	private final Context mContext; 
	private OpenVpnService mOpenVpnService;
	private final CheckBoxPreference mDaemonCheckBoxPref;
	private final File mConfigFile;
	private final CharSequence mOriginalSummary;

	private final IntentFilter mDaemonStateFilter;
	private BroadcastReceiver mDaemonStateReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent)
		{
			//TODO: move into IntentFilter, or dispatch from OpenVPNSettings to DaemonEnabler.receive()
			if ( mConfigFile.getAbsolutePath().equals( intent.getStringExtra( Intents.EXTRA_CONFIG ) ) )
			{
				if ( Intents.DEAMON_STATE_CHANGED.equals( intent.getAction() ) ) {
					handleDaemonStateChanged(
							intent.getIntExtra(Intents.EXTRA_DAEMON_STATE, Intents.DAEMON_STATE_UNKNOWN),
							intent.getIntExtra(Intents.EXTRA_PREVIOUS_DAEMON_STATE, Intents.DAEMON_STATE_UNKNOWN)
					);
				} else if ( Intents.NETWORK_STATE_CHANGED.equals( intent.getAction() ) ) {
					handleNetworkStateChanged( intent );
				}	
			}
		}
	};

	public DaemonEnabler(Context contex, OpenVpnService openVpnServiceShell, CheckBoxPreference daemonCheckBoxPreference, File configFile)
	{
		mContext = contex;
		mDaemonCheckBoxPref = daemonCheckBoxPreference;
		mConfigFile = configFile;

		mOriginalSummary = mDaemonCheckBoxPref.getSummary();
		mDaemonCheckBoxPref.setPersistent( false );

		mDaemonStateFilter = new IntentFilter(Intents.DEAMON_STATE_CHANGED);
		mDaemonStateFilter.addAction(Intents.NETWORK_STATE_CHANGED);

		setOpenVpnService( openVpnServiceShell );
	}

	public void setOpenVpnService(OpenVpnService openVpnService)
	{
		mOpenVpnService = openVpnService;
		mDaemonCheckBoxPref.setEnabled( isEnabledByDependency() && mOpenVpnService != null );
		if ( mOpenVpnService != null )
			mOpenVpnService.daemonQueryState(mConfigFile);
	}

	boolean isRegistered = false;
	public void resume()
	{
		//    	int state;
		//    	if ( mControlShell == null )
		//    	{
		//    		state = Intents.EXTRA_STATE_UNKNOWN;
		//    	}
		//    	else
		//    	{
		//    		state = mControlShell.daemonState( mConfigfileName );
		//    	}
		// This is the widget enabled state, not the preference toggled state

		mContext.registerReceiver(mDaemonStateReceiver, mDaemonStateFilter);
		isRegistered = true;
		mDaemonCheckBoxPref.setOnPreferenceChangeListener(this);
	}

	public void pause()
	{
		if ( isRegistered )
		{
			mContext.unregisterReceiver(mDaemonStateReceiver);
			isRegistered = false;
		}
		mDaemonCheckBoxPref.setOnPreferenceChangeListener(null);
	}

	public boolean onPreferenceChange(Preference preference, Object value) {
		final boolean updateGui;
		
		// Turn on/off OpenVPN daemon
		if ( mOpenVpnService == null )
		{
			mDaemonCheckBoxPref.setSummary( "Error: not bound to OpenVPN service!" );
			updateGui = false; // Don't update UI to opposite
		}
		else
		{

			boolean currentState = mOpenVpnService.isDaemonStarted( mConfigFile );
			boolean newState = (Boolean) value;
			
			if ( currentState == newState )
			{
				if (LOCAL_LOGD)
					Log.d(TAG, String.format( 
							"Daemon for config %s was already %s ",
							mConfigFile,
							currentState ? "started" : "stopped" ) );
				updateGui = true; // Update GUI to refelect current state
			}
			else
			{
				mDaemonCheckBoxPref.setEnabled(false);
				
				if (newState)
					mOpenVpnService.daemonStart(mConfigFile);
				else
					mOpenVpnService.daemonStop(mConfigFile);
				updateGui = false; // Don't update UI to opposite state until we're sure
			}
			
			PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean(
					Preferences.KEY_CONFIG_INTENDED_STATE( mConfigFile ), newState
			).commit();
		}        

		return updateGui;
	}


	protected void handleDaemonStateChanged(int daemonState, int previousDeamonState)
	{
		if (LOCAL_LOGD)
			Log.d(TAG, "Received OpenVPN daemon state changed from "
					+ getHumanReadableDaemonState(previousDeamonState) + " to "
					+ getHumanReadableDaemonState(daemonState));


		switch ( daemonState ) {
		case Intents.DAEMON_STATE_ENABLED:
			mDaemonCheckBoxPref.setChecked( true );
			mDaemonCheckBoxPref.setSummary( null );
			break;
		case Intents.DAEMON_STATE_DISABLED:
			mDaemonCheckBoxPref.setChecked( false );
			mDaemonCheckBoxPref.setSummary( mOriginalSummary );
			break;
		case Intents.DAEMON_STATE_STARTUP:
			mDaemonCheckBoxPref.setChecked(false);
			mDaemonCheckBoxPref.setSummary( "Startup..." );
			break;
		case Intents.DAEMON_STATE_UNKNOWN:
			mDaemonCheckBoxPref.setChecked(false);
			mDaemonCheckBoxPref.setSummary( "State unknown" );
			break;

		default:
			mDaemonCheckBoxPref.setSummary( "unkwnown daemon state: " + daemonState );
		}		
		mDaemonCheckBoxPref.setEnabled( isEnabledByDependency() && mOpenVpnService != null );
	}

	protected void handleNetworkStateChanged(Intent intent)
	{
		int networkState = intent.getIntExtra(Intents.EXTRA_NETWORK_STATE, Intents.NETWORK_STATE_UNKNOWN);
		int previousNetworkState = intent.getIntExtra(Intents.EXTRA_PREVIOUS_NETWORK_STATE, Intents.NETWORK_STATE_UNKNOWN);

		if (LOCAL_LOGD)
			Log.d(TAG, "Received OpenVPN network state changed from "
					+ getHumanReadableNetworkState(previousNetworkState) + " to "
					+ getHumanReadableNetworkState(networkState));

		if ( mOpenVpnService != null && mOpenVpnService.isDaemonStarted( mConfigFile ) )
		{
			final String summary;
			switch (networkState) {
			case Intents.NETWORK_STATE_UNKNOWN:
				summary = "Unknown";
				break;
			case Intents.NETWORK_STATE_CONNECTING:
				summary = "Connecting";
				break;
			case Intents.NETWORK_STATE_RECONNECTING:
				String cause = intent.getStringExtra( Intents.EXTRA_NETWORK_CAUSE );
				if ( cause == null )
					summary = "Reconnecting";
				else
					summary = String.format(
							"Reconnecting (caused by %s)",
							cause
					);
				break;
			case Intents.NETWORK_STATE_RESOLVE:
				summary = "Resolve";
				break;
			case Intents.NETWORK_STATE_WAIT:
				summary = "Wait";
				break;
			case Intents.NETWORK_STATE_AUTH:
				summary = "Auth";
				break;
			case Intents.NETWORK_STATE_GET_CONFIG:
				summary = "Get Config";
				break;
			case Intents.NETWORK_STATE_CONNECTED:
				summary = String.format(
						"Connected to %s as %s",
						intent.getStringExtra( Intents.EXTRA_NETWORK_REMOTEIP ),
						intent.getStringExtra( Intents.EXTRA_NETWORK_LOCALIP )
				);
				break;
			case Intents.NETWORK_STATE_ASSIGN_IP:
				summary = String.format(
						"Assign IP %s",
						intent.getStringExtra( Intents.EXTRA_NETWORK_LOCALIP )
				);
				break;
			case Intents.NETWORK_STATE_ADD_ROUTES:
				summary = "Add Routes";
				break;
			case Intents.NETWORK_STATE_EXITING:
				summary = "Exiting";
				break;
			default:
				summary = String.format( "Some other state (%d)!", networkState );    
			}
			mDaemonCheckBoxPref.setSummary( summary );
		}
	}

	// what are dependencies??
	// copied from com.android.settings.wifi.WifiEnabler
	private boolean isEnabledByDependency() {
		Preference dep = getDependencyPreference();
		if (dep == null) {
			return true;
		}

		return !dep.shouldDisableDependents();
	}

	// what are dependencies??
	// copied from com.android.settings.wifi.WifiEnabler
	private Preference getDependencyPreference() {
		String depKey = mDaemonCheckBoxPref.getDependency();
		if (TextUtils.isEmpty(depKey)) {
			return null;
		}

		return mDaemonCheckBoxPref.getPreferenceManager().findPreference(depKey);
	}


	private String getHumanReadableDaemonState(int daemonState) {
		switch (daemonState) {
		case Intents.DAEMON_STATE_ENABLED:
			return "Enabled";
		case Intents.DAEMON_STATE_DISABLED:
			return "Disabled";
		case Intents.DAEMON_STATE_UNKNOWN:
			return "Unknown";
		default:
			return String.format( "Some other state (%d)!", daemonState );    
		}
	}

	private String getHumanReadableNetworkState(int networkState) {
		switch (networkState) {
		case Intents.NETWORK_STATE_UNKNOWN:
			return "Unknown";
		case Intents.NETWORK_STATE_CONNECTING:
			return "Connecting";
		case Intents.NETWORK_STATE_RECONNECTING:
			return "Reconnecting";
		case Intents.NETWORK_STATE_RESOLVE:
			return "Resolve";
		case Intents.NETWORK_STATE_WAIT:
			return "Wait";
		case Intents.NETWORK_STATE_AUTH:
			return "Auth";
		case Intents.NETWORK_STATE_GET_CONFIG:
			return "Get Config";
		case Intents.NETWORK_STATE_CONNECTED:
			return "Connected";
		case Intents.NETWORK_STATE_ASSIGN_IP:
			return "Assign IP";
		case Intents.NETWORK_STATE_ADD_ROUTES:
			return "Add Routes";
		case Intents.NETWORK_STATE_EXITING:
			return "Exiting";
		default:
			return String.format( "Some other state (%d)!", networkState );    
		}
	}

}
