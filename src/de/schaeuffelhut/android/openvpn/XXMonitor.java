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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;
import android.widget.Toast;

public class XXMonitor extends Activity
{
	
	OpenVpnService mControlShell = null;
	
	ServiceConnection mControlShellConnection = new ServiceConnection(){
		private Thread lt;
		public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
			mControlShell = ((OpenVpnService.ServiceBinder)serviceBinder).getService();
			Toast.makeText(XXMonitor.this, "Connected to ControlShell", Toast.LENGTH_SHORT).show();

//			final LoggerThread daemonLogger = mControlShell.getDaemonLogger(config);
//			if (daemonLogger != null)
			{
//				lt = new HandlerThread("Logger"){
//					public void run() {
//						getLooper().p
//						String[] buffer;
//						while( ( buffer = daemonLogger.getBuffer() ) != null )
//						{
//							for(String l : buffer )
//							{
//								tf.append( l );
//								tf.append( "\n" );
//							}
//						}
//					}
//				};
//				lt.start();
			}

		}
		public void onServiceDisconnected(ComponentName name) {
			mControlShell = null;
			Toast.makeText(XXMonitor.this, "Execpectedly disconnected from ControlShell", Toast.LENGTH_SHORT).show();
		}
	};

	private String config;

	private TextView tf;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView( R.layout.monitor );
		
		config = getIntent().getData().getLastPathSegment();
		setTitle( getString( R.string.monitor_title, config ) );
		
		tf = (TextView)findViewById( R.id.monitor_stdout );

		if ( !bindService(
        		new Intent(
        				XXMonitor.this,
        				OpenVpnService.class
        		),
        		mControlShellConnection,
        		Context.BIND_AUTO_CREATE
        ) )
        {
        	Toast.makeText(
        			getApplicationContext(), 
        			"Could not bind to ControlShell",
        			Toast.LENGTH_SHORT
        	).show();
        }

		
	}
}
