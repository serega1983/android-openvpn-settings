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

import android.content.SharedPreferences;
import de.schaeuffelhut.android.openvpn.Preferences;
import de.schaeuffelhut.android.openvpn.util.Shell;
import de.schaeuffelhut.android.openvpn.util.UnexpectedSwitchValueException;
import de.schaeuffelhut.android.openvpn.util.Util;

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
    private TunLoaders()
    {
    }


    /**
     * Create a TunLoader using the definition provided by the advanced settings dialog.
     *
     * @param preferences the shared preferences holding the configuration.
     * @return the Tun Loader.
     */
    public static TunLoader createFromLegacyDefinition(SharedPreferences preferences)
    {
        if (!hasLegacyDefinition( preferences ))
            throw new IllegalStateException( "No legacy tun loading method defined" );
        final String modprobeAlternative = Preferences.getModprobeAlternative( preferences );
        final File pathToModule = new File( Preferences.getPathToTun( preferences ) );
        if ("modprobe".equals( modprobeAlternative ))
        {
            if ("tun".equals( pathToModule.getPath() ))
                return new LoadTunViaModprobe();
            else
                return new LoadTunViaModprobeWithParameter( pathToModule );
        }
        if ("insmod".equals( modprobeAlternative ))
            return new LoadTunViaInsmod( pathToModule );
        throw new UnexpectedSwitchValueException( modprobeAlternative );
    }

    public static boolean hasLegacyDefinition(SharedPreferences preferences)
    {
        return Preferences.getDoModprobeTun( preferences );
    }


    public static class NullTunLoader implements TunLoader
    {
        public String getName()
        {
            return "None";
        }

        public boolean hasPathToModule()
        {
            return false;
        }

        public File getPathToModule()
        {
            return null;
        }

        public void load()
        {
            // NOP
        }

        public void save(TunLoaderPreferences preferences)
        {
            preferences.setTypeToNone();
        }
    }

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

        public void save(TunLoaderPreferences preferences)
        {
            preferences.setTypeToModprobe();
        }

        @Override
        public String toString()
        {
            return getClass().getSimpleName();
        }
    }

    /*
     * In versions prior to 0.4.11 loading tun via modprobe would also accept a parameter.
     * This is now deprecated and is only kept for backward compatibility.
     * Remove, when all users have upgraded to 0.4.11 or above.
     */
    @Deprecated
    static class LoadTunViaModprobeWithParameter implements TunLoader
    {
        private final File pathToModule;

        public LoadTunViaModprobeWithParameter(File pathToModule)
        {
            this.pathToModule = pathToModule;
        }

        public String getName()
        {
            return "modprobe";
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
            Shell modprobe = new Shell(
                    "OpenVPN",
                    "modprobe " + Util.shellEscape( pathToModule.getPath() ),
                    Shell.SU
            );
            modprobe.run();
        }


        public void save(TunLoaderPreferences preferences)
        {
            preferences.setTypeToLegacy();
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
            if (!pathToModule.exists())
                return;

            Shell insmod = new Shell(
                    "OpenVPN",
                    "insmod '" + pathToModule.getAbsolutePath().replace( "'", "\\'" ) + "'",
                    Shell.SU
            );
            insmod.run();
        }

        public void save(TunLoaderPreferences preferences)
        {
            preferences.setTypeToInsmod( pathToModule );
        }

        @Override
        public String toString()
        {
            return "LoadTunViaInsmod{" +
                    "pathToModule=" + pathToModule +
                    '}';
        }
    }

    public enum Types
    {
        NONE( false )
                {
                    @Override
                    public TunLoader createTunLoader(TunLoaderPreferences tunLoaderPreferences, SharedPreferences sharedPreferences)
                    {
                        return new NullTunLoader();
                    }
                },
        MODPROBE( true )
                {
                    @Override
                    public TunLoader createTunLoader(TunLoaderPreferences tunLoaderPreferences, SharedPreferences sharedPreferences)
                    {
                        return new LoadTunViaModprobe();
                    }
                },
        INSMOD( true )
                {
                    @Override
                    public TunLoader createTunLoader(TunLoaderPreferences tunLoaderPreferences, SharedPreferences sharedPreferences)
                    {
                        return new LoadTunViaInsmod( tunLoaderPreferences.getPathToModule() );
                    }
                },
        LEGACY( true )
                {
                    @Override
                    public TunLoader createTunLoader(TunLoaderPreferences tunLoaderPreferences, SharedPreferences sharedPreferences)
                    {
                        if (TunLoaders.hasLegacyDefinition( sharedPreferences ))
                            return TunLoaders.createFromLegacyDefinition( sharedPreferences );
                        else
                            return new NullTunLoader();
                    }
                };
        public final boolean canLoadTun;

        private Types(boolean canLoadTun)
        {
            this.canLoadTun = canLoadTun;
        }

        public abstract TunLoader createTunLoader(TunLoaderPreferences tunLoaderPreferences, SharedPreferences sharedPreferences);
    }
}
