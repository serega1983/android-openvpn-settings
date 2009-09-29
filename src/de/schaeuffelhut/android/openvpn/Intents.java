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

import android.content.Intent;
import android.net.Uri;

public class Intents
{
	public final static String NS = Intents.class.getName();
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
	public final static int DAEMON_STATE_ENABLED = 1;
	public final static int DAEMON_STATE_DISABLED = 2;

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

	static Intent daemonStateChanged(String config, int state){
		return new Intent(DEAMON_STATE_CHANGED)
		.putExtra(EXTRA_CONFIG, config)
		.putExtra(EXTRA_DAEMON_STATE, state);
	}

	static Intent networkStateChanged(String config, int state, int previousState, long time){
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

	public static Uri config2Uri(String config) {
		return Uri.parse("content://de.schaeuffelhut.openvpn/"
				+ (config == null ? "" : config));
	}
	public static String URI2config(Uri uri) {
		return uri.getLastPathSegment();
	}
}
