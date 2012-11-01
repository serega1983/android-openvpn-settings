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

import android.content.Intent;

public final class Intents
{
	public final static String NS = Intents.class.getName();

    public final static String OPEN_VPN_SERVICE_STARTED = NS + ".OPEN_VPN_SERVICE_STARTED";
	
	public final static String DEAMON_STATE_CHANGED = NS + ".DAEMON_STATE_CHANGED";
	public final static String NETWORK_STATE_CHANGED = NS + ".NETWORK_STATE_CHANGED";
	
	public final static String EXTRA_CONFIG = "config";
	public final static String EXTRA_DAEMON_STATE = "daemon-state";
	public final static String EXTRA_PREVIOUS_DAEMON_STATE = "previous-daemon-state";
	public final static String EXTRA_NETWORK_STATE = "network-state";
	public final static String EXTRA_PREVIOUS_NETWORK_STATE = "previous-network-state";
	public final static String EXTRA_NETWORK_TIME = "network-time";
	public final static String EXTRA_NETWORK_LOCALIP = "network-localip";
	public final static String EXTRA_NETWORK_REMOTEIP = "network-remoteip";
	public final static String EXTRA_NETWORK_CAUSE = "network-cause";
	
	public final static int DAEMON_STATE_UNKNOWN = 0;
	public final static int DAEMON_STATE_STARTUP = 1;
	public final static int DAEMON_STATE_ENABLED = 2;
	public final static int DAEMON_STATE_DISABLED = 3;

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

    public static final String BROADCAST_NEED_PASSWORD = NS + ".NEED_PASSWORD";

    public final static Intent daemonStateChanged(String config, int state){
		return new Intent(DEAMON_STATE_CHANGED)
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

	public final static String DATA_TYPE_CONFIG = NS+"/config";
	
	private Intents() {}

//	public final static Uri config2Uri(String config) {
//		return Uri.parse("content://de.schaeuffelhut.openvpn/"
//				+ (config == null ? "" : config));
//	}
//	public final static String URI2config(Uri uri) {
//		return uri.getLastPathSegment();
//	}
}
