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

import android.net.Uri;
import de.schaeuffelhut.android.openvpn.lib.app.R;
import de.schaeuffelhut.android.util.FakeFile;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: fries
 * Date: 4/30/12
 * Time: 3:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProbeExecutableTest extends TestCase
{
    private FakeFile XBIN = new FakeFile( "/system/xbin/openvpn" );
    private FakeFile BIN = new FakeFile( "/system/bin/openvpn" );

    public void test_probe_returns_not_null()
    {
        ProbeExecutable probe = new ProbeExecutable( "Title1", "", R.string.prerequisites_item_title_getTunInstaller, Uri.parse( "market://details?id=stericson.busybox" ), (File)XBIN );
        ProbeResult result = probe.probe();
        Assert.assertNotNull( result );
    }

    public void test_probe_returns_failure_when_not_exists()
    {
        XBIN.setExists( false );
        ProbeExecutable probe = new ProbeExecutable( "Title1", "", R.string.prerequisites_item_title_getTunInstaller, Uri.parse( "market://details?id=stericson.busybox" ), XBIN );
        ProbeResult result = probe.probe();
        Assert.assertEquals( PrerequisitesActivity.Status.FAILED, result.status );
    }

    public void test_probe_returns_success_when_exists()
    {
        XBIN.setExists( true );
        ProbeExecutable probe = new ProbeExecutable( "Title1", "", R.string.prerequisites_item_title_getTunInstaller, Uri.parse( "market://details?id=stericson.busybox" ), XBIN );
        ProbeResult result = probe.probe();
        Assert.assertEquals( PrerequisitesActivity.Status.SUCCESS, result.status );
    }

    public void test_probe_returns_success_on_second_file()
    {
        XBIN.setExists( false );
        BIN.setExists( true );
        ProbeExecutable probe = new ProbeExecutable( "Title1", "", R.string.prerequisites_item_title_getTunInstaller, Uri.parse( "market://details?id=stericson.busybox" ), (File)XBIN,  (File)BIN );
        ProbeResult result = probe.probe();
        Assert.assertEquals( PrerequisitesActivity.Status.SUCCESS, result.status );
    }

    public void test_probe_returns_title1()
    {
        ProbeExecutable probe = new ProbeExecutable( "Title1", "", R.string.prerequisites_item_title_getTunInstaller, Uri.parse( "market://details?id=stericson.busybox" ), (File)XBIN,  (File)BIN );
        ProbeResult result = probe.probe();
        Assert.assertEquals( "Title1", result.title );
    }

    public void test_probe_returns_title2()
    {
        ProbeExecutable probe = new ProbeExecutable( "Title2", "", R.string.prerequisites_item_title_getTunInstaller, Uri.parse( "market://details?id=stericson.busybox" ), (File)XBIN,  (File)BIN );
        ProbeResult result = probe.probe();
        Assert.assertEquals( "Title2", result.title );
    }

    public void test_probe_returns_subtitle1()
    {
        ProbeExecutable probe = new ProbeExecutable( "Title1", "Subtitle1", R.string.prerequisites_item_title_getTunInstaller, Uri.parse( "market://details?id=stericson.busybox" ), (File)XBIN,  (File)BIN );
        ProbeResult result = probe.probe();
        Assert.assertEquals( "Subtitle1", result.subtitle );
    }

    public void test_probe_returns_subtitle2()
    {
        ProbeExecutable probe = new ProbeExecutable( "Title2", "Subtitle2", R.string.prerequisites_item_title_getTunInstaller, Uri.parse( "market://details?id=stericson.busybox" ), (File)XBIN,  (File)BIN );
        ProbeResult result = probe.probe();
        Assert.assertEquals( "Subtitle2", result.subtitle );
    }

}
