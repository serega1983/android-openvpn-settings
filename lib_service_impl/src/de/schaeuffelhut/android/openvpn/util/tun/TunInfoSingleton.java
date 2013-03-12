package de.schaeuffelhut.android.openvpn.util.tun;

import android.content.Context;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-03-12
 */
public class TunInfoSingleton
{
    private final static TunInfoSingleton SINGLETON = new TunInfoSingleton();

    private TunInfo tunInfo;

    public final static TunInfoSingleton get()
    {
        return SINGLETON;
    }

    public void setTunInfo(TunInfo tunInfo)
    {
        this.tunInfo = tunInfo;
    }

    public TunInfo getTunInfo(Context context)
    {
        if (tunInfo == null)
        {
            // return new instance on each call, otherwise we leak the context!
            return new TunInfoImpl( context );
        }
        return tunInfo;
    }

}
