package de.schaeuffelhut.android.openvpn.lib.service.impl;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-03-12
 */
public class CmdLineBuilder4 extends CmdLineBuilder
{
    private final File ip;

    public CmdLineBuilder4(Context context)
    {
        this( resolveBinary( context, "openvpn" ), resolveBinary( context, "ip" ) );
    }

    CmdLineBuilder4(File openvpn, File ip)
    {
        super( openvpn );

        if ( ip == null )
            throw new NullPointerException();

        this.ip = ip;
    }

    @Override
    public boolean requiresRoot()
    {
        return true;
    }

    @Override
    protected void addIpRoute(ArrayList<String> argv)
    {
        argv.add( "--iproute" );
        argv.add( ip.getAbsolutePath() );
    }

    @Override
    public boolean canExecute(String logTag)
    {
        if ( !ip.exists() ){
            Log.d( logTag, "ip not found: " + ip.getAbsolutePath() );
            return false;
        }
        return super.canExecute( logTag );
    }
}
