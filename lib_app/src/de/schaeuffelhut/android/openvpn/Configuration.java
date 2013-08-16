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

package de.schaeuffelhut.android.openvpn;

import android.text.TextUtils;
import android.util.Log;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-11-25
 */
public class Configuration
{
    @Deprecated
    public final static String BUG_SENSE_API_KEY = null;

    @Deprecated
    public final static String TUN_COLLECTOR_SECRET = "muoleef5IeghieX7Ooc1aiwieK7Ta2ee";

    @Deprecated
    public static final String TUN_COLLECTOR_URL = "http://tuncollector.android.schaeuffelhut.de:8080/tuncollector/TunCollector/";

    private final static Configuration INSTANCE = new Configuration();

    public static Configuration getInstance()
    {
        return INSTANCE;
    }

    private final String bugSenseApiKey;
    private final boolean isBugSenseEnabled;

    private Configuration()
    {
        this( Configuration.class.getResourceAsStream( "/config.properties" ) );
    }

    Configuration(InputStream stream)
    {
        final Properties properties = new Properties();
        load( stream, properties );
        bugSenseApiKey = ifBlank( properties.getProperty( "BugSenseApiKey" ), "disabled" );
        isBugSenseEnabled = !"disabled".equals( bugSenseApiKey );

        Log.d( "TorGuard", "BugSense is " + (isBugSenseEnabled ? "enabled" : "disabled") );
    }

    private String ifBlank(String value, String defaultValue)
    {
        if (value == null)
            return defaultValue;
        if (TextUtils.isEmpty( value.trim() ))
            return defaultValue;
        return value;
    }

    private void load(InputStream stream, Properties properties)
    {
        if (stream == null)
        {
            Log.d( "TorGuard", "Configuration: config.properties not found" );
            return;
        }
        try
        {
            properties.load( stream );
        }
        catch (IOException e)
        {
            Log.e( "TorGuard", "Reading Configuration Properties", e );
        }
        finally
        {
            IOUtils.closeQuietly( stream );
        }
    }

    public String getBugSenseApiKey()
    {
        return bugSenseApiKey;
    }

    public boolean isBugSenseEnabled()
    {
        return isBugSenseEnabled;
    }
}
