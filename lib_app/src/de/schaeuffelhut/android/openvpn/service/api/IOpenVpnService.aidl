package de.schaeuffelhut.android.openvpn.service.api;

import de.schaeuffelhut.android.openvpn.service.api.OpenVpnConfig;
import de.schaeuffelhut.android.openvpn.service.api.OpenVpnCredentials;
import de.schaeuffelhut.android.openvpn.service.api.OpenVpnPassphrase;
import de.schaeuffelhut.android.openvpn.service.api.OpenVpnState;

/** Interface to single OpenVPN  */
interface IOpenVpnService
{
    void connect(in OpenVpnConfig config);
    void supplyCredentials(in OpenVpnCredentials credentials);
    void supplyPassphrase(in OpenVpnPassphrase passphrase);
    OpenVpnState getStatus();
    void disconnect();
}
