package de.schaeuffelhut.android.openvpn.service.api;

import de.schaeuffelhut.android.openvpn.service.api.OpenVpnConfig;
import de.schaeuffelhut.android.openvpn.service.api.OpenVpnCredentials;
import de.schaeuffelhut.android.openvpn.service.api.OpenVpnPassphrase;
import de.schaeuffelhut.android.openvpn.service.api.OpenVpnState;
import de.schaeuffelhut.android.openvpn.service.api.OpenVpnDaemonState;
import de.schaeuffelhut.android.openvpn.service.api.OpenVpnNetworkState;

/** Listenes to changes occuring in the OpenVPN Settings service
  */
oneway interface IOpenVpnStateListener
{
    void onDaemonStateChanged(in OpenVpnDaemonState toState);
    void onRequestPassphrase();
    void onRequestCredentials();
    void onNetworkStateChanged(in OpenVpnNetworkState fromState, in OpenVpnNetworkState toState, in long time, in String cause, in String localIp, in String remoteIp);
    void onByteCountChanged(in long received, in long sent);
}
