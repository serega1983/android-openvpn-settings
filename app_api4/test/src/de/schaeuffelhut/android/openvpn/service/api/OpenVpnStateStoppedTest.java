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

    public void test_init_with_state_UNKNOWN()
    {
        assertEquals( OpenVpnDaemonState.UNKNOWN, new OpenVpnState.Stopped( OpenVpnDaemonState.UNKNOWN ).getDaemonState() );
    }

    public void test_init_with_state_DISABLED()
    {
        assertEquals( OpenVpnDaemonState.DISABLED, new OpenVpnState.Stopped( OpenVpnDaemonState.DISABLED ).getDaemonState() );
    }

    public void test_init_with_state_STARTUP()
    {
        try
        {
            new OpenVpnState.Stopped( OpenVpnDaemonState.STARTUP );
            fail( "IllegalArgumentException expected" );
        }
        catch (IllegalArgumentException e)
        {
            assertEquals( "state: STARTUP", e.getMessage() );
        }
    }

    public void test_init_with_state_ENABLED()
    {
        try
        {
            new OpenVpnState.Stopped( OpenVpnDaemonState.ENABLED );
            fail( "IllegalArgumentException expected" );
        }
        catch (IllegalArgumentException e)
        {
            assertEquals( "state: ENABLED", e.getMessage() );
        }
    }

    public void test_getDaemonState()
    {
        assertEquals( OpenVpnDaemonState.UNKNOWN, new OpenVpnState.Stopped( OpenVpnDaemonState.UNKNOWN ).getDaemonState() );
        assertEquals( OpenVpnDaemonState.DISABLED, new OpenVpnState.Stopped( OpenVpnDaemonState.DISABLED ).getDaemonState() );
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

    public void test_getPasswordRequest()
    {
        assertEquals( OpenVpnPasswordRequest.NONE, createOpenVpnState().getPasswordRequest() );
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


    public void test_getLocalIp()
    {
        try
        {
            createOpenVpnState().getLocalIp();
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
        return new OpenVpnState.Stopped( OpenVpnDaemonState.DISABLED );
    }


    /**
     * Do NOT change this test, it ensures compatibility with older clients.
     */
    public void test_read_TYPE_STOPPED_VERSION_1()
    {
        Parcel parcel = Parcel.obtain();
        parcel.writeByte( (byte) 2 );
        parcel.writeParcelable( OpenVpnDaemonState.DISABLED, 0 );

        parcel.setDataPosition( 0 );
        OpenVpnState copy = OpenVpnState.CREATOR.createFromParcel( parcel );

        assertFalse( copy.isStarted() );
        assertEquals( OpenVpnDaemonState.DISABLED, copy.getDaemonState() );
    }

    public void test_write_TYPE_STOPPED_VERSION_1()
    {
        Parcel parcel = Parcel.obtain();
        new OpenVpnState.Stopped( OpenVpnDaemonState.DISABLED ).writeToParcel( parcel, 0 );

        parcel.setDataPosition( 0 );

        assertEquals( 2, parcel.readByte() );
        assertEquals( OpenVpnDaemonState.DISABLED, parcel.readParcelable( OpenVpnDaemonState.class.getClassLoader() ) );
    }

}
