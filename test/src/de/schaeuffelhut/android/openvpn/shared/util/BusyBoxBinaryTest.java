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

package de.schaeuffelhut.android.openvpn.shared.util;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-01-25
 */
public class BusyBoxBinaryTest extends TestCase
{
    public void test_BUSYBOX_USAGE_SMALL_has_16_lines()
    {
        Assert.assertEquals( 16, BUSYBOX_USAGE_SMALL.size() );
    }

    public void test_version_1209()
    {
        BusyBoxBinary busyBoxBinary = new BusyBoxBinary(
                new File( "busybox" ),
                Collections.unmodifiableList( Arrays.asList( ("" +
                        "BusyBox v1.20.9.git (2013-01-25 17:41:48 CET) multi-call binary.\n" +
                        "BusyBox is copyrighted by many authors between 1998-2012.\n" +
                        "Licensed under GPLv2. See source distribution for detailed\n" +
                        "copyright notices.\n" +
                        "\n" +
                        "Usage: busybox [function [arguments]...]\n")
                        .split( "\n" )
                ) ),
                BUSYBOX_USAGE_SMALL_LIST
        );
        Assert.assertEquals( "v1.20.9.git", busyBoxBinary.getVersion() );
    }

    public void test_version_1210()
    {
        BusyBoxBinary busyBoxBinary = new BusyBoxBinary( new File( "busybox" ), BUSYBOX_USAGE_SMALL, BUSYBOX_USAGE_SMALL_LIST );
        Assert.assertEquals( "v1.21.0.git", busyBoxBinary.getVersion() );
    }

    public void test_hasIpAppletIp_true()
    {
        BusyBoxBinary busyBoxBinary = new BusyBoxBinary( new File( "busybox" ), BUSYBOX_USAGE_SMALL, BUSYBOX_USAGE_SMALL_LIST );
        Assert.assertTrue( busyBoxBinary.hasIpApplet() );
    }

    public void test_hasIpAppletIp_false()
    {
        BusyBoxBinary busyBoxBinary = new BusyBoxBinary( new File( "busybox" ), BUSYBOX_USAGE_SMALL,
                Collections.unmodifiableList( Arrays.asList(
                        "insmod", "ipaddr", "iplink", "iproute", "iprule", "iptunnel", "lsmod", "rmmod"
                ) )
        );
        Assert.assertFalse( busyBoxBinary.hasIpApplet() );
    }


    private static final List<String> BUSYBOX_USAGE_SMALL = Collections.unmodifiableList( Arrays.asList( ("" +
            "BusyBox v1.21.0.git (2013-01-25 17:41:48 CET) multi-call binary.\n" +
            "BusyBox is copyrighted by many authors between 1998-2012.\n" +
            "Licensed under GPLv2. See source distribution for detailed\n" +
            "copyright notices.\n" +
            "\n" +
            "Usage: busybox [function [arguments]...]\n" +
            "   or: busybox --list\n" +
            "   or: function [arguments]...\n" +
            "\n" +
            "\tBusyBox is a multi-call binary that combines many common Unix\n" +
            "\tutilities into a single executable.  Most people will create a\n" +
            "\tlink to busybox for each function they wish to use and BusyBox\n" +
            "\twill act like whatever it was invoked as.\n" +
            "\n" +
            "Currently defined functions:\n" +
            "\tinsmod, ip, ipaddr, iplink, iproute, iprule, iptunnel, lsmod, rmmod\n")
            .split( "\n" )
    ) );

    private static final List<String> BUSYBOX_USAGE_SMALL_LIST = Collections.unmodifiableList( Arrays.asList(
            "insmod", "ip", "ipaddr", "iplink", "iproute", "iprule", "iptunnel", "lsmod", "rmmod"
    ) );
}
