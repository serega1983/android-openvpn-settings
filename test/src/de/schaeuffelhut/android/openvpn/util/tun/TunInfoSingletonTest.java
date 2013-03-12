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

package de.schaeuffelhut.android.openvpn.util.tun;

import android.test.InstrumentationTestCase;
import android.test.MoreAsserts;
import de.schaeuffelhut.android.openvpn.IocContext;
import de.schaeuffelhut.android.openvpn.util.tun.TunInfo;
import de.schaeuffelhut.android.openvpn.util.tun.TunInfoFake;
import de.schaeuffelhut.android.openvpn.util.tun.TunInfoImpl;
import de.schaeuffelhut.android.openvpn.util.tun.TunInfoSingleton;
import junit.framework.Assert;

/**
 * Created with IntelliJ IDEA.
 * User: fries
 * Date: 4/24/12
 * Time: 5:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class TunInfoSingletonTest extends InstrumentationTestCase
{
    public void testGet() throws Exception
    {
        Assert.assertNotNull( IocContext.get() );
    }

    public void testGetTunInfo() throws Exception
    {
        MoreAsserts.assertAssignableFrom( TunInfoImpl.class, TunInfoSingleton.get().getTunInfo( getInstrumentation().getContext() ) );
    }

    public void testSetTunInfo() throws Exception
    {
        TunInfoFake expected = new TunInfoFake();
        TunInfoSingleton.get().setTunInfo( expected );

        TunInfo actual = TunInfoSingleton.get().getTunInfo( getInstrumentation().getContext() );

        assertSame( expected, actual );
    }
}
