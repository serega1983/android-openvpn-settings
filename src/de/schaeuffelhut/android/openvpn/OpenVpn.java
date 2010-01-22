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
import java.io.FilenameFilter;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.Preference;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class OpenVpn extends ListActivity
{
	private static final int IMPORT_FILES = 1;

	private String[] configs;

	OpenVpnService mControlShell = null;
	
	ServiceConnection mControlShellConnection = new ServiceConnection(){
		public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
			mControlShell = ((OpenVpnService.ServiceBinder)serviceBinder).getService();
			Toast.makeText(OpenVpn.this, "Connected to ControlShell", Toast.LENGTH_SHORT).show();
		}
		public void onServiceDisconnected(ComponentName name) {
			mControlShell = null;
			Toast.makeText(OpenVpn.this, "Execpectedly disconnected from ControlShell", Toast.LENGTH_SHORT).show();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if ( !bindService(
        		new Intent(
        				OpenVpn.this,
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
		refreshFileList();
		getListView().setTextFilterEnabled(false);
		registerForContextMenu( getListView() );
	}

	private void refreshFileList() {
		File[] files = new File( getApplicationContext().getFilesDir(), "config.d" ).listFiles( new Util.FileExtensionFilter( ".conf" ) );
		configs = new String[files.length];
		for(int i=0; i<files.length; i++)
			configs[i] = files[i].getName();
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				this,
				android.R.layout.simple_list_item_multiple_choice,
				configs
		){
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				CheckedTextView tv = (CheckedTextView)super.getView(position, convertView, parent);
				tv.setChecked(true);
				tv.setHint("hint");
				return tv;
			}
		};
		setListAdapter( arrayAdapter );
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		Intent intent = new Intent( getApplicationContext(), Monitor.class );
		intent.setData( Uri.parse( "content://de.schaeuffelhut.openvpn/"+ (String)l.getItemAtPosition(position) ) );
		startActivity( intent );
	}
	
	/* Creates the menu items */
	
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.configs_options_menu, menu);
	    menu.findItem( R.id.configs_options_startall ).setVisible( configs.length > 0 );
	    menu.findItem( R.id.configs_options_restartall ).setVisible( mControlShell.hasDaemonsStarted() );
	    menu.findItem( R.id.configs_options_stopall ).setVisible( mControlShell.hasDaemonsStarted() );
	    return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
	    menu.findItem( R.id.configs_options_startall ).setVisible( configs.length > 0 );
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
			refreshFileList();
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
	
	@Override
	public void onCreateContextMenu(
			ContextMenu menu, View v, ContextMenuInfo menuInfo
	) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.configs_context_menu, menu);
	    
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		String config = configs[(int)info.id];
	    boolean isDaemonStarted = mControlShell.isDaemonStarted( config );
	    
	    menu.findItem( R.id.configs_contex_start ).setVisible( !isDaemonStarted );
	    menu.findItem( R.id.configs_contex_restart ).setVisible( isDaemonStarted );
	    menu.findItem( R.id.configs_contex_stop ).setVisible( isDaemonStarted );
	    menu.findItem( R.id.configs_contex_monitor ).setVisible( isDaemonStarted );
	}
		
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		String config = configs[(int)info.id];
		switch ( item.getItemId() ) {
		case R.id.configs_contex_start:
			mControlShell.daemonStart( config );
			return true;
		case R.id.configs_contex_restart:
			mControlShell.daemonRestart( config );
			return true;
		case R.id.configs_contex_stop:
			mControlShell.daemonStop( config );
			return true;
		case R.id.configs_contex_monitor:
			return true;
		case R.id.configs_contex_edit:
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch ( requestCode ) {
		case IMPORT_FILES:
			if ( resultCode == ImportFiles.RESULT_OK )
				refreshFileList();
			break;

		default:
			break;
		}
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}
}
