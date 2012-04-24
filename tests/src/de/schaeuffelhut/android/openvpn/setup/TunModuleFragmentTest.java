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

package de.schaeuffelhut.android.openvpn.setup;

import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import de.schaeuffelhut.android.openvpn.IocContext;
import de.schaeuffelhut.android.openvpn.R;
import junit.framework.Assert;

import java.io.File;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class de.schaeuffelhut.android.openvpn.OpenVpnSettingsTest \
 * de.schaeuffelhut.android.openvpn.tests/android.test.InstrumentationTestRunner
 */
public class TunModuleFragmentTest extends ActivityInstrumentationTestCase2<TunModuleFragmentActivityTestSupport>
{


    protected static final String NO = "No";
    protected static final String YES = "Yes";
    private TunInfoFake tunInfo;
    private TunLoaderProbeFake tunLoaderProbe;

    public TunModuleFragmentTest()
    {
        super( "de.schaeuffelhut.android.openvpn", TunModuleFragmentActivityTestSupport.class );
    }

    public void setUp()
    {
        tunInfo = new TunInfoFake();
        IocContext.get().setTunInfo( tunInfo );
        tunLoaderProbe = new TunLoaderProbeFake( tunInfo );
        IocContext.get().setTunLoderProbe( tunLoaderProbe );
    }

    public void tearDown() throws InterruptedException
    {
//        Thread.sleep( 1000 );
        getActivity().finish();
    }

    public void test_isDeviceNodeAvailable_No()
    {
        tunInfo.setDeviceNodeAvailable( false );
        assertTextViewEquals( R.id.setup_wizard_tun_module_has_device_node, "Not Available" );
    }

    public void test_isDeviceNodeAvailable_Yes()
    {
        tunInfo.setDeviceNodeAvailable( true );
        assertTextViewEquals( R.id.setup_wizard_tun_module_has_device_node, "Available" );
    }

    public void test_hasTunLoader_No()
    {
        tunInfo.setTunLoader( null );
        assertTextViewEquals( R.id.setup_wizard_tun_module_tun_loader, "none" );
    }

    public void test_hasTunLoader_modprobe()
    {
        tunInfo.setTunLoader( new TunLoaderFake( "modprobe" ) );
        assertTextViewEquals( R.id.setup_wizard_tun_module_tun_loader, "modprobe" );
    }

    public void test_hasTunLoader_insmod()
    {
        tunInfo.setTunLoader( new TunLoaderFake( "insmod" ) );
        assertTextViewEquals( R.id.setup_wizard_tun_module_tun_loader, "insmod" );
    }

    public void test_pathToModule_is_invisible_when_tun_loader_is_not_specified()
    {
        tunInfo.setTunLoader( null );
        assertInvisible( R.id.setup_wizard_tun_module_path_to_module_table_row );
//        assertInvisible( R.id.setup_wizard_tun_module_path_to_module );
    }

    public void test_pathToModule_noPath()
    {
        tunInfo.setTunLoader( new TunLoaderFake( "modprobe" ) );
        assertInvisible( R.id.setup_wizard_tun_module_path_to_module_table_row );
//        assertInvisible( R.id.setup_wizard_tun_module_path_to_module );
    }

    public void test_pathToModule_withPath()
    {
        tunInfo.setTunLoader( new TunLoaderFake( "modprobe", new File( "/system/lib/modules/tun.ko" ) ) );
        assertVisible( R.id.setup_wizard_tun_module_path_to_module_table_row );
//        assertInvisible( R.id.setup_wizard_tun_module_path_to_module );
    }

    public void test_pathToModule_withPath_1()
    {
        test_pathToModule( "/system/lib/modules/tun.ko" );
    }

    public void test_pathToModule_withPath_2()
    {
        test_pathToModule( "/lib/modules/tun.ko" );
    }

    private void test_pathToModule(String path)
    {
        tunInfo.setTunLoader( new TunLoaderFake( "insmod", new File( path ) ) );
        assertTextViewEquals( R.id.setup_wizard_tun_module_path_to_module, path );
    }

    public void test_hide_section_module_load_if_tun_is_available()
    {
        tunInfo.setDeviceNodeAvailable( true );
        assertInvisible( R.id.setup_wizard_tun_module_section_load_module );
    }

    public void test_show_section_module_load_if_tun_is_not_available()
    {
        tunInfo.setDeviceNodeAvailable( false );
        assertVisible( R.id.setup_wizard_tun_module_section_load_module );
    }

    public void test_hide_option_try_current_tun_loader_if_none_is_defined()
    {
        tunInfo.setTunLoader( null );
        assertGone( R.id.setup_wizard_tun_module_option_try_current_tun_loader );
    }

    public void test_show_option_try_current_tun_loader_if_defined()
    {
        tunInfo.setTunLoader( new DummyTunLoader() );
        assertVisible( R.id.setup_wizard_tun_module_option_try_current_tun_loader );
    }

