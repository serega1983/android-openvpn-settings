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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.InstrumentationTestCase;
import de.schaeuffelhut.android.openvpn.lib.service.impl.Preferences2;

import java.io.File;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-11-24
 */
public class Preferences2Test extends InstrumentationTestCase
{
    private final long uniqueId = System.currentTimeMillis();
    private final File uniqueFile = new File( "/dev/null/test" + uniqueId + ".conf" );
    private final String KEY_PREFIX = "openvpn_configurations[" + uniqueFile.getAbsolutePath() + "].";
    private Preferences2 preferences;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        preferences = new Preferences2( getContext(), uniqueFile );
    }

    public void test_getMgmtPort_9090()
    {
        test_getMgmtPort( 9090 );
    }

    public void test_getMgmtPort_12345()
    {
        test_getMgmtPort( 12345 );
    }

    private void test_getMgmtPort(int port)
    {
        editPreference().putInt( KEY_PREFIX + "mgmt_port", port ).commit();
        assertEquals( port, preferences.getMgmtPort() );
    }

    public void test_getMgmtPort_default()
    {
        editPreference().remove( KEY_PREFIX + "mgmt_port" ).commit();
        assertEquals( -1, preferences.getMgmtPort() );
    }

    public void test_setMgmtPort_9191()
    {
        test_setMgmtPort( 9191 );
    }

    public void test_setMgmtPort_23456()
    {
        test_setMgmtPort( 23456 );
    }

    private void test_setMgmtPort(int port)
    {
        preferences.setMgmtPort( port );
        assertEquals( port, preference().getInt( KEY_PREFIX + "mgmt_port", -1 ) );
    }

    public void test_getVpnDns_192_168_1_1()
    {
        test_getVpnDns( "192.168.1.1" );
    }

    public void test_getVpnDns_10_0_0_1()
    {
        test_getVpnDns( "10.0.0.1" );
    }

    private void test_getVpnDns(String port)
    {
        editPreference().putString( KEY_PREFIX + "vpndns1", port ).commit();
        assertEquals( port, preferences.getVpnDns() );
    }

    public void test_getVpnDns_default()
    {
        editPreference().remove( KEY_PREFIX + "vpndns1" ).commit();
        assertEquals( "", preferences.getVpnDns() );
    }

    public void test_getVpnDnsEnabled_true()
    {
        test_getVpnDnsEnabled( true );
    }

    public void test_getVpnDnsEnabled_false()
    {
        test_getVpnDnsEnabled( false );
    }

    private void test_getVpnDnsEnabled(boolean enabled)
    {
        editPreference().putBoolean( KEY_PREFIX + "vpndns.enable", enabled ).commit();
        assertEquals( enabled, preferences.getVpnDnsEnabled() );
    }

    public void test_getVpnDnsEnabled_default()
    {
        editPreference().remove( KEY_PREFIX + "vpndns.enable" ).commit();
        assertEquals( false, preferences.getVpnDnsEnabled() );
    }

    public void test_getDnsChange_100()
    {
        test_getDnsChange( 100 );
    }

    public void test_getDnsChange_123()
    {
        test_getDnsChange( 123 );
    }

    private void test_getDnsChange(int port)
    {
        editPreference().putInt( KEY_PREFIX + "dnschange", port ).commit();
        assertEquals( port, preferences.getDnsChange() );
    }

    public void test_getDnsChange_default()
    {
        editPreference().remove( KEY_PREFIX + "dnschange" ).commit();
        assertEquals( -1, preferences.getDnsChange() );
    }

    public void test_getDns1_10_1_1_1()
    {
        test_getDns1( "10.1.1.1" );
    }

    public void test_getDns1_172_16_3_5()
    {
        test_getDns1( "172.16.3.5" );
    }

    private void test_getDns1(String dns)
    {
        editPreference().putString( KEY_PREFIX + "dns1", dns ).commit();
        assertEquals( dns, preferences.getDns1() );
    }

    public void test_getDns1_default()
    {
        editPreference().remove( KEY_PREFIX + "dns1" ).commit();
        assertEquals( "", preferences.getDns1() );
    }

    public void test_setDns1_12__10_1_1_1()
    {
        test_setDns1( 12, "10.1.1.1" );
    }

    public void test_setDns1_15__172_16_3_5()
    {
        test_setDns1( 15, "172.16.3.5" );
    }

    private void test_setDns1(int newDnsChange, String dns)
    {
        preferences.setDns1( newDnsChange, dns );
        assertEquals( newDnsChange, preference().getInt( KEY_PREFIX + "dnschange", -1 ) );
        assertEquals( dns, preference().getString( KEY_PREFIX + "dns1", "" ) );
    }

    public void test_getScriptSecurityLevel_1()
    {
        test_getScriptSecurityLevel( 1 );
    }

    public void test_getScriptSecurityLevel_2()
    {
        test_getScriptSecurityLevel( 2 );
    }

    private void test_getScriptSecurityLevel(int value)
    {
        editPreference().putString( KEY_PREFIX + "script_security.level", Integer.toString( value ) ).commit();
        assertEquals( value, preferences.getScriptSecurityLevel() );
    }

    public void test_getScriptSecurityLevel()
    {
        editPreference().remove( KEY_PREFIX + "script_security.level" ).commit();
        assertEquals( 1, preferences.getScriptSecurityLevel() );
    }

    public void test_getLogStdoutEnable_true()
    {
        test_getLogStdoutEnable( true );
    }

    public void test_getLogStdoutEnable_false()
    {
        test_getLogStdoutEnable( false );
    }

    private void test_getLogStdoutEnable(boolean enabled)
    {
        editPreference().putBoolean( KEY_PREFIX + "log_stdout.enable", enabled ).commit();
        assertEquals( enabled, preferences.getLogStdoutEnable() );
    }

    public void test_getLogStdoutEnable_default()
    {
        editPreference().remove( KEY_PREFIX + "log_stdout.enable" ).commit();
        assertEquals( false, preferences.getLogStdoutEnable() );
    }

    public void test_logFileFor()
    {
        assertEquals( new File( "/dev/null/test" + uniqueId + ".log" ), preferences.logFileFor() );
    }

    public void test_getIntendedState_true()
    {
        test_getIntendedState( true );
    }

    public void test_getIntendedState_false()
    {
        test_getIntendedState( false );
    }

    private void test_getIntendedState(boolean enabled)
    {
        editPreference().putBoolean( KEY_PREFIX + "intended_state", enabled ).commit();
        assertEquals( enabled, preferences.getIntendedState() );
    }

    public void test_getIntendedState_default()
    {
        editPreference().remove( KEY_PREFIX + "intended_state" ).commit();
        assertEquals( false, preferences.getIntendedState() );
    }

    public void test_hasPassphrase_true()
    {
        editPreference().putString( KEY_PREFIX + "passphrase", "passphrase" ).commit();
        assertEquals( true, preferences.hasPassphrase() );
    }

    public void test_hasPassphrase_false()
    {
        editPreference().remove( KEY_PREFIX + "passphrase" ).commit();
        assertEquals( false, preferences.hasPassphrase() );
    }

    public void test_getPassphrase_1()
    {
        test_getPassphrase( "a pass phrase" );
    }

    public void test_getPassphrase_2()
    {
        test_getPassphrase( "an other pass phrase" );
    }

    private void test_getPassphrase(String passphrase)
    {
        editPreference().putString( KEY_PREFIX + "passphrase", passphrase ).commit();
        assertEquals( passphrase, preferences.getPassphrase() );
    }

    public void test_getPassphrase_default()
    {
        editPreference().remove( KEY_PREFIX + "passphrase" ).commit();
        assertEquals( "", preferences.getPassphrase() );
    }


    public void test_hasCredentials_true()
    {
        editPreference().putString( KEY_PREFIX + "username", "username" ).commit();
        editPreference().putString( KEY_PREFIX + "password", "password" ).commit();
        assertEquals( true, preferences.hasCredentials() );
    }

    public void test_hasCredentials_false_1()
    {
        editPreference().putString( KEY_PREFIX + "username", "username" ).commit();
        editPreference().remove( KEY_PREFIX + "password" ).commit();
        assertEquals( false, preferences.hasCredentials() );
    }

    public void test_hasCredentials_false_2()
    {
        editPreference().putString( KEY_PREFIX + "password", "password" ).commit();
        editPreference().remove( KEY_PREFIX + "username" ).commit();
        assertEquals( false, preferences.hasCredentials() );
    }

    public void test_getUsernamePassword_1()
    {
        test_getUsername( "peter" );
    }

    public void test_getUsernamePassword_2()
    {
        test_getUsername( "jack" );
    }

    private void test_getUsername(String username)
    {
        editPreference().putString( KEY_PREFIX + "username", username ).commit();
        assertEquals( username, preferences.getUsername() );
    }

    public void test_getUsername_default()
    {
        editPreference().remove( KEY_PREFIX + "username" ).commit();
        assertEquals( "", preferences.getUsername() );
    }

    public void test_getPassword_1()
    {
        test_getPassword( "secure" );
    }

    public void test_getPassword_2()
    {
        test_getPassword( "unsecure" );
    }

    private void test_getPassword(String password)
    {
        editPreference().putString( KEY_PREFIX + "password", password ).commit();
        assertEquals( password, preferences.getPassword() );
    }

    public void test_getPassword_default()
    {
        editPreference().remove( KEY_PREFIX + "password" ).commit();
        assertEquals( "", preferences.getPassword() );
    }

    public void test_getNotificationId_1()
    {
        editPreference().remove( "openvpn_next_notification_id" ).commit();
        editPreference().remove( KEY_PREFIX + "notification_id" ).commit();
        assertEquals( 1000000, preferences.getNotificationId() );
        assertEquals( 1000001, preference().getInt( "openvpn_next_notification_id", -1 ) );
    }

    public void test_getNotificationId_2()
    {
        editPreference().putInt( "openvpn_next_notification_id", 1000002 ).commit();
        editPreference().remove( KEY_PREFIX + "notification_id" ).commit();
        assertEquals( 1000002, preferences.getNotificationId() );
        assertEquals( 1000003, preference().getInt( "openvpn_next_notification_id", -1 ) );
    }

    public void test_getNotificationId_3()
    {
        editPreference().putInt( "openvpn_next_notification_id", 1000004 ).commit();
        editPreference().putInt( KEY_PREFIX + "notification_id", 1000003 ).commit();
        assertEquals( 1000003, preferences.getNotificationId() );
        assertEquals( 1000004, preference().getInt( "openvpn_next_notification_id", -1 ) );
    }

    // Global Preferences

    public void test_getFixHtcRoutes_true()
    {
        test_getFixHtcRoutes( true );
    }

    public void test_getFixHtcRoutes_false()
    {
        test_getFixHtcRoutes( false );
    }

    private void test_getFixHtcRoutes(boolean enabled)
    {
        editPreference().putBoolean( "fix_htc_routes", enabled ).commit();
        assertEquals( enabled, preferences.getFixHtcRoutes() );
    }

    public void test_getFixHtcRoutes_default()
    {
        editPreference().remove( "fix_htc_routes" ).commit();
        assertEquals( false, preferences.getFixHtcRoutes() );
    }

    public void test_getPathToBinaryAsFile_true()
    {
        test_getPathToBinaryAsFile( "/system/xbin/openvpn" );
    }

    public void test_getPathToBinaryAsFile_false()
    {
        test_getPathToBinaryAsFile( "/data/data/de.schaeuffelhut.android.openvpn/bin/openvpn" );
    }

    private void test_getPathToBinaryAsFile(String path)
    {
        editPreference().putString( "openvpn_path_to_binary", path ).commit();
        assertEquals( new File( path ), preferences.getPathToBinaryAsFile() );
    }

    public void test_getPathToBinaryAsFile_default()
    {
        editPreference().remove( "openvpn_path_to_binary" ).commit();
        assertEquals( null, preferences.getPathToBinaryAsFile() );
    }

    // Utilities

    private SharedPreferences.Editor editPreference()
    {
        return preference().edit();
    }

    private SharedPreferences preference()
    {
        return PreferenceManager.getDefaultSharedPreferences( getContext() );
    }

    private Context getContext()
    {
        return getInstrumentation().getTargetContext();
    }
}
