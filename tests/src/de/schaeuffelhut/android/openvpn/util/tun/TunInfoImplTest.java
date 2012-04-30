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

/**
 * Created with IntelliJ IDEA.
 * User: fries
 * Date: 4/11/12
 * Time: 9:40 PM
 * To change this template use File | Settings | File Templates.
 */
//public class TunInfoImplTest extends TestCase
//{
//    private TunInfo tunInfo = new TunInfoImpl();
//    private Capability capability;
//
//    private enum Capability
//    {
//        TUN_MODULE_IN_SYSTEM( true )
//                {
//
//                },
//        TUN_KERNEL( true )
//                {
//
//                },
//        TUN_NONE( false )
//                {
//
//                };
//
//        public final boolean expectedTunSupport;
//
//        private Capability(boolean expectedTunSupport)
//        {
//            this.expectedTunSupport = expectedTunSupport;
//        }
//    }
//
//    public void setUp()
//    {
//        String property = SystemPropertyUtil.getProperty( "ovpn-test-cfg.tun" );
//        if (TextUtils.isEmpty( property ))
//            fail( String.format( "Start the emulator with '-prop ovpn-test-cfg.tun=<%s>'", TextUtils.join( "|", Capability.values() ) ) );
//
//        capability = Capability.valueOf( property );
//    }
//
//    public void test_hasTunSupport()
//    {
//        assertEquals( capability.expectedTunSupport, tunInfo.hasTunSupport() );
//        Log.d( "OpenVPN-Settings", "=============> " + capability );
//    }
//}
