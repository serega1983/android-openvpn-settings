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

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import de.schaeuffelhut.android.openvpn.lib.app.R;
import de.schaeuffelhut.android.openvpn.service.api.OpenVpnCredentials;
import de.schaeuffelhut.android.openvpn.service.api.OpenVpnServiceWrapper;

public class EnterUserPassword extends Activity {

	private static final String TAG = "OpenVPN-EnterPassphrase";
	
	private final OpenVpnServiceWrapper mOpenVpnService = new OpenVpnServiceWrapper( this ) {

        @Override
        public synchronized void onServiceConnectedHook(ComponentName name, IBinder serviceBinder) {
            Log.d( TAG, "Connected to OpenVpnService" );

            Button button = getNeutralButtonFromDialog();
            if ( button != null )
                button.setEnabled( true );
        }

        @Override
        public synchronized void onServiceDisconnectedHook(ComponentName name) {
            Log.d( TAG, "Disconnected from OpenVpnService" );

            Button button = getNeutralButtonFromDialog();
            if ( button != null )
                button.setEnabled( false );
        }

    };

    private Button getNeutralButtonFromDialog()
    {
        if ( mDialog == null )
            return null;
        return mDialog.getButton( AlertDialog.BUTTON_NEUTRAL );
    }

    private AlertDialog mDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		showDialog( 1 );

		if ( !mOpenVpnService.bindService() )
		{
            //TODO: service is not running and no username/password can be submitted, abort Activity
			Log.w(TAG, "Could not bind to ControlShell" );
		}
	}

	@Override
	protected void onDestroy() {
		super.onStop();
        mOpenVpnService.unbindService();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		DialogInterface.OnClickListener ok = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				View view = ((AlertDialog) dialog).getWindow().getDecorView();
                CheckBox remember = (CheckBox) view.findViewById( R.id.enter_user_password_remember );
                EditText username = (EditText)view.findViewById( R.id.enter_user_password_user);
				EditText password = (EditText)view.findViewById( R.id.enter_user_password_password );
                if ( remember.isChecked() )
                    Preferences.setCredentials( getApplicationContext(), getConfigFile(), username.getText().toString(), password.getText().toString() );
                else
                    Preferences.clearPassphraseOrCredentials( getApplicationContext(), getConfigFile() );
				mOpenVpnService.supplyCredentials( new OpenVpnCredentials( username.getText().toString(), password.getText().toString() ) );
                finish();
			}
		};
		
		//TODO: find out how to access dialog without field 
        View view = LayoutInflater.from( this ).inflate( R.layout.enter_user_password, null );
        EditText username = (EditText)view.findViewById( R.id.enter_user_password_user);
        EditText password = (EditText)view.findViewById( R.id.enter_user_password_password );
        CheckBox remember = (CheckBox) view.findViewById( R.id.enter_user_password_remember );
        username.setText( Preferences.getUsername( this, getConfigFile() ) );
        password.setText( Preferences.getPassword( this, getConfigFile() ) );
        remember.setChecked( Preferences.hasCredentials( this, getConfigFile() ) );
        mDialog = new AlertDialog.Builder(this)
		.setTitle( "Password required" )
		.setView( view )
		.setNeutralButton("OK", ok).create();
		

		return mDialog;
	}

	@Override
	protected synchronized void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		AlertDialog alertDialog = (AlertDialog)dialog;
		alertDialog.setTitle( "Password required" );
		((TextView)alertDialog.findViewById( R.id.enter_user_password_config_name )).setText( Preferences.getConfigName( this, getConfigFile() ) );
		alertDialog.getButton( AlertDialog.BUTTON_NEUTRAL ).setEnabled( mOpenVpnService != null );
	}

	private File getConfigFile()
	{
		return new File( getIntent().getData().getPath() );
	}
}
