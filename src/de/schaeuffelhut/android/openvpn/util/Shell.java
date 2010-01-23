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
package de.schaeuffelhut.android.openvpn.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import android.util.Log;


public abstract class Shell extends Thread
{
	private final static boolean LOCAL_LOGD = true;
	
	private final String mSu;
	
	private final String mTag;
	private Process mShellProcess;
	private LoggerThread mStdoutLogger;
	private LoggerThread mStderrLogger;
	private PrintStream stdout;

	public Shell(String tag)
	{
		super( tag + "-stdin" );
		mTag = tag;
		
		mSu = findBinary( "su" );
	}

	private static String findBinary(String executable) {
		for (String bin : new String[]{"/system/bin/", "/system/xbin/"}) {
			String path = bin+executable;
			if ( new File( path ).exists() )
				return path;
		}
		throw new RuntimeException( "executable not found: " + executable );
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
		// not needed in Androi 1.5 anymore?
//		stdout.println( "export ANDROID_ASSETS='/system/app'" );
//		stdout.println( "export ANDROID_BOOTLOGO='1'"  );
//		stdout.println( "export ANDROID_DATA='/data'" );
//		stdout.println( "export ANDROID_PROPERTY_WORKSPACE='10,32768'" );
//		stdout.println( "export ANDROID_ROOT='/system'" );
//		stdout.println( "export BOOTCLASSPATH='/system/framework/core.jar:/system/framework/ext.jar:/system/framework/framework.jar:/system/framework/android.policy.jar:/system/framework/services.jar:/system/framework/com.htc.framework.jar'" );
//		stdout.println( "export EXTERNAL_STORAGE='/sdcard'" );
//		stdout.println( "export PATH='/usr/bin:/usr/sbin:/bin:/sbin:/system/sbin:/system/bin:/system/xbin:/system/xbin/bb:/data/local/bin'" );
//		stdout.println( "export TERMINFO='/system/etc/terminfo'" );
	}

	public final void exec(String cmd)
	{
		if ( LOCAL_LOGD )
			Log.d( mTag, "exec " + cmd );

		stdout.print( "exec " );
		stdout.println( cmd );
		stdout.flush();
	}

	public final void su()
	{
		exec( mSu );
	}

	public final void cmd(String cmd)
	{
		if ( LOCAL_LOGD )
			Log.d( mTag, cmd );

		stdout.println( cmd );
		stdout.flush();
	}

	public final void exit()
	{
		cmd( "exit" );
	}
	
	protected abstract void onShellPrepared();

	protected void onStdout(String line) {
		//overwrite if desired
	}

	protected void onStderr(String line) {
		//overwrite if desired
	}

	protected void onShellTerminated(){}

	public final int waitForQuietly()
	{
		return Util.waitForQuietly( mShellProcess );
	}
}
