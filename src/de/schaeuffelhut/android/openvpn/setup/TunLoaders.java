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

package de.schaeuffelhut.android.openvpn.setup;

import de.schaeuffelhut.android.openvpn.util.Shell;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: fries
 * Date: 4/15/12
 * Time: 7:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class TunLoaders
{
    private TunLoaders(){}

    static class LoadTunViaModprobe implements TunLoader
    {

        public String getName()
        {
            return "modprobe tun";
        }

        public boolean hasPathToModule()
        {
            return false;
        }

        public File getPathToModule()
        {
            throw new UnsupportedOperationException( "modprobe has no module path" );
        }

        public void load()
        {
            Shell modprobe = new Shell(
                    "OpenVPN",
                    "modprobe tun",
                    Shell.SU
            );
            modprobe.run();
        }

        @Override
        public String toString()
        {
            return getClass().getSimpleName();
        }
    }
    static class LoadTunViaInsmod implements TunLoader
    {
        private final File pathToModule;

        LoadTunViaInsmod(File pathToModule)
        {
            this.pathToModule = pathToModule;
        }

        public String getName()
        {
            return "insmod";
        }

        public boolean hasPathToModule()
        {
            return true;
        }

        public File getPathToModule()
        {
            return pathToModule;
        }

        public void load()
        {
            if ( !pathToModule.exists() )
                return;

            Shell insmod = new Shell(
                    "OpenVPN",
                    "insmod " + pathToModule.getAbsolutePath(),
                    Shell.SU
            );
            insmod.run();
        }

        @Override
        public String toString()
        {
            return "LoadTunViaInsmod{" +
                    "pathToModule=" + pathToModule +
                    '}';
        }
    }
}
