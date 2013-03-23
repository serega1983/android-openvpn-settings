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
package org.apache.commons.io.output;
 
import java.io.IOException;
import java.io.OutputStream;

/**
 * This OutputStream writes all data to the famous <b>/dev/null</b>.
 * <p>
 * This output stream has no destination (file/socket etc.) and all
 * bytes written to it are ignored and lost.
 * 
 * @author Jeremias Maerki
 * @version $Id$
 */
public class NullOutputStream extends OutputStream {
    
    /**
     * A singleton.
     */
    public static final NullOutputStream NULL_OUTPUT_STREAM = new NullOutputStream();

    /**
     * Does nothing - output to <code>/dev/null</code>.
     * @param b The bytes to write
     * @param off The start offset
     * @param len The number of bytes to write
     */
    @Override
    public void write(byte[] b, int off, int len) {
        //to /dev/null
    }

    /**
     * Does nothing - output to <code>/dev/null</code>.
     * @param b The byte to write
     */
    @Override
    public void write(int b) {
        //to /dev/null
    }

    /**
     * Does nothing - output to <code>/dev/null</code>.
     * @param b The bytes to write
     * @throws IOException never
     */
    @Override
    public void write(byte[] b) throws IOException {
        //to /dev/null
    }

}