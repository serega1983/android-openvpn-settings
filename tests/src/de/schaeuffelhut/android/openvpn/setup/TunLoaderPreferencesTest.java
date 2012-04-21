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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.InstrumentationTestCase;
import android.test.MoreAsserts;
import de.schaeuffelhut.android.openvpn.Preferences;
import junit.framework.Assert;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: fries
 * Date: 4/21/12
 * Time: 8:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class TunLoaderPreferencesTest extends InstrumentationTestCase
{
    private Context context;
    private TunLoaderPreferences preferences;
    private SharedPreferences sharedPreferences;

    public void setUp()
    {
        context = getInstrumentation().getContext();
        preferences = new TunLoaderPreferences( context );
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );
    }

    /*
     * getType()
     */

    public void test_getType_with_default_value()
    {
        preferences.removeType();
        Assert.assertEquals( TunLoaders.Types.NONE, preferences.getType() );
    }

    public void test_getType_with_value_NONE()
    {
        preferences.setTypeToNone();
        Assert.assertEquals( TunLoaders.Types.NONE, preferences.getType() );
    }

    public void test_getType_with_value_LEGACY()
    {
        preferences.setTypeToLegacy();
        Assert.assertEquals( TunLoaders.Types.LEGACY, preferences.getType() );
    }

    public void test_getType_with_value_MODPROBE()
    {
        preferences.setTypeToModprobe();
        Assert.assertEquals( TunLoaders.Types.MODPROBE, preferences.getType() );
    }

    public void test_getType_with_value_INSMOD()
    {
        preferences.setTypeToInsmod( new File( "/system/lib/modules/tun.ko" ) );
        Assert.assertEquals( TunLoaders.Types.INSMOD, preferences.getType() );
    }

    public void test_setTypeToInsmod_sets_pathToModule()
    {
        final String uniqueFileName = "/system/lib/modules/tun.ko-" + System.currentTimeMillis();
        preferences.setTypeToInsmod( new File( uniqueFileName ) );
        Assert.assertEquals( new File( uniqueFileName ), preferences.getPathToModule() );
    }

    public void test_getPathToModule_with_value1()
    {
        final String uniqueFileName = "/system/lib/modules/tun.ko-" + System.currentTimeMillis();
        preferences.setPathToModule( new File( uniqueFileName ) );
        Assert.assertEquals( new File( uniqueFileName ), preferences.getPathToModule() );
    }

    public void test_createTunLoader_type_NONE()
    {
        preferences.setTypeToNone();
        TunLoader tunLoader = preferences.createTunLoader();
        MoreAsserts.assertAssignableFrom( TunLoaders.NullTunLoader.class, tunLoader );
        Assert.assertFalse( tunLoader.hasPathToModule() );
    }

    public void test_createTunLoader_type_MODPROBE()
    {
        preferences.setTypeToModprobe();
        TunLoader tunLoader = preferences.createTunLoader();
        MoreAsserts.assertAssignableFrom( TunLoaders.LoadTunViaModprobe.class, tunLoader );
        Assert.assertFalse( tunLoader.hasPathToModule() );
    }

    public void test_createTunLoader_type_INSMOD()
    {
        final String uniqueFileName = "/system/lib/modules/tun.ko-" + System.currentTimeMillis();
        preferences.setTypeToInsmod( new File( uniqueFileName ) );
        TunLoader tunLoader = preferences.createTunLoader();
        MoreAsserts.assertAssignableFrom( TunLoaders.LoadTunViaInsmod.class, tunLoader );
        Assert.assertTrue( tunLoader.hasPathToModule() );
        Assert.assertEquals( new File( uniqueFileName ), tunLoader.getPathToModule() );
    }

    public void test_createTunLoader_type_LEGACY_modprobe()
    {
        preferences.setTypeToLegacy();
        Preferences.setDoModprobeTun( context, true );
        Preferences.setModprobeAlternativeToModprobe( sharedPreferences );
        Preferences.setPathToTun( sharedPreferences, new File( "tun" ) );
        MoreAsserts.assertAssignableFrom( TunLoaders.LoadTunViaModprobe.class, preferences.createTunLoader() );
    }

    public void test_createTunLoader_type_LEGACY_modprobe_with_parameter()
    {
        preferences.setTypeToLegacy();
        Preferences.setDoModprobeTun( context, true );
        Preferences.setModprobeAlternativeToModprobe( sharedPreferences );
        Preferences.setPathToTun( sharedPreferences, new File( "/system/lib/modules/tun.ko" ) );
        MoreAsserts.assertAssignableFrom( TunLoaders.LoadTunViaModprobeWithParameter.class, TunLoaders.createFromLegacyDefinition( sharedPreferences ) );
    }

    public void test_createTunLoader_type_LEGACY_insmod()
    {
        preferences.setTypeToLegacy();
        Preferences.setDoModprobeTun( context, true );
        Preferences.setModprobeAlternativeToInsmod( sharedPreferences );
        Preferences.setPathToTun( sharedPreferences, new File( "/system/lib/modules/tun.ko" ) );
        MoreAsserts.assertAssignableFrom( TunLoaders.LoadTunViaInsmod.class, preferences.createTunLoader() );
    }

    public void test_createTunLoader_type_LEGACY_insmod_check_pathToModule()
    {
        preferences.setTypeToLegacy();
        Preferences.setDoModprobeTun( context, true );
        Preferences.setModprobeAlternativeToInsmod( sharedPreferences );
        String uniqueFile = "/system/lib/modules/tun.ko" + System.currentTimeMillis();
        Preferences.setPathToTun( sharedPreferences, new File( uniqueFile ) );
        Assert.assertEquals( new File( uniqueFile ), preferences.createTunLoader().getPathToModule() );
    }

    public void test_createTunLoader_type_LEGACY_modprobe_with_parameter_check_hasPathToModule_equals_true()
    {
        preferences.setTypeToLegacy();
        Preferences.setDoModprobeTun( context, true );
        Preferences.setModprobeAlternativeToModprobe( sharedPreferences );
        Preferences.setPathToTun( sharedPreferences, new File( "some-tun" ) );
        Assert.assertTrue( preferences.createTunLoader().hasPathToModule() );
    }

    public void test_createTunLoader_type_LEGACY_modprobe_check_hasPathToModule_equals_false()
    {
        preferences.setTypeToLegacy();
        Preferences.setDoModprobeTun( context, true );
        Preferences.setModprobeAlternativeToModprobe( sharedPreferences );
        Preferences.setPathToTun( sharedPreferences, new File( "tun" ) );
        Assert.assertFalse( preferences.createTunLoader().hasPathToModule() );
    }

    public void test_createTunLoader_type_LEGACY_modprobe_with_parameter_check_getPathToModule()
    {
        preferences.setTypeToLegacy();
        Preferences.setDoModprobeTun( context, true );
        Preferences.setModprobeAlternativeToModprobe( sharedPreferences );
        String uniqueFile = "/system/lib/modules/tun.ko" + System.currentTimeMillis();
        Preferences.setPathToTun( sharedPreferences, new File( uniqueFile ) );
        Assert.assertEquals( new File( uniqueFile ), preferences.createTunLoader().getPathToModule() );
    }

    public void test_createTunLoader_type_LEGACY_with_doModprobe_false_returns_NullTunLoader()
    {
        preferences.setTypeToLegacy();
        Preferences.setDoModprobeTun( context, false );
        MoreAsserts.assertAssignableFrom( TunLoaders.NullTunLoader.class, preferences.createTunLoader() );
    }

}
