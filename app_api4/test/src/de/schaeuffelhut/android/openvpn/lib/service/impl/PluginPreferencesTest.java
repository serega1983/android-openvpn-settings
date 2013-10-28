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

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.test.InstrumentationTestCase;
import de.schaeuffelhut.android.openvpn.EnterPassphrase;
import de.schaeuffelhut.android.openvpn.EnterUserPassword;
import de.schaeuffelhut.android.openvpn.OpenVpnSettings;
import de.schaeuffelhut.android.openvpn.lib.service.impl.PluginPreferences;

import java.io.File;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-11-24
 */
public class PluginPreferencesTest extends InstrumentationTestCase
{
    private static final String PLUGIN_PACKAGE_NAME_1 = "de.schaeuffelhut.android.openvpn.plugin1";
    private static final String PLUGIN_PACKAGE_NAME_2 = "de.schaeuffelhut.android.openvpn.plugin2";

    private PluginPreferences pluginPreferences;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        editPreference().clear().commit();
        pluginPreferences = new PluginPreferences( getInstrumentation().getContext(), PLUGIN_PACKAGE_NAME_1 );
    }


    /**
     * Ensure we return our own EnterPassphrase activity by default.
     * This test bind the component name defined as a sting to it's implementation class.
     *
     * @throws Exception
     */
    public void test_getActivityHandlingPassphraseRequest_default() throws Exception
    {
        assertEquals(
                new ComponentName( getInstrumentation().getTargetContext(), EnterPassphrase.class ),
                pluginPreferences.getActivityHandlingPassphraseRequest()
        );
    }

    public void test_set_get_ActivityHandlingPassphraseRequest() throws Exception
    {
        ComponentName componentName = new ComponentName( "de.schaeuffelhut.android.openvpn.plugin1", "de.schaeuffelhut.android.openvpn.plugin1.EnterPassphrase" );
        pluginPreferences.edit().setActivityHandlingPassphraseRequest( componentName ).commit();
        assertEquals( componentName, pluginPreferences.getActivityHandlingPassphraseRequest() );
    }

    public void test_keep_KEY_ACTIVITY_HANDLING_PASSPHRASE_REQUEST_constant() throws Exception
    {
        editPreference().putString( "activity_handling_passphrase_request", "a/a.b" ).commit();
        assertEquals( new ComponentName( "a", "a.b" ), pluginPreferences.getActivityHandlingPassphraseRequest() );
    }

    /**
     * Make sure we do not store the default component name,
     * so we do not need to migrate any properties after refactoring
     * that component.
     *
     * @throws Exception
     */
    public void test_setActivityHandlingPassphraseRequest_does_not_store_default() throws Exception
    {
        ComponentName componentName = new ComponentName( getInstrumentation().getTargetContext(), EnterPassphrase.class );
        pluginPreferences.edit().setActivityHandlingPassphraseRequest( componentName ).commit();
        assertFalse( preference().contains( "activity_handling_passphrase_request" ) );
    }


    /**
     * Ensure we return our own EnterUserPassword activity by default.
     * This test bind the component name defined as a sting to it's implementation class.
     *
     * @throws Exception
     */
    public void test_getActivityHandlingCredentialsRequest_default() throws Exception
    {
        assertEquals(
                new ComponentName( getInstrumentation().getTargetContext(), EnterUserPassword.class ),
                pluginPreferences.getActivityHandlingCredentialsRequest()
        );
    }

    public void test_set_get_ActivityHandlingCredentialsRequest() throws Exception
    {
        ComponentName componentName = new ComponentName( "de.schaeuffelhut.android.openvpn.plugin1", "de.schaeuffelhut.android.openvpn.plugin1.EnterCredentials" );
        pluginPreferences.edit().setActivityHandlingCredentialsRequest( componentName ).commit();
        assertEquals( componentName, pluginPreferences.getActivityHandlingCredentialsRequest() );
    }

    public void test_keep_KEY_ACTIVITY_HANDLING_CREDENTIALS_REQUEST_constant() throws Exception
    {
        editPreference().putString( "activity_handling_credentials_request", "a/a.b" ).commit();
        assertEquals( new ComponentName( "a", "a.b" ), pluginPreferences.getActivityHandlingCredentialsRequest() );
    }


    /**
     * Make sure we do not store the default component name,
     * so we do not need to migrate any properties after refactoring
     * that component.
     *
     * @throws Exception
     */
    public void test_setActivityHandlingCredentialsRequest_does_not_store_default() throws Exception
    {
        ComponentName componentName = new ComponentName( getInstrumentation().getTargetContext(), EnterUserPassword.class );
        pluginPreferences.edit().setActivityHandlingCredentialsRequest( componentName ).commit();
        assertFalse( preference().contains( "activity_handling_credentials_request" ) );
    }

    /**
     * Ensure we return our own EnterUserPassword activity by default.
     * This test bind the component name defined as a sting to it's implementation class.
     *
     * @throws Exception
     */
    public void test_getActivityHandlingOngoingNotification_default() throws Exception
    {
        assertEquals(
                new ComponentName( getInstrumentation().getTargetContext(), OpenVpnSettings.class ),
                pluginPreferences.getActivityHandlingOngoingNotification()
        );
    }

    public void test_set_get_ActivityHandlingOngoingNotification() throws Exception
    {
        ComponentName componentName = new ComponentName( "de.schaeuffelhut.android.openvpn.plugin1", "de.schaeuffelhut.android.openvpn.plugin1.OngoingNotificationActivity" );
        pluginPreferences.edit().setActivityHandlingOngoingNotification( componentName ).commit();
        assertEquals( componentName, pluginPreferences.getActivityHandlingOngoingNotification() );
    }

    public void test_keep_KEY_ACTIVITY_HANDLING_ONGOING_NOTIFICATION_constant() throws Exception
    {
        editPreference().putString( "activity_handling_ongoing_notification", "a/a.b" ).commit();
        assertEquals( new ComponentName( "a", "a.b" ), pluginPreferences.getActivityHandlingOngoingNotification() );
    }


    /**
     * Make sure we do not store the default component name,
     * so we do not need to migrate any properties after refactoring
     * that component.
     *
     * @throws Exception
     */
    public void test_setActivityHandlingOngoingNotification_does_not_store_default() throws Exception
    {
        ComponentName componentName = new ComponentName( getInstrumentation().getTargetContext(), OpenVpnSettings.class );
        pluginPreferences.edit().setActivityHandlingOngoingNotification( componentName ).commit();
        assertFalse( preference().contains( "activity_handling_ongoing_notification" ) );
    }


    public void test_getConfigDir_default() throws Exception
    {
        assertEquals( new File( Environment.getExternalStorageDirectory(), "openvpn" ), pluginPreferences.getConfigDir() );
    }

    public void test_set_get_ConfigDir() throws Exception
    {
        pluginPreferences.edit().setConfigDir( new File( "/data/data/" + PLUGIN_PACKAGE_NAME_1 + "/config" ) ).commit();
        assertEquals( new File( "/data/data/" + PLUGIN_PACKAGE_NAME_1 + "/config" ), pluginPreferences.getConfigDir() );
    }

    public void test_keep_KEY_CONFIG_DIR_constant() throws Exception
    {
        editPreference().putString( "config_dir", "/sdcard/openvpn" ).commit();
        assertEquals( new File( "/sdcard/openvpn" ), pluginPreferences.getConfigDir() );
    }


    // Utilities

    private SharedPreferences.Editor editPreference()
    {
        return preference().edit();
    }

    private SharedPreferences preference()
    {
        return getInstrumentation().getContext().getSharedPreferences( "plugin_" + PLUGIN_PACKAGE_NAME_1 + "_preferences.xml", Context.MODE_PRIVATE );
    }

}
