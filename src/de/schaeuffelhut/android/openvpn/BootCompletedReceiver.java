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

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver
{
	public static final String TAG = "OpenVPN";
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		// just make sure we are getting the right intent (better safe than sorry)
		  if( Intent.ACTION_BOOT_COMPLETED.equals( intent.getAction() ) )
		  {
			  ComponentName comp = new ComponentName(
					  context.getPackageName(),
					  ControlShell.class.getName()
			  );
			  ComponentName service = context.startService(
					  new Intent().setComponent( comp )
			  );
			  if ( service == null )
			  {
				  // something really wrong here
				  Log.e(TAG, "Could not start service " + comp.toString() );
			  }
			  else
			  {
				  Log.i(TAG, comp.toString() + "started" );
			  }
		  }
		  else
		  {
			  Log.e(TAG, "Received unexpected intent " + intent.toString());   
		  }
	}
}
