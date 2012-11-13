package de.schaeuffelhut.android.openvpn.service.api;

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

    @Override
    public void test_isStarted()
    {
        assertFalse( createOpenVpnState().isStarted() );
    }

    @Override
    public void test_getState()
    {
        try
        {
            createOpenVpnState().getState();
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


    @Override
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
}
