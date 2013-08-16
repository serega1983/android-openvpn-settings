package de.schaeuffelhut.android.openvpn.lib.service.impl;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-03-14
 */
public class IfConfigFactoryImpl implements IfConfigFactory
{
    @Override
    public IfConfig createIfConfig()
    {
        return new IfConfig();
    }
}
