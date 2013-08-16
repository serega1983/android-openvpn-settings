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
package de.schaeuffelhut.android.openvpn.shared.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import android.content.pm.ApplicationInfo;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;
import de.schaeuffelhut.android.openvpn.shared.util.apilevel.ApiLevel;

public class Shell extends Thread
{
	private final static boolean LOCAL_LOGD = true;
	
	public final static boolean SU = true;
	public final static boolean SH = false;
	
	
	private final String mSh;
	private final String mSu;
	
	private final String mTag;
	private final String mCmd;
    @Deprecated //TODO: Used to set LD_LIBRARY_PATH. remove once openvpn-14 does not depend on it anymore
    private final ApplicationInfo mApplicationInfo;
	private final boolean mRoot;

	private Process mShellProcess;
	private LoggerThread mStdoutLogger;
	private LoggerThread mStderrLogger;
    private boolean mDoBugSenseExec = true;

    public Shell(String tag, String cmd, boolean root)
    {
        this( tag, cmd, null, root );
    }

    @Deprecated
    //TODO: Setting the LD_LIBRARY_PATH is fragile. Bionic support for LD_LIBRARY_PATH was broken once before. Also the location for app local libraries might change with devices or future android versions.
    public Shell(String tag, String cmd, ApplicationInfo applicationInfo, boolean root)
	{
		super( tag + "-stdin" );
		mTag = tag;
		mCmd = cmd;
        mApplicationInfo = applicationInfo;
        mRoot = root;
		
		mSh = findBinary( "sh" );
		mSu = findBinary( "su" );
	}

    /**
     * Set to false if exec failures should not be reported via BugSense.
     *
     * @param bugSenseExec {@code true} if exec failures should be reported via BugSense, {@code false} otherwise.
     */
    protected void setDoBugSenseExec(boolean bugSenseExec)
    {
        this.mDoBugSenseExec = bugSenseExec;
    }

    public static String findBinary(String executable) {
		for (String bin : new String[]{"/system/bin/", "/system/xbin/"}) {
			String path = bin+executable;
			if ( new File( path ).exists() )
				return path;
		}
		return executable;
		
		// causes Force Close on unrooted phones
//		throw new RuntimeException( "executable not found: " + executable );
	}

	public final void run()
	{
		onBeforeExecute();
							
		forkShell();
		
		if ( !hasShellProcess() )
			return;

		startStdoutThread();
		startStderrThread();

		executeCommand();
		
		final int exitCode = awaitTermination();

		onCmdTerminated( exitCode );
	}
	
	protected void onBeforeExecute() {
		//overwrite if desired
	}

	private void forkShell()
	{
		final ProcessBuilder shellBuilder = new ProcessBuilder( mRoot ? mSu : mSh );

        setUpLdLibraryPath( shellBuilder );

		if ( LOCAL_LOGD )
			Log.d( mTag, String.format( 
					"invoking external process: %s", 
					Util.join( shellBuilder.command(), ' ' )
			));

		try
		{
			mShellProcess = shellBuilder.start();
		}
		catch (IOException e)
		{
			// Something went fundamentally wrong as either
			// /system/bin/sh or /system/bin/su could not be
			// found. This is a border line case which really
			// should have been already handled before run()
			// was called!
			
			Log.e( mTag, String.format( 
					"invoking external process: %s", 
					Util.join( shellBuilder.command(), ' ' ),
					e
			));
            if (mDoBugSenseExec)
            {
                BugSenseHandler.sendExceptionMessage(
                        "DEBUG",
                        String.format(
                                "invoking external process: %s",
                                Util.join( shellBuilder.command(), ' ' )
                        ),
                        e
                );
            }

			onExecuteFailed( e );
		}
	}

    @Deprecated //TODO: remove once openvpn4 does not depend on it anymore
    private void setUpLdLibraryPath(ProcessBuilder shellBuilder)
    {
        if ( mApplicationInfo == null )
            return;
        ApiLevel.get().addNativeLibDirToLdLibraryPath( shellBuilder, mApplicationInfo );
    }

    protected void onExecuteFailed(IOException e) {
		//overwrite if desired
		// if this method is called either su or sh was not found or may not be executed.
	}

	private boolean hasShellProcess()
	{
		return mShellProcess != null;
	}

	private void startStdoutThread()
	{
		mStdoutLogger = new LoggerThread( mTag+"-stdout", mShellProcess.getInputStream(), true ){
			@Override
			protected void onLogLine(String line) {
				onStdout(line);
			}
		};
		mStdoutLogger.start();
	}

	/**
	 * Shell.onStdout() is called from a separate thread reading the shells
	 * STDOUT channel line by line.
	 * 
	 * @param line A line read from the shells STDOUT channel.
	 */
	protected void onStdout(String line) {
		//overwrite if desired
	}

	private void startStderrThread()
	{
		mStderrLogger = new LoggerThread( mTag+"-stderr", mShellProcess.getErrorStream(), true ){
			@Override
			protected void onLogLine(String line) {
				onStderr(line);
			}
		};
		mStderrLogger.start();
	}

	/**
	 * Shell.onStderr() is called from a separate thread reading the shells
	 * STDERR channel line by line.
	 * 
	 * @param line A line read from the shells STDERR channel.
	 */
	protected void onStderr(String line) {
		//overwrite if desired
	}

	private void executeCommand()
	{
		if ( LOCAL_LOGD )
			Log.d( mTag, String.format( "invoking command line: %s", mCmd ) );
	
		final PrintStream stdin = new PrintStream( mShellProcess.getOutputStream() );
		try
		{
			stdin.println( mCmd );
			//TODO: check the streams error state

			stdin.flush();
			//TODO: check the streams error state
		
			onCmdStarted();
		}
		finally
		{
			Util.closeQuietly( stdin );
		}
	}

	/**
	 * Shell.onCmdStarted() is called after the command has been
	 * flushed to the forked shells STDIN.
	 */
	protected void onCmdStarted() {
		//overwrite if desired
	}

	private int awaitTermination()
	{
		try { joinLoggers(); } catch (InterruptedException e) {Log.e( mTag, "joining loggers", e);}
		final int exitCode = waitForQuietly();
		return exitCode;
	}

	private final void joinLoggers() throws InterruptedException
	{
		mStdoutLogger.join();
		mStderrLogger.join();
	}
	
	private final int waitForQuietly()
	{
		return Util.waitForQuietly( mShellProcess );
	}

	/**
	 * Shell.onCmdTerminated is called when the forked process
	 * has returned and the threads reading STDOUT and STDERR
	 * are finished.
	 *  
	 * @param exitCode The exit code returned by the forked process.
	 */
	protected void onCmdTerminated(int exitCode){
		//overwrite if desired
	}
}
