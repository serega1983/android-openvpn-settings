package de.schaeuffelhut.android.openvpn.lib.service.impl;

import android.content.Context;
import de.schaeuffelhut.android.openvpn.lib.openvpn.Installer;

import java.io.File;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-03-12
 */
public class CmdLineBuilder4Test extends CmdLineBuilderBaseTest
{

    public void test_requiresRoot() throws Exception
    {
        CmdLineBuilder4 cmdLineBuilder = new CmdLineBuilder4( new File( "openvpn" ), new File( "ip" ) );
        assertTrue( cmdLineBuilder.requiresRoot() );
    }

    public void testBuildCmdLine() throws Exception
    {
        CmdLineBuilder4 cmdLineBuilder = new CmdLineBuilder4(
                new File( OPENVPN1 ),
                new File( "/data/data/de.schaeuffelhut.android.openvpn/app_bin/bb/ip" )
        );

        testBuildCmdLine(
                cmdLineBuilder,
                "--iproute /data/data/de.schaeuffelhut.android.openvpn/app_bin/bb/ip "
        );
    }

    public void testBuildCmdLine_from_context() throws Exception
    {
        CmdLineBuilder4 cmdLineBuilder = new CmdLineBuilder4( getInstrumentation().getTargetContext() );

        testBuildCmdLine(
                cmdLineBuilder,
                new File ( getInstrumentation().getTargetContext().getDir( "bin", Context.MODE_PRIVATE ), "openvpn").getAbsolutePath(),
                "--iproute " + new File ( getInstrumentation().getTargetContext().getDir( "bin", Context.MODE_PRIVATE ), "ip").getAbsolutePath() + " "
        );
    }

    public void testBuildCmdLine_WithShellEscape() throws Exception
    {
        CmdLineBuilder4 cmdLineBuilder = new CmdLineBuilder4(
                new File( OPENVPN2 ),
                new File( "/data/data/de. .openvpn/app_bin/bb/ip" )
        );

        testBuildCmdLine_WithShellEscape( cmdLineBuilder, "--iproute '/data/data/de. .openvpn/app_bin/bb/ip' " );
    }

    public void test_canExecute() throws Exception
    {
        Installer installer = new Installer( getInstrumentation().getTargetContext() );
        File openvpn = installer.installOpenVpn();

        File busybox = installer.installBusyBox();
        File ip = new File( busybox.getParent(), "ip" );

        CmdLineBuilder4 cmdLineBuilder = new CmdLineBuilder4(
                openvpn, ip
        );
        cmdLineBuilder.setConfigLocation( new File( "/dev/null" ) );
        cmdLineBuilder.setMgmtSocketLocation( new File( "/dev/socket" ) );

        assertTrue( cmdLineBuilder.canExecute( "OpenVpnSettings" ) );
    }

    public void test_canExecute_fails_because_of_openvpn() throws Exception
    {
        Installer installer = new Installer( getInstrumentation().getTargetContext() );
        File busybox = installer.installBusyBox();
        File ip = new File( busybox.getParent(), "ip" );

        CmdLineBuilder4 cmdLineBuilder = new CmdLineBuilder4(
                new File("/system/NO/openvpn"), ip
        );

        assertFalse( cmdLineBuilder.canExecute( "OpenVpnSettings" ) );
    }

    public void test_canExecute_fails_because_of_ip() throws Exception
    {
        Installer installer = new Installer( getInstrumentation().getTargetContext() );
        File openvpn = installer.installOpenVpn();

        CmdLineBuilder4 cmdLineBuilder = new CmdLineBuilder4(
                openvpn, new File("/system/NO/ip")
        );

        assertFalse( cmdLineBuilder.canExecute( "OpenVpnSettings" ) );
    }

    public void test_canExecute_fails_because_of_config() throws Exception
    {
        Installer installer = new Installer( getInstrumentation().getTargetContext() );
        File openvpn = installer.installOpenVpn();

        File busybox = installer.installBusyBox();
        File ip = new File( busybox.getParent(), "ip" );

        CmdLineBuilder4 cmdLineBuilder = new CmdLineBuilder4(
                openvpn, ip
        );
        cmdLineBuilder.setConfigLocation( new File("/etc/no/config.ovpn") );

        assertFalse( cmdLineBuilder.canExecute( "OpenVpnSettings" ) );
    }

    public void test_canExecute_fails_because_of_config_not_set() throws Exception
    {
        Installer installer = new Installer( getInstrumentation().getTargetContext() );
        File openvpn = installer.installOpenVpn();

        File busybox = installer.installBusyBox();
        File ip = new File( busybox.getParent(), "ip" );

        CmdLineBuilder4 cmdLineBuilder = new CmdLineBuilder4(
                openvpn, ip
        );
        // do not call cmdLineBuilder.setConfigLocation

        assertFalse( cmdLineBuilder.canExecute( "OpenVpnSettings" ) );
    }

    public void test_canExecute_fails_because_of_mgmt_parent_is_missing() throws Exception
    {
        Installer installer = new Installer( getInstrumentation().getTargetContext() );
        File openvpn = installer.installOpenVpn();

        File busybox = installer.installBusyBox();
        File ip = new File( busybox.getParent(), "ip" );

        CmdLineBuilder4 cmdLineBuilder = new CmdLineBuilder4(
                openvpn, ip
        );
        cmdLineBuilder.setConfigLocation( new File( "/dev/null" ) );
        cmdLineBuilder.setMgmtSocketLocation( new File( "/dev/NO/socket" ) );

        assertFalse( cmdLineBuilder.canExecute( "OpenVpnSettings" ) );
    }

    public void test_canExecute_fails_because_of_mgmt_is_not_set() throws Exception
    {
        Installer installer = new Installer( getInstrumentation().getTargetContext() );
        File openvpn = installer.installOpenVpn();

        File busybox = installer.installBusyBox();
        File ip = new File( busybox.getParent(), "ip" );

        CmdLineBuilder4 cmdLineBuilder = new CmdLineBuilder4(
                openvpn, ip
        );
        cmdLineBuilder.setConfigLocation( new File( "/dev/null" ) );
        // do not call cmdLineBuilder.setMgmtSocketLocation

        assertFalse( cmdLineBuilder.canExecute( "OpenVpnSettings" ) );
    }

}
