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

/**
 * Created with IntelliJ IDEA.
 * User: fries
 * Date: 4/6/12
 * Time: 11:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class PrerequisitesImplTest extends ActivityInstrumentationTestCase2<SystemSetupWizard>
{

    public PrerequisitesImplTest() {
            super( "de.schaeuffelhut.android.openvpn", SystemSetupWizard.class);
    }

    Prerequisites prerequisites = new PrerequisitesImpl();

    public void testHasRootShell()
    {
        SystemSetupWizard activity = getActivity();
        assertTrue( prerequisites.hasRootShell() );
    }

    public void testHasTunDevice()
    {
        //TODO: use IocContext to set test values
        assertTrue( prerequisites.hasTunDevice() );
    }

    public void testHasInsmod()
    {
        assertTrue( prerequisites.hasInsmod() );
    }

    public void testHasTunKernelModule()
    {
        assertTrue( prerequisites.hasTunKernelModule() );
    }

    public void testHasOpenVPN()
    {
        assertTrue( prerequisites.hasOpenVPN() );
    }

    public void testHasBusyBox()
    {
        assertTrue( prerequisites.hasBusyBox() );
    }
}
