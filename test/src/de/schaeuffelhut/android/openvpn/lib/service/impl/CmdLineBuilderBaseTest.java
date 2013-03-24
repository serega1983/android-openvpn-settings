package de.schaeuffelhut.android.openvpn.lib.service.impl;

import android.test.InstrumentationTestCase;
import junit.framework.TestCase;

import java.io.File;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-03-12
 */
abstract class CmdLineBuilderBaseTest extends InstrumentationTestCase
{
    static final String OPENVPN1 = "/data/data/de.schaeuffelhut.android.openvpn/app_bin/openvpn";
    static final String OPENVPN2 = "/data/data/de. .openvpn/app_bin/openvpn";
    static final String IP_ROUTE_NOT_REQUIRED = "";

    protected void testBuildCmdLine(CmdLineBuilder cmdLineBuilder, String ip)
    {
        testBuildCmdLine( cmdLineBuilder, "/data/data/de.schaeuffelhut.android.openvpn/app_bin/openvpn", ip );
    }

    protected void testBuildCmdLine(CmdLineBuilder cmdLineBuilder, String openvpn, String ip)
    {
        cmdLineBuilder.setConfigLocation( new File( "/sdcard/openvpn/client.conf" ) );
        cmdLineBuilder.setMgmtSocketLocation( new File( "/data/data/de.schaeuffelhut.android.openvpn/app_mgmt/client.mgmt" ) );
        cmdLineBuilder.setScriptSecurityLevel( 2 );

        assertEquals( "" +
                openvpn + " " +
                "--cd /sdcard/openvpn --config client.conf " +
                "--script-security 2 " +
                ip +
                "--management /data/data/de.schaeuffelhut.android.openvpn/app_mgmt/client.mgmt unix " +
                "--management-query-passwords --verb 3",
                cmdLineBuilder.buildCmdLine()
        );
    }

    protected void testBuildCmdLine_WithShellEscape(CmdLineBuilder cmdLineBuilder, String ip)
    {
        cmdLineBuilder.setConfigLocation( new File( "/sdcard/open vpn/at home.conf" ) );
        cmdLineBuilder.setMgmtSocketLocation( new File( "/data/data/de. .openvpn/app_mgmt/client.mgmt" ) );
        cmdLineBuilder.setScriptSecurityLevel( 2 );

        assertEquals( "" +
                "'/data/data/de. .openvpn/app_bin/openvpn' " +
                "--cd '/sdcard/open vpn' --config 'at home.conf' " +
                "--script-security 2 " +
                ip +
                "--management '/data/data/de. .openvpn/app_mgmt/client.mgmt' unix " +
                "--management-query-passwords --verb 3",
                cmdLineBuilder.buildCmdLine()
        );
    }

}
