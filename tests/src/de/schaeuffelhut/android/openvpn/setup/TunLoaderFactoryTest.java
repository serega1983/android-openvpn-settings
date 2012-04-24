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
 * Time: 6:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class TunLoaderFactoryTest extends InstrumentationTestCase
{
    protected static final File PATH1 = new File( "/system/lib/modules/tun.ko" );
    protected static final File PATH2 = new File( "/lib/modules/tun.ko" );
    private Context context;
    private SharedPreferences preferences;

    public void setUp()
    {
        context = getInstrumentation().getContext();
        preferences = PreferenceManager.getDefaultSharedPreferences( context );
    }

    public void test_createFromLegacyDefinition__should_return_LoadTunViaModprobe()
    {
        Preferences.setDoModprobeTun( context, true );
        Preferences.setModprobeAlternativeToModprobe( preferences );
        Preferences.setPathToTun( preferences, new File( "tun" ) );
        MoreAsserts.assertAssignableFrom( TunLoaderFactory.LoadTunViaModprobe.class, TunLoaderFactory.createFromLegacyDefinition( preferences ) );
    }

    public void test_createFromLegacyDefinition__should_return_LoadTunViaModprobeWithParameter()
    {
        Preferences.setDoModprobeTun( context, true );
        Preferences.setModprobeAlternativeToModprobe( preferences );
        Preferences.setPathToTun( preferences, PATH1 );
        MoreAsserts.assertAssignableFrom( TunLoaderFactory.LoadTunViaModprobeWithParameter.class, TunLoaderFactory.createFromLegacyDefinition( preferences ) );
    }

    public void test_createFromLegacyDefinition__should_return_LoadTunViaInsmod()
    {
        Preferences.setDoModprobeTun( context, true );
        Preferences.setModprobeAlternativeToInsmod( preferences );
        MoreAsserts.assertAssignableFrom( TunLoaderFactory.LoadTunViaInsmod.class, TunLoaderFactory.createFromLegacyDefinition( preferences ) );
    }

    public void test_createFromLegacyDefinition__should_return_LoadTunViaInsmod_getPathToModule_equals_path1()
    {
        Preferences.setDoModprobeTun( context, true );
        Preferences.setModprobeAlternativeToInsmod( preferences );
        Preferences.setPathToTun( preferences, PATH1 );
        Assert.assertEquals( PATH1, TunLoaderFactory.createFromLegacyDefinition( preferences ).getPathToModule() );
    }

    public void test_createFromLegacyDefinition__should_return_LoadTunViaInsmod_getPathToModule_equals_path2()
    {
        Preferences.setDoModprobeTun( context, true );
        Preferences.setModprobeAlternativeToInsmod( preferences );
        Preferences.setPathToTun( preferences, PATH2 );
        Assert.assertEquals( PATH2, TunLoaderFactory.createFromLegacyDefinition( preferences ).getPathToModule() );
    }

    public void test_createFromLegacyDefinition__should_return_LoadTunViaModprobe_hasPathToModule_equals_true()
    {
        Preferences.setDoModprobeTun( context, true );
        Preferences.setModprobeAlternativeToModprobe( preferences );
        Preferences.setPathToTun( preferences, new File( "some-tun" ) );
        Assert.assertTrue( TunLoaderFactory.createFromLegacyDefinition( preferences ).hasPathToModule() );
    }

    public void test_createFromLegacyDefinition__should_return_LoadTunViaModprobe_hasPathToModule_equals_false()
    {
        Preferences.setDoModprobeTun( context, true );
        Preferences.setModprobeAlternativeToModprobe( preferences );
        Preferences.setPathToTun( preferences, new File( "tun" ) );
        Assert.assertFalse( TunLoaderFactory.createFromLegacyDefinition( preferences ).hasPathToModule() );
    }

    public void test_createFromLegacyDefinition__should_return_LoadTunViaModprobe_getPathToModule()
    {
        Preferences.setDoModprobeTun( context, true );
        Preferences.setModprobeAlternativeToModprobe( preferences );
        Preferences.setPathToTun( preferences, new File( "some-tun" ) );
        Assert.assertEquals( new File( "some-tun" ), TunLoaderFactory.createFromLegacyDefinition( preferences ).getPathToModule() );
    }

    public void test_hasLegacyDefinition_returns_true()
    {
        Preferences.setDoModprobeTun( context, true );
        Assert.assertTrue( TunLoaderFactory.hasLegacyDefinition( preferences ) );
    }

    public void test_hasLegacyDefinition_returns_false()
    {
        Preferences.setDoModprobeTun( context, false );
        Assert.assertFalse( TunLoaderFactory.hasLegacyDefinition( preferences ) );
    }

    public void test_createFromLegacyDefinition_throws_when_hasLegacyDefinition_returns_false()
    {
        Preferences.setDoModprobeTun( context, false );
        try
        {
            TunLoaderFactory.createFromLegacyDefinition( preferences );
            fail( "IllegalStateException expected" );
        }
        catch (IllegalStateException e)
        {
            //ok
        }
    }

    public void test_NullTunLoader_makeDefault()
    {
        TunLoader tunLoader = new TunLoaderFactory.NullTunLoader();
        tunLoader.makeDefault( new TunLoaderPreferences( context ) );
        Assert.assertEquals( TunLoaderFactory.Types.NONE, new TunLoaderPreferences( context ).getType() );
    }

    public void test_LoadTunViaInsmod_makeDefault()
    {
        TunLoader tunLoader = new TunLoaderFactory.LoadTunViaInsmod( PATH1 );
        tunLoader.makeDefault( new TunLoaderPreferences( context ) );
        Assert.assertEquals( TunLoaderFactory.Types.INSMOD, new TunLoaderPreferences( context ).getType() );
    }

    public void test_LoadTunViaModprobe_makeDefault()
    {
        TunLoader tunLoader = new TunLoaderFactory.LoadTunViaModprobe();
        tunLoader.makeDefault( new TunLoaderPreferences( context ) );
        Assert.assertEquals( TunLoaderFactory.Types.MODPROBE, new TunLoaderPreferences( context ).getType() );
    }

    public void test_LoadTunViaModprobeWithParameter_makeDefault()
    {
        TunLoader tunLoader = new TunLoaderFactory.LoadTunViaModprobeWithParameter( PATH1 );
        tunLoader.makeDefault( new TunLoaderPreferences( context ) );
        Assert.assertEquals( TunLoaderFactory.Types.LEGACY, new TunLoaderPreferences( context ).getType() );
    }

    public void test_createModprobe()
    {
        MoreAsserts.assertAssignableFrom( TunLoaderFactory.LoadTunViaModprobe.class, new TunLoaderFactory().createModprobe() );
    }

    public void test_createInsmod()
    {
        File pathToModule = new File( "/lib/modules/tun.ko-"+System.currentTimeMillis() );
        TunLoader insmod = new TunLoaderFactory().createInsmod( pathToModule );
        MoreAsserts.assertAssignableFrom( TunLoaderFactory.LoadTunViaInsmod.class, insmod );
        Assert.assertEquals( pathToModule, insmod.getPathToModule() );
    }

    public void test_createNullTunLoader()
    {
        MoreAsserts.assertAssignableFrom( TunLoaderFactory.NullTunLoader.class, new TunLoaderFactory().createNullTunLoader() );
    }


}
