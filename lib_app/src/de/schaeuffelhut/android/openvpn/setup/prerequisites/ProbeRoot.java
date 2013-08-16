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

import de.schaeuffelhut.android.openvpn.shared.util.Shell;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: fries
 * Date: 4/26/12
 * Time: 10:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProbeRoot
{
    static ProbeResult probeRoot()
    {
        StringBuilder detail = new StringBuilder( "Executing command 'id' using a root shell.\n" );

        CheckForRoot checkForRoot = new CheckForRoot();
        checkForRoot.run();
        detail.append( "Output:\n" );
        detail.append( checkForRoot.getOutput() );
        detail.append( "Evaluation:\n" );

        final boolean hasRoot;
        if (checkForRoot.getExitCode() == 0)
        {
            detail.append( "Exit code was 0, that is good.\n" );
            if ("0".equals( checkForRoot.getUserId() ))
            {
                detail.append( "Detected user id 0, that is good.\n" );
                hasRoot = true;
            }
            else if ("unknown".equals( checkForRoot.getUserId() ))
            {
                detail.append( "Could not find user id in output, that is bad.\n" );
                hasRoot = false;
            }
            else
            {
                detail.append( "User id is " + checkForRoot.getUserId() + ", that is bad.\n" );
                hasRoot = false;
            }
        }
        else
        {
            detail.append( "Exit code was " + checkForRoot.getExitCode() + ", that is bad.\n" );
            hasRoot = false;
        }

        //TODO: link to online resources
        if (!hasRoot)
            detail.append( "Please find out how to root your device before proceeding." );

        if (hasRoot)
            return newProbeResult( detail, PrerequisitesActivity.Status.SUCCESS );
        else
            return newProbeResult( detail, PrerequisitesActivity.Status.FAILED );
    }

    private static ProbeResult newProbeResult(StringBuilder detail, PrerequisitesActivity.Status state)
    {
        return new ProbeResult(
                state,
                "ROOT privileges",
                "Check if device is rooted.",
                detail.toString()
        );
    }

    private static class CheckForRoot extends Shell
    {
        private StringBuilder output = new StringBuilder();
        private int exitCode;
        private String userId = "unknown";

        public CheckForRoot()
        {
            super( "OpenVPN-Settings", "id", true );

            // avoid reporting thousands of failed su attempts, when probing for root access.
            setDoBugSenseExec( false );
        }

        @Override
        protected synchronized void onStdout(String line)
        {
            if (line.startsWith( "uid=" ))
            {
                Matcher matcher = Pattern.compile( "uid=([0-9]+).*" ).matcher( line );

                if (matcher.matches())
                    userId = matcher.group( 1 );
            }
            output.append( line );
            output.append( '\n' );
        }

        @Override
        protected synchronized void onStderr(String line)
        {
            output.append( line );
            output.append( '\n' );
        }

        @Override
        protected void onCmdTerminated(int exitCode)
        {
            this.exitCode = exitCode;
            super.onCmdTerminated( exitCode );
        }

        int getExitCode()
        {
            return exitCode;
        }

        String getOutput()
        {
            return output.toString();
        }

        public String getUserId()
        {
            return userId;
        }
    }
}
