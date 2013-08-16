package de.schaeuffelhut.android.openvpn.lib.service.impl;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-03-12
 */
public final class NullShareTun implements ShareTun
{
    @Override
    public void shareTun()
    {
        // noop
    }
}
