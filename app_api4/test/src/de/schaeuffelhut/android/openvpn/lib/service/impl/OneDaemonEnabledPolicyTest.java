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

package de.schaeuffelhut.android.openvpn.lib.service.impl;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.InstrumentationTestCase;
import de.schaeuffelhut.android.openvpn.Preferences;
import de.schaeuffelhut.android.openvpn.lib.service.impl.OneDaemonEnabledPolicy;
import org.mockito.Mockito;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.verify;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-11-03
 */
public class OneDaemonEnabledPolicyTest extends InstrumentationTestCase
{

    private SharedPreferences sharedPreferences;

    private OneDaemonEnabledPolicy newOneDaemonEnabledPolicy(List<File> configFiles)
    {
        configurePreferences( configFiles );
        return new OneDaemonEnabledPolicy( sharedPreferences, configFiles );
    }

    private void configurePreferences(List<File> configFiles)
    {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        for (File configFile : configFiles)
            edit.putBoolean( intendedStateOf( configFile ), configFile.getName().contains( "ALIVE" ) );
        edit.commit();
    }

    private void expectIllegalStateExceptionOnGetConfig(OneDaemonEnabledPolicy oneDaemonEnabledPolicy)
    {
        try
        {
            oneDaemonEnabledPolicy.getCurrent();
            fail( "IllegalStateException expected" );
        }
        catch (IllegalStateException e)
        {
            // expected
        }
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences( getInstrumentation().getContext() );
    }


    public void test_getCurrent__with_no_configs()
    {
        OneDaemonEnabledPolicy oneDaemonEnabledPolicy = newOneDaemonEnabledPolicy( Collections.<File>emptyList() );
        oneDaemonEnabledPolicy.initialize();

        assertFalse( oneDaemonEnabledPolicy.hasEnabledConfig() );
        expectIllegalStateExceptionOnGetConfig( oneDaemonEnabledPolicy );
    }

    public void test_getCurrent__with_one_alive_config()
    {
        OneDaemonEnabledPolicy oneDaemonEnabledPolicy = newOneDaemonEnabledPolicy( Arrays.asList( newExistingFile( "/sdcard/openvpn/test1-ALIVE.conf" ) ) );
        oneDaemonEnabledPolicy.initialize();

        assertTrue( oneDaemonEnabledPolicy.hasEnabledConfig() );
        assertEquals( new File( "/sdcard/openvpn/test1-ALIVE.conf" ), oneDaemonEnabledPolicy.getCurrent() );
    }

    public void test_getCurrent__with_one_dead_config()
    {
        OneDaemonEnabledPolicy oneDaemonEnabledPolicy = newOneDaemonEnabledPolicy( Arrays.asList( newExistingFile( "/sdcard/openvpn/test1-DEAD.conf" ) ) );
        oneDaemonEnabledPolicy.initialize();

        assertFalse( oneDaemonEnabledPolicy.hasEnabledConfig() );
        expectIllegalStateExceptionOnGetConfig( oneDaemonEnabledPolicy );
    }

    public void test_getCurrent__with_one_dead_and_one_alive_config()
    {
        OneDaemonEnabledPolicy oneDaemonEnabledPolicy = newOneDaemonEnabledPolicy( Arrays.asList(
                newExistingFile( "/sdcard/openvpn/test1-DEAD.conf" ),
                newExistingFile( "/sdcard/openvpn/test2-ALIVE.conf" )
        ) );
        oneDaemonEnabledPolicy.initialize();

        assertTrue( oneDaemonEnabledPolicy.hasEnabledConfig() );
        assertEquals( new File( "/sdcard/openvpn/test2-ALIVE.conf" ), oneDaemonEnabledPolicy.getCurrent() );
    }

    public void test_getCurrent__with_two_alive_configs()
    {
        OneDaemonEnabledPolicy oneDaemonEnabledPolicy = newOneDaemonEnabledPolicy( Arrays.asList(
                newExistingFile( "/sdcard/openvpn/test1-ALIVE.conf" ),
                newExistingFile( "/sdcard/openvpn/test2-ALIVE.conf" )
        ) );
        oneDaemonEnabledPolicy.initialize();

        assertFalse( oneDaemonEnabledPolicy.hasEnabledConfig() );
        expectIllegalStateExceptionOnGetConfig( oneDaemonEnabledPolicy );
    }


