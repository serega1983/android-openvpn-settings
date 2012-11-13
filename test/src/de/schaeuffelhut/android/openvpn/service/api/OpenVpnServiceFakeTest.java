package de.schaeuffelhut.android.openvpn.service.api;

import android.content.Intent;
import android.os.IBinder;
import android.test.ServiceTestCase;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-10-28
 */
public class OpenVpnServiceFakeTest extends ServiceTestCase<OpenVpnServiceFake>
{
    public OpenVpnServiceFakeTest()
    {
        super( OpenVpnServiceFake.class );
    }

    public void test_bind()
    {
        IBinder iBinder = bindService( new Intent() );
        assertNotNull( iBinder );
        assertTrue( iBinder instanceof IOpenVpnService );
        assertTrue( IOpenVpnService.Stub.asInterface( iBinder ) instanceof IOpenVpnService );
    }
}
