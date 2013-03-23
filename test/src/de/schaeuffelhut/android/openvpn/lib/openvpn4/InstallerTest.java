package de.schaeuffelhut.android.openvpn.lib.openvpn4;

import android.test.InstrumentationTestCase;
import de.schaeuffelhut.android.openvpn.shared.util.OpenVpnBinary;
import junit.framework.Assert;

import java.io.File;


/**
 * @author Friedrich Schäuffelhut
 * @since 2013-03-23
 */
public class InstallerTest extends InstrumentationTestCase
{
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
    }


    public void test_hasResource_for_armeabi()
    {
        Assert.assertTrue( Installer.hasResource( "armeabi" ) );
    }

    public void test_selectResource_for_armeabi()
    {
        Assert.assertEquals( R.raw.minivpn_armeabi, Installer.selectResource( "armeabi" ) );
    }

    public void test_hasResource_for_armeabi_v7a()
    {
        Assert.assertTrue( Installer.hasResource( "armeabi-v7a" ) );
    }

    public void test_selectResource_for_armeabi_v7a()
    {
        Assert.assertEquals( R.raw.minivpn_armeabi_v7a, Installer.selectResource( "armeabi-v7a" ) );
    }

    public void test_hasResource_for_mips()
    {
        Assert.assertTrue( Installer.hasResource( "mips" ) );
    }

    public void test_selectResource_for_mips()
    {
        Assert.assertEquals( R.raw.minivpn_mips, Installer.selectResource( "mips" ) );
    }

    public void test_hasResource_for_x86()
    {
        Assert.assertTrue( Installer.hasResource( "x86" ) );
    }

    public void test_selectResource_for_x86()
    {
        Assert.assertEquals( R.raw.minivpn_x86, Installer.selectResource( "x86" ) );
    }


    public void test_installOpenVpn() throws InstallFailed
    {
        File pathToOpenVpn = new Installer( getInstrumentation().getTargetContext() ).installMiniOpenVpn();
        Assert.assertNotNull( pathToOpenVpn );
        Assert.assertEquals( "2.1.1", new OpenVpnBinary( pathToOpenVpn ).getVersion() );
    }
}