    public void test_option_try_current_tun_loader_is_checked_by_default()
    {
        tunInfo.setTunLoader( new DummyTunLoader() );
        assertChecked( R.id.setup_wizard_tun_module_option_try_current_tun_loader );
    }

    public void test_option_scan_device_for_tun_is_checked_by_default()
    {
        tunInfo.setTunLoader( new DummyTunLoader() );
        assertChecked( R.id.setup_wizard_tun_module_option_scan_device_for_tun );
    }

    public void test_option_try_sdcard_is_not_checked_by_default()
    {
        tunInfo.setTunLoader( new DummyTunLoader() );
        assertNotChecked( R.id.setup_wizard_tun_module_option_try_sdcard );
    }

    public void test_try_to_load_module_calls_tunLoaderProbe()
    {
        TouchUtils.clickView( this, getActivity().findViewById( R.id.setup_wizard_tun_module_try_to_load_module ) );
        Assert.assertEquals( 1, tunLoaderProbe.makeSuccessfullyProbedTunLoaderTheDefaultCallCount );
    }

    public void test_try_to_load_module_calls_tunLoaderProbe_with_option_try_current_tun_loader()
    {
        tunInfo.setTunLoader( new DummyTunLoader() );
        TouchUtils.clickView( this, getActivity().findViewById( R.id.setup_wizard_tun_module_try_to_load_module ) );
        Assert.assertEquals( 1, tunLoaderProbe.tryCurrentTunLoaderCallCount );
    }

    public void test_try_to_load_module_calls_tunLoaderProbe_with_option_try_current_tun_loader_is_not_visible()
    {
        tunInfo.setTunLoader( null );
        TouchUtils.clickView( this, getActivity().findViewById( R.id.setup_wizard_tun_module_try_to_load_module ) );
        Assert.assertEquals( 0, tunLoaderProbe.tryCurrentTunLoaderCallCount );
    }

    public void test_try_to_load_module_calls_tunLoaderProbe_with_option_scan_device()
    {
        TouchUtils.clickView( this, getActivity().findViewById( R.id.setup_wizard_tun_module_try_to_load_module ) );
        Assert.assertEquals( 1, tunLoaderProbe.scanDeviceForTunCallCount );
    }

    public void test_try_to_load_module_calls_tunLoaderProbe_without_option_try_current_tun_loader_is_not_checked()
    {
        TouchUtils.clickView( this, getActivity().findViewById( R.id.setup_wizard_tun_module_option_scan_device_for_tun ) );
        TouchUtils.clickView( this, getActivity().findViewById( R.id.setup_wizard_tun_module_try_to_load_module ) );
        Assert.assertEquals( 0, tunLoaderProbe.scanDeviceForTunCallCount );
    }

    public void test_try_to_load_module_calls_tunLoaderProbe_with_option_try_sdcard()
    {
        TouchUtils.clickView( this, getActivity().findViewById( R.id.setup_wizard_tun_module_option_try_sdcard ) );
        TouchUtils.clickView( this, getActivity().findViewById( R.id.setup_wizard_tun_module_try_to_load_module ) );
        Assert.assertEquals( 1, tunLoaderProbe.trySdCardCallCount );
    }

    public void test_try_to_load_module_calls_tunLoaderprobe_without_option_try_sdcard_is_not_checked()
    {
        TouchUtils.clickView( this, getActivity().findViewById( R.id.setup_wizard_tun_module_try_to_load_module ) );
        Assert.assertEquals( 0, tunLoaderProbe.trySdCardCallCount );
    }


    public void test_refresh_after_try_to_load_module()
    {
        tunLoaderProbe.onCallToTryToLoadTunModuleSetDeviceNodeAvailableTo( true );
        TouchUtils.clickView( this, getActivity().findViewById( R.id.setup_wizard_tun_module_try_to_load_module ) );
        assertTrue( tunInfo.isDeviceNodeAvailable() );
        assertTextViewEquals( R.id.setup_wizard_tun_module_has_device_node, "Available" );
    }


    private void assertTextViewEquals(int componentId, String text)
    {
        assertEquals( text, ((TextView) getActivity().findViewById( componentId )).getText() );
    }

    private void assertInvisible(int componentId)
    {
        Assert.assertEquals( View.INVISIBLE, getActivity().findViewById( componentId ).getVisibility() );
    }

    private void assertVisible(int componentId)
    {
        Assert.assertEquals( View.VISIBLE, getActivity().findViewById( componentId ).getVisibility() );
    }

    private void assertGone(int componentId)
    {
        Assert.assertEquals( View.GONE, getActivity().findViewById( componentId ).getVisibility() );
    }

    private void assertChecked(int componentId)
    {
        Assert.assertTrue( ((CheckBox) getActivity().findViewById( componentId )).isChecked() );
    }

    private void assertNotChecked(int componentId)
    {
        Assert.assertFalse( ((CheckBox) getActivity().findViewById( componentId )).isChecked() );
    }

}
