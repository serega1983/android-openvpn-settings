package de.schaeuffelhut.android.openvpn.lib.service.impl;

import java.io.File;

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

    public void testBuildCmdLine_WithShellEscape() throws Exception
    {
        CmdLineBuilder14 cmdLineBuilder = new CmdLineBuilder14( new File( OPENVPN2 ) );

        testBuildCmdLine_WithShellEscape( cmdLineBuilder, IP_ROUTE_NOT_REQUIRED );
    }

}
