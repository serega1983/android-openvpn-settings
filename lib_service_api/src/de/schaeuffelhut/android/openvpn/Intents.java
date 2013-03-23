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

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public final class Intents
{
	public final static String NS = Intents.class.getName();

    // broadcasted intents
	public final static String DAEMON_STATE_CHANGED = NS + ".DAEMON_STATE_CHANGED"; //TODO: duplicated by [lib_service_api]Intents.DAEMON_STATE_CHANGED.getAction()
	public final static String NETWORK_STATE_CHANGED = NS + ".NETWORK_STATE_CHANGED"; //TODO: duplicated by [lib_service_api]Intents.NETWORK_STATE_CHANGED.getAction()
    public static final String BROADCAST_NEED_PASSWORD = NS + ".NEED_PASSWORD";

    // extras used by multiple intents
    public final static String EXTRA_CONFIG = "config";

    // extras used by DEAMON_STATE_CHANGED
	public final static String EXTRA_DAEMON_STATE = "daemon-state";
	public final static String EXTRA_PREVIOUS_DAEMON_STATE = "previous-daemon-state";

    // values for EXTRA_DAEMON_STATE and EXTRA_PREVIOUS_DAEMON_STATE
	public final static int DAEMON_STATE_UNKNOWN = 0;
	public final static int DAEMON_STATE_STARTUP = 1;
	public final static int DAEMON_STATE_ENABLED = 2;
	public final static int DAEMON_STATE_DISABLED = 3;

    // extras used by NETWORK_STATE_CHANGED
    public final static String EXTRA_NETWORK_STATE = "network-state";
	public final static String EXTRA_PREVIOUS_NETWORK_STATE = "previous-network-state";
	public final static String EXTRA_NETWORK_TIME = "network-time";
	public final static String EXTRA_NETWORK_LOCALIP = "network-localip";
	public final static String EXTRA_NETWORK_REMOTEIP = "network-remoteip";
	public final static String EXTRA_NETWORK_CAUSE = "network-cause";

    // values for EXTRA_NETWORK_STATE and EXTRA_PREVIOUS_NETWORK_STATE
	public final static int NETWORK_STATE_UNKNOWN = 0;
	public final static int NETWORK_STATE_CONNECTING = 1;
	public final static int NETWORK_STATE_RECONNECTING = 2;
	public static final int NETWORK_STATE_RESOLVE = 3;
	public static final int NETWORK_STATE_WAIT = 4;
	public static final int NETWORK_STATE_AUTH = 5;
	public static final int NETWORK_STATE_GET_CONFIG = 6;
	public final static int NETWORK_STATE_CONNECTED = 7;
	public static final int NETWORK_STATE_ASSIGN_IP = 8;
	public static final int NETWORK_STATE_ADD_ROUTES = 9;		
	public final static int NETWORK_STATE_EXITING = 10;

    // startService intents
    public static final String START_DAEMON = NS+".START_DAEMON";
    public static final String STOP_DAEMON = NS+".STOP_DAEMON";


    public final static Intent daemonStateChanged(String config, int state){
		return new Intent( DAEMON_STATE_CHANGED )
		.putExtra(EXTRA_CONFIG, config)
		.putExtra(EXTRA_DAEMON_STATE, state);
	}

	public final static Intent networkStateChanged(String config, int state, int previousState, long time){
		Intent intent = new Intent(NETWORK_STATE_CHANGED)
		.putExtra(EXTRA_CONFIG, config)
		.putExtra(EXTRA_NETWORK_STATE, state)
		.putExtra(EXTRA_PREVIOUS_NETWORK_STATE, previousState);
		if ( time != 0 )
			intent.putExtra(EXTRA_NETWORK_TIME, time);
		return intent;
	}

    /**
     * Returns the latest NETWORK_STATE_CHANGED broadcast or null if there is none.
     * @param context
     * @return the latest NETWORK_STATE_CHANGED broadcast or null if there is none.
     */
    public static Intent getLatestNetworkStateChangedIntent(Context context)
    {
        return context.registerReceiver( null, new IntentFilter( NETWORK_STATE_CHANGED ) );
    }

//	public final static String DATA_TYPE_CONFIG = NS+"/config";
	
	private Intents() {}

//	public final static Uri config2Uri(String config) {
//		return Uri.parse("content://de.schaeuffelhut.openvpn/"
//				+ (config == null ? "" : config));
//	}
//	public final static String URI2config(Uri uri) {
//		return uri.getLastPathSegment();
//	}

    public static String getHumanReadableSummaryForNetworkStateChanged(Intent intent)
    {
        final String summary;
        final int networkState = intent.getIntExtra( EXTRA_NETWORK_STATE, NETWORK_STATE_UNKNOWN );
        switch (networkState)
        {
            case NETWORK_STATE_UNKNOWN:
                summary = "Unknown";
                break;
            case NETWORK_STATE_CONNECTING:
                summary = "Connecting";
                break;
            case NETWORK_STATE_RECONNECTING:
                final String cause = intent.getStringExtra( EXTRA_NETWORK_CAUSE );
                if (cause == null)
                    summary = "Reconnecting";
                else
                    summary = String.format( "Reconnecting (caused by %s)", cause );
                break;
            case NETWORK_STATE_RESOLVE:
                summary = "Resolve";
                break;
            case NETWORK_STATE_WAIT:
                summary = "Wait";
                break;
            case NETWORK_STATE_AUTH:
                summary = "Auth";
                break;
            case NETWORK_STATE_GET_CONFIG:
                summary = "Get Config";
                break;
            case NETWORK_STATE_CONNECTED:
                summary = String.format(
                        "Connected to %s as %s",
                        intent.getStringExtra( EXTRA_NETWORK_REMOTEIP ),
                        intent.getStringExtra( EXTRA_NETWORK_LOCALIP )
                );
                break;
            case NETWORK_STATE_ASSIGN_IP:
                summary = String.format(
                        "Assign IP %s",
                        intent.getStringExtra( EXTRA_NETWORK_LOCALIP )
                );
                break;
            case NETWORK_STATE_ADD_ROUTES:
                summary = "Add Routes";
                break;
            case NETWORK_STATE_EXITING:
                summary = "Exiting";
                break;
            default:
                summary = String.format( "Some other state (%d)!", networkState );
        }
        return summary;
    }

    public static String getHumanReadableNetworkState(int networkState) {
        switch (networkState) {
        case NETWORK_STATE_UNKNOWN:
            return "Unknown";
        case NETWORK_STATE_CONNECTING:
            return "Connecting";
        case NETWORK_STATE_RECONNECTING:
            return "Reconnecting";
        case NETWORK_STATE_RESOLVE:
            return "Resolve";
        case NETWORK_STATE_WAIT:
            return "Wait";
        case NETWORK_STATE_AUTH:
            return "Auth";
        case NETWORK_STATE_GET_CONFIG:
            return "Get Config";
        case NETWORK_STATE_CONNECTED:
            return "Connected";
        case NETWORK_STATE_ASSIGN_IP:
            return "Assign IP";
        case NETWORK_STATE_ADD_ROUTES:
            return "Add Routes";
        case NETWORK_STATE_EXITING:
            return "Exiting";
        default:
            return String.format( "Some other state (%d)!", networkState );
        }
    }

    public static String getHumanReadableDaemonState(int daemonState) {
        switch (daemonState) {
        case DAEMON_STATE_STARTUP:
            return "Startup";
        case DAEMON_STATE_ENABLED:
            return "Enabled";
        case DAEMON_STATE_DISABLED:
            return "Disabled";
        case DAEMON_STATE_UNKNOWN:
            return "Unknown";
        default:
            return String.format( "Some other state (%d)!", daemonState );
        }
    }
}
