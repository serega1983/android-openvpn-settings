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

package de.schaeuffelhut.android.openvpn.lib.openvpn;

import android.test.InstrumentationTestCase;
import android.util.Log;
import de.schaeuffelhut.android.openvpn.shared.util.BusyBoxBinary;
import de.schaeuffelhut.android.openvpn.shared.util.OpenVpnBinary;
import junit.framework.Assert;

import java.io.File;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-01-25
 */
public class InstallerTest extends InstrumentationTestCase
{

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
    }


    public void test_installOpenVpn() throws InstallFailed
    {
        File pathToOpenVpn = new Installer( getInstrumentation().getTargetContext() ).installOpenVpn();
        Assert.assertNotNull( pathToOpenVpn );
        Assert.assertEquals( "2.1.1", new OpenVpnBinary( pathToOpenVpn ).getVersion() );
    }

    public void test_installBusyBox() throws InstallFailed
    {
        File pathToBusybox = new Installer( getInstrumentation().getTargetContext() ).installBusyBox();
        Assert.assertNotNull( pathToBusybox );
        Assert.assertEquals( "v1.21.0", new BusyBoxBinary( pathToBusybox ).getVersion() );
    }

}
