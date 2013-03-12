package de.schaeuffelhut.android.openvpn.lib.service.impl;

import java.io.File;
import java.util.ArrayList;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-03-12
 */
public class CmdLineBuilder4 extends CmdLineBuilder
{
    private final File openvpn;
    private final File ip;

    public CmdLineBuilder4(File openvpn, File ip)
    {
        if ( openvpn == null )
            throw new NullPointerException();
        if ( ip == null )
            throw new NullPointerException();

        this.openvpn = openvpn;
        this.ip = ip;
    }

    @Override
    public boolean requiresRoot()
    {
        return true;
    }

    @Override
    protected void addOpenvpnLocation(ArrayList<String> argv)
    {
        argv.add( openvpn.getAbsolutePath() );
    }

    @Override
    protected void addIpRoute(ArrayList<String> argv)
    {
        argv.add( "--iproute" );
        argv.add( ip.getAbsolutePath() );
    }
}
