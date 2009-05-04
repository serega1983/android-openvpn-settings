/**
 * 
 */
package de.schaeuffelhut.android.openvpn;

import java.io.File;
import java.io.PrintStream;

import android.content.res.AssetManager;
import android.util.Log;

class Installer
{
	private final static String TAG = "OpenVPN-Installer";

	final AssetManager assetManager;
	final File binOpenvpn;
	final File binLibcrypto;
	final File binLiblzo;


	Installer(AssetManager assetManager, File binOpenvpn, File binLibcrypto, File binLiblzo) {
		this.assetManager = assetManager;
		this.binOpenvpn = binOpenvpn;
		this.binLibcrypto = binLibcrypto;
		this.binLiblzo = binLiblzo;
	}

	void installOpenVPN()
	{
		Util.copyAsset(TAG, assetManager, "bin/openvpn2.1", binOpenvpn);
		Util.copyAsset(TAG, assetManager, "bin/libcrypto.so", binLibcrypto);
		Util.copyAsset(TAG, assetManager, "bin/liblzo.so", binLiblzo);

		final ProcessBuilder pb = new ProcessBuilder( "/system/bin/su", "-s", "-x", "-b" );

		Log.d( TAG, String.format( 
				"invoking external process: %s", 
				Util.join( pb.command(), ' ' )
		));

		Process p = null;
		try
		{
			p = pb.start();
			shell(p);
		}
		catch (Exception e)
		{
			Log.e( TAG, "unexpected shutdown", e );
		}
		finally
		{
			int exitCode = Util.waitForQuietly( p );
			Log.i( TAG, "Shell terminated with exit code " + exitCode );
		}
	}

	private void shell(Process p)
	{
		Thread stdoutThread = new LoggerThread( TAG + "-shell-stdout", p.getInputStream(), true );
		stdoutThread.start();
		
		Thread stderrThread = new LoggerThread( TAG + "-shell-stderr", p.getErrorStream(), true );
		stderrThread.start();

		PrintStream stdout = null;
		try {
			stdout = new PrintStream( p.getOutputStream() );
			stdout.println( "export ANDROID_ASSETS='/system/app'" );
			stdout.println( "export ANDROID_BOOTLOGO='1'"  );
			stdout.println( "export ANDROID_DATA='/data'" );
			stdout.println( "export ANDROID_PROPERTY_WORKSPACE='9,32768'" );
			stdout.println( "export ANDROID_ROOT='/system'" );
			stdout.println( "export BOOTCLASSPATH='/system/framework/core.jar:/system/framework/ext.jar:/system/framework/framework.jar:/system/framework/android.policy.jar:/system/framework/services.jar'" );
			stdout.println( "export EXTERNAL_STORAGE='/sdcard'" );
			stdout.println( "export PATH='/usr/bin:/usr/sbin:/bin:/sbin:/system/sbin:/system/bin:/system/xbin:/system/xbin/bb:/data/local/bin'" );
			stdout.println( "export ANDROID_ROOT='/system'" );

			stdout.println( String.format( "export CHMOD=/system/bin/chmod" ) );
			stdout.println( String.format( "export MODPROBE=/system/xbin/bb/modprobe" ) );
			stdout.println( String.format( "export MKDIR=/system/bin/mkdir" ) );
			stdout.println( String.format( "export MKNOD=/system/xbin/bb/mknod" ) );

//			stdout.println( "exec /system/bin/su -s -x -b" );

			Log.d( TAG, "making binaries executable" );
			stdout.println( "$CHMOD 555 " + binOpenvpn.getAbsolutePath() );
			stdout.println( "$CHMOD 555 " + binLiblzo.getAbsolutePath() );
			stdout.println( "$CHMOD 555 " + binLibcrypto.getAbsolutePath() );

			Log.d( TAG, "activating tun kernel module" );
			stdout.println( "$MODPROBE tun" );
			if ( ! new File( "/dev/net" ).exists() )
				stdout.println( "$MKDIR /dev/net" );
			if ( ! new File( "/dev/net/tun" ).exists() )
				stdout.println( "$MKNOD /dev/net/tun c 10 200" );

			stdout.println( "exit" );

			stdout.flush();

			Log.d( TAG, "joining stdout logging thread" );
			Util.joinQuietly( stdoutThread );

			Log.d( TAG, "joining stderr logging thread" );
			Util.joinQuietly( stderrThread );
		}
		finally
		{
			Util.closeQuietly( stdout );
		}
	}
}