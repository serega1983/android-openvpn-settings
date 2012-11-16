package de.schaeuffelhut.android.openvpn.service.api;

import android.os.Parcel;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-10-28
 */
public class OpenVpnStateStartedTest extends OpenVpnStateTestBase<OpenVpnState.Started>
{
    private static final OpenVpnDaemonState DUMMY_DAEMON_STATE = OpenVpnDaemonState.ENABLED;
    private static final OpenVpnNetworkState DUMMY_NETWORK_STATE = OpenVpnNetworkState.AUTH;
    private static final String DUMMY_CONNECTED_TO = "USA1";
    private static final String DUMMY_IP = "192.168.1.1";
    private static final long DUMMY_BYTES_SENT = 1036847L;
    private static final long DUMMY_BYTES_RECEIVED = 54398530925L;
    private static final int DUMMY_CONNECTED_SECONDS = 61;


    public OpenVpnStateStartedTest(){
        super( OpenVpnState.Started.class );
    }

    public void test_isStarted()
    {
        assertTrue( createOpenVpnState().isStarted() );
    }

    public void test_getDaemonState()
    {
        for(OpenVpnDaemonState state : OpenVpnDaemonState.values() )
            assertEquals( state, copy( new OpenVpnState.Started( state, DUMMY_NETWORK_STATE, DUMMY_CONNECTED_TO, DUMMY_IP, DUMMY_BYTES_SENT, DUMMY_BYTES_RECEIVED, DUMMY_CONNECTED_SECONDS ) ).getDaemonState() );
    }

    public void test_getNetworkState()
    {
        for(OpenVpnNetworkState state : OpenVpnNetworkState.values() )
            assertEquals( state, copy( new OpenVpnState.Started( DUMMY_DAEMON_STATE, state, DUMMY_CONNECTED_TO, DUMMY_IP, DUMMY_BYTES_SENT, DUMMY_BYTES_RECEIVED, DUMMY_CONNECTED_SECONDS ) ).getNetworkState() );
    }

    public void test_getConnectedTo_USA1()
    {
            assertEquals( "USA1", copy( new OpenVpnState.Started( DUMMY_DAEMON_STATE, DUMMY_NETWORK_STATE, "USA1", DUMMY_IP, DUMMY_BYTES_SENT, DUMMY_BYTES_RECEIVED, DUMMY_CONNECTED_SECONDS ) ).getConnectedTo() );
    }

    public void test_getConnectedTo_France()
    {
            assertEquals( "France", copy( new OpenVpnState.Started( DUMMY_DAEMON_STATE, DUMMY_NETWORK_STATE, "France", DUMMY_IP, DUMMY_BYTES_SENT, DUMMY_BYTES_RECEIVED, DUMMY_CONNECTED_SECONDS ) ).getConnectedTo() );
    }

    public void test_getIp()
    {
        assertEquals( "192.168.1.1", copy( new OpenVpnState.Started( DUMMY_DAEMON_STATE, DUMMY_NETWORK_STATE, DUMMY_CONNECTED_TO, "192.168.1.1", DUMMY_BYTES_SENT, DUMMY_BYTES_RECEIVED, DUMMY_CONNECTED_SECONDS ) ).getIp() );
    }

    public void test_getIp_2()
    {
        assertEquals( "172.24.2.5", copy( new OpenVpnState.Started( DUMMY_DAEMON_STATE, DUMMY_NETWORK_STATE, DUMMY_CONNECTED_TO, "172.24.2.5", DUMMY_BYTES_SENT, DUMMY_BYTES_RECEIVED, DUMMY_CONNECTED_SECONDS ) ).getIp() );
    }

    public void test_getBytesSent_0()
    {
        assertEquals( 0, copy( new OpenVpnState.Started( DUMMY_DAEMON_STATE, DUMMY_NETWORK_STATE, DUMMY_CONNECTED_TO, "172.24.2.5", 0, DUMMY_BYTES_RECEIVED, DUMMY_CONNECTED_SECONDS ) ).getBytesSent() );
    }

    public void test_getBytesSent_87981534597980()
    {
        assertEquals( 87981534597980L, copy( new OpenVpnState.Started( DUMMY_DAEMON_STATE, DUMMY_NETWORK_STATE, DUMMY_CONNECTED_TO, "172.24.2.5", 87981534597980L, DUMMY_BYTES_RECEIVED, DUMMY_CONNECTED_SECONDS ) ).getBytesSent() );
    }

    public void test_getBytesReceived_0()
    {
        assertEquals( 0, copy( new OpenVpnState.Started( DUMMY_DAEMON_STATE, DUMMY_NETWORK_STATE, DUMMY_CONNECTED_TO, "172.24.2.5", DUMMY_BYTES_SENT, 0, DUMMY_CONNECTED_SECONDS ) ).getBytesReceived() );
    }

    public void test_getBytesReceived_87981534597980()
    {
        assertEquals( 87981534597980L, copy( new OpenVpnState.Started( DUMMY_DAEMON_STATE, DUMMY_NETWORK_STATE, DUMMY_CONNECTED_TO, "172.24.2.5", DUMMY_BYTES_SENT, 87981534597980L, DUMMY_CONNECTED_SECONDS ) ).getBytesReceived() );
    }

    public void test_getConnectedSeconds_0()
    {
        assertEquals( 0, copy( new OpenVpnState.Started( DUMMY_DAEMON_STATE, DUMMY_NETWORK_STATE, DUMMY_CONNECTED_TO, "172.24.2.5", DUMMY_BYTES_SENT, DUMMY_BYTES_RECEIVED, 0 ) ).getConnectedSeconds() );
    }

    public void test_getConnectedSeconds_31536000()
    {
        assertEquals( 31536000, copy( new OpenVpnState.Started( DUMMY_DAEMON_STATE, DUMMY_NETWORK_STATE, DUMMY_CONNECTED_TO, "172.24.2.5", DUMMY_BYTES_SENT, DUMMY_BYTES_RECEIVED, 31536000 ) ).getConnectedSeconds() );
    }



    @Override
    protected OpenVpnState.Started createOpenVpnState()
    {
        return new OpenVpnState.Started();
    }



    /**
     * Do NOT change this test, it ensures compatibility with older clients.
     */
    public void test_read_TYPE_STARTED_VERSION_1()
    {
        Parcel parcel = Parcel.obtain();
        parcel.writeByte( (byte)1 );
        parcel.writeParcelable( OpenVpnDaemonState.ENABLED, 0 );
        parcel.writeParcelable( OpenVpnNetworkState.CONNECTED, 0 );
        parcel.writeString( "Server" );
        parcel.writeString( "10.0.0.2" );
        parcel.writeLong( 101 );
        parcel.writeLong( 102 );
        parcel.writeInt( 60 );

        parcel.setDataPosition(0);
        OpenVpnState copy = OpenVpnState.CREATOR.createFromParcel( parcel );

        assertTrue( copy.isStarted() );
        assertEquals( OpenVpnDaemonState.ENABLED, copy.getDaemonState() );
        assertEquals( OpenVpnNetworkState.CONNECTED, copy.getNetworkState() );
        assertEquals( "Server", copy.getConnectedTo() );
        assertEquals( "10.0.0.2", copy.getIp() );
        assertEquals( 101, copy.getBytesSent() );
        assertEquals( 102, copy.getBytesReceived() );
        assertEquals( 60, copy.getConnectedSeconds() );
    }

    public void test_write_TYPE_STARTED_VERSION_1()
    {
        Parcel parcel = Parcel.obtain();
        new OpenVpnState.Started( DUMMY_DAEMON_STATE, OpenVpnNetworkState.CONNECTED, "Server", "10.0.0.2", 101, 102, 60 ).writeToParcel( parcel, 0 );

        parcel.setDataPosition(0);

        assertEquals( 1, parcel.readByte() );
        assertEquals( OpenVpnDaemonState.ENABLED, parcel.readParcelable( OpenVpnDaemonState.class.getClassLoader() ) );
        assertEquals( OpenVpnNetworkState.CONNECTED, parcel.readParcelable( OpenVpnNetworkState.class.getClassLoader() ) );
        assertEquals( "Server", parcel.readString() );
        assertEquals( "10.0.0.2", parcel.readString() );
        assertEquals( 101, parcel.readLong() );
        assertEquals( 102, parcel.readLong() );
        assertEquals( 60, parcel.readInt() );
    }
}
