package de.schaeuffelhut.android.openvpn.lib.service.impl;

import java.io.File;
import java.util.ArrayList;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-03-12
 */
public class CmdLineBuilder14 extends CmdLineBuilder
{
    private final File openvpn;

    public CmdLineBuilder14(File openvpn)
    {
        if ( openvpn == null )
            throw new NullPointerException();

        this.openvpn = openvpn;
    }

    @Override
    public boolean requiresRoot()
    {
        return false;
    }

    @Override
    protected void addOpenvpnLocation(ArrayList<String> argv)
    {
        argv.add( openvpn.getAbsolutePath() );
    }

    @Override
    protected void addIpRoute(ArrayList<String> argv)
    {
        // iproute not needed
    }
}
