package de.schaeuffelhut.android.openvpn.service.api;

import android.os.Parcel;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-10-28
 */
public class OpenVpnStateStoppedTest extends OpenVpnStateTestBase<OpenVpnState.Stopped>
{
    public OpenVpnStateStoppedTest()
    {
        super( OpenVpnState.Stopped.class );
    }

    public void test_isStarted()
    {
        assertFalse( createOpenVpnState().isStarted() );
    }

    public void test_getNetworkState()
    {
        try
        {
            createOpenVpnState().getNetworkState();
            fail( "IllegalStateException expected" );
        }
        catch (IllegalStateException e)
        {
            assertEquals( "Service is stopped", e.getMessage() );
        }
    }

    public void test_getConnectedTo()
    {
        try
        {
            createOpenVpnState().getConnectedTo();
            fail( "IllegalStateException expected" );
        }
        catch (IllegalStateException e)
        {
            assertEquals( "Service is stopped", e.getMessage() );
        }
    }


    public void test_getIp()
    {
        try
        {
            createOpenVpnState().getIp();
            fail( "IllegalStateException expected" );
        }
        catch (IllegalStateException e)
        {
            assertEquals( "Service is stopped", e.getMessage() );
        }
    }

    public void test_getBytesSent()
    {
        try
        {
            createOpenVpnState().getBytesSent();
            fail( "IllegalStateException expected" );
        }
        catch (IllegalStateException e)
        {
            assertEquals( "Service is stopped", e.getMessage() );
        }
    }

    public void test_getBytesReceived()
    {
        try
        {
            createOpenVpnState().getBytesReceived();
            fail( "IllegalStateException expected" );
        }
        catch (IllegalStateException e)
        {
            assertEquals( "Service is stopped", e.getMessage() );
        }
    }

    public void test_getConnectedSeconds()
    {
        try
        {
            createOpenVpnState().getConnectedSeconds();
            fail( "IllegalStateException expected" );
        }
        catch (IllegalStateException e)
        {
            assertEquals( "Service is stopped", e.getMessage() );
        }
    }


    @Override
    protected OpenVpnState.Stopped createOpenVpnState()
    {
        return new OpenVpnState.Stopped();
    }


    /**
     * Do NOT change this test, it ensures compatibility with older clients.
     */
    public void test_read_TYPE_STOPPED_VERSION_1()
    {
        Parcel parcel = Parcel.obtain();
        parcel.writeByte( (byte)2 );

        parcel.setDataPosition(0);
        OpenVpnState copy = OpenVpnState.CREATOR.createFromParcel( parcel );

        assertFalse( copy.isStarted() );
    }

    public void test_write_TYPE_STOPPED_VERSION_1()
    {
        Parcel parcel = Parcel.obtain();
        new OpenVpnState.Stopped().writeToParcel( parcel, 0 );

        parcel.setDataPosition(0);

        assertEquals( 2, parcel.readByte() );
    }

}
