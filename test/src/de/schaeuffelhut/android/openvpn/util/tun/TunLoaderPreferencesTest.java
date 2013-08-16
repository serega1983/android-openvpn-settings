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

package de.schaeuffelhut.android.openvpn.util.tun;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.InstrumentationTestCase;
import android.test.MoreAsserts;
import de.schaeuffelhut.android.openvpn.util.tun.TunPreferences;
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
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );
        sharedPreferences.edit().clear().commit(); // Very important, otherwise we depend on unknown values
        preferences = new TunLoaderPreferences( context );
    }

    /*
     * getType()
     */

    public void test_getType_defaults_to_NONE_when_doModprobeIs_false()
    {
        preferences.removeType();
        TunPreferences.setDoModprobeTun( context, false );
        Assert.assertEquals( TunLoaderFactoryImpl.Types.NONE, preferences.getType() );
    }

    public void test_getType_defaults_to_LEGACY_when_doModprobeIs_true()
    {
        preferences.removeType();
        TunPreferences.setDoModprobeTun( context, true );
        Assert.assertEquals( TunLoaderFactoryImpl.Types.LEGACY, preferences.getType() );
    }

    public void test_getType_with_value_NONE()
    {
        preferences.setTypeToNone();
        Assert.assertEquals( TunLoaderFactoryImpl.Types.NONE, preferences.getType() );
    }

    public void test_getType_with_value_LEGACY()
    {
        preferences.setTypeToLegacy();
        Assert.assertEquals( TunLoaderFactoryImpl.Types.LEGACY, preferences.getType() );
    }

    public void test_getType_with_value_MODPROBE()
    {
        preferences.setTypeToModprobe();
        Assert.assertEquals( TunLoaderFactoryImpl.Types.MODPROBE, preferences.getType() );
    }

    public void test_getType_with_value_INSMOD()
    {
        preferences.setTypeToInsmod( new File( "/system/lib/modules/tun.ko" ) );
        Assert.assertEquals( TunLoaderFactoryImpl.Types.INSMOD, preferences.getType() );
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
        MoreAsserts.assertAssignableFrom( TunLoaderFactoryImpl.NullTunLoader.class, tunLoader );
        Assert.assertFalse( tunLoader.hasPathToModule() );
    }

    public void test_createTunLoader_type_MODPROBE()
    {
        preferences.setTypeToModprobe();
        TunLoader tunLoader = preferences.createTunLoader();
        MoreAsserts.assertAssignableFrom( TunLoaderFactoryImpl.LoadTunViaModprobe.class, tunLoader );
        Assert.assertFalse( tunLoader.hasPathToModule() );
    }

    public void test_createTunLoader_type_INSMOD()
    {
        final String uniqueFileName = "/system/lib/modules/tun.ko-" + System.currentTimeMillis();
        preferences.setTypeToInsmod( new File( uniqueFileName ) );
        TunLoader tunLoader = preferences.createTunLoader();
        MoreAsserts.assertAssignableFrom( TunLoaderFactoryImpl.LoadTunViaInsmod.class, tunLoader );
        Assert.assertTrue( tunLoader.hasPathToModule() );
        Assert.assertEquals( new File( uniqueFileName ), tunLoader.getPathToModule() );
    }

    public void test_createTunLoader_type_LEGACY_modprobe()
    {
        preferences.setTypeToLegacy();
        TunPreferences.setDoModprobeTun( context, true );
        TunPreferences.setModprobeAlternativeToModprobe( sharedPreferences );
        TunPreferences.setPathToTun( sharedPreferences, new File( "tun" ) );
        MoreAsserts.assertAssignableFrom( TunLoaderFactoryImpl.LoadTunViaModprobe.class, preferences.createTunLoader() );
    }

    public void test_createTunLoader_type_LEGACY_modprobe_with_parameter()
    {
        preferences.setTypeToLegacy();
        TunPreferences.setDoModprobeTun( context, true );
        TunPreferences.setModprobeAlternativeToModprobe( sharedPreferences );
        TunPreferences.setPathToTun( sharedPreferences, new File( "/system/lib/modules/tun.ko" ) );
        MoreAsserts.assertAssignableFrom( TunLoaderFactoryImpl.LoadTunViaModprobeWithParameter.class, TunLoaderFactoryImpl.createFromLegacyDefinition( sharedPreferences ) );
    }

    public void test_createTunLoader_type_LEGACY_insmod()
    {
        preferences.setTypeToLegacy();
        TunPreferences.setDoModprobeTun( context, true );
        TunPreferences.setModprobeAlternativeToInsmod( sharedPreferences );
        TunPreferences.setPathToTun( sharedPreferences, new File( "/system/lib/modules/tun.ko" ) );
        MoreAsserts.assertAssignableFrom( TunLoaderFactoryImpl.LoadTunViaInsmod.class, preferences.createTunLoader() );
    }

    public void test_createTunLoader_type_LEGACY_insmod_check_pathToModule()
    {
        preferences.setTypeToLegacy();
        TunPreferences.setDoModprobeTun( context, true );
        TunPreferences.setModprobeAlternativeToInsmod( sharedPreferences );
        String uniqueFile = "/system/lib/modules/tun.ko" + System.currentTimeMillis();
        TunPreferences.setPathToTun( sharedPreferences, new File( uniqueFile ) );
        Assert.assertEquals( new File( uniqueFile ), preferences.createTunLoader().getPathToModule() );
    }

    public void test_createTunLoader_type_LEGACY_modprobe_with_parameter_check_hasPathToModule_equals_true()
    {
        preferences.setTypeToLegacy();
        TunPreferences.setDoModprobeTun( context, true );
        TunPreferences.setModprobeAlternativeToModprobe( sharedPreferences );
        TunPreferences.setPathToTun( sharedPreferences, new File( "some-tun" ) );
        Assert.assertTrue( preferences.createTunLoader().hasPathToModule() );
    }

    public void test_createTunLoader_type_LEGACY_modprobe_check_hasPathToModule_equals_false()
    {
        preferences.setTypeToLegacy();
        TunPreferences.setDoModprobeTun( context, true );
        TunPreferences.setModprobeAlternativeToModprobe( sharedPreferences );
        TunPreferences.setPathToTun( sharedPreferences, new File( "tun" ) );
        Assert.assertFalse( preferences.createTunLoader().hasPathToModule() );
    }

    public void test_createTunLoader_type_LEGACY_modprobe_with_parameter_check_getPathToModule()
    {
        preferences.setTypeToLegacy();
        TunPreferences.setDoModprobeTun( context, true );
        TunPreferences.setModprobeAlternativeToModprobe( sharedPreferences );
        String uniqueFile = "/system/lib/modules/tun.ko" + System.currentTimeMillis();
        TunPreferences.setPathToTun( sharedPreferences, new File( uniqueFile ) );
        Assert.assertEquals( new File( uniqueFile ), preferences.createTunLoader().getPathToModule() );
    }

    public void test_createTunLoader_type_LEGACY_with_doModprobe_false_returns_NullTunLoader()
    {
        preferences.setTypeToLegacy();
        TunPreferences.setDoModprobeTun( context, false );
        MoreAsserts.assertAssignableFrom( TunLoaderFactoryImpl.NullTunLoader.class, preferences.createTunLoader() );
    }

    public void test_fix_for__createTunLoader_type_LEGACY_did_throw_a_NPE()
    {
        preferences.setTypeToLegacy();
        preferences.removePathToModule();
        Assert.assertNotNull( preferences.createTunLoader() );
    }

}

