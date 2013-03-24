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

package de.schaeuffelhut.android.openvpn.lib.openvpn;

import android.content.Context;

import java.io.*;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-01-21
 */
//TODO: merge with openvpn4.Installer, extract common base class AbstractInstaller
public class Installer
{
    private static final String OPENVPN = "openvpn";
    private static final String BUSYBOX = "busybox";

    private final Context context;
    private final File bin;



    public Installer(Context context)
    {
        this.context = context;
        bin = context.getDir( "bin", Context.MODE_PRIVATE );
    }

    public File installOpenVpn() throws InstallFailed
    {
        File openvpn = new File( bin, OPENVPN );
        install( R.raw.openvpn, openvpn );
        return openvpn;
    }

    public File installBusyBox() throws InstallFailed
    {
        File busybox = new File( bin, BUSYBOX );
        install( R.raw.busybox, busybox );
        installApplets( busybox );
        return busybox;
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
            // Also make writable so future install operations won't fail.
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

    private void installApplets(File busybox) throws InstallFailed
    {
        ProcessBuilder pb = new ProcessBuilder( busybox.getAbsolutePath(), "--install", busybox.getParentFile().getAbsolutePath() );
        try
        {
            pb.start().waitFor();
        }
        catch (InterruptedException e)
        {
            throw new InstallFailed( e );
        }
        catch (IOException e)
        {
            throw new InstallFailed( e );
        }
    }
}
