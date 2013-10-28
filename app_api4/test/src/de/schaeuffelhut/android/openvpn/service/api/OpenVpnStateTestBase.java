package de.schaeuffelhut.android.openvpn.service.api;

import android.os.Parcel;
import junit.framework.TestCase;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-10-28
 */
public abstract class OpenVpnStateTestBase<T extends OpenVpnState> extends TestCase
{
    private final Class<T> expectedClass;

    protected OpenVpnStateTestBase(Class<T> expectedClass)
    {
        this.expectedClass = expectedClass;
    }

    protected abstract T createOpenVpnState();

    public void test_writeToParcel_returns_expected_subclass() throws Exception
    {
        assertEquals( expectedClass, copy( createOpenVpnState() ).getClass() );
    }

    public void test_describeContents()
    {
        assertEquals( 0, createOpenVpnState().describeContents() );
    }

    protected final T copy(T openVpnState)
    {
        Parcel parcel = Parcel.obtain();
        openVpnState.writeToParcel( parcel, 0 );
        parcel.setDataPosition(0);
        return (T)OpenVpnState.CREATOR.createFromParcel( parcel );
    }

    public void test_read_protocol_version_unexpected()
    {
        byte unexpectedProtocolVersion = (byte) 3;

        Parcel parcel = Parcel.obtain();
        parcel.writeByte( unexpectedProtocolVersion );

        parcel.setDataPosition( 0 );
        try
        {
            OpenVpnState.CREATOR.createFromParcel( parcel );
            fail( "RuntimeException expected" );
        }
        catch (RuntimeException e)
        {
            assertEquals( "Unexpected protocol version: " + unexpectedProtocolVersion, e.getMessage() );
        }
    }

    public void test_stopped()
    {
        assertFalse( OpenVpnState.stopped().isStarted() );
    }
}
