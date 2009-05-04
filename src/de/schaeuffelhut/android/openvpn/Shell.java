package de.schaeuffelhut.android.openvpn;

import java.io.IOException;
import java.io.PrintStream;

import android.util.Log;


abstract class Shell extends Thread
{
	private final static boolean LOCAL_LOGD = true;
	
	private final String mTag;
	private Process mShellProcess;
	private LoggerThread mStdoutLogger;
	private LoggerThread mStderrLogger;
	private PrintStream stdout;

	public Shell(String tag)
	{
		super( tag + "-stdin" );
		mTag = tag;
	}

	public final void run()
	{
		final ProcessBuilder pb = new ProcessBuilder( "/system/bin/sh", "-s", "-x" );

		if ( LOCAL_LOGD )
			Log.d( mTag, String.format( 
					"invoking external process: %s", 
					Util.join( pb.command(), ' ' )
			));

		try {
			mShellProcess = pb.start();
			
			mStdoutLogger = new LoggerThread( mTag+"-stdout", mShellProcess.getInputStream(), true ){
				@Override
				protected void onLogLine(String line) {
					onStdout(line);
				}
			};
			mStdoutLogger.start();

			mStderrLogger = new LoggerThread( mTag+"-stderr", mShellProcess.getErrorStream(), true ){
				@Override
				protected void onLogLine(String line) {
					onStderr(line);
				}
			};
			mStderrLogger.start();
			
			stdout = new PrintStream( mShellProcess.getOutputStream() );

			initEnvironment();
			
			onShellPrepared();
		}
		catch (IOException e)
		{
			Log.e( mTag, String.format( 
					"invoking external process: %s", 
					Util.join( pb.command(), ' ' ),
					e
			));
		}
		finally
		{
			Util.closeQuietly( stdout );
			onShellTerminated();
		}
	}

	private void initEnvironment()
	{
		stdout.println( "export ANDROID_ASSETS='/system/app'" );
		stdout.println( "export ANDROID_BOOTLOGO='1'"  );
		stdout.println( "export ANDROID_DATA='/data'" );
		stdout.println( "export ANDROID_PROPERTY_WORKSPACE='9,32768'" );
		stdout.println( "export ANDROID_ROOT='/system'" );
		stdout.println( "export BOOTCLASSPATH='/system/framework/core.jar:/system/framework/ext.jar:/system/framework/framework.jar:/system/framework/android.policy.jar:/system/framework/services.jar'" );
		stdout.println( "export EXTERNAL_STORAGE='/sdcard'" );
		stdout.println( "export PATH='/usr/bin:/usr/sbin:/bin:/sbin:/system/sbin:/system/bin:/system/xbin:/system/xbin/bb:/data/local/bin'" );
		stdout.println( "export ANDROID_ROOT='/system'" );
	}

	void exec(String cmd)
	{
		stdout.print( "exec " );
		stdout.println( cmd );
		stdout.flush();
	}

	void su()
	{
		exec( "/system/bin/su -s -x" );
	}

	void cmd(String cmd)
	{
		stdout.println( cmd );
		stdout.flush();
	}

	void exit()
	{
		cmd( "exit" );
	}
	
	abstract void onShellPrepared();

	protected void onStdout(String line) {
		//overwrite if desired
	}

	protected void onStderr(String line) {
		//overwrite if desired
	}

	void onShellTerminated(){}

	int waitForQuietly()
	{
		return Util.waitForQuietly( mShellProcess );
	}
}
