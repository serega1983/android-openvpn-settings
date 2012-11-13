package de.schaeuffelhut.android.openvpn.service.api;

import android.content.Intent;
import android.os.IBinder;
import android.test.ServiceTestCase;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-10-28
 */
public class OpenVpnServiceServiceTest extends ServiceTestCase<OpenVpnServiceService>
{
    public OpenVpnServiceServiceTest()
    {
        super( OpenVpnServiceService.class );
    }

    public void test_bind()
    {
        IBinder iBinder = bindService( new Intent() );
        assertNotNull( iBinder );
        assertTrue( iBinder instanceof IfcOpenVpnService );
        assertTrue( IfcOpenVpnService.Stub.asInterface( iBinder ) instanceof IfcOpenVpnService );
    }
}
