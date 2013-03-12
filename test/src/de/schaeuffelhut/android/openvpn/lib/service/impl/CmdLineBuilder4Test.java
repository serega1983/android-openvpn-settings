package de.schaeuffelhut.android.openvpn.lib.service.impl;

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
                "--ip /data/data/de.schaeuffelhut.android.openvpn/app_bin/bb/ip "
        );
    }

    public void testBuildCmdLine_WithShellEscape() throws Exception
    {
        CmdLineBuilder4 cmdLineBuilder = new CmdLineBuilder4(
                new File( OPENVPN2 ),
                new File( "/data/data/de. .openvpn/app_bin/bb/ip" )
        );

        testBuildCmdLine_WithShellEscape( cmdLineBuilder, "--ip '/data/data/de. .openvpn/app_bin/bb/ip' " );
    }
}
