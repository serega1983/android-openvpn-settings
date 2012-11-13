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
}
