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
    private BroadcastReceiver mDaemonStateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            //TODO: move into IntentFilter, or dispatch from OpenVPNSettings to DaemonEnabler.receive()
            if (!intentAddressesThisDaemonEnabler( intent ))
                return;

            if (Intents.DEAMON_STATE_CHANGED.equals( intent.getAction() ))
            {
                handleDaemonStateChanged(
                        intent.getIntExtra( Intents.EXTRA_PREVIOUS_DAEMON_STATE, Intents.DAEMON_STATE_UNKNOWN ),
                        intent.getIntExtra( Intents.EXTRA_DAEMON_STATE, Intents.DAEMON_STATE_UNKNOWN )
                );
            }
            else if (Intents.NETWORK_STATE_CHANGED.equals( intent.getAction() ))
            {
                handleNetworkStateChanged( intent );
            }
        }

        private boolean intentAddressesThisDaemonEnabler(Intent intent)
        {
            return mConfigFile.getAbsolutePath().equals( intent.getStringExtra( Intents.EXTRA_CONFIG ) );
        }
    };

    public DaemonEnabler(Context context, OpenVpnService openVpnServiceShell, CheckBoxPreference daemonCheckBoxPreference, File configFile)
	{
		mContext = context;
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
		mDaemonCheckBoxPref.setEnabled( isEnabledByDependency() && isOpenVpnServiceEnabled() );
        daemonQueryState();
    }

    boolean isRegistered = false;
	public void resume()
	{
		mContext.registerReceiver(mDaemonStateReceiver, mDaemonStateFilter);
		isRegistered = true; //TODO: is this flag really necessary?
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
        if (isOpenVpnServiceDisabled())
        {
			mDaemonCheckBoxPref.setSummary( "Error: not bound to OpenVPN service!" );
			updateGui = false; // Don't update UI to opposite
		}
		else
		{
			final boolean currentState = isDaemonStarted();
			final boolean newState = (Boolean) value;
			
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
                // Disable checkbox until we receive DEAMON_STATE_CHANGED from service
                mDaemonCheckBoxPref.setEnabled( false );

                if (newState)
                    daemonStart();
                else
                    daemonStop();
                updateGui = false; // Don't update UI to opposite state until we're sure
            }
			
			PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean(
					Preferences.KEY_CONFIG_INTENDED_STATE( mConfigFile ), newState
			).commit();
		}        

		return updateGui;
	}

    protected void handleDaemonStateChanged(int previousDaemonState, int daemonState)
	{
		if (LOCAL_LOGD)
			Log.d(TAG, "Received OpenVPN daemon state changed from "
					+ getHumanReadableDaemonState(previousDaemonState) + " to "
					+ getHumanReadableDaemonState(daemonState));


		switch ( daemonState ) {
		case Intents.DAEMON_STATE_ENABLED:
			mDaemonCheckBoxPref.setChecked( true );
			mDaemonCheckBoxPref.setSummary( getLatestSummary() ); //TODO: get message from latest sticky NETWORK_STATE_CHANGED broadcast
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
			mDaemonCheckBoxPref.setSummary( "unknown daemon state: " + daemonState );
		}		
		mDaemonCheckBoxPref.setEnabled( isEnabledByDependency() && isOpenVpnServiceEnabled() );
	}

    protected void handleNetworkStateChanged(Intent intent)
	{
        {
            final int previousNetworkState = intent.getIntExtra( Intents.EXTRA_PREVIOUS_NETWORK_STATE, Intents.NETWORK_STATE_UNKNOWN );
            final int networkState = intent.getIntExtra( Intents.EXTRA_NETWORK_STATE, Intents.NETWORK_STATE_UNKNOWN );

            if (LOCAL_LOGD)
                Log.d( TAG, "Received OpenVPN network state changed from "
                        + getHumanReadableNetworkState( previousNetworkState ) + " to "
                        + getHumanReadableNetworkState( networkState ) );
        }

        if (isDaemonStarted()) // TODO: why is this condition necessary? Remove it if possible
            mDaemonCheckBoxPref.setSummary( getHumanReadableSummaryForNetworkStateChanged( intent ) );
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
		case Intents.DAEMON_STATE_STARTUP:
			return "Startup";
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

    private String getHumanReadableSummaryForNetworkStateChanged(Intent intent)
    {
        final String summary;
        final int networkState = intent.getIntExtra( Intents.EXTRA_NETWORK_STATE, Intents.NETWORK_STATE_UNKNOWN );
        switch (networkState)
        {
            case Intents.NETWORK_STATE_UNKNOWN:
                summary = "Unknown";
                break;
            case Intents.NETWORK_STATE_CONNECTING:
                summary = "Connecting";
                break;
            case Intents.NETWORK_STATE_RECONNECTING:
                final String cause = intent.getStringExtra( Intents.EXTRA_NETWORK_CAUSE );
                if (cause == null)
                    summary = "Reconnecting";
                else
                    summary = String.format( "Reconnecting (caused by %s)", cause );
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
        return summary;
    }




    private boolean isDaemonStarted()
    {
        if ( mOpenVpnService == null )
            return false;
        return mOpenVpnService.isDaemonStarted( mConfigFile );
    }

    private boolean isOpenVpnServiceDisabled()
    {
        return mOpenVpnService == null;
    }

    private boolean isOpenVpnServiceEnabled()
    {
        return mOpenVpnService != null; // should we check OpenVpnService.isOpenVpnServiceEnabled()? Or should we just rely on Prefernce openvpn_enabled?
    }

    private void daemonStart()
    {
        mOpenVpnService.daemonStart( mConfigFile );
    }

    private void daemonStop()
    {
        mOpenVpnService.daemonStop( mConfigFile );
    }

    private void daemonQueryState()
    {
//        if (isOpenVpnServiceEnabled())
//            mOpenVpnService.daemonQueryState(mConfigFile);
    }

    public CharSequence getLatestSummary()
    {
        Intent intent = mDaemonCheckBoxPref.getContext().registerReceiver( null, new IntentFilter( Intents.NETWORK_STATE_CHANGED ) );
        if ( intent == null )
            return "State is unknown";  // TODO: should we issue a 'state' command to the openvpn daemon to update the sticky broadcast?

        /* TODO: should we check if the intent is for this config (EXTRA_CONFIG)?
         *       If not we could remove the sticky broadcast and
         *       issue a 'state' command to the openvpn daemon,
         *       to generate a new sticky broadcast
         */

        return getHumanReadableSummaryForNetworkStateChanged( intent );
    }
}
