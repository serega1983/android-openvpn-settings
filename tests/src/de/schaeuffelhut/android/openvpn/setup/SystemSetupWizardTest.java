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
import android.widget.TextView;
import de.schaeuffelhut.android.openvpn.IocContext;
import de.schaeuffelhut.android.openvpn.R;

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
public class SystemSetupWizardTest extends ActivityInstrumentationTestCase2<SystemSetupWizard> {


    protected static final String NO = "No";
    protected static final String YES = "Yes";

    public SystemSetupWizardTest() {
        super( "de.schaeuffelhut.android.openvpn", SystemSetupWizard.class );
    }

    private PrerequisitesFake prerequisites = new PrerequisitesFake();

    public void setUp()
    {
        IocContext.get().setPrerequisites( prerequisites );
    }

    public void tearDown() throws InterruptedException
    {
//        Thread.sleep( 500 );
        getActivity().finish();
    }

    public void test_hasRootShell_No()
    {
        prerequisites.set( PrerequisitesFake.FLAG_NONE );
        assertTextViewEquals( R.id.setup_wizard_prerequisites_hasRoot, NO );
    }
    public void test_hasRootShell_Yes()
    {
        prerequisites.set( PrerequisitesFake.FLAG_HAS_ROOT_SHELL );
        assertTextViewEquals( R.id.setup_wizard_prerequisites_hasRoot, YES );
    }

    public void test_hasTunDevice_No()
    {
        prerequisites.set( PrerequisitesFake.FLAG_NONE );
        assertTextViewEquals( R.id.setup_wizard_prerequisites_hasTunDevice, NO );
    }
    public void test_hasTunDevice_Yes()
    {
        prerequisites.set( PrerequisitesFake.FLAG_HAS_TUN_DEVICE );
        assertTextViewEquals( R.id.setup_wizard_prerequisites_hasTunDevice, YES );
    }

    public void test_hasTunModule_No()
    {
        prerequisites.set( PrerequisitesFake.FLAG_NONE );
        assertTextViewEquals( R.id.setup_wizard_prerequisites_hasTunModule, NO );
    }
    public void test_hasTunModule_Yes()
    {
        prerequisites.set( PrerequisitesFake.FLAG_HAS_TUN_KERNEL_MODULE );
        assertTextViewEquals( R.id.setup_wizard_prerequisites_hasTunModule, YES );
    }

    public void test_hasInsmod_No()
    {
        prerequisites.set( PrerequisitesFake.FLAG_NONE );
        assertTextViewEquals( R.id.setup_wizard_prerequisites_hasInsmod, NO );
    }
    public void test_hasInsmod_Yes()
    {
        prerequisites.set( PrerequisitesFake.FLAG_HAS_INSMOD );
        assertTextViewEquals( R.id.setup_wizard_prerequisites_hasInsmod, YES );
    }

    public void test_BusyBox_No()
    {
        prerequisites.set( PrerequisitesFake.FLAG_NONE );
        assertTextViewEquals( R.id.setup_wizard_prerequisites_hasBusybox, NO );
    }
    public void test_hasBusyBox_Yes()
    {
        prerequisites.set( PrerequisitesFake.FLAG_HAS_BUSYBOX );
        assertTextViewEquals( R.id.setup_wizard_prerequisites_hasBusybox, YES );
    }

    public void test_OpenVpn_No()
    {
        prerequisites.set( PrerequisitesFake.FLAG_NONE );
        assertTextViewEquals( R.id.setup_wizard_prerequisites_hasOpenVpn, NO );
    }
    public void test_OpenVpn_Yes()
    {
        prerequisites.set( PrerequisitesFake.FLAG_HAS_OPENVPN );
        assertTextViewEquals( R.id.setup_wizard_prerequisites_hasOpenVpn, YES );
    }

    private void assertTextViewEquals(int componentId, String text)
    {
        assertEquals( text, ((TextView) getActivity().findViewById( componentId )).getText() );
    }
}
