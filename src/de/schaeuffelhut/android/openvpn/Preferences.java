package de.schaeuffelhut.android.openvpn;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public final class Preferences {
	
	public static final String KEY_OPENVPN_SETTINGS_CATEGORY = "openvpn_settings_category";
	public static final String KEY_OPENVPN_ENABLED = "openvpn_enabled";
	public static final String KEY_OPENVPN_CONFIGURATIONS = "openvpn_configurations";
	public static final String KEY_OPENVPN_USE_INTERNAL_STORAGE = "openvpn_use_internal_storage";
	public static final String KEY_OPENVPN_EXTERNAL_STORAGE = "openvpn_external_storage";
	public static final String KEY_OPENVPN_PATH_TO_BINARY = "openvpn_path_to_binary";
	public static final String KEY_OPENVPN_PATH_TO_SU = "openvpn_path_to_su";
	public static final String KEY_OPENVPN_SU_ARGUMENTS = "openvpn_su_arguments";
	public static final String KEY_OPENVPN_DO_MODPROBE_TUN = "openvpn_do_modprobe_tun";

	public final static String KEY_CONFIG(String config){
		return String.format( "%s[%s]", KEY_OPENVPN_CONFIGURATIONS, config );
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

	private Preferences() {
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

	public final static String getExternalStorage(SharedPreferences sharedPreferences)
	{
		return sharedPreferences.getString( Preferences.KEY_OPENVPN_EXTERNAL_STORAGE, "/sdcard/openvpn" );
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

	public final static boolean getDoModprobeTun(SharedPreferences sharedPreferences) {
		return sharedPreferences.getBoolean( Preferences.KEY_OPENVPN_DO_MODPROBE_TUN, false);
	}
}
