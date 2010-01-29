package de.schaeuffelhut.android.openvpn;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import de.schaeuffelhut.android.openvpn.service.OpenVpnService;

public class EnterPassphrase extends Activity implements ServiceConnection {

	private static final String TAG = "OpenVPN-EnterPassphrase";
	
	public static String EXTRA_FILENAME = "extra_filename";

	private File mConfigFile;

	private OpenVpnService mOpenVpnService;

	private AlertDialog mDialog;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mConfigFile = new File( getIntent().getStringExtra( EXTRA_FILENAME ) );
		showDialog( 1 );

		if ( !bindService(
				new Intent( this, OpenVpnService.class ),
				this,
				Context.BIND_AUTO_CREATE
		) )
		{
			Log.w(TAG, "Could not bind to ControlShell" );
		}
	}

	@Override
	protected void onDestroy() {
		super.onStop();
		if ( mOpenVpnService != null )
			unbindService( this );
	}
	
	
	@Override
	protected Dialog onCreateDialog(int id) {
		
		final EditText passphrase = new EditText(this);
		passphrase.setInputType( InputType.TYPE_TEXT_VARIATION_PASSWORD );
		passphrase.setHint( "Passphrase" );
			
		DialogInterface.OnClickListener ok = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mOpenVpnService.daemonPassphrase( mConfigFile, passphrase.getText().toString() );
				Notifications.cancelPassphraseRequired(EnterPassphrase.this);
				finish();
			}
		};
		
		//TODO: find out how to acces dialog without field 
		mDialog = new AlertDialog.Builder(this)
		.setTitle( "Passphrase for " + mConfigFile.getName() )
		.setView( passphrase )
		.setNeutralButton("OK", ok).create();
		

		return mDialog;
	}

	@Override
	protected synchronized void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		
		((AlertDialog)dialog).getButton( AlertDialog.BUTTON_NEUTRAL ).setEnabled( mOpenVpnService != null );
	}
	
	public synchronized void onServiceConnected(ComponentName name, IBinder serviceBinder) {
		mOpenVpnService = ((OpenVpnService.ServiceBinder)serviceBinder).getService();
		Log.d( TAG, "Connected to OpenVpnService" );
		if ( mDialog != null && mDialog.getButton( AlertDialog.BUTTON_NEUTRAL ) != null )
			mDialog.getButton( AlertDialog.BUTTON_NEUTRAL ).setEnabled( true );
	}
	
	public synchronized void onServiceDisconnected(ComponentName name) {
		mOpenVpnService = null;
		Log.d( TAG, "Disconnected from OpenVpnService" );
		if ( mDialog != null && mDialog.getButton( AlertDialog.BUTTON_NEUTRAL ) != null )
			mDialog.getButton( AlertDialog.BUTTON_NEUTRAL ).setEnabled( false );
	}
	
}
