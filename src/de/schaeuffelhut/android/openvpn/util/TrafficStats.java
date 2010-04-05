/**
 * Copyright 2010 Christophe Vandeplas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package de.schaeuffelhut.android.openvpn.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * @author chri
 *
 */
public class TrafficStats {

	public static final int mPollInterval = 3;
	
	DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
	private int tuntapReadBytes = 0;
	private int tuntapWriteBytes = 0;
	
	private int tuntapReadBytesPerSec = 0;
	private int tuntapWriteBytesPerSec = 0;
	
	public TrafficStats() {
		
	}
	
	public void setStats(final int newReadBytes, final int newWriteBytes) {
		tuntapReadBytesPerSec = deltaPerSecond(tuntapReadBytes, newReadBytes);
		tuntapReadBytes = newReadBytes;
		
		tuntapWriteBytesPerSec = deltaPerSecond(tuntapWriteBytes, newWriteBytes);
		tuntapWriteBytes = newWriteBytes;
	}

	private int deltaPerSecond(int oldBytes, int newBytes) {
		return ((newBytes - oldBytes)/ mPollInterval);
	}
	

	public String toSmallInOutPerSecString() {
		return "up: " 
				+ Util.roundDecimalsToString((double) tuntapReadBytesPerSec / 1000) 
				+ " kBps - down: "
				+ Util.roundDecimalsToString((double) tuntapWriteBytesPerSec / 1000) 
				+ " kBps";
	}
	
	
}
