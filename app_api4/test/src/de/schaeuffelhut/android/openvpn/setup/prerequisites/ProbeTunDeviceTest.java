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

package de.schaeuffelhut.android.openvpn.setup.prerequisites;

import android.test.InstrumentationTestCase;
import de.schaeuffelhut.android.openvpn.util.tun.TunInfoFake;
import de.schaeuffelhut.android.openvpn.util.tun.TunInfoSingleton;
import de.schaeuffelhut.android.openvpn.util.tun.TunLoaderFake;
import junit.framework.Assert;

/**
 * Created with IntelliJ IDEA.
 * User: fries
 * Date: 4/26/12
 * Time: 10:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProbeTunDeviceTest extends InstrumentationTestCase
{
    private TunInfoFake tunInfo;
    private ProbeTunDevice probeFactory;

    public void setUp()
    {
        tunInfo = new TunInfoFake();
        TunInfoSingleton.get().setTunInfo( tunInfo );
        probeFactory = new ProbeTunDevice( getInstrumentation().getContext() );
    }

    public void test_message_value1()
    {
        probeFactory.message( "A first message" );
        Assert.assertEquals( "A first message", probeFactory.messages.get( 0 ) );
    }

    public void test_message_value2()
    {
        probeFactory.message( "A second message" );
        Assert.assertEquals( "A second message", probeFactory.messages.get( 0 ) );
    }

    public void test_message_with2values()
    {
        probeFactory.message( "A first message" );
        probeFactory.message( "A second message" );
        Assert.assertEquals( "A first message", probeFactory.messages.get( 0 ) );
        Assert.assertEquals( "A second message", probeFactory.messages.get( 1 ) );
    }

    //TODO: Fix ignored test
    public void IGNORE___test_checkForTunDevice_with_tun_exists_not()
    {
        tunInfo.setDeviceNodeAvailable( false );
        Assert.assertFalse( probeFactory.checkForTunDevice() );
        assertLastMessageEquals( "TUN device not found." );
    }

    public void test_checkForTunDevice_with_tun_exists()
    {
        tunInfo.setDeviceNodeAvailable( true );
        Assert.assertTrue( probeFactory.checkForTunDevice() );
        assertLastMessageEquals( "TUN device is at /dev/tun." );
    }

//    public test_tryDefaultTunLoader()
//    {
//
//    }




    //TODO: Fix ignored test
    public void IGNORE___test_probe()
    {
        ProbeResult probeResult = probeFactory.probe();
        Assert.assertNotNull( probeResult );
        Assert.assertEquals( "TUN Device Driver", probeResult.title );
        Assert.assertEquals( "", probeResult.subtitle );
    }

    public void test_probe__tun_initially_available()
    {
        tunInfo.setDeviceNodeAvailable( true );
        ProbeResult probeResult = probeFactory.probe();
        assertLastMessageEquals( "TUN device is at /dev/tun." );
        assertMessageCount( 1 );
        assertSuccess( probeResult );
    }

    //TODO: Fix ignored test
    public void IGNORE___test_probe__tun_initially_unavailable__with_tunLoader__loading_tun()
    {
        tunInfo.setDeviceNodeAvailable( false );
        tunInfo.setTunLoader( new TunLoaderFake("fake"){
            @Override
            public void loadModule()
            {
                tunInfo.setDeviceNodeAvailable( true );
            }
        });
        ProbeResult probeResult = probeFactory.probe();
        assertMessageEquals( 0, "TUN device not found." );
        assertMessageEquals( 1, "A TUN loader is defined." );
        assertMessageEquals( 2, "Executing..." );
        assertMessageEquals( 3, "TUN device is at /dev/tun." );
        assertMessageCount( 4 );
        assertSuccess( probeResult );
    }

    //TODO: Fix ignored test
    public void IGNORE___test_probe__tun_initially_unavailable__with_tunLoader__not_loading_tun()
    {
        tunInfo.setDeviceNodeAvailable( false );
        tunInfo.setTunLoader( new TunLoaderFake("fake") );
        ProbeResult probeResult = probeFactory.probe();
        assertMessageEquals( 0, "TUN device not found." );
        assertMessageEquals( 1, "A TUN loader is defined." );
        assertMessageEquals( 2, "Executing..." );
        assertMessageEquals( 3, "TUN device not found." );
        assertMessageCount( 4 );
        assertFailed( probeResult );
    }


    //TODO: Fix ignored test
    public void IGNORE___test_probe__tun_initially_unavailable__without_tunLoader__trying_standard_locations()
    {
        tunInfo.setDeviceNodeAvailable( false );
        tunInfo.setTunLoader( null );
        ProbeResult probeResult = probeFactory.probe();
        assertMessageEquals( 0, "TUN device not found." );
        assertMessageEquals( 1, "No TUN loader defined." );
        assertMessageEquals( 2, "Defining TUN loader: insmod /system/lib/modules/tun.ko" );
        assertMessageCount( 3 );
        assertFailed( probeResult );
    }


    private void assertMessageEquals(int messageNo, String expected)
    {
        Assert.assertEquals( expected, probeFactory.messages.get( messageNo ) );
    }
    private void assertLastMessageEquals(String expected)
    {
        Assert.assertEquals( expected, probeFactory.messages.get( probeFactory.messages.size() - 1 ) );
    }
    private void assertMessageCount(int expected)
    {
        Assert.assertEquals( expected, probeFactory.messages.size() );
    }

    private void assertSuccess(ProbeResult probeResult)
    {
        Assert.assertEquals( PrerequisitesActivity.Status.SUCCESS, probeResult.status );
    }

    private void assertFailed(ProbeResult probeResult)
    {
        Assert.assertEquals( PrerequisitesActivity.Status.FAILED, probeResult.status );
    }
}
