package de.schaeuffelhut.android.openvpn.shared.util;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-03-06
 */
public interface IDelegatingService<T extends ServiceDelegate>
{
    public T getServiceDelegate();
}
