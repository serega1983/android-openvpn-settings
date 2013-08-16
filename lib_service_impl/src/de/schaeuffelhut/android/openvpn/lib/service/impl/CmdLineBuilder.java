package de.schaeuffelhut.android.openvpn.lib.service.impl;

import android.content.Context;
import android.util.Log;
import de.schaeuffelhut.android.openvpn.shared.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates the commandline necessary to start an OpenVpn child process.
 *
 * @author Friedrich Schäuffelhut
 * @since 2013-03-12
 */
public abstract class CmdLineBuilder
{
    protected final File openvpn;
    private File config;
    private int scriptSecurity;
    private File mgmtSocket;

    public CmdLineBuilder(File openvpn)
    {
        if ( openvpn == null )
            throw new NullPointerException();

        this.openvpn = openvpn;
    }

    protected static File resolveBinary(Context context, String binary)
    {
        return new File( context.getDir( "bin", Context.MODE_PRIVATE ), binary );
    }

    public abstract boolean requiresRoot();

    public void setMgmtSocketLocation(File mgmtSocket)
    {
        if (mgmtSocket == null)
            throw new NullPointerException();
        this.mgmtSocket = mgmtSocket;
    }

    public void setConfigLocation(File config)
    {
        if (config == null)
            throw new NullPointerException();
        this.config = config;
    }

    public void setScriptSecurityLevel(int scriptSecurity)
    {
        this.scriptSecurity = scriptSecurity;
    }

    public List<String> buildArgv()
    {
        ArrayList<String> argv = new ArrayList<String>();

        argv.add( openvpn.getAbsolutePath() );

        argv.add( "--cd" );
        argv.add( config.getParentFile().getAbsolutePath() );
        argv.add( "--config" );
        argv.add( config.getName() );

        argv.add( "--script-security" );
        argv.add( Integer.toString( scriptSecurity ) );

        addIpRoute( argv );

        argv.add( "--management" );
        argv.add( mgmtSocket.getAbsolutePath() );
        argv.add( "unix" );

        argv.add( "--management-query-passwords" );

        argv.add( "--verb" );
        argv.add( "3" );

        return argv;
    }

    protected abstract void addIpRoute(ArrayList<String> argv);

    public String buildCmdLine()
    {
        StringBuilder sb = new StringBuilder();
        for (String arg : buildArgv())
        {
            if (sb.length() != 0)
                sb.append( " " );
            sb.append( Util.optionalShellEscape( arg) );
        }
        return sb.toString();
    }

    public boolean canExecute(String logTag)
    {
        if ( !openvpn.exists() ){
            Log.d( logTag, "openvpn not found: " + openvpn.getAbsolutePath() );
            return false;
        }

        if ( config == null ) {
            Log.d( logTag, "config file not set" );
            return false;
        }
        if ( !config.exists() ) {
            Log.d( logTag, "config file not found: " + config.getAbsolutePath() );
            return false;
        }

        if ( mgmtSocket == null ) {
            Log.d( logTag, "management socket not set" );
            return false;
        }
        if ( !mgmtSocket.getParentFile().exists() ) {
            Log.d( logTag, "parent directory of management socket not found: " + mgmtSocket.getParentFile().getAbsolutePath() );
            return false;
        }


        return true;
    }
}
