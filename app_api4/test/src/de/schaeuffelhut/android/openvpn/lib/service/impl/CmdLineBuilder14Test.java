package de.schaeuffelhut.android.openvpn.lib.service.impl;




import android.content.Context;

import java.io.File;
import java.io.IOException;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-03-12
 */
public class CmdLineBuilder14Test extends CmdLineBuilderBaseTest
{

    public void test_requiresRoot() throws Exception
    {
        CmdLineBuilder14 cmdLineBuilder = new CmdLineBuilder14( new File( "openvpn" ) );
        assertFalse( cmdLineBuilder.requiresRoot() );
    }

    public void testBuildCmdLine() throws Exception
    {
        CmdLineBuilder14 cmdLineBuilder = new CmdLineBuilder14( new File( OPENVPN1 ) );

        testBuildCmdLine(
                cmdLineBuilder,
                IP_ROUTE_NOT_REQUIRED
        );
    }

    public void testBuildCmdLine_from_context() throws Exception
    {
        CmdLineBuilder14 cmdLineBuilder = new CmdLineBuilder14( getInstrumentation().getTargetContext() );

        testBuildCmdLine(
                cmdLineBuilder,
                new File ( getInstrumentation().getTargetContext().getDir( "bin", Context.MODE_PRIVATE ), "miniopenvpn").getAbsolutePath(),
                IP_ROUTE_NOT_REQUIRED
        );
    }

    public void testBuildCmdLine_WithShellEscape() throws Exception
    {
        CmdLineBuilder14 cmdLineBuilder = new CmdLineBuilder14( new File( OPENVPN2 ) );

        testBuildCmdLine_WithShellEscape( cmdLineBuilder, IP_ROUTE_NOT_REQUIRED );
    }


    private static File installFakeOpenVpn(Context context) throws IOException
    {
        File openvpn = File.createTempFile( "openvpn", ".tmp", context.getDir( "tmp", Context.MODE_PRIVATE ) );
        openvpn.createNewFile();
        openvpn.deleteOnExit();
        return openvpn;
    }

    public void test_canExecute() throws Exception
    {
        File openvpn = installFakeOpenVpn( getInstrumentation().getTargetContext() );

        CmdLineBuilder14 cmdLineBuilder = new CmdLineBuilder14( openvpn );
        cmdLineBuilder.setConfigLocation( new File( "/dev/null" ) );
        cmdLineBuilder.setMgmtSocketLocation( new File( "/dev/socket" ) );

        assertTrue( cmdLineBuilder.canExecute( "OpenVpnSettings" ) );
    }

    public void test_canExecute_fails_because_of_openvpn() throws Exception
    {
        CmdLineBuilder14 cmdLineBuilder = new CmdLineBuilder14( new File( "/system/NO/openvpn") );

        assertFalse( cmdLineBuilder.canExecute( "OpenVpnSettings" ) );
    }

    public void test_canExecute_fails_because_of_ip() throws Exception
    {
        File openvpn = installFakeOpenVpn( getInstrumentation().getTargetContext() );

        CmdLineBuilder14 cmdLineBuilder = new CmdLineBuilder14( openvpn );

        assertFalse( cmdLineBuilder.canExecute( "OpenVpnSettings" ) );
    }

    public void test_canExecute_fails_because_of_config() throws Exception
    {
        File openvpn = installFakeOpenVpn( getInstrumentation().getTargetContext() );

        CmdLineBuilder14 cmdLineBuilder = new CmdLineBuilder14( openvpn );
        cmdLineBuilder.setConfigLocation( new File("/etc/no/config.ovpn") );

        assertFalse( cmdLineBuilder.canExecute( "OpenVpnSettings" ) );
    }

    public void test_canExecute_fails_because_of_config_not_set() throws Exception
    {
        File openvpn = installFakeOpenVpn( getInstrumentation().getTargetContext() );

        CmdLineBuilder14 cmdLineBuilder = new CmdLineBuilder14( openvpn );
        // do not call cmdLineBuilder.setConfigLocation

        assertFalse( cmdLineBuilder.canExecute( "OpenVpnSettings" ) );
    }

    public void test_canExecute_fails_because_of_mgmt_parent_is_missing() throws Exception
    {
        File openvpn = installFakeOpenVpn( getInstrumentation().getTargetContext() );

        CmdLineBuilder14 cmdLineBuilder = new CmdLineBuilder14( openvpn );
        cmdLineBuilder.setConfigLocation( new File( "/dev/null" ) );
        cmdLineBuilder.setMgmtSocketLocation( new File( "/dev/NO/socket" ) );

        assertFalse( cmdLineBuilder.canExecute( "OpenVpnSettings" ) );
    }

    public void test_canExecute_fails_because_of_mgmt_is_not_set() throws Exception
    {
        File openvpn = installFakeOpenVpn( getInstrumentation().getTargetContext() );

        CmdLineBuilder14 cmdLineBuilder = new CmdLineBuilder14( openvpn );
        cmdLineBuilder.setConfigLocation( new File( "/dev/null" ) );
        // do not call cmdLineBuilder.setMgmtSocketLocation

        assertFalse( cmdLineBuilder.canExecute( "OpenVpnSettings" ) );
    }
}
