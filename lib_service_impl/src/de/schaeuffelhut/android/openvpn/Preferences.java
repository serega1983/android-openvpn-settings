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
import java.util.*;

import android.os.Environment;
import de.schaeuffelhut.android.openvpn.util.tun.TunPreferences;
import org.apache.commons.io.FilenameUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import de.schaeuffelhut.android.openvpn.shared.util.Util;

public final class Preferences {
	
	/**
	 * Stores applications current version code. Used to detect upgrades.
	 */
	public static final String KEY_OPENVPN_VERSION_CODE = "openvpn_version_code";
	
	public static final String KEY_OPENVPN_SETTINGS_CATEGORY = "openvpn_settings_category";
	public static final String KEY_OPENVPN_ENABLED = "openvpn_enabled";
	public static final String KEY_OPENVPN_CONFIGURATIONS = "openvpn_configurations";
	public static final String KEY_OPENVPN_USE_INTERNAL_STORAGE = "openvpn_use_internal_storage";
    public static final String KEY_OPENVPN_EXTERNAL_STORAGE = "openvpn_external_storage";
	public static final String KEY_OPENVPN_PATH_TO_BINARY = "openvpn_path_to_binary";
	public static final String KEY_OPENVPN_PATH_TO_SU = "openvpn_path_to_su";
	public static final String KEY_OPENVPN_SU_ARGUMENTS = "openvpn_su_arguments";
    public static final String KEY_OPENVPN_SHOW_ADS = "show_ads";

    public static final String KEY_NEXT_NOTIFICATION_ID = "openvpn_next_notification_id";
	public static final String KEY_FIX_HTC_ROUTES = "fix_htc_routes"; 	// see issue #35: http://code.google.com/p/android-openvpn-settings/issues/detail?id=35

    private static final int FIRST_CONFIG_ID = 1000000;

    public final static String KEY_CONFIG(String config){
		return String.format( "%s[%s]", KEY_OPENVPN_CONFIGURATIONS, config );
	}

	public final static File configOf(String key) {
		if ( !isConfigKey(key) )
			throw new IllegalArgumentException( "Not a config preference key: "+key );
		int start = key.indexOf( '[' );
		int end = key.lastIndexOf( ']' );
		return new File( key.substring( start + 1, end ) );
	}

	public static boolean isConfigKey(String key) {
		return key.startsWith( KEY_OPENVPN_CONFIGURATIONS + "[" );
	}
	
	public final static String KEY_CONFIG_NAME(File config){
		return KEY_CONFIG(config.getAbsolutePath())+".name";
	}
	public final static String KEY_CONFIG_ENABLED(File config){
		return KEY_CONFIG(config.getAbsolutePath())+".enabled";
	}
	public final static String KEY_CONFIG_INTENDED_STATE(File config){
		return KEY_CONFIG(config.getAbsolutePath())+".intended_state";
	}
	public final static String KEY_CONFIG_MGMT_PORT(File config){
		return KEY_CONFIG(config.getAbsolutePath())+".mgmt_port";
	}
	public final static String KEY_CONFIG_NOTIFICATION_ID(File config){
		return KEY_CONFIG(config.getAbsolutePath())+".notification_id";
	}
	public final static String KEY_CONFIG_DNSCHANGE(File config){
		return KEY_CONFIG(config.getAbsolutePath())+".dnschange";
	}
	public final static String KEY_CONFIG_DNS1(File config){
		return KEY_CONFIG(config.getAbsolutePath())+".dns1";
	}
	public final static String KEY_CONFIG_LOG_STDOUT_ENABLE(File config){
		return KEY_CONFIG(config.getAbsolutePath())+".log_stdout.enable";
	}
	public final static String KEY_VPN_DNS(File config){
		return KEY_CONFIG(config.getAbsolutePath())+".vpndns1";
	}
	public final static String KEY_VPN_DNS_ENABLE(File config){
		return KEY_CONFIG(config.getAbsolutePath())+".vpndns.enable";
	}
	public final static String KEY_SCRIPT_SECURITY_LEVEL(File config){
		return KEY_CONFIG(config.getAbsolutePath())+".script_security.level";
	}
    private static String KEY_CONFIG_USERNAME(File config)
    {
		return KEY_CONFIG(config.getAbsolutePath())+".username";
    }
    private static String KEY_CONFIG_PASSWORD(File config)
    {
		return KEY_CONFIG(config.getAbsolutePath())+".password";
    }
    private static String KEY_CONFIG_PASSPHRASE(File config)
    {
		return KEY_CONFIG(config.getAbsolutePath())+".passphrase";
    }


