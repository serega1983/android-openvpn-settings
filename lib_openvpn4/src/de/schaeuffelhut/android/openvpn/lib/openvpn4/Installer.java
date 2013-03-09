package de.schaeuffelhut.android.openvpn.lib.openvpn4;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Installer
{
    public static File install(Context context) throws InstallFailed
    {
        final File bin = context.getDir( "bin", Context.MODE_PRIVATE );

        final File mvpnout = new File( bin, "miniopenvpn" );
        if (mvpnout.exists() && mvpnout.canExecute())
            return mvpnout;

        IOException e2 = null;

        try
        {
            InputStream mvpn;

            try
            {
                mvpn = context.getAssets().open( "minivpn." + Build.CPU_ABI );
            }
            catch (IOException errabi)
            {
                Log.e( "OpenVpnSettings", "Failed getting assets for architecture " + Build.CPU_ABI );
                e2 = errabi;
                mvpn = context.getAssets().open( "minivpn." + Build.CPU_ABI2 );
            }

            FileOutputStream fout = new FileOutputStream( mvpnout );

            byte buf[] = new byte[4096];

            int lenread = mvpn.read( buf );
            while (lenread > 0)
            {
                fout.write( buf, 0, lenread );
                lenread = mvpn.read( buf );
            }
            fout.close();

            if (!mvpnout.setExecutable( true ))
            {
                Log.e( "OpenVpnSettings", "Failed to set minivpn executable" );
                throw new InstallFailed();
            }

            return mvpnout;
        }
        catch (IOException e)
        {
            if (e2 != null)
                Log.e( "OpenVpnSettings", "Could not install minivpn", e2 );
            Log.e( "OpenVpnSettings", "Could not install minivpn", e2 );
            throw new InstallFailed( e );
        }
    }
}
