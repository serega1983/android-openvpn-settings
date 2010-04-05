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
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;
import de.schaeuffelhut.android.openvpn.service.OpenVpnService;

public class EnterUserPassword extends Activity implements ServiceConnection {

	private static final String TAG = "OpenVPN-EnterPassphrase";
	
	public static String EXTRA_FILENAME = "extra_filename";

	private OpenVpnService mOpenVpnService;

	private AlertDialog mDialog;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

//		final EditText username = new EditText(this);
//		username.setHint( "Username" );

//		final EditText password = new EditText(this);
//		password.setInputType( InputType.TYPE_TEXT_VARIATION_PASSWORD );
//		password.setHint( "Passphrase" );

//		LinearLayout linearLayout = new LinearLayout(this);
//		linearLayout.addView(username);
//		linearLayout.addView(password);
		
		DialogInterface.OnClickListener ok = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				EditText username = (EditText)((AlertDialog)dialog).findViewById( R.id.enter_user_password_user);
				EditText password = (EditText)((AlertDialog)dialog).findViewById( R.id.enter_user_password_password );
				mOpenVpnService.daemonUsernamePassword( 
						getConfigFile(),
						username.getText().toString(), 
						password.getText().toString() 
				);
				finish();
			}
		};
		
		//TODO: find out how to acces dialog without field 
		mDialog = new AlertDialog.Builder(this)
		.setTitle( "Passphrase for " + getConfigFile().getName() )
		.setView( LayoutInflater.from(this).inflate( R.layout.enter_user_password, null) )
		.setNeutralButton("OK", ok).create();
		

		return mDialog;
	}

	@Override
	protected synchronized void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		AlertDialog alertDialog = (AlertDialog)dialog;
		alertDialog.setTitle( "Passphrase for " + getConfigFile().getName() );
		alertDialog.getButton( AlertDialog.BUTTON_NEUTRAL ).setEnabled( mOpenVpnService != null );
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

	private File getConfigFile()
	{
		return new File( getIntent().getStringExtra( EXTRA_FILENAME ) );
	}	
}
