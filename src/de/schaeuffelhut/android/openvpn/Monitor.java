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

public class Monitor extends Activity
{
	
	ControlShell mControlShell = null;
	
	ServiceConnection mControlShellConnection = new ServiceConnection(){
		private Thread lt;
		public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
			mControlShell = ((ControlShell.ServiceBinder)serviceBinder).getService();
			Toast.makeText(Monitor.this, "Connected to ControlShell", Toast.LENGTH_SHORT).show();

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
			Toast.makeText(Monitor.this, "Execpectedly disconnected from ControlShell", Toast.LENGTH_SHORT).show();
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
        				Monitor.this,
        				ControlShell.class
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
