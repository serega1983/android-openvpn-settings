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

package de.schaeuffelhut.android.openvpn;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.InstrumentationTestCase;
import de.schaeuffelhut.android.openvpn.util.tun.TunPreferences;
import junit.framework.Assert;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: fries
 * Date: 4/21/12
 * Time: 6:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class PreferencesTest extends InstrumentationTestCase
{
    private Context context;
    private SharedPreferences preferences;

    public void setUp()
    {
        context = getInstrumentation().getContext();
        preferences = PreferenceManager.getDefaultSharedPreferences( context );
    }

    public void test_doModprobeTun()
    {
        TunPreferences.setDoModprobeTun( context, true );
        Assert.assertTrue( TunPreferences.getDoModprobeTun( preferences ) );

        TunPreferences.setDoModprobeTun( context, false );
        Assert.assertFalse( TunPreferences.getDoModprobeTun( preferences ) );
    }

    public void test_getLoadTunModulCommand_modprobe_tun()
    {
        TunPreferences.setModprobeAlternativeToModprobe( preferences );
        TunPreferences.setPathToTun( preferences, new File( "tun" ) );
        Assert.assertEquals( "modprobe 'tun'", TunPreferences.getLoadTunModuleCommand( preferences ) );
    }

    public void test_getLoadTunModulCommand_insmod_system_lib_modules_tun_ko()
    {
        TunPreferences.setModprobeAlternativeToInsmod( preferences );
        TunPreferences.setPathToTun( preferences, new File( "/system/lib/modules/tun.ko" ) );
        Assert.assertEquals( "insmod '/system/lib/modules/tun.ko'", TunPreferences.getLoadTunModuleCommand( preferences ) );
    }

}
