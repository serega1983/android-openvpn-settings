package de.schaeuffelhut.android.openvpn.lib.service.impl;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-03-12
 */
public class CmdLineBuilder14 extends CmdLineBuilder
{
    public CmdLineBuilder14(Context context)
    {
        super( resolveBinary( context, "miniopenvpn" ) );
    }

    CmdLineBuilder14(File openvpn)
    {
        super( openvpn );
    }

    @Override
    public boolean requiresRoot()
    {
        return false;
    }

    @Override
    protected void addIpRoute(ArrayList<String> argv)
    {
        // iproute not needed
    }
}