    public void test_getCurrent__with_two_dead_configs()
    {
        OneDaemonEnabledPolicy oneDaemonEnabledPolicy = newOneDaemonEnabledPolicy( Arrays.asList(
                newExistingFile( "/sdcard/openvpn/test1-DEAD.conf" ),
                newExistingFile( "/sdcard/openvpn/test2-DEAD.conf" )
        ) );
        oneDaemonEnabledPolicy.initialize();

        assertFalse( oneDaemonEnabledPolicy.hasEnabledConfig() );
        expectIllegalStateExceptionOnGetConfig( oneDaemonEnabledPolicy );
    }

    public void test_init__with_two_alive_configs_disables_preferences()
    {
        OneDaemonEnabledPolicy oneDaemonEnabledPolicy = newOneDaemonEnabledPolicy( Arrays.asList(
                newExistingFile( "/sdcard/openvpn/test1-ALIVE.conf" ),
                newExistingFile( "/sdcard/openvpn/test2-ALIVE.conf" )
        ) );

        assertTrue( sharedPreferences.getBoolean( intendedStateOf( newExistingFile( "/sdcard/openvpn/test1-ALIVE.conf" ) ), false ) );
        assertTrue( sharedPreferences.getBoolean( intendedStateOf( newExistingFile( "/sdcard/openvpn/test2-ALIVE.conf" ) ), false ) );

        oneDaemonEnabledPolicy.initialize();

        assertFalse( sharedPreferences.getBoolean( intendedStateOf( newExistingFile( "/sdcard/openvpn/test1-ALIVE.conf" ) ), false ) );
        assertFalse( sharedPreferences.getBoolean( intendedStateOf( newExistingFile( "/sdcard/openvpn/test2-ALIVE.conf" ) ), false ) );
    }

    public void test_init__with_alive_dead_alive_configs_stops_daemons()
    {
        OneDaemonEnabledPolicy oneDaemonEnabledPolicy = newOneDaemonEnabledPolicy( Arrays.asList(
                newExistingFile( "/sdcard/openvpn/test1-ALIVE.conf" ),
                newExistingFile( "/sdcard/openvpn/test2-DEAD.conf" ),
                newExistingFile( "/sdcard/openvpn/test3-ALIVE.conf" )
        ) );

        assertTrue( sharedPreferences.getBoolean( intendedStateOf( newExistingFile( "/sdcard/openvpn/test1-ALIVE.conf" ) ), false ) );
        assertFalse( sharedPreferences.getBoolean( intendedStateOf( newExistingFile( "/sdcard/openvpn/test2-DEAD.conf" ) ), false ) );
        assertTrue( sharedPreferences.getBoolean( intendedStateOf( newExistingFile( "/sdcard/openvpn/test3-ALIVE.conf" ) ), false ) );

        oneDaemonEnabledPolicy.initialize();

        assertFalse( sharedPreferences.getBoolean( intendedStateOf( newExistingFile( "/sdcard/openvpn/test1-ALIVE.conf" ) ), false ) );
        assertFalse( sharedPreferences.getBoolean( intendedStateOf( newExistingFile( "/sdcard/openvpn/test2-DEAD.conf" ) ), false ) );
        assertFalse( sharedPreferences.getBoolean( intendedStateOf( newExistingFile( "/sdcard/openvpn/test3-ALIVE.conf" ) ), false ) );
    }

    private String intendedStateOf(File config)
    {
        return Preferences.KEY_CONFIG_INTENDED_STATE( config );
    }

    public void test_disables_enabled_config_when_file_does_not_exist()
    {
        OneDaemonEnabledPolicy oneDaemonEnabledPolicy = newOneDaemonEnabledPolicy( Arrays.asList(
                newDeletedFile( "/sdcard/openvpn/test1-ALIVE.conf" )
        ) );

        assertTrue( sharedPreferences.getBoolean( intendedStateOf( newExistingFile( "/sdcard/openvpn/test1-ALIVE.conf" ) ), false ) );

        oneDaemonEnabledPolicy.initialize();

        assertFalse( sharedPreferences.getBoolean( intendedStateOf( newExistingFile( "/sdcard/openvpn/test1-ALIVE.conf" ) ), false ) );
        assertFalse( oneDaemonEnabledPolicy.hasEnabledConfig() );
    }

    private class FakeFile extends File {

        private final boolean exists;

        public FakeFile(String path, boolean exists)
        {
            super( path );
            this.exists = exists;
        }

        @Override
        public boolean exists()
        {
            return exists;
        }
    }
    private File newExistingFile(String path)
    {
        return new FakeFile( path, true );
    }

    private File newDeletedFile(String path)
    {
        return new FakeFile( path, false );
    }
}
