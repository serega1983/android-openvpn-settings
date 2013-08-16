package de.schaeuffelhut.android.openvpn.lib.openvpn4;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//TODO: merge with openvpn.Installer, extract common base class AbstractInstaller
public class Installer
{
    @Deprecated
    public static File install(Context context) throws InstallFailed
    {
        return new Installer( context ).installMiniOpenVpn();
    }

    private static final String OPENVPN = "miniopenvpn";
    private static final Map<String, Integer> RESOURCE_BY_CPU_ABI;

    static
    {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put( "armeabi", R.raw.minivpn_armeabi );
        map.put( "armeabi-v7a", R.raw.minivpn_armeabi_v7a );
        map.put( "mips", R.raw.minivpn_mips );
        map.put( "x86", R.raw.minivpn_x86 );
        RESOURCE_BY_CPU_ABI = Collections.unmodifiableMap( map );
    }

    private final Context context;
    private final File bin;

    public Installer(Context context)
    {
        this.context = context;
        bin = context.getDir( "bin", Context.MODE_PRIVATE );
    }

    public File installMiniOpenVpn() throws InstallFailed
    {
        File openvpn = new File( bin, OPENVPN );
        install( selectResource(), openvpn );
        return openvpn;
    }

    static int selectResource() throws InstallFailed
    {
        if ( hasResource( Build.CPU_ABI ) )
            return  selectResource( Build.CPU_ABI );
        if ( hasResource( Build.CPU_ABI2 ) )
            return  selectResource( Build.CPU_ABI2 );
        throw new InstallFailed();
    }

    static boolean hasResource(String cpuAbi)
    {
        boolean contains = RESOURCE_BY_CPU_ABI.containsKey( cpuAbi );
        if ( !contains )
            Log.d( "OpenVpnSettings", "Failed getting raw resource for architecture " + cpuAbi );
        return contains;
    }

    static int selectResource(String cpuAbi)
    {
        return RESOURCE_BY_CPU_ABI.get( cpuAbi );
    }

    private void install(int resourceId, File target) throws InstallFailed
    {
        install( openResource( resourceId ), target );
    }

    private void install(InputStream input, File target) throws InstallFailed
    {
        try
        {
            if (target.exists())
                target.delete();
            final OutputStream os = openTarget( target );

            try
            {
                copyAssetToTarget( input, os );
            }
            finally
            {
                close( os );
            }
        }
        finally
        {
            close( input );
        }

        makeExecutable( target );
    }

    private void makeExecutable(File target) throws InstallFailed
    {
        try
        {
            Runtime.getRuntime().exec( new String[]{"chmod", "700", target.getAbsolutePath() } );
        }
        catch (IOException e)
        {
            throw new InstallFailed( e );
        }
    }

    private void copyAssetToTarget(InputStream input, OutputStream os) throws InstallFailed
    {
        byte[] buffer = new byte[4096];
        int count;
        try
        {
            while ((count = input.read( buffer )) >= 0)
                os.write( buffer, 0, count );
        }
        catch (IOException e)
        {
            throw new InstallFailed( e );
        }
    }

    private OutputStream openTarget(File target) throws InstallFailed
    {
        final OutputStream os;
        try
        {
            os = new FileOutputStream( target );
        }
        catch (FileNotFoundException e)
        {
            throw new InstallFailed( e );
        }
        return os;
    }

    private void close(Closeable closeable) throws InstallFailed
    {
        try
        {
            closeable.close();
        }
        catch (IOException e)
        {
            throw new InstallFailed( e );
        }
    }

    private InputStream openResource(int id) throws InstallFailed
    {
        // Assets can not be part of a library project. Use a raw resource instead.
        return context.getResources().openRawResource(  id );
    }
}
