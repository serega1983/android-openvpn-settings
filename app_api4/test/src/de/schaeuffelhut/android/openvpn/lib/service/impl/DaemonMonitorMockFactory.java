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

package de.schaeuffelhut.android.openvpn.lib.service.impl;

import de.schaeuffelhut.android.openvpn.lib.service.impl.DaemonMonitor;
import de.schaeuffelhut.android.openvpn.lib.service.impl.DaemonMonitorFactory;
import junit.framework.AssertionFailedError;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
* @author Friedrich Schäuffelhut
* @since 2012-11-03
*/
public class DaemonMonitorMockFactory implements DaemonMonitorFactory
{
    final List<DaemonMonitor> mockDaemonMonitors = new ArrayList<DaemonMonitor>();

    public DaemonMonitor createDaemonMonitorFor(File configFile)
    {
        class State { boolean isStarted; }
        final State state = new State();
        state.isStarted = configFile.getName().contains( "ALIVE" );

        DaemonMonitor daemonMonitor = Mockito.mock( DaemonMonitor.class );
        mockDaemonMonitors.add( daemonMonitor );

        Mockito.when( daemonMonitor.isAlive() ).then( new Answer<Object>()
        {
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                return state.isStarted;
            }
        } );

        Mockito.when( daemonMonitor.getConfigFile() ).thenReturn( configFile );

        Mockito.doAnswer( new Answer()
        {
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                state.isStarted = false;
                return null;
            }
        }).when( daemonMonitor ).stop();

        Mockito.doAnswer( new Answer()
        {
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                state.isStarted = true;
                return null;
            }
        }).when( daemonMonitor ).start();

        return daemonMonitor;
    }

    public DaemonMonitor getLastMockDaemonMonitorCreated()
    {
        if ( mockDaemonMonitors.isEmpty() )
            throw new AssertionFailedError( "Expected at least one created MockDaemonMonitor" );
        return mockDaemonMonitors.get( mockDaemonMonitors.size() - 1 );
    }
}
