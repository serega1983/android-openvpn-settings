package de.schaeuffelhut.android.openvpn.lib.openvpn4;

import android.content.Context;
import android.test.InstrumentationTestCase;
import de.schaeuffelhut.android.openvpn.shared.util.OpenVpnBinary;
import junit.framework.Assert;

import java.io.File;


/**
 * @author Friedrich Schäuffelhut
 * @since 2013-03-23
 */
//TODO: merge with openvpn.InstallerTest
public class InstallerTest extends InstrumentationTestCase
{
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        removeBinDir();
    }

    private void removeBinDir()
    {
        File binDir = getBinDir();
        File[] files = binDir.listFiles();
        if (files != null)
            for (File file : files)
                file.delete();
        binDir.delete();
        Assert.assertFalse( binDir.exists() );
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


    public void test_installOpenVpn_installs_binary() throws InstallFailed
    {
        File pathToOpenVpn = new Installer( getTargetContext() ).installMiniOpenVpn();
        Assert.assertNotNull( pathToOpenVpn );
        Assert.assertEquals( "2.3_rc1+dspatch3", new OpenVpnBinary( pathToOpenVpn, getTargetContext().getApplicationInfo() ).getVersion() );
    }

    public void test_installOpenVpn_verify_location() throws InstallFailed
    {
        File pathToOpenVpn = new Installer( getTargetContext() ).installMiniOpenVpn();
        Assert.assertEquals(
                new File( getBinDir(), "miniopenvpn" ),
                pathToOpenVpn
        );
    }

    public void test_installOpenVpn_twice_succeeds() throws InstallFailed
    {
        new Installer( getTargetContext() ).installMiniOpenVpn();
        new Installer( getTargetContext() ).installMiniOpenVpn();
    }



    /**
     * Returns path to the directory where app local binaries are stored.
     * This directory should always be retrieved through out the app the
     * same way as implemente in this method.
     *
     * @return path to the directory where app local binaries are stored.
     */
    private File getBinDir()
    {
        return getTargetContext().getDir( "bin", Context.MODE_PRIVATE );
    }

    private Context getTargetContext()
    {
        return getInstrumentation().getTargetContext();
    }
}

