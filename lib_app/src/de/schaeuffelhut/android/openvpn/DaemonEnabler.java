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
import de.schaeuffelhut.android.openvpn.service.api.OpenVpnConfig;
import de.schaeuffelhut.android.openvpn.service.api.OpenVpnServiceWrapper;

public class DaemonEnabler implements Preference.OnPreferenceChangeListener
{
	private static final boolean LOCAL_LOGD = true;
	private static final String TAG = "OpenVPNDaemonEnabler";

	private final Context mContext; 
	private final OpenVpnServiceWrapper mOpenVpnService;
	private final CheckBoxPreference mDaemonCheckBoxPref;
	private final File mConfigFile;
	private final CharSequence mOriginalSummary;

	private final IntentFilter mDaemonStateFilter;
    private final BroadcastReceiver mDaemonStateReceiver = new MyBroadcastReceiver();

    public DaemonEnabler(Context context, OpenVpnServiceWrapper openVpnService, CheckBoxPreference daemonCheckBoxPreference, File configFile)
	{
		mContext = context;
        mOpenVpnService = openVpnService;
		mDaemonCheckBoxPref = daemonCheckBoxPreference;
		mConfigFile = configFile;

		mOriginalSummary = mDaemonCheckBoxPref.getSummary();
		mDaemonCheckBoxPref.setPersistent( false );

		mDaemonStateFilter = new IntentFilter(Intents.DAEMON_STATE_CHANGED );
		mDaemonStateFilter.addAction( Intents.NETWORK_STATE_CHANGED );

        refreshGui();
	}

	public void refreshGui()
	{
		mDaemonCheckBoxPref.setEnabled( isEnabledByDependency() && isOpenVpnServiceEnabled() );
        daemonQueryState();
    }

    int resume = 0;
    boolean isRegistered = false;
	public void resume()
	{
        resume++; Log.d(TAG, "=====> resume: " + resume );
		mContext.registerReceiver(mDaemonStateReceiver, mDaemonStateFilter);
		isRegistered = true; //TODO: is this flag really necessary?
		mDaemonCheckBoxPref.setOnPreferenceChangeListener(this);
	}

	public void pause()
	{
        resume--; Log.d(TAG, "=====> resume: " + resume );
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


    private class MyBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            //TODO: move into IntentFilter, or dispatch from OpenVPNSettings to DaemonEnabler.receive()
            if (!intentAddressesThisDaemonEnabler( intent ))
                return;

            if (Intents.DAEMON_STATE_CHANGED.equals( intent.getAction() ))
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
    }

    protected void handleDaemonStateChanged(int previousDaemonState, int daemonState)
	{
		if (LOCAL_LOGD)
			Log.d(TAG, "Received OpenVPN daemon state changed from "
					+ Intents.getHumanReadableDaemonState( previousDaemonState ) + " to "
					+ Intents.getHumanReadableDaemonState( daemonState ));


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
                        + Intents.getHumanReadableNetworkState( previousNetworkState ) + " to "
                        + Intents.getHumanReadableNetworkState( networkState ) );
        }

        if (isDaemonStarted()) // TODO: why is this condition necessary? Remove it if possible
            mDaemonCheckBoxPref.setSummary( Intents.getHumanReadableSummaryForNetworkStateChanged( intent ) );
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

    private boolean isDaemonStarted()
    {
        return mOpenVpnService.getStatusFor( new OpenVpnConfig( mConfigFile ) ).isStarted();
    }

    private boolean isOpenVpnServiceDisabled()
    {
        return !mOpenVpnService.isBound();
    }

    private boolean isOpenVpnServiceEnabled()
    {
        return mOpenVpnService.isBound(); // should we check OpenVpnService.isOpenVpnServiceEnabled()? Or should we just rely on Prefernce openvpn_enabled?
    }

    private void daemonStart()
    {
        mOpenVpnService.connect( new OpenVpnConfig( mConfigFile ) );
    }

    private void daemonStop()
    {
        mOpenVpnService.disconnect();
    }

    private void daemonQueryState()
    {
//        if (isOpenVpnServiceEnabled())
//            mOpenVpnService.daemonQueryState(mConfigFile);
    }

    public CharSequence getLatestSummary()
    {
        Intent intent = Intents.getLatestNetworkStateChangedIntent( mDaemonCheckBoxPref.getContext() );
        if ( intent == null )
            return "State is unknown";  // TODO: should we issue a 'state' command to the openvpn daemon to update the sticky broadcast?

        /* TODO: should we check if the intent is for this config (EXTRA_CONFIG)?
         *       If not we could remove the sticky broadcast and
         *       issue a 'state' command to the openvpn daemon,
         *       to generate a new sticky broadcast
         */

        return Intents.getHumanReadableSummaryForNetworkStateChanged( intent );
    }
}
