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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CyclicBarrier;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * @author M.Sc. Friedrich Schäuffelhut
 *
 */
final class XXControlShell extends Service
{
	/*
	 * Service API
	 */
	
	public XXControlShell() {
	}
	
	final class ServiceBinder extends Binder {
		XXControlShell getService() {
            return XXControlShell.this;
        }
	}

	final IBinder mBinder = new ServiceBinder();
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		startup();
	}

	
	@Override
	public void onDestroy() {
		super.onDestroy();
		shutdown();
	}

	
	/*
	 * ControlShell
	 */
	
	private final Object cmdChannelLock = new Object();
	private Thread mControlShell;
	private File mComDir;
	private File mBinDir;
	private PrintStream stdout;
	private final HashMap<String, OpenVPNProcess> registry = new HashMap<String, OpenVPNProcess>();
	private File binOpenvpn;
	private File binLibcrypto;
	private File binLiblzo;
	private CyclicBarrier startupBarrier;
	static class OpenVPNProcess
	{
		int pid;
		final File stdout;
		final String config;
		final LoggerThread logger;
		final DaemonMonitor monitor;
		OpenVPNProcess(int pid, File stdout, String config, LoggerThread logger, DaemonMonitor monitor)
		{
			super();
			this.pid = pid;
			this.stdout = stdout;
			this.config = config;
			this.logger = logger;
			this.monitor = monitor;
		}
	}
	
	void startup()
	{
		if ( mControlShell == null || !mControlShell.isAlive() )
		{
			Log.i("OpenVPN", "control.shell startup");

			mComDir = new File( getApplicationContext().getFilesDir(), "com.d" );
			if ( !mComDir.exists() )
				mComDir.mkdirs();
			Log.d( "OpenVPN", "control.shell OPENVPN_COM_DIR=" + mComDir );

			mBinDir = new File( getApplicationContext().getFilesDir(), "bin" );
			if ( !mBinDir.exists() )
				mBinDir.mkdirs();
			Log.d( "OpenVPN", "control.shell OPENVPN_BIN_DIR=" + mBinDir );

			Log.d( "OpenVPN", "installing binaries" );
			binOpenvpn = new File( mBinDir, "openvpn2.1");
			binLiblzo = new File( mBinDir, "liblzo.so");
			binLibcrypto = new File( mBinDir, "libcrypto.so");
			copy("bin/openvpn2.1", binOpenvpn );
			copy("bin/liblzo.so", binLiblzo );
			copy("bin/libcrypto.so", binLibcrypto );

			startupBarrier = new CyclicBarrier( 2 );
			mControlShell = new ControlShellThread();
			mControlShell.start();
			
			// wait until environment in sh process is set up
			try {
				startupBarrier.await();
			} catch (Exception e) {
				Log.e( "OpenVPN", "exception while barrier.await()", e );
			}
			
			Log.d( "OpenVPN", "making binaries executable" );
			setXBit(binOpenvpn);
			setXBit(binLiblzo);
			setXBit(binLibcrypto);
			
			Log.d( "OpenVPN", "activating tun kernel module" );
			synchronized ( cmdChannelLock )
			{
				stdout.println( "modprobe tun" );
				if ( ! new File( "/dev/net" ).exists() )
					stdout.println( "mkdir /dev/net" );
				if ( ! new File( "/dev/net/tun" ).exists() )
					stdout.println( "mknod /dev/net/tun c 10 200" );
			}
			
			// start enabled configs
			File[] configFiles = new File(
					getApplicationContext().getFilesDir(),
					"config.d"
			).listFiles(new Util.FileExtensionFilter(".conf"));
			for (int i = 0; configFiles != null && i < configFiles.length; i++)
			{
				final String configFileName = configFiles[i].getName();
				daemonStart( configFileName );
			}
		}
		else
		{
			Log.w("OpenVPN", "control.shell startup, already running");
		}
	}

	void shutdown()
	{
		Log.i("OpenVPN", "control.shell shutown");

		if ( stdout != null ) synchronized ( cmdChannelLock )
		{
			// start enabled configs
			File[] configFiles = new File(
					getApplicationContext().getFilesDir(),
					"config.d"
			).listFiles(new Util.FileExtensionFilter(".conf"));
			for (int i = 0; configFiles != null && i < configFiles.length; i++)
			{
				final String configFileName = configFiles[i].getName();
				if ( isDaemonStarted( configFileName ) )
						daemonStop( configFileName );
			}

			//TODO: kill all remaining daemons
			// wait for 
			stdout.println( "echo *OPENVPN-CONTROL-SHELL* shutdown" );
			stdout.println( "exit" );
			stdout.flush();
		}
	}

	private void setXBit(File binary)
	{
		if ( stdout != null ) synchronized ( cmdChannelLock )
		{
			stdout.println( "$CHMOD 555 " + binary.getAbsolutePath() );
			stdout.flush();
		}
	}

	void daemonStart(String config)
	{
		//TODO make a parameter
//		String configDir = "/data/openvpn";
		String configDir = new File( getApplicationContext().getFilesDir(), "config.d" ).getPath();
		if ( registry.containsKey( config ) )
		{
			Log.i( "OpenVPN", config + " is already running" );
		}
		else if ( stdout != null ) synchronized ( cmdChannelLock )
		{
//			final String configSh = Util.makeShellCompatible( config );
//			stdout.println( String.format( "OPENVPN_STDOUT_%s=$OPENVPN_COM_DIR/stdout-%s", configSh, configSh ) );
//			stdout.println( String.format( "$MKNOD $OPENVPN_STDOUT_%s p", configSh ) );
//			stdout.println( String.format( "$OPENVPN --cd %s --config %s > $OPENVPN_STDOUT_%s &", configDir, config, configSh ) );
//			stdout.println( String.format( "OPENVPN_PID_%s=$!", configSh ) );
//			stdout.println( String.format( "echo *OPENVPN-CONTROL-SHELL* start $OPENVPN_PID_%s $OPENVPN_STDOUT_%s %s", configSh, configSh, config ) );
//			stdout.flush();
			DaemonMonitor daemonMonitor = new DaemonMonitor(
					this,
					binOpenvpn,
					new File( new File( getApplicationContext().getFilesDir(), "config.d" ), config ),
					new File( getApplicationContext().getFilesDir(), "com.d" )
			);
			daemonMonitor.start();
			register(new OpenVPNProcess(
					0,null,config,null,daemonMonitor
			));
		}
	}

	void daemonRestart(String config)
	{
		if ( !registry.containsKey( config ) )
		{
			Log.i( "OpenVPN", config + " is not running" );
		}
		else if ( stdout != null ) synchronized ( cmdChannelLock )
		{
			OpenVPNProcess p = registry.get( config );
			stdout.println( "kill -1 " + p.pid );
			//stdout.println( String.format( "echo *OPENVPN-CONTROL-SHELL* restart $OPENVPN_PID_%s %s", makeShellCompatible( config ), config ) );
			stdout.flush();
		}
	}

	void daemonRestart()
	{
		if ( stdout != null ) synchronized ( cmdChannelLock )
		{
			for(String config : registry.keySet() )
				daemonRestart( config );
		}
	}

	void daemonStop(String config)
	{
		if ( !registry.containsKey( config ) )
		{
			Log.i( "OpenVPN", config + " is not running" );
		}
		else if ( stdout != null ) synchronized ( cmdChannelLock )
		{
			OpenVPNProcess p = registry.get( config );
			stdout.println( "kill " + p.pid );
			//stdout.println( String.format( "echo *OPENVPN-CONTROL-SHELL* stop $OPENVPN_PID_%s %s", makeShellCompatible( config ), config ) );
			stdout.flush();
		}
	}

	void daemonStop()
	{
		if ( stdout != null ) synchronized ( cmdChannelLock )
		{
			for ( String config : registry.keySet() )
				daemonStop( config );
		}
	}

	boolean isDaemonStarted(String config)
	{
		synchronized ( cmdChannelLock )
		{
			return registry.containsKey( config );
		}
	}
	
	public boolean hasDaemonsStarted()
	{
		synchronized ( cmdChannelLock )
		{
			return !registry.isEmpty();
		}
	}

	public LoggerThread getDaemonLogger(String config)
	{
		synchronized ( cmdChannelLock )
		{
			OpenVPNProcess openVPNProcess = registry.get(config);
 			return openVPNProcess == null ? null : openVPNProcess.logger;
		}
	}

	private void register(OpenVPNProcess openVPNProcess)
	{
		synchronized ( cmdChannelLock )
		{
			registry.put( 
					openVPNProcess.config,
					openVPNProcess
			);
		}
		Log.i("OpenVPN", String.format( "registered: %d, %s, %s",
				openVPNProcess.pid,
				openVPNProcess.stdout, 
				openVPNProcess.config
		) );
	}

	private void unregister(String config)
	{
		synchronized ( cmdChannelLock )
		{
			try
			{
				String configSh = Util.makeShellCompatible( config );
				OpenVPNProcess p = registry.remove( config );
				stdout.println( "wait " + p.pid );
				stdout.println( String.format( "unset OPENVPN_PID_%s", configSh ) );
				stdout.println( String.format( "unset OPENVPN_STDOUT_%s", configSh ) );
				stdout.flush();
				//TODO make sure child is dead
				//TODO make sure logger is dead
				Log.d("OpenVPN", "unregistered " + p.config );
			}
			finally
			{
				sendBroadcast( Intents.daemonStateChanged(config, Intents.DAEMON_STATE_DISABLED ) );
			}
		}
	}

	private final class ControlShellThread extends Thread
	{
		public void run()
		{
			final ProcessBuilder pb = new ProcessBuilder( "/system/bin/sh", "-s", "-x", "-b" );

			Log.d( "OpenVPN", String.format( 
					"invoking external process: %s", 
					Util.join( pb.command(), ' ' )
			));

			try
			{
				Process p = pb.start();
				loop(p);
			}
			catch (Exception e)
			{
				Log.e( "OpenVPN", "unexpected shutdown", e );
			}
			finally
			{
				Log.i( "OpenVPN", "control-shell terminated" );
			}
		}

		private void loop(Process p)
		{
			Thread stderrThread = new LoggerThread( "stderr", p.getErrorStream(), true );
			stderrThread.start();
			
			LineNumberReader stdin = new LineNumberReader(
					new InputStreamReader( p.getInputStream() ),
					256
			);
			
			synchronized ( cmdChannelLock )
			{
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

				stdout.println( String.format( "export OPENVPN_COM_DIR='%s'", mComDir.getAbsolutePath() ) );
				stdout.println( String.format( "export OPENVPN_BIN_DIR='%s'", mBinDir.getAbsolutePath() ) );
				
				stdout.println( String.format( "export MKDIR=/system/bin/mkdir" ) );
				stdout.println( String.format( "export MODPROBE=/system/bin/modprobe" ) );
				stdout.println( String.format( "export CHMOD=/system/bin/chmod" ) );
				stdout.println( String.format( "export MKNOD=/system/xbin/bb/mknod" ) );
				stdout.println( String.format( "export OPENVPN='%s'", binOpenvpn.getAbsolutePath() ) );
//				stdout.println( String.format( "export OPENVPN=/data/openvpn/openvpn2.1" ) );

				stdout.println( "cd $OPENVPN_BIN_DIR" );
				stdout.println( "exec /system/bin/su -s -x -b" );

				
				stdout.flush();
			}

			// block startup() until environment in sh process is set up
			try {
				startupBarrier.await();
			} catch (Exception e) {
				Log.e( "OpenVPN", "exception while barrier.await()", e );
			}
			
			Log.i("OpenVPN", "controll-shell ready" );
			
			try {
				String line;
				while( ( line = stdin.readLine() ) != null )
				{
					Log.d( "OpenVPN", "control-shell received" + ": " + line );
					if ( line.startsWith( "*OPENVPN-CONTROL-SHELL*" ) )
					{
						String[] cmd = line.split( " " );
						handle(cmd);
					}
				}
			}
			catch( Exception e )
			{
				Log.e( "OpenVPN", "control-shell: unexpected shutdown", e );
			}
			finally
			{
				synchronized ( cmdChannelLock )
				{
					stdout = null;
				}
				Util.closeQuietly( stdin );
				Util.closeQuietly( stdout );
			}
			
			waitFor: do
			{
				try {
					int exitCode = p.waitFor();
					Log.i( "OpenVPN", "OpenVPN control shell terminated with exit code " + exitCode );
				}
				catch (InterruptedException e)
				{
					continue waitFor;
				}
			} while ( false );
			
			// make shure all children are dead
		}

		private void handle(String[] cmd)
		{
			if ( "start".equals( cmd[1] ) )
			{
				final int pid = Integer.parseInt( cmd[2] );
				final File daemon_stdout = new File( cmd[3] );
				final String config = cmd[4];
				
				sendBroadcast( Intents.daemonStateChanged(config, Intents.DAEMON_STATE_ENABLED) );
				
				LoggerThread logger = null;
				try
				{
					logger = new LoggerThread( config, new FileInputStream( daemon_stdout ), true ){
						protected void onTerminate(){
							unregister( config );
						}
					};
					logger.start();
				}
				catch (FileNotFoundException e)
				{
					Log.e( "OpenVPN", "starting logger for " + config, e );
				}
				register(new OpenVPNProcess(
						pid,
						daemon_stdout,
						config,
						logger,
						null
				));
			}
//			else if ( "restart".equals( cmd[1] ) )
//			{
//				Log.i("OpenVPN", "control-shell: restart" );
//			}
//			else if ( "stop".equals( cmd[1] ) )
//			{
//				int pid = Integer.parseInt( cmd[2] );
//				String config = cmd[3];
//				Log.i("OpenVPN", String.format( "control-shell: %d, %s", pid, config ) );
//				//TODO should be unregistered by is LoggerThread
//				//TODO make shure LoggerThread dies
//			}
//			else if ( "shutdown".equals( cmd[1] ) )
//			{
//				Log.i("OpenVPN", "control-shell: shutdown" );
//			}
			else
			{
				Log.e( "OpenVPN", "control-shell: unknown command: " + cmd[1] );
			}
		}

	}
	
	static class LoggerThread
	extends Thread
	{
		final InputStream is;
		final boolean closeInput;
		final String channelName;

		public LoggerThread(String channelName, InputStream is, boolean closeInput)
		{
			this.is = is;
			this.closeInput = closeInput;
			this.channelName = channelName;
			setName( "OpenVPN-control-shell-" + channelName );
		}

		private ArrayList<String> buffer = new ArrayList<String>(100);
		
		String[] getBuffer() {
			synchronized (buffer) {
				while ( buffer.isEmpty() )
				{
					if ( !isAlive() )
						return null;
					try { buffer.wait(); } catch (InterruptedException e) {}
				}

				String[] log = buffer.toArray( new String[buffer.size()] );
				buffer.clear();
				return log;
			}
		}
		
		@Override
		public void run()
		{
			try
			{
				LineNumberReader lnr = new LineNumberReader(
						new InputStreamReader( is ),
						256
				);
				String line;
				while( null != ( line = lnr.readLine() ) )
				{
					Log.d( "OpenVPN", channelName + ": " + line );
					synchronized (buffer) {
						if ( buffer.size() > 15 )
							buffer.remove(0);
						buffer.add( line );
						buffer.notifyAll();
					}
				}
			}
			catch (Exception e)
			{
				Log.e( "OpenVPN", "logging "+channelName, e );
			}
			finally
			{
				if ( closeInput )
					Util.closeQuietly( is );
				Log.i( "OpenVPN", "LoggerThread terminated: " + channelName );
				onTerminate();
				synchronized (buffer) {
					buffer.notifyAll();
				}
			}
		}

		protected void onTerminate() {
			//overwrite if desired
		}
	}	

	private void copy(String asset, File binary)
	{
		if ( binary.exists() )
		{
			Log.d( "OpenVPN", String.format( "asset %s already installed at %s", asset, binary ) );
		}
		else
		{
			Log.d( "OpenVPN", String.format( "copy asset %s to %s", asset, binary ) );
			InputStream is = null;
			OutputStream os = null;
			try
			{
				is = getAssets().open( asset );
				os = new FileOutputStream( binary );
				byte[] buffer = new byte[1024];
				int count;
				while( ( count = is.read( buffer ) ) != -1 )
					os.write( buffer, 0, count );
			}
			catch (IOException e)
			{
				Log.e( "OpenVPN", "copy", e );
			}
			finally
			{
				Util.closeQuietly( is );
				Util.closeQuietly( os );
			}
		}
	}
}