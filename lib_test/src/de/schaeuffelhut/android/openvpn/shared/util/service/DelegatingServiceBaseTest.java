/*
 * This file is part of OpenVPN-Settings.
 *
 * Copyright © 2009-2012  Friedrich Schäuffelhut
 *
 * OpenVPN-Settings is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenVPN-Settings is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenVPN-Settings.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Report bugs or new features at: http://code.google.com/p/android-openvpn-settings/
 * Contact the author at:          android.openvpn@schaeuffelhut.de
 */

package de.schaeuffelhut.android.openvpn.shared.util.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import de.schaeuffelhut.android.openvpn.shared.util.service.IDelegatingService;
import de.schaeuffelhut.android.openvpn.shared.util.service.ServiceDelegate;
import junit.framework.TestCase;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-03-06
 */
abstract class DelegatingServiceBaseTest extends TestCase
{
    private final ServiceDelegate serviceDelegate = Mockito.mock( ServiceDelegate.class );
    private final Service service = newDelegatingService( serviceDelegate );

    protected abstract Service newDelegatingService(ServiceDelegate serviceDelegate);

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        service.onCreate();
    }

    public final void test_getServiceDelegate()
    {
        assertSame( serviceDelegate, ((IDelegatingService) service).getServiceDelegate() );
    }

    public final void test_onCreate_delegates_to_serviceDelegate()
    {
        //service.onCreate(); // already called in setUp()
        Mockito.verify( serviceDelegate ).onCreate();
    }

    public final void test_onStart_delegates_to_serviceDelegate()
    {
        Intent intent = new Intent();
        int startId = (int) Double.doubleToLongBits( Math.random() );
        service.onStart( intent, startId );
        Mockito.verify( serviceDelegate ).onStart( intent, startId );
    }

    public final void test_onDestroy_delegates_to_serviceDelegate()
    {
        service.onDestroy();
        Mockito.verify( serviceDelegate ).onDestroy();
    }

    public final void test_onBind_delegates_to_serviceDelegate()
    {
        Intent intent = new Intent();
        IBinder iBinder = service.onBind( intent );
        Mockito.verify( serviceDelegate ).onBind( intent );
    }

    public final void test_onBind_returns_value_from_serviceDelegate()
    {
        IBinder expectedBinder = Mockito.mock( IBinder.class );
        Intent intent = new Intent();
        Mockito.when( serviceDelegate.onBind( intent ) ).thenReturn( expectedBinder );

        IBinder iBinder = service.onBind( intent );

        assertSame( expectedBinder, iBinder );
    }
}
