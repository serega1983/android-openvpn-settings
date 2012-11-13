package de.schaeuffelhut.android.openvpn.service.api;

import de.schaeuffelhut.android.openvpn.service.api.OpenVpnConfig;
import de.schaeuffelhut.android.openvpn.service.api.OpenVpnState;

/** Example service interface */
interface IfcOpenVpnService
 {
    void connectTo(in OpenVpnConfig config);
    void authenticate(in String username, in String password);
    OpenVpnState getStatus();
    void disconnect();
}
