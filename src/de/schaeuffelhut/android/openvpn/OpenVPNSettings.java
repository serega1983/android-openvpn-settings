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
import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class OpenVPNSettings extends PreferenceActivity implements ServiceConnection
{
	private static final int IMPORT_FILES = 1;
    private static final String KEY_PARENT = "parent";

    static String KEY_CONFIG(String config){
    	return String.format("config[%s]", config);
    }
    static String KEY_CONFIG_ENABLED(String config){
    	return KEY_CONFIG(config)+".enabled";
    }
    
    ArrayList<DaemonEnabler> mDaemonEnablers = new ArrayList<DaemonEnabler>(4);
	ControlShell mControlShell = null;
    
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource( R.xml.preferences );

        initToggles();
        
		if ( !bindService(
        		new Intent( this, ControlShell.class ),
        		this,
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

    
    private void initToggles()
	{
		PreferenceCategory parent = (PreferenceCategory) findPreference(KEY_PARENT);
		parent.removeAll();
		mDaemonEnablers.clear();
		
		File[] configFiles = new File(
				getApplicationContext().getFilesDir(),
				"config.d"
		).listFiles(new Util.FileExtensionFilter(".conf"));
		
		for (int i = 0; configFiles != null && i < configFiles.length; i++)
		{
			final String configFileName = configFiles[i].getName();

			CheckBoxPreference pref = new CheckBoxPreference( getApplicationContext() );
			pref.setTitle( configFileName );
			pref.setSummary( "Select to turn on OpenVPN tunel");
			pref.setKey( KEY_CONFIG_ENABLED( configFileName ) );

			parent.addPreference(pref);
			mDaemonEnablers.add( new DaemonEnabler( 
					getApplicationContext(), mControlShell, pref, configFileName ) );			
		}
	}


	public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
		mControlShell = ((ControlShell.ServiceBinder)serviceBinder).getService();
		Toast.makeText(this, "Connected to ControlShell", Toast.LENGTH_SHORT).show();

		for(DaemonEnabler daemonEnabler : mDaemonEnablers )
			daemonEnabler.setControlShell( mControlShell );
	}
	
	public void onServiceDisconnected(ComponentName name) {
		mControlShell = null;
		Toast.makeText(this, "Execpectedly disconnected from ControlShell", Toast.LENGTH_SHORT).show();

		for(DaemonEnabler daemonEnabler : mDaemonEnablers )
			daemonEnabler.setControlShell( null );
	}


    @Override
    protected void onResume() {
		super.onResume();
		
		for(DaemonEnabler daemonEnabler : mDaemonEnablers )
			daemonEnabler.resume();
	}
	
    @Override
    protected void onPause() {
    	super.onPause();
    	
		for(DaemonEnabler daemonEnabler : mDaemonEnablers )
			daemonEnabler.pause();
    }

    
    
    

	    
    

	/*
	 * options menu
	 */
	
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.configs_options_menu, menu);
	    return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
//	    menu.findItem( R.id.configs_options_startall ).setVisible( configs.length > 0 );
	    menu.findItem( R.id.configs_options_restartall ).setVisible( mControlShell.hasDaemonsStarted() );
	    menu.findItem( R.id.configs_options_stopall ).setVisible( mControlShell.hasDaemonsStarted() );
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch ( item.getItemId() ) {
		case R.id.configs_options_import:
			Intent intent = new Intent( getApplicationContext(), ImportFiles.class );
			startActivityForResult(intent, IMPORT_FILES);
			return true;
		case R.id.configs_options_refresh:
			initToggles();
			return true;
		case R.id.configs_options_startall:
			return true;
		case R.id.configs_options_restartall:
			return true;
		case R.id.configs_options_stopall:
			return true;
		case R.id.configs_options_settings:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
