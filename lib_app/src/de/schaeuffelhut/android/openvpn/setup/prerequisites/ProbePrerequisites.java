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

package de.schaeuffelhut.android.openvpn.setup.prerequisites;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-04-30
 */
public class ProbePrerequisites
{
    private List<ProbeResult> probeResults = Collections.emptyList();

    public void probe(Context context)
    {
        probeResults = new ArrayList<ProbeResult>();
        probeResults.add( ProbeRoot.probeRoot() );
        probeResults.add( new ProbeTunDevice( context ).probe() );
        probeResults.add( new ProbeOpenVpn( context ).probe() );
        probeResults.add( new ProbeBusyBox( context ).probe() );
    }

    public List<ProbeResult> getProbeResults()
    {
        return Collections.unmodifiableList( probeResults );
    }

    public boolean isSuccess()
    {
        for(ProbeResult probeResult : probeResults )
            if ( !PrerequisitesActivity.Status.SUCCESS.equals( probeResult.status ) )
                return false;
        return true;
    }
}