    private Preferences() {
	}

	public static boolean getOpenVpnEnabled(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_OPENVPN_ENABLED, false );
	}

	public final static boolean getUseInternalStorage(SharedPreferences sharedPreferences)
	{
		return sharedPreferences.getBoolean( KEY_OPENVPN_USE_INTERNAL_STORAGE, false );	
	}

	public final static void setUseInternalStorage(Context context, boolean b)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = sharedPreferences.edit();
		edit.putBoolean( KEY_OPENVPN_USE_INTERNAL_STORAGE, true );
		edit.commit();
	}

    public static boolean getShowAds(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_OPENVPN_SHOW_ADS, true );
	}
	
	

	public final static String getExternalStorage(SharedPreferences sharedPreferences)
	{
        String defaultPath = new File( Environment.getExternalStorageDirectory(), "openvpn" ).getAbsolutePath();
        return sharedPreferences.getString( Preferences.KEY_OPENVPN_EXTERNAL_STORAGE, defaultPath );
	}

	public final static File getExternalStorageAsFile( SharedPreferences sharedPreferences)
	{
		return new File( getExternalStorage(sharedPreferences) );
	}
	

	public final static File getConfigDir(Context context, SharedPreferences sharedPreferences)
	{
		final File configDir;
		if ( getUseInternalStorage(sharedPreferences))
		{
			configDir = new File( context.getFilesDir(), "config.d" );
			if ( !configDir.exists() )
				configDir.mkdir();
		}
		else
		{
			configDir = getExternalStorageAsFile(sharedPreferences);
		}
		return configDir;
	}

	public final static String getPathToBinary(SharedPreferences sharedPreferences)
	{
		String path = sharedPreferences.getString( Preferences.KEY_OPENVPN_PATH_TO_BINARY, null );
		if ( path == null || "".equals( path ) )
		{
			path = null;
			for( File f : new File[]{ new File( "/system/xbin/openvpn" ), new File( "/system/bin/openvpn" ) } )
			{
				if ( f.exists() )
				{
					path = f.getAbsolutePath();
					break;
				}
			}
		}
		return path;
	}

	public static File getPathToBinaryAsFile(SharedPreferences sharedPreferences)
	{
		String path = getPathToBinary(sharedPreferences);
		return path == null ? null : new File( path );
	}

	public final static String getPathToSu(SharedPreferences sharedPreferences)
	{
		String path = sharedPreferences.getString( Preferences.KEY_OPENVPN_PATH_TO_SU, null );
		if ( path == null )
		{
			for( File f : new File[]{ new File( "/system/xbin/su" ), new File( "/system/bin/su" ), new File( "/sbin/su" ) } )
			{
				if ( f.exists() )
				{
					path = f.getAbsolutePath();
					break;
				}
			}
		}
		return path;
	}

	public static File getPathToSuAsFile(SharedPreferences sharedPreferences)
	{
		String path = getPathToSu(sharedPreferences);
		return path == null ? null : new File( path );
	}

	public final static String getSuArguments(SharedPreferences sharedPreferences)
	{
		return sharedPreferences.getString( Preferences.KEY_OPENVPN_SU_ARGUMENTS, "-s -x" );
	}

	public static void setMgmtPort(Context context, File configFile, int mgmtPort) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = sharedPreferences.edit();
		edit.putInt( KEY_CONFIG_MGMT_PORT(configFile), mgmtPort );
		edit.commit();
	}

	public final static int getMgmtPort(Context context, File configFile)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences.getInt( Preferences.KEY_CONFIG_MGMT_PORT(configFile), -1 );
	}

	public final static int getNotificationId(Context context, File configFile)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		int id = sharedPreferences.getInt( Preferences.KEY_CONFIG_NOTIFICATION_ID(configFile), -1 );
		if ( id == -1 ) {
			synchronized ( KEY_NEXT_NOTIFICATION_ID ) {
				id = sharedPreferences.getInt( KEY_NEXT_NOTIFICATION_ID, FIRST_CONFIG_ID);
				Editor edit = sharedPreferences.edit();
				edit.putInt( Preferences.KEY_CONFIG_NOTIFICATION_ID(configFile), id );
				edit.putInt( KEY_NEXT_NOTIFICATION_ID, id + 1);
				edit.commit();
			}
		}
		return id;
	}
	
	public final static void setDns1(Context context, File configFile, Integer dnsChange, String dns)
	{
		Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();		
		edit.putInt( Preferences.KEY_CONFIG_DNSCHANGE(configFile), dnsChange == null ? -1 : dnsChange.intValue() );
		edit.putString( Preferences.KEY_CONFIG_DNS1(configFile), dns );
		edit.commit();
	}
	public final static int getDnsChange(Context context, File configFile)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences.getInt( Preferences.KEY_CONFIG_DNSCHANGE(configFile), -1 );
	}
	public final static String getDns1(Context context, File configFile)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences.getString( Preferences.KEY_CONFIG_DNS1(configFile), "" );
	}

	public static String getVpnDns(Context context, File configFile) 
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences.getString( Preferences.KEY_VPN_DNS(configFile), "" );
	}
	public static boolean getVpnDnsEnabled(Context context, File configFile) 
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences.getBoolean( Preferences.KEY_VPN_DNS_ENABLE(configFile), false );
	}

	public static int getScriptSecurityLevel(Context context, File configFile) 
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt( sharedPreferences.getString( Preferences.KEY_SCRIPT_SECURITY_LEVEL(configFile), "1" ) );
	}

	public static boolean getLogStdoutEnable(Context context, File configFile) 
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences.getBoolean( Preferences.KEY_CONFIG_LOG_STDOUT_ENABLE(configFile), false );
	}

	public static File logFileFor(File config)
	{
		return new File( config.getParentFile(), FilenameUtils.getBaseName( config.getAbsolutePath() ) + ".log" );
	}

    public static boolean getIntendedState(Context context, File configFile)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean( Preferences.KEY_CONFIG_INTENDED_STATE( configFile ), false );
    }

    public static void clearPassphraseOrCredentials(Context context, File configFile)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );
        sharedPreferences.edit()
                .remove( Preferences.KEY_CONFIG_USERNAME( configFile ) )
                .remove( Preferences.KEY_CONFIG_PASSWORD( configFile ) )
                .remove( Preferences.KEY_CONFIG_PASSPHRASE( configFile) )
                .commit();
    }
    public static void setCredentials(Context context, File configFile, String username, String password)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );
        sharedPreferences.edit()
                .putString( Preferences.KEY_CONFIG_USERNAME( configFile ), username )
                .putString( Preferences.KEY_CONFIG_PASSWORD( configFile ), password )
                .remove( Preferences.KEY_CONFIG_PASSPHRASE( configFile ) )
                .commit();
    }

    public static boolean hasCredentials(Context context, File configFile)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.contains( Preferences.KEY_CONFIG_USERNAME( configFile ) ) && sharedPreferences.contains( Preferences.KEY_CONFIG_PASSWORD( configFile ) );
    }

    public static String getUsername(Context context, File configFile)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString( Preferences.KEY_CONFIG_USERNAME( configFile ), "" );
    }

    public static String getPassword(Context context, File configFile)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString( Preferences.KEY_CONFIG_PASSWORD( configFile ), "" );
    }

    public static void setPassphrase(Context context, File configFile, String passphrase)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );
        sharedPreferences.edit()
                .remove( Preferences.KEY_CONFIG_USERNAME( configFile ) )
                .remove( Preferences.KEY_CONFIG_PASSWORD( configFile ) )
                .putString( Preferences.KEY_CONFIG_PASSPHRASE( configFile ), passphrase )
                .commit();
    }

    public static boolean hasPassphrase(Context context, File configFile)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.contains( Preferences.KEY_CONFIG_PASSPHRASE( configFile ) );
    }

    public static String getPassphrase(Context context, File configFile)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString( Preferences.KEY_CONFIG_PASSPHRASE( configFile ), "" );
    }


    public final static ArrayList<File> listExistingConfigs(Context context)
	{
        ArrayList<File> configs = listKnownConfigs( context );
        for( Iterator<File> it = configs.iterator(); it.hasNext(); )
            if ( !it.next().exists() )
                it.remove();
        return configs;
    }
    public final static ArrayList<File> listKnownConfigs(Context context)
	{
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( context );
        HashSet<File> configs = new HashSet<File>();
        for (String key : preferences.getAll().keySet())
            if (isConfigKey( key ) )
                configs.add( configOf( key ) );
        configs.addAll( configs( getConfigDir( context, preferences ) ) );
        ArrayList<File> sortedConfigs = new ArrayList<File>( configs );
        Collections.sort( sortedConfigs );
        return sortedConfigs;
    }
	
	public final static ArrayList<File> configs(File configDir)
	{
		ArrayList<File> configFiles = new ArrayList<File>();
		if ( configDir != null )
		{
			ArrayList<File> configDirs = new ArrayList<File>();
			configDirs.add( configDir );
			configDirs.addAll( Arrays.asList( Util.listFiles( configDir, new Util.IsDirectoryFilter() ) ) );
			
			for ( File dir : configDirs )
				configFiles.addAll( Arrays.asList( Util.listFiles( dir, new Util.FileExtensionFilter(".conf",".ovpn") ) ) );
		}
		
		return configFiles;
	}
	
	public static boolean getFixHtcRoutes(Context context) 
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);	
		return sharedPreferences.getBoolean( Preferences.KEY_FIX_HTC_ROUTES, false );
	}

	public static String getConfigName( Context context, File configFile)
	{
		if ( configFile == null )
			return "null";

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		{
			String name = sharedPreferences.getString( Preferences.KEY_CONFIG_NAME( configFile ), null );
			if ( name != null && !"".equals( name ) )
				return name;
		}
		
		File configDir = getConfigDir( context, sharedPreferences );
		if ( configDir == null )
			return configFile.getAbsolutePath();

		String name;
		String configDirName = configDir.getAbsolutePath();
		String configFileName = configFile.getAbsolutePath();
		if ( configFileName.startsWith( configDirName ) )
		{
			name = configFileName.substring( configDirName.length() );
			if ( name.startsWith( "/" ) )
				name = name.substring( 1 );
		}
		else
		{
			name = configFileName;
		}
		return name;
	}

    public final static boolean applicationWasUpdated(Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int applicationVersionCode = Util.applicationVersionCode( context );
        int storedVersionCode = sharedPreferences.getInt( KEY_OPENVPN_VERSION_CODE, -1 );
        final boolean wasUpdated = applicationVersionCode > storedVersionCode;
        if ( wasUpdated )
            sharedPreferences.edit().putInt( KEY_OPENVPN_VERSION_CODE, applicationVersionCode ).commit();
        return wasUpdated;
    }
}
