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
package de.schaeuffelhut.android.openvpn.util;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;
import de.schaeuffelhut.android.openvpn.Preferences;
import de.schaeuffelhut.android.openvpn.service.OpenVpnService;

public class BootCompletedReceiver extends BroadcastReceiver
{
	public static final String TAG = "OpenVPN";
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		// just make sure we are getting the right intent (better safe than sorry)
		  if( Intent.ACTION_BOOT_COMPLETED.equals( intent.getAction() ) )
		  {
			  if ( PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Preferences.KEY_OPENVPN_ENABLED, false ) )
			  {
				  Log.d(TAG, "OpenVPN-Service enabled in preferences, starting!" );

				  ComponentName service = context.startService( new Intent( context, OpenVpnService.class ) );

				  //Why so complicated?
				  //			  ComponentName comp = new ComponentName(
				  //					  context.getPackageName(),
				  //					  OpenVpnService.class.getName()
				  //			  );
				  //			  ComponentName service = context.startService(
				  //					  new Intent().setComponent( comp )
				  //			  );
				  if ( service == null )
				  {
					  // something really wrong here
					  Log.e(TAG, "Could not start service " + service.toString() );
				  }
				  else
				  {
					  Log.i(TAG, service.toString() + "started" );
				  }
			  }
			  else
			  {
				  Log.d(TAG, "OpenVPN-Service disabled in preferences, not starting!" );
			  }
		  }
		  else
		  {
			  Log.e(TAG, "Received unexpected intent " + intent.toString());   
		  }
	}
}
