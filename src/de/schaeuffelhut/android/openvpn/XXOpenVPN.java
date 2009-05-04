package de.schaeuffelhut.android.openvpn;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class XXOpenVPN extends Activity {
	
	ControlShell mControlShell = null;
	
	ServiceConnection mControlShellConnection = new ServiceConnection(){
		public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
			mControlShell = ((ControlShell.ServiceBinder)serviceBinder).getService();
			Toast.makeText(XXOpenVPN.this, "Connected to ControlShell", Toast.LENGTH_SHORT).show();
		}
		public void onServiceDisconnected(ComponentName name) {
			mControlShell = null;
			Toast.makeText(XXOpenVPN.this, "Execpectedly disconnected from ControlShell", Toast.LENGTH_SHORT).show();
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.main );
                
        if ( !bindService(
        		new Intent(
        				XXOpenVPN.this,
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
        
        ((Button) findViewById(R.id.start)).setOnClickListener(
        		new Button.OnClickListener() {
        			public void onClick(View v) {
        				mControlShell.daemonStart( "desert.conf" );
        			}
        		}
        );

        ((Button) findViewById(R.id.stop)).setOnClickListener(
        		new Button.OnClickListener() {
        			public void onClick(View v) {
        				mControlShell.daemonStop( "desert.conf" );
        			}
        		}
        );

        ((Button) findViewById(R.id.restart)).setOnClickListener(
        		new Button.OnClickListener() {
        			public void onClick(View v) {
        				mControlShell.daemonRestart( "desert.conf" );
        			}
        		}
        );

        ((Button) findViewById(R.id.restartall)).setOnClickListener(
        		new Button.OnClickListener() {
        			public void onClick(View v) {
        				//mControlShell.daemonRestart();
        			}
        		}
        );

        TextView textView = (TextView)findViewById( R.id.text );
        
//        connectivityListener = new NetworkConnectivityListener();
//        connectivityListener.registerHandler( mHandler, CONNECTIVITY_MSG );
//        connectivityListener.startListening( getApplicationContext() ); 
    }
        
//    private static final int CONNECTIVITY_MSG = 0;
//
//	protected static final int RSTART_NOTIFICATION = 0;
//    
//    private Handler mHandler = new Handler() {
//    	public void handleMessage(Message msg) {
//    		switch(msg.what) {
//    		case CONNECTIVITY_MSG:
//    			NetworkInfo networkInfo = connectivityListener.getNetworkInfo();
//    			TextView textView = (TextView)findViewById( R.id.text );
//    			textView.append( networkInfo.toString() );
//    			try
//    			{
//        			textView.append( "\n" );
//        			textView.append( "Restaring OpenVPN: " );
//    				Process exec = Runtime.getRuntime().exec( new String[]{
//    							"/system/bin/su", "-c", "/system/xbin/bb/killall -USR1 openvpn2.1"
//    					});
//    				int waitFor = exec.waitFor();
//        			textView.append( ""+waitFor );
//    			} catch (Exception e) {
//        			textView.append( "\nException: "+e.getMessage() );
//				}
//    			textView.append( "\n" );
//    			break;
//    		}
//    	}
//    };
//
//	private NetworkConnectivityListener connectivityListener;

}