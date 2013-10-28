package de.schaeuffelhut.android.openvpn.service.api;

import android.os.Parcel;
import junit.framework.TestCase;

import java.io.File;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-10-28
 */
public class OpenVpnConfigTest extends TestCase
{
    private final String uniqueName = "abc" + System.currentTimeMillis();

    public void test_getFile() throws Exception
    {
        assertEquals( new File( uniqueName ), new OpenVpnConfig( new File( uniqueName ) ).getFile() );
    }

    public void test_writeToParcel_1() throws Exception
    {
        test_writeToParcel( new File( uniqueName ) );
    }

    public void test_writeToParcel_2() throws Exception
    {
        test_writeToParcel( new File( "/"+uniqueName ) );
    }

    private void test_writeToParcel(File file)
    {
        Parcel parcel = Parcel.obtain();
        new OpenVpnConfig( file ).writeToParcel( parcel, 0 );
        parcel.setDataPosition(0);
        OpenVpnConfig copy = OpenVpnConfig.CREATOR.createFromParcel( parcel );

        assertEquals( file, copy.getFile() );
    }

    /**
     * Do NOT change this test, it ensures compatibility with older clients.
     */
    public void test_read_protocol_version_1()
    {
        Parcel parcel = Parcel.obtain();
        parcel.writeByte( (byte)1 );
        parcel.writeString( uniqueName );

        parcel.setDataPosition(0);
        OpenVpnConfig copy = OpenVpnConfig.CREATOR.createFromParcel( parcel );

        assertEquals( new File( uniqueName ), copy.getFile() );
    }

    public void test_write_protocol_version_1()
    {
        Parcel parcel = Parcel.obtain();
        new OpenVpnConfig( new File( uniqueName ) ).writeToParcel( parcel, 0 );

        parcel.setDataPosition(0);

        assertEquals( 1, parcel.readByte() );
        assertEquals( uniqueName, parcel.readString() );
    }

    public void test_read_protocol_version_unexpected()
    {
        byte unexpectedProtocolVersion = (byte) 2;

        Parcel parcel = Parcel.obtain();
        parcel.writeByte( unexpectedProtocolVersion );

        parcel.setDataPosition( 0 );
        try
        {
            OpenVpnConfig.CREATOR.createFromParcel( parcel );
            fail( "RuntimeException expected" );
        }
        catch (RuntimeException e)
        {
            assertEquals( "Unexpected protocol version: " + unexpectedProtocolVersion, e.getMessage() );
        }
    }
}
