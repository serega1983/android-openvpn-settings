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

import android.content.Context;
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
//TODO: merge with openvpn4.InstallerTest
public class InstallerTest extends InstrumentationTestCase
{
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        removeBinDir();
    }

    private void removeBinDir()
    {
        File binDir = getBinDir();
        File[] files = binDir.listFiles();
        if (files != null)
            for (File file : files)
                file.delete();
        binDir.delete();
        Assert.assertFalse( binDir.exists() );
    }


    public void test_installOpenVpn_installs_binary() throws InstallFailed
    {
        File pathToOpenVpn = new Installer( getTargetContext() ).installOpenVpn();
        Assert.assertNotNull( pathToOpenVpn );
        Assert.assertEquals( "2.1.1", new OpenVpnBinary( pathToOpenVpn ).getVersion() );
    }

    public void test_installOpenVpn_verify_location() throws InstallFailed
    {
        File pathToOpenVpn = new Installer( getTargetContext() ).installOpenVpn();
        Assert.assertEquals(
                new File( getBinDir(), "openvpn" ),
                pathToOpenVpn
        );
    }

    public void test_installOpenVpn_twice_succeeds() throws InstallFailed
    {
        new Installer( getTargetContext() ).installOpenVpn();
        new Installer( getTargetContext() ).installOpenVpn();
    }

    public void test_installBusyBox_installs_binary_and_applets() throws InstallFailed
    {
        File pathToBusybox = new Installer( getTargetContext() ).installBusyBox();
        Assert.assertNotNull( pathToBusybox );
        Assert.assertEquals( "v1.21.0", new BusyBoxBinary( pathToBusybox ).getVersion() );

        Assert.assertTrue( new File( pathToBusybox.getParentFile(), "ip" ).exists() );
        Assert.assertTrue( new File( pathToBusybox.getParentFile(), "ipaddr" ).exists() );
        Assert.assertTrue( new File( pathToBusybox.getParentFile(), "iproute" ).exists() );
        Assert.assertTrue( new File( pathToBusybox.getParentFile(), "iprule" ).exists() );
        Assert.assertTrue( new File( pathToBusybox.getParentFile(), "iptunnel" ).exists() );
        Assert.assertTrue( new File( pathToBusybox.getParentFile(), "kill" ).exists() );
        Assert.assertTrue( new File( pathToBusybox.getParentFile(), "killall" ).exists() );
        Assert.assertTrue( new File( pathToBusybox.getParentFile(), "insmod" ).exists() );
        Assert.assertTrue( new File( pathToBusybox.getParentFile(), "lsmod" ).exists() );
        Assert.assertTrue( new File( pathToBusybox.getParentFile(), "rmmod" ).exists() );
    }

    public void test_installBusyBox_verify_location() throws InstallFailed
    {
        File pathToBusybox = new Installer( getTargetContext() ).installBusyBox();
        Assert.assertEquals(
                new File( getBinDir(), "busybox" ),
                pathToBusybox
        );
    }

    public void test_installBusyBox_twice_succeeds() throws InstallFailed
    {
        new Installer( getTargetContext() ).installBusyBox();
        new Installer( getTargetContext() ).installBusyBox();
    }


    /**
     * Returns path to the directory where app local binaries are stored.
     * This directory should always be retrieved through out the app the
     * same way as implemente in this method.
     *
     * @return path to the directory where app local binaries are stored.
     */
    private File getBinDir()
    {
        return getTargetContext().getDir( "bin", Context.MODE_PRIVATE );
    }

    private Context getTargetContext()
    {
        return getInstrumentation().getTargetContext();
    }
}
