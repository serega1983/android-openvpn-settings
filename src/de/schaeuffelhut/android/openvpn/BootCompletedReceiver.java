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
