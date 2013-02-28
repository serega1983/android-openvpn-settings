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

package de.schaeuffelhut.android.openvpn.shared.util;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-01-25
 */
public class OpenVpnBinaryTest extends TestCase
{
    public void test_OPENVPN_USAGE_211_NOIPROUTE_has_x_lines()
    {
        Assert.assertEquals( 408, OPENVPN_USAGE_211_NOIPROUTE.size() );
    }

    public void test_OPENVPN_USAGE_211_IPROUTE_has_409_lines()
    {
        Assert.assertEquals( 409, OPENVPN_USAGE_211_IPROUTE.size() );
    }

    public void test_version_231()
    {
        OpenVpnBinary openVpnBinary = new OpenVpnBinary( new File( "openvpn" ), Collections.unmodifiableList( Arrays.asList( ("" +
                "OpenVPN 2.3.1 i686-pc-linux-gnu [SSL] [LZO2] [EPOLL] built on Jan 25 2013\n" +
                "\n" +
                "General Options:\n" +
                "--config file   : Read configuration options from file.\n")
                .split( "\n" )
        ) ) );
        Assert.assertEquals( "2.3.1", openVpnBinary.getVersion() );
    }

    public void test_version_211()
    {
        OpenVpnBinary openVpnBinary = new OpenVpnBinary( new File( "openvpn" ), OPENVPN_USAGE_211_IPROUTE );
        Assert.assertEquals( "2.1.1", openVpnBinary.getVersion() );
    }

    public void test_hasIpRoute_with_disabled_iproute()
    {
        OpenVpnBinary openVpnBinary = new OpenVpnBinary( new File( "openvpn" ), OPENVPN_USAGE_211_NOIPROUTE );
        Assert.assertFalse( openVpnBinary.hasIpRoute() );
    }

    public void test_hasIpRoute_with_enabled_iproute()
    {
        OpenVpnBinary openVpnBinary = new OpenVpnBinary( new File( "openvpn" ), OPENVPN_USAGE_211_IPROUTE );
        Assert.assertTrue( openVpnBinary.hasIpRoute() );
    }


    private final List<String> OPENVPN_USAGE_211_NOIPROUTE = Collections.unmodifiableList( Arrays.asList( ("" +
            "OpenVPN 2.1.1 i686-pc-linux-gnu [SSL] [LZO2] [EPOLL] built on Jan 25 2013\n" +
            "\n" +
            "General Options:\n" +
            "--config file   : Read configuration options from file.\n" +
            "--help          : Show options.\n" +
            "--version       : Show copyright and version information.\n" +
            "\n" +
            "Tunnel Options:\n" +
            "--local host    : Local host name or ip address. Implies --bind.\n" +
            "--remote host [port] : Remote host name or ip address.\n" +
            "--remote-random : If multiple --remote options specified, choose one randomly.\n" +
            "--remote-random-hostname : Add a random string to remote DNS name.\n" +
            "--mode m        : Major mode, m = 'p2p' (default, point-to-point) or 'server'.\n" +
            "--proto p       : Use protocol p for communicating with peer.\n" +
            "                  p = udp (default), tcp-server, or tcp-client\n" +
            "--connect-retry n : For --proto tcp-client, number of seconds to wait\n" +
            "                    between connection retries (default=5).\n" +
            "--connect-timeout n : For --proto tcp-client, connection timeout (in seconds).\n" +
            "--connect-retry-max n : Maximum connection attempt retries, default infinite.\n" +
            "--auto-proxy    : Try to sense proxy settings (or lack thereof) automatically.\n" +
            "--http-proxy s p [up] [auth] : Connect to remote host\n" +
            "                  through an HTTP proxy at address s and port p.\n" +
            "                  If proxy authentication is required,\n" +
            "                  up is a file containing username/password on 2 lines, or\n" +
            "                  'stdin' to prompt from console.  Add auth='ntlm' if\n" +
            "                  the proxy requires NTLM authentication.\n" +
            "--http-proxy s p 'auto': Like the above directive, but automatically determine\n" +
            "                         auth method and query for username/password if needed.\n" +
            "--http-proxy-retry     : Retry indefinitely on HTTP proxy errors.\n" +
            "--http-proxy-timeout n : Proxy timeout in seconds, default=5.\n" +
            "--http-proxy-option type [parm] : Set extended HTTP proxy options.\n" +
            "                                  Repeat to set multiple options.\n" +
            "                  VERSION version (default=1.0)\n" +
            "                  AGENT user-agent\n" +
            "--socks-proxy s [p]: Connect to remote host through a Socks5 proxy at address\n" +
            "                  s and port p (default port = 1080).\n" +
            "--socks-proxy-retry : Retry indefinitely on Socks proxy errors.\n" +
            "--resolv-retry n: If hostname resolve fails for --remote, retry\n" +
            "                  resolve for n seconds before failing (disabled by default).\n" +
            "                  Set n=\"infinite\" to retry indefinitely.\n" +
            "--float         : Allow remote to change its IP address/port, such as through\n" +
            "                  DHCP (this is the default if --remote is not used).\n" +
            "--ipchange cmd  : Execute shell command cmd on remote ip address initial\n" +
            "                  setting or change -- execute as: cmd ip-address port#\n" +
            "--port port     : TCP/UDP port # for both local and remote.\n" +
            "--lport port    : TCP/UDP port # for local (default=1194). Implies --bind.\n" +
            "--rport port    : TCP/UDP port # for remote (default=1194).\n" +
            "--bind          : Bind to local address and port. (This is the default unless\n" +
            "                  --proto tcp-client or --http-proxy or --socks-proxy is used).\n" +
            "--nobind        : Do not bind to local address and port.\n" +
            "--dev tunX|tapX : tun/tap device (X can be omitted for dynamic device.\n" +
            "--dev-type dt   : Which device type are we using? (dt = tun or tap) Use\n" +
            "                  this option only if the tun/tap device used with --dev\n" +
            "                  does not begin with \"tun\" or \"tap\".\n" +
            "--dev-node node : Explicitly set the device node rather than using\n" +
            "                  /dev/net/tun, /dev/tun, /dev/tap, etc.\n" +
            "--lladdr hw     : Set the link layer address of the tap device.\n" +
            "--topology t    : Set --dev tun topology: 'net30', 'p2p', or 'subnet'.\n" +
            "--tun-ipv6      : Build tun link capable of forwarding IPv6 traffic.\n" +
            "--ifconfig l rn : TUN: configure device to use IP address l as a local\n" +
            "                  endpoint and rn as a remote endpoint.  l & rn should be\n" +
            "                  swapped on the other peer.  l & rn must be private\n" +
            "                  addresses outside of the subnets used by either peer.\n" +
            "                  TAP: configure device to use IP address l as a local\n" +
            "                  endpoint and rn as a subnet mask.\n" +
            "--ifconfig-noexec : Don't actually execute ifconfig/netsh command, instead\n" +
            "                    pass --ifconfig parms by environment to scripts.\n" +
            "--ifconfig-nowarn : Don't warn if the --ifconfig option on this side of the\n" +
            "                    connection doesn't match the remote side.\n" +
            "--route network [netmask] [gateway] [metric] :\n" +
            "                  Add route to routing table after connection\n" +
            "                  is established.  Multiple routes can be specified.\n" +
            "                  netmask default: 255.255.255.255\n" +
            "                  gateway default: taken from --route-gateway or --ifconfig\n" +
            "                  Specify default by leaving blank or setting to \"nil\".\n" +
            "--max-routes n :  Specify the maximum number of routes that may be defined\n" +
            "                  or pulled from a server.\n" +
            "--route-gateway gw|'dhcp' : Specify a default gateway for use with --route.\n" +
            "--route-metric m : Specify a default metric for use with --route.\n" +
            "--route-delay n [w] : Delay n seconds after connection initiation before\n" +
            "                  adding routes (may be 0).  If not specified, routes will\n" +
            "                  be added immediately after tun/tap open.  On Windows, wait\n" +
            "                  up to w seconds for TUN/TAP adapter to come up.\n" +
            "--route-up cmd  : Execute shell cmd after routes are added.\n" +
            "--route-noexec  : Don't add routes automatically.  Instead pass routes to\n" +
            "                  --route-up script using environmental variables.\n" +
            "--route-nopull  : When used with --client or --pull, accept options pushed\n" +
            "                  by server EXCEPT for routes.\n" +
            "--allow-pull-fqdn : Allow client to pull DNS names from server for\n" +
            "                    --ifconfig, --route, and --route-gateway.\n" +
            "--redirect-gateway [flags]: Automatically execute routing\n" +
            "                  commands to redirect all outgoing IP traffic through the\n" +
            "                  VPN.  Add 'local' flag if both OpenVPN servers are directly\n" +
            "                  connected via a common subnet, such as with WiFi.\n" +
            "                  Add 'def1' flag to set default route using using 0.0.0.0/1\n" +
            "                  and 128.0.0.0/1 rather than 0.0.0.0/0.  Add 'bypass-dhcp'\n" +
            "                  flag to add a direct route to DHCP server, bypassing tunnel.\n" +
            "                  Add 'bypass-dns' flag to similarly bypass tunnel for DNS.\n" +
            "--redirect-private [flags]: Like --redirect-gateway, but omit actually changing\n" +
            "                  the default gateway.  Useful when pushing private subnets.\n" +
            "--setenv name value : Set a custom environmental variable to pass to script.\n" +
            "--setenv FORWARD_COMPATIBLE 1 : Relax config file syntax checking to allow\n" +
            "                  directives for future OpenVPN versions to be ignored.\n" +
            "--script-security level mode : mode='execve' (default) or 'system', level=\n" +
            "                  0 -- strictly no calling of external programs\n" +
            "                  1 -- (default) only call built-ins such as ifconfig\n" +
            "                  2 -- allow calling of built-ins and scripts\n" +
            "                  3 -- allow password to be passed to scripts via env\n" +
            "--shaper n      : Restrict output to peer to n bytes per second.\n" +
            "--keepalive n m : Helper option for setting timeouts in server mode.  Send\n" +
            "                  ping once every n seconds, restart if ping not received\n" +
            "                  for m seconds.\n" +
            "--inactive n [bytes] : Exit after n seconds of activity on tun/tap device\n" +
            "                  produces a combined in/out byte count < bytes.\n" +
            "--ping-exit n   : Exit if n seconds pass without reception of remote ping.\n" +
            "--ping-restart n: Restart if n seconds pass without reception of remote ping.\n" +
            "--ping-timer-rem: Run the --ping-exit/--ping-restart timer only if we have a\n" +
            "                  remote address.\n" +
            "--ping n        : Ping remote once every n seconds over TCP/UDP port.\n" +
            "--multihome     : Configure a multi-homed UDP server.\n" +
            "--fast-io       : (experimental) Optimize TUN/TAP/UDP writes.\n" +
            "--remap-usr1 s  : On SIGUSR1 signals, remap signal (s='SIGHUP' or 'SIGTERM').\n" +
            "--persist-tun   : Keep tun/tap device open across SIGUSR1 or --ping-restart.\n" +
            "--persist-remote-ip : Keep remote IP address across SIGUSR1 or --ping-restart.\n" +
            "--persist-local-ip  : Keep local IP address across SIGUSR1 or --ping-restart.\n" +
            "--persist-key   : Don't re-read key files across SIGUSR1 or --ping-restart.\n" +
            "--tun-mtu n     : Take the tun/tap device MTU to be n and derive the\n" +
            "                  TCP/UDP MTU from it (default=1500).\n" +
            "--tun-mtu-extra n : Assume that tun/tap device might return as many\n" +
            "                  as n bytes more than the tun-mtu size on read\n" +
            "                  (default TUN=0 TAP=32).\n" +
            "--link-mtu n    : Take the TCP/UDP device MTU to be n and derive the tun MTU\n" +
            "                  from it.\n" +
            "--mtu-disc type : Should we do Path MTU discovery on TCP/UDP channel?\n" +
            "                  'no'    -- Never send DF (Don't Fragment) frames\n" +
            "                  'maybe' -- Use per-route hints\n" +
            "                  'yes'   -- Always DF (Don't Fragment)\n" +
            "--mtu-test      : Empirically measure and report MTU.\n" +
            "--fragment max  : Enable internal datagram fragmentation so that no UDP\n" +
            "                  datagrams are sent which are larger than max bytes.\n" +
            "                  Adds 4 bytes of overhead per datagram.\n" +
            "--mssfix [n]    : Set upper bound on TCP MSS, default = tun-mtu size\n" +
            "                  or --fragment max value, whichever is lower.\n" +
            "--sndbuf size   : Set the TCP/UDP send buffer size.\n" +
            "--rcvbuf size   : Set the TCP/UDP receive buffer size.\n" +
            "--txqueuelen n  : Set the tun/tap TX queue length to n (Linux only).\n" +
            "--mlock         : Disable Paging -- ensures key material and tunnel\n" +
            "                  data will never be written to disk.\n" +
            "--up cmd        : Shell cmd to execute after successful tun device open.\n" +
            "                  Execute as: cmd tun/tap-dev tun-mtu link-mtu \\\n" +
            "                              ifconfig-local-ip ifconfig-remote-ip\n" +
            "                  (pre --user or --group UID/GID change)\n" +
            "--up-delay      : Delay tun/tap open and possible --up script execution\n" +
            "                  until after TCP/UDP connection establishment with peer.\n" +
            "--down cmd      : Shell cmd to run after tun device close.\n" +
            "                  (post --user/--group UID/GID change and/or --chroot)\n" +
            "                  (script parameters are same as --up option)\n" +
            "--down-pre      : Call --down cmd/script before TUN/TAP close.\n" +
            "--up-restart    : Run up/down scripts for all restarts including those\n" +
            "                  caused by --ping-restart or SIGUSR1\n" +
            "--user user     : Set UID to user after initialization.\n" +
            "--group group   : Set GID to group after initialization.\n" +
            "--chroot dir    : Chroot to this directory after initialization.\n" +
            "--cd dir        : Change to this directory before initialization.\n" +
            "--daemon [name] : Become a daemon after initialization.\n" +
            "                  The optional 'name' parameter will be passed\n" +
            "                  as the program name to the system logger.\n" +
            "--syslog [name] : Output to syslog, but do not become a daemon.\n" +
            "                  See --daemon above for a description of the 'name' parm.\n" +
            "--inetd [name] ['wait'|'nowait'] : Run as an inetd or xinetd server.\n" +
            "                  See --daemon above for a description of the 'name' parm.\n" +
            "--log file      : Output log to file which is created/truncated on open.\n" +
            "--log-append file : Append log to file, or create file if nonexistent.\n" +
            "--suppress-timestamps : Don't log timestamps to stdout/stderr.\n" +
            "--writepid file : Write main process ID to file.\n" +
            "--nice n        : Change process priority (>0 = lower, <0 = higher).\n" +
            "--echo [parms ...] : Echo parameters to log output.\n" +
            "--verb n        : Set output verbosity to n (default=1):\n" +
            "                  (Level 3 is recommended if you want a good summary\n" +
            "                  of what's happening without being swamped by output).\n" +
            "                : 0 -- no output except fatal errors\n" +
            "                : 1 -- startup info + connection initiated messages +\n" +
            "                       non-fatal encryption & net errors\n" +
            "                : 2,3 -- show TLS negotiations & route info\n" +
            "                : 4 -- show parameters\n" +
            "                : 5 -- show 'RrWw' chars on console for each packet sent\n" +
            "                       and received from TCP/UDP (caps) or tun/tap (lc)\n" +
            "                : 6 to 11 -- debug messages of increasing verbosity\n" +
            "--mute n        : Log at most n consecutive messages in the same category.\n" +
            "--status file n : Write operational status to file every n seconds.\n" +
            "--status-version [n] : Choose the status file format version number.\n" +
            "                  Currently, n can be 1, 2, or 3 (default=1).\n" +
            "--disable-occ   : Disable options consistency check between peers.\n" +
            "--gremlin mask  : Special stress testing mode (for debugging only).\n" +
            "--comp-lzo      : Use fast LZO compression -- may add up to 1 byte per\n" +
            "                  packet for uncompressible data.\n" +
            "--comp-noadapt  : Don't use adaptive compression when --comp-lzo\n" +
            "                  is specified.\n" +
            "--management ip port [pass] : Enable a TCP server on ip:port to handle\n" +
            "                  management functions.  pass is a password file\n" +
            "                  or 'stdin' to prompt from console.\n" +
            "                  To listen on a unix domain socket, specific the pathname\n" +
            "                  in place of ip and use 'unix' as the port number.\n" +
            "--management-client : Management interface will connect as a TCP client to\n" +
            "                      ip/port rather than listen as a TCP server.\n" +
            "--management-query-passwords : Query management channel for private key\n" +
            "                  and auth-user-pass passwords.\n" +
            "--management-hold : Start OpenVPN in a hibernating state, until a client\n" +
            "                    of the management interface explicitly starts it.\n" +
            "--management-signal : Issue SIGUSR1 when management disconnect event occurs.\n" +
            "--management-forget-disconnect : Forget passwords when management disconnect\n" +
            "                                 event occurs.\n" +
            "--management-log-cache n : Cache n lines of log file history for usage\n" +
            "                  by the management channel.\n" +
            "--management-client-user u  : When management interface is a unix socket, only\n" +
            "                              allow connections from user u.\n" +
            "--management-client-group g : When management interface is a unix socket, only\n" +
            "                              allow connections from group g.\n" +
            "--management-client-auth : gives management interface client the responsibility\n" +
            "                           to authenticate clients after their client certificate\n" +
            "\t\t\t      has been verified.\n" +
            "--management-client-pf : management interface clients must specify a packet\n" +
            "                         filter file for each connecting client.\n" +
            "--plugin m [str]: Load plug-in module m passing str as an argument\n" +
            "                  to its initialization function.\n" +
            "\n" +
            "Multi-Client Server options (when --mode server is used):\n" +
            "--server network netmask : Helper option to easily configure server mode.\n" +
            "--server-bridge [IP netmask pool-start-IP pool-end-IP] : Helper option to\n" +
            "                    easily configure ethernet bridging server mode.\n" +
            "--push \"option\" : Push a config file option back to the peer for remote\n" +
            "                  execution.  Peer must specify --pull in its config file.\n" +
            "--push-reset    : Don't inherit global push list for specific\n" +
            "                  client instance.\n" +
            "--ifconfig-pool start-IP end-IP [netmask] : Set aside a pool of subnets\n" +
            "                  to be dynamically allocated to connecting clients.\n" +
            "--ifconfig-pool-linear : Use individual addresses rather than /30 subnets\n" +
            "                  in tun mode.  Not compatible with Windows clients.\n" +
            "--ifconfig-pool-persist file [seconds] : Persist/unpersist ifconfig-pool\n" +
            "                  data to file, at seconds intervals (default=600).\n" +
            "                  If seconds=0, file will be treated as read-only.\n" +
            "--ifconfig-push local remote-netmask : Push an ifconfig option to remote,\n" +
            "                  overrides --ifconfig-pool dynamic allocation.\n" +
            "                  Only valid in a client-specific config file.\n" +
            "--iroute network [netmask] : Route subnet to client.\n" +
            "                  Sets up internal routes only.\n" +
            "                  Only valid in a client-specific config file.\n" +
            "--disable       : Client is disabled.\n" +
            "                  Only valid in a client-specific config file.\n" +
            "--client-cert-not-required : Don't require client certificate, client\n" +
            "                  will authenticate using username/password.\n" +
            "--username-as-common-name  : For auth-user-pass authentication, use\n" +
            "                  the authenticated username as the common name,\n" +
            "                  rather than the common name from the client cert.\n" +
            "--auth-user-pass-verify cmd method: Query client for username/password and\n" +
            "                  run script cmd to verify.  If method='via-env', pass\n" +
            "                  user/pass via environment, if method='via-file', pass\n" +
            "                  user/pass via temporary file.\n" +
            "--opt-verify    : Clients that connect with options that are incompatible\n" +
            "                  with those of the server will be disconnected.\n" +
            "--auth-user-pass-optional : Allow connections by clients that don't\n" +
            "                  specify a username/password.\n" +
            "--no-name-remapping : Allow Common Name and X509 Subject to include\n" +
            "                      any printable character.\n" +
            "--client-to-client : Internally route client-to-client traffic.\n" +
            "--duplicate-cn  : Allow multiple clients with the same common name to\n" +
            "                  concurrently connect.\n" +
            "--client-connect cmd : Run script cmd on client connection.\n" +
            "--client-disconnect cmd : Run script cmd on client disconnection.\n" +
            "--client-config-dir dir : Directory for custom client config files.\n" +
            "--ccd-exclusive : Refuse connection unless custom client config is found.\n" +
            "--tmp-dir dir   : Temporary directory, used for --client-connect return file.\n" +
            "--hash-size r v : Set the size of the real address hash table to r and the\n" +
            "                  virtual address table to v.\n" +
            "--bcast-buffers n : Allocate n broadcast buffers.\n" +
            "--tcp-queue-limit n : Maximum number of queued TCP output packets.\n" +
            "--tcp-nodelay   : Macro that sets TCP_NODELAY socket flag on the server\n" +
            "                  as well as pushes it to connecting clients.\n" +
            "--learn-address cmd : Run script cmd to validate client virtual addresses.\n" +
            "--connect-freq n s : Allow a maximum of n new connections per s seconds.\n" +
            "--max-clients n : Allow a maximum of n simultaneously connected clients.\n" +
            "--max-routes-per-client n : Allow a maximum of n internal routes per client.\n" +
            "--port-share host port : When run in TCP mode, proxy incoming HTTPS sessions\n" +
            "                  to a web server at host:port.\n" +
            "\n" +
            "Client options (when connecting to a multi-client server):\n" +
            "--client         : Helper option to easily configure client mode.\n" +
            "--auth-user-pass [up] : Authenticate with server using username/password.\n" +
            "                  up is a file containing username/password on 2 lines,\n" +
            "                  or omit to prompt from console.\n" +
            "--pull           : Accept certain config file options from the peer as if they\n" +
            "                  were part of the local config file.  Must be specified\n" +
            "                  when connecting to a '--mode server' remote host.\n" +
            "--auth-retry t  : How to handle auth failures.  Set t to\n" +
            "                  none (default), interact, or nointeract.\n" +
            "--server-poll-timeout n : when polling possible remote servers to connect to\n" +
            "                  in a round-robin fashion, spend no more than n seconds\n" +
            "                  waiting for a response before trying the next server.\n" +
            "--explicit-exit-notify [n] : On exit/restart, send exit signal to\n" +
            "                  server/remote. n = # of retries, default=1.\n" +
            "\n" +
            "Data Channel Encryption Options (must be compatible between peers):\n" +
            "(These options are meaningful for both Static Key & TLS-mode)\n" +
            "--secret f [d]  : Enable Static Key encryption mode (non-TLS).\n" +
            "                  Use shared secret file f, generate with --genkey.\n" +
            "                  The optional d parameter controls key directionality.\n" +
            "                  If d is specified, use separate keys for each\n" +
            "                  direction, set d=0 on one side of the connection,\n" +
            "                  and d=1 on the other side.\n" +
            "--auth alg      : Authenticate packets with HMAC using message\n" +
            "                  digest algorithm alg (default=SHA1).\n" +
            "                  (usually adds 16 or 20 bytes per packet)\n" +
            "                  Set alg=none to disable authentication.\n" +
            "--cipher alg    : Encrypt packets with cipher algorithm alg\n" +
            "                  (default=BF-CBC).\n" +
            "                  Set alg=none to disable encryption.\n" +
            "--prng alg [nsl] : For PRNG, use digest algorithm alg, and\n" +
            "                   nonce_secret_len=nsl.  Set alg=none to disable PRNG.\n" +
            "--keysize n     : Size of cipher key in bits (optional).\n" +
            "                  If unspecified, defaults to cipher-specific default.\n" +
            "--engine [name] : Enable OpenSSL hardware crypto engine functionality.\n" +
            "--no-replay     : Disable replay protection.\n" +
            "--mute-replay-warnings : Silence the output of replay warnings to log file.\n" +
            "--replay-window n [t]  : Use a replay protection sliding window of size n\n" +
            "                         and a time window of t seconds.\n" +
            "                         Default n=64 t=15\n" +
            "--no-iv         : Disable cipher IV -- only allowed with CBC mode ciphers.\n" +
            "--replay-persist file : Persist replay-protection state across sessions\n" +
            "                  using file.\n" +
            "--test-crypto   : Run a self-test of crypto features enabled.\n" +
            "                  For debugging only.\n" +
            "\n" +
            "TLS Key Negotiation Options:\n" +
            "(These options are meaningful only for TLS-mode)\n" +
            "--tls-server    : Enable TLS and assume server role during TLS handshake.\n" +
            "--tls-client    : Enable TLS and assume client role during TLS handshake.\n" +
            "--key-method m  : Data channel key exchange method.  m should be a method\n" +
            "                  number, such as 1 (default), 2, etc.\n" +
            "--ca file       : Certificate authority file in .pem format containing\n" +
            "                  root certificate.\n" +
            "--capath dir    : A directory of trusted certificates (CAs and CRLs).\n" +
            "--dh file       : File containing Diffie Hellman parameters\n" +
            "                  in .pem format (for --tls-server only).\n" +
            "                  Use \"openssl dhparam -out dh1024.pem 1024\" to generate.\n" +
            "--cert file     : Local certificate in .pem format -- must be signed\n" +
            "                  by a Certificate Authority in --ca file.\n" +
            "--key file      : Local private key in .pem format.\n" +
            "--pkcs12 file   : PKCS#12 file containing local private key, local certificate\n" +
            "                  and optionally the root CA certificate.\n" +
            "--tls-cipher l  : A list l of allowable TLS ciphers separated by : (optional).\n" +
            "                : Use --show-tls to see a list of supported TLS ciphers.\n" +
            "--tls-timeout n : Packet retransmit timeout on TLS control channel\n" +
            "                  if no ACK from remote within n seconds (default=2).\n" +
            "--reneg-bytes n : Renegotiate data chan. key after n bytes sent and recvd.\n" +
            "--reneg-pkts n  : Renegotiate data chan. key after n packets sent and recvd.\n" +
            "--reneg-sec n   : Renegotiate data chan. key after n seconds (default=3600).\n" +
            "--hand-window n : Data channel key exchange must finalize within n seconds\n" +
            "                  of handshake initiation by any peer (default=60).\n" +
            "--tran-window n : Transition window -- old key can live this many seconds\n" +
            "                  after new key renegotiation begins (default=3600).\n" +
            "--single-session: Allow only one session (reset state on restart).\n" +
            "--tls-exit      : Exit on TLS negotiation failure.\n" +
            "--tls-auth f [d]: Add an additional layer of authentication on top of the TLS\n" +
            "                  control channel to protect against DoS attacks.\n" +
            "                  f (required) is a shared-secret passphrase file.\n" +
            "                  The optional d parameter controls key directionality,\n" +
            "                  see --secret option for more info.\n" +
            "--askpass [file]: Get PEM password from controlling tty before we daemonize.\n" +
            "--auth-nocache  : Don't cache --askpass or --auth-user-pass passwords.\n" +
            "--crl-verify crl: Check peer certificate against a CRL.\n" +
            "--tls-verify cmd: Execute shell command cmd to verify the X509 name of a\n" +
            "                  pending TLS connection that has otherwise passed all other\n" +
            "                  tests of certification.  cmd should return 0 to allow\n" +
            "                  TLS handshake to proceed, or 1 to fail.  (cmd is\n" +
            "                  executed as 'cmd certificate_depth X509_NAME_oneline')\n" +
            "--tls-remote x509name: Accept connections only from a host with X509 name\n" +
            "                  x509name. The remote host must also pass all other tests\n" +
            "                  of verification.\n" +
            "--ns-cert-type t: Require that peer certificate was signed with an explicit\n" +
            "                  nsCertType designation t = 'client' | 'server'.\n" +
            "--remote-cert-ku v ... : Require that the peer certificate was signed with\n" +
            "                  explicit key usage, you can specify more than one value.\n" +
            "                  value should be given in hex format.\n" +
            "--remote-cert-eku oid : Require that the peer certificate was signed with\n" +
            "                  explicit extended key usage. Extended key usage can be encoded\n" +
            "                  as an object identifier or OpenSSL string representation.\n" +
            "--remote-cert-tls t: Require that peer certificate was signed with explicit\n" +
            "                  key usage and extended key usage based on RFC3280 TLS rules.\n" +
            "                  t = 'client' | 'server'.\n" +
            "\n" +
            "SSL Library information:\n" +
            "--show-ciphers  : Show cipher algorithms to use with --cipher option.\n" +
            "--show-digests  : Show message digest algorithms to use with --auth option.\n" +
            "--show-engines  : Show hardware crypto accelerator engines (if available).\n" +
            "--show-tls      : Show all TLS ciphers (TLS used only as a control channel).\n" +
            "\n" +
            "Generate a random key (only for non-TLS static key encryption mode):\n" +
            "--genkey        : Generate a random key to be used as a shared secret,\n" +
            "                  for use with the --secret option.\n" +
            "--secret file   : Write key to file.\n" +
            "\n" +
            "Tun/tap config mode (available with linux 2.4+):\n" +
            "--mktun         : Create a persistent tunnel.\n" +
            "--rmtun         : Remove a persistent tunnel.\n" +
            "--dev tunX|tapX : tun/tap device\n" +
            "--dev-type dt   : Device type.  See tunnel options above for details.\n" +
            "--user user     : User to set privilege to.\n" +
            "--group group   : Group to set privilege to.\n")
            .split( "\n" )
    ) );

    private final List<String> OPENVPN_USAGE_211_IPROUTE = Collections.unmodifiableList( Arrays.asList( ("" +
            "OpenVPN 2.1.1 i686-pc-linux-gnu [SSL] [LZO2] [EPOLL] built on Jan 25 2013\n" +
            "\n" +
            "General Options:\n" +
            "--config file   : Read configuration options from file.\n" +
            "--help          : Show options.\n" +
            "--version       : Show copyright and version information.\n" +
            "\n" +
            "Tunnel Options:\n" +
            "--local host    : Local host name or ip address. Implies --bind.\n" +
            "--remote host [port] : Remote host name or ip address.\n" +
            "--remote-random : If multiple --remote options specified, choose one randomly.\n" +
            "--remote-random-hostname : Add a random string to remote DNS name.\n" +
            "--mode m        : Major mode, m = 'p2p' (default, point-to-point) or 'server'.\n" +
            "--proto p       : Use protocol p for communicating with peer.\n" +
            "                  p = udp (default), tcp-server, or tcp-client\n" +
            "--connect-retry n : For --proto tcp-client, number of seconds to wait\n" +
            "                    between connection retries (default=5).\n" +
            "--connect-timeout n : For --proto tcp-client, connection timeout (in seconds).\n" +
            "--connect-retry-max n : Maximum connection attempt retries, default infinite.\n" +
            "--auto-proxy    : Try to sense proxy settings (or lack thereof) automatically.\n" +
            "--http-proxy s p [up] [auth] : Connect to remote host\n" +
            "                  through an HTTP proxy at address s and port p.\n" +
            "                  If proxy authentication is required,\n" +
            "                  up is a file containing username/password on 2 lines, or\n" +
            "                  'stdin' to prompt from console.  Add auth='ntlm' if\n" +
            "                  the proxy requires NTLM authentication.\n" +
            "--http-proxy s p 'auto': Like the above directive, but automatically determine\n" +
            "                         auth method and query for username/password if needed.\n" +
            "--http-proxy-retry     : Retry indefinitely on HTTP proxy errors.\n" +
            "--http-proxy-timeout n : Proxy timeout in seconds, default=5.\n" +
            "--http-proxy-option type [parm] : Set extended HTTP proxy options.\n" +
            "                                  Repeat to set multiple options.\n" +
            "                  VERSION version (default=1.0)\n" +
            "                  AGENT user-agent\n" +
            "--socks-proxy s [p]: Connect to remote host through a Socks5 proxy at address\n" +
            "                  s and port p (default port = 1080).\n" +
            "--socks-proxy-retry : Retry indefinitely on Socks proxy errors.\n" +
            "--resolv-retry n: If hostname resolve fails for --remote, retry\n" +
            "                  resolve for n seconds before failing (disabled by default).\n" +
            "                  Set n=\"infinite\" to retry indefinitely.\n" +
            "--float         : Allow remote to change its IP address/port, such as through\n" +
            "                  DHCP (this is the default if --remote is not used).\n" +
            "--ipchange cmd  : Execute shell command cmd on remote ip address initial\n" +
            "                  setting or change -- execute as: cmd ip-address port#\n" +
            "--port port     : TCP/UDP port # for both local and remote.\n" +
            "--lport port    : TCP/UDP port # for local (default=1194). Implies --bind.\n" +
            "--rport port    : TCP/UDP port # for remote (default=1194).\n" +
            "--bind          : Bind to local address and port. (This is the default unless\n" +
            "                  --proto tcp-client or --http-proxy or --socks-proxy is used).\n" +
            "--nobind        : Do not bind to local address and port.\n" +
            "--dev tunX|tapX : tun/tap device (X can be omitted for dynamic device.\n" +
            "--dev-type dt   : Which device type are we using? (dt = tun or tap) Use\n" +
            "                  this option only if the tun/tap device used with --dev\n" +
            "                  does not begin with \"tun\" or \"tap\".\n" +
            "--dev-node node : Explicitly set the device node rather than using\n" +
            "                  /dev/net/tun, /dev/tun, /dev/tap, etc.\n" +
            "--lladdr hw     : Set the link layer address of the tap device.\n" +
            "--topology t    : Set --dev tun topology: 'net30', 'p2p', or 'subnet'.\n" +
            "--tun-ipv6      : Build tun link capable of forwarding IPv6 traffic.\n" +
            "--iproute cmd   : Use this command instead of default ip.\n" +
            "--ifconfig l rn : TUN: configure device to use IP address l as a local\n" +
            "                  endpoint and rn as a remote endpoint.  l & rn should be\n" +
            "                  swapped on the other peer.  l & rn must be private\n" +
            "                  addresses outside of the subnets used by either peer.\n" +
            "                  TAP: configure device to use IP address l as a local\n" +
            "                  endpoint and rn as a subnet mask.\n" +
            "--ifconfig-noexec : Don't actually execute ifconfig/netsh command, instead\n" +
            "                    pass --ifconfig parms by environment to scripts.\n" +
            "--ifconfig-nowarn : Don't warn if the --ifconfig option on this side of the\n" +
            "                    connection doesn't match the remote side.\n" +
            "--route network [netmask] [gateway] [metric] :\n" +
            "                  Add route to routing table after connection\n" +
            "                  is established.  Multiple routes can be specified.\n" +
            "                  netmask default: 255.255.255.255\n" +
            "                  gateway default: taken from --route-gateway or --ifconfig\n" +
            "                  Specify default by leaving blank or setting to \"nil\".\n" +
            "--max-routes n :  Specify the maximum number of routes that may be defined\n" +
            "                  or pulled from a server.\n" +
            "--route-gateway gw|'dhcp' : Specify a default gateway for use with --route.\n" +
            "--route-metric m : Specify a default metric for use with --route.\n" +
            "--route-delay n [w] : Delay n seconds after connection initiation before\n" +
            "                  adding routes (may be 0).  If not specified, routes will\n" +
            "                  be added immediately after tun/tap open.  On Windows, wait\n" +
            "                  up to w seconds for TUN/TAP adapter to come up.\n" +
            "--route-up cmd  : Execute shell cmd after routes are added.\n" +
            "--route-noexec  : Don't add routes automatically.  Instead pass routes to\n" +
            "                  --route-up script using environmental variables.\n" +
            "--route-nopull  : When used with --client or --pull, accept options pushed\n" +
            "                  by server EXCEPT for routes.\n" +
            "--allow-pull-fqdn : Allow client to pull DNS names from server for\n" +
            "                    --ifconfig, --route, and --route-gateway.\n" +
            "--redirect-gateway [flags]: Automatically execute routing\n" +
            "                  commands to redirect all outgoing IP traffic through the\n" +
            "                  VPN.  Add 'local' flag if both OpenVPN servers are directly\n" +
            "                  connected via a common subnet, such as with WiFi.\n" +
            "                  Add 'def1' flag to set default route using using 0.0.0.0/1\n" +
            "                  and 128.0.0.0/1 rather than 0.0.0.0/0.  Add 'bypass-dhcp'\n" +
            "                  flag to add a direct route to DHCP server, bypassing tunnel.\n" +
            "                  Add 'bypass-dns' flag to similarly bypass tunnel for DNS.\n" +
            "--redirect-private [flags]: Like --redirect-gateway, but omit actually changing\n" +
            "                  the default gateway.  Useful when pushing private subnets.\n" +
            "--setenv name value : Set a custom environmental variable to pass to script.\n" +
            "--setenv FORWARD_COMPATIBLE 1 : Relax config file syntax checking to allow\n" +
            "                  directives for future OpenVPN versions to be ignored.\n" +
            "--script-security level mode : mode='execve' (default) or 'system', level=\n" +
            "                  0 -- strictly no calling of external programs\n" +
            "                  1 -- (default) only call built-ins such as ifconfig\n" +
            "                  2 -- allow calling of built-ins and scripts\n" +
            "                  3 -- allow password to be passed to scripts via env\n" +
            "--shaper n      : Restrict output to peer to n bytes per second.\n" +
            "--keepalive n m : Helper option for setting timeouts in server mode.  Send\n" +
            "                  ping once every n seconds, restart if ping not received\n" +
            "                  for m seconds.\n" +
            "--inactive n [bytes] : Exit after n seconds of activity on tun/tap device\n" +
            "                  produces a combined in/out byte count < bytes.\n" +
            "--ping-exit n   : Exit if n seconds pass without reception of remote ping.\n" +
            "--ping-restart n: Restart if n seconds pass without reception of remote ping.\n" +
            "--ping-timer-rem: Run the --ping-exit/--ping-restart timer only if we have a\n" +
            "                  remote address.\n" +
            "--ping n        : Ping remote once every n seconds over TCP/UDP port.\n" +
            "--multihome     : Configure a multi-homed UDP server.\n" +
            "--fast-io       : (experimental) Optimize TUN/TAP/UDP writes.\n" +
            "--remap-usr1 s  : On SIGUSR1 signals, remap signal (s='SIGHUP' or 'SIGTERM').\n" +
            "--persist-tun   : Keep tun/tap device open across SIGUSR1 or --ping-restart.\n" +
            "--persist-remote-ip : Keep remote IP address across SIGUSR1 or --ping-restart.\n" +
            "--persist-local-ip  : Keep local IP address across SIGUSR1 or --ping-restart.\n" +
            "--persist-key   : Don't re-read key files across SIGUSR1 or --ping-restart.\n" +
            "--tun-mtu n     : Take the tun/tap device MTU to be n and derive the\n" +
            "                  TCP/UDP MTU from it (default=1500).\n" +
            "--tun-mtu-extra n : Assume that tun/tap device might return as many\n" +
            "                  as n bytes more than the tun-mtu size on read\n" +
            "                  (default TUN=0 TAP=32).\n" +
            "--link-mtu n    : Take the TCP/UDP device MTU to be n and derive the tun MTU\n" +
            "                  from it.\n" +
            "--mtu-disc type : Should we do Path MTU discovery on TCP/UDP channel?\n" +
            "                  'no'    -- Never send DF (Don't Fragment) frames\n" +
            "                  'maybe' -- Use per-route hints\n" +
            "                  'yes'   -- Always DF (Don't Fragment)\n" +
            "--mtu-test      : Empirically measure and report MTU.\n" +
            "--fragment max  : Enable internal datagram fragmentation so that no UDP\n" +
            "                  datagrams are sent which are larger than max bytes.\n" +
            "                  Adds 4 bytes of overhead per datagram.\n" +
            "--mssfix [n]    : Set upper bound on TCP MSS, default = tun-mtu size\n" +
            "                  or --fragment max value, whichever is lower.\n" +
            "--sndbuf size   : Set the TCP/UDP send buffer size.\n" +
            "--rcvbuf size   : Set the TCP/UDP receive buffer size.\n" +
            "--txqueuelen n  : Set the tun/tap TX queue length to n (Linux only).\n" +
            "--mlock         : Disable Paging -- ensures key material and tunnel\n" +
            "                  data will never be written to disk.\n" +
            "--up cmd        : Shell cmd to execute after successful tun device open.\n" +
            "                  Execute as: cmd tun/tap-dev tun-mtu link-mtu \\\n" +
            "                              ifconfig-local-ip ifconfig-remote-ip\n" +
            "                  (pre --user or --group UID/GID change)\n" +
            "--up-delay      : Delay tun/tap open and possible --up script execution\n" +
            "                  until after TCP/UDP connection establishment with peer.\n" +
            "--down cmd      : Shell cmd to run after tun device close.\n" +
            "                  (post --user/--group UID/GID change and/or --chroot)\n" +
            "                  (script parameters are same as --up option)\n" +
            "--down-pre      : Call --down cmd/script before TUN/TAP close.\n" +
            "--up-restart    : Run up/down scripts for all restarts including those\n" +
            "                  caused by --ping-restart or SIGUSR1\n" +
            "--user user     : Set UID to user after initialization.\n" +
            "--group group   : Set GID to group after initialization.\n" +
            "--chroot dir    : Chroot to this directory after initialization.\n" +
            "--cd dir        : Change to this directory before initialization.\n" +
            "--daemon [name] : Become a daemon after initialization.\n" +
            "                  The optional 'name' parameter will be passed\n" +
            "                  as the program name to the system logger.\n" +
            "--syslog [name] : Output to syslog, but do not become a daemon.\n" +
            "                  See --daemon above for a description of the 'name' parm.\n" +
            "--inetd [name] ['wait'|'nowait'] : Run as an inetd or xinetd server.\n" +
            "                  See --daemon above for a description of the 'name' parm.\n" +
            "--log file      : Output log to file which is created/truncated on open.\n" +
            "--log-append file : Append log to file, or create file if nonexistent.\n" +
            "--suppress-timestamps : Don't log timestamps to stdout/stderr.\n" +
            "--writepid file : Write main process ID to file.\n" +
            "--nice n        : Change process priority (>0 = lower, <0 = higher).\n" +
            "--echo [parms ...] : Echo parameters to log output.\n" +
            "--verb n        : Set output verbosity to n (default=1):\n" +
            "                  (Level 3 is recommended if you want a good summary\n" +
            "                  of what's happening without being swamped by output).\n" +
            "                : 0 -- no output except fatal errors\n" +
            "                : 1 -- startup info + connection initiated messages +\n" +
            "                       non-fatal encryption & net errors\n" +
            "                : 2,3 -- show TLS negotiations & route info\n" +
            "                : 4 -- show parameters\n" +
            "                : 5 -- show 'RrWw' chars on console for each packet sent\n" +
            "                       and received from TCP/UDP (caps) or tun/tap (lc)\n" +
            "                : 6 to 11 -- debug messages of increasing verbosity\n" +
            "--mute n        : Log at most n consecutive messages in the same category.\n" +
            "--status file n : Write operational status to file every n seconds.\n" +
            "--status-version [n] : Choose the status file format version number.\n" +
            "                  Currently, n can be 1, 2, or 3 (default=1).\n" +
            "--disable-occ   : Disable options consistency check between peers.\n" +
            "--gremlin mask  : Special stress testing mode (for debugging only).\n" +
            "--comp-lzo      : Use fast LZO compression -- may add up to 1 byte per\n" +
            "                  packet for uncompressible data.\n" +
            "--comp-noadapt  : Don't use adaptive compression when --comp-lzo\n" +
            "                  is specified.\n" +
            "--management ip port [pass] : Enable a TCP server on ip:port to handle\n" +
            "                  management functions.  pass is a password file\n" +
            "                  or 'stdin' to prompt from console.\n" +
            "                  To listen on a unix domain socket, specific the pathname\n" +
            "                  in place of ip and use 'unix' as the port number.\n" +
            "--management-client : Management interface will connect as a TCP client to\n" +
            "                      ip/port rather than listen as a TCP server.\n" +
            "--management-query-passwords : Query management channel for private key\n" +
            "                  and auth-user-pass passwords.\n" +
            "--management-hold : Start OpenVPN in a hibernating state, until a client\n" +
            "                    of the management interface explicitly starts it.\n" +
            "--management-signal : Issue SIGUSR1 when management disconnect event occurs.\n" +
            "--management-forget-disconnect : Forget passwords when management disconnect\n" +
            "                                 event occurs.\n" +
            "--management-log-cache n : Cache n lines of log file history for usage\n" +
            "                  by the management channel.\n" +
            "--management-client-user u  : When management interface is a unix socket, only\n" +
            "                              allow connections from user u.\n" +
            "--management-client-group g : When management interface is a unix socket, only\n" +
            "                              allow connections from group g.\n" +
            "--management-client-auth : gives management interface client the responsibility\n" +
            "                           to authenticate clients after their client certificate\n" +
            "\t\t\t      has been verified.\n" +
            "--management-client-pf : management interface clients must specify a packet\n" +
            "                         filter file for each connecting client.\n" +
            "--plugin m [str]: Load plug-in module m passing str as an argument\n" +
            "                  to its initialization function.\n" +
            "\n" +
            "Multi-Client Server options (when --mode server is used):\n" +
            "--server network netmask : Helper option to easily configure server mode.\n" +
            "--server-bridge [IP netmask pool-start-IP pool-end-IP] : Helper option to\n" +
            "                    easily configure ethernet bridging server mode.\n" +
            "--push \"option\" : Push a config file option back to the peer for remote\n" +
            "                  execution.  Peer must specify --pull in its config file.\n" +
            "--push-reset    : Don't inherit global push list for specific\n" +
            "                  client instance.\n" +
            "--ifconfig-pool start-IP end-IP [netmask] : Set aside a pool of subnets\n" +
            "                  to be dynamically allocated to connecting clients.\n" +
            "--ifconfig-pool-linear : Use individual addresses rather than /30 subnets\n" +
            "                  in tun mode.  Not compatible with Windows clients.\n" +
            "--ifconfig-pool-persist file [seconds] : Persist/unpersist ifconfig-pool\n" +
            "                  data to file, at seconds intervals (default=600).\n" +
            "                  If seconds=0, file will be treated as read-only.\n" +
            "--ifconfig-push local remote-netmask : Push an ifconfig option to remote,\n" +
            "                  overrides --ifconfig-pool dynamic allocation.\n" +
            "                  Only valid in a client-specific config file.\n" +
            "--iroute network [netmask] : Route subnet to client.\n" +
            "                  Sets up internal routes only.\n" +
            "                  Only valid in a client-specific config file.\n" +
            "--disable       : Client is disabled.\n" +
            "                  Only valid in a client-specific config file.\n" +
            "--client-cert-not-required : Don't require client certificate, client\n" +
            "                  will authenticate using username/password.\n" +
            "--username-as-common-name  : For auth-user-pass authentication, use\n" +
            "                  the authenticated username as the common name,\n" +
            "                  rather than the common name from the client cert.\n" +
            "--auth-user-pass-verify cmd method: Query client for username/password and\n" +
            "                  run script cmd to verify.  If method='via-env', pass\n" +
            "                  user/pass via environment, if method='via-file', pass\n" +
            "                  user/pass via temporary file.\n" +
            "--opt-verify    : Clients that connect with options that are incompatible\n" +
            "                  with those of the server will be disconnected.\n" +
            "--auth-user-pass-optional : Allow connections by clients that don't\n" +
            "                  specify a username/password.\n" +
            "--no-name-remapping : Allow Common Name and X509 Subject to include\n" +
            "                      any printable character.\n" +
            "--client-to-client : Internally route client-to-client traffic.\n" +
            "--duplicate-cn  : Allow multiple clients with the same common name to\n" +
            "                  concurrently connect.\n" +
            "--client-connect cmd : Run script cmd on client connection.\n" +
            "--client-disconnect cmd : Run script cmd on client disconnection.\n" +
            "--client-config-dir dir : Directory for custom client config files.\n" +
            "--ccd-exclusive : Refuse connection unless custom client config is found.\n" +
            "--tmp-dir dir   : Temporary directory, used for --client-connect return file.\n" +
            "--hash-size r v : Set the size of the real address hash table to r and the\n" +
            "                  virtual address table to v.\n" +
            "--bcast-buffers n : Allocate n broadcast buffers.\n" +
            "--tcp-queue-limit n : Maximum number of queued TCP output packets.\n" +
            "--tcp-nodelay   : Macro that sets TCP_NODELAY socket flag on the server\n" +
            "                  as well as pushes it to connecting clients.\n" +
            "--learn-address cmd : Run script cmd to validate client virtual addresses.\n" +
            "--connect-freq n s : Allow a maximum of n new connections per s seconds.\n" +
            "--max-clients n : Allow a maximum of n simultaneously connected clients.\n" +
            "--max-routes-per-client n : Allow a maximum of n internal routes per client.\n" +
            "--port-share host port : When run in TCP mode, proxy incoming HTTPS sessions\n" +
            "                  to a web server at host:port.\n" +
            "\n" +
            "Client options (when connecting to a multi-client server):\n" +
            "--client         : Helper option to easily configure client mode.\n" +
            "--auth-user-pass [up] : Authenticate with server using username/password.\n" +
            "                  up is a file containing username/password on 2 lines,\n" +
            "                  or omit to prompt from console.\n" +
            "--pull           : Accept certain config file options from the peer as if they\n" +
            "                  were part of the local config file.  Must be specified\n" +
            "                  when connecting to a '--mode server' remote host.\n" +
            "--auth-retry t  : How to handle auth failures.  Set t to\n" +
            "                  none (default), interact, or nointeract.\n" +
            "--server-poll-timeout n : when polling possible remote servers to connect to\n" +
            "                  in a round-robin fashion, spend no more than n seconds\n" +
            "                  waiting for a response before trying the next server.\n" +
            "--explicit-exit-notify [n] : On exit/restart, send exit signal to\n" +
            "                  server/remote. n = # of retries, default=1.\n" +
            "\n" +
            "Data Channel Encryption Options (must be compatible between peers):\n" +
            "(These options are meaningful for both Static Key & TLS-mode)\n" +
            "--secret f [d]  : Enable Static Key encryption mode (non-TLS).\n" +
            "                  Use shared secret file f, generate with --genkey.\n" +
            "                  The optional d parameter controls key directionality.\n" +
            "                  If d is specified, use separate keys for each\n" +
            "                  direction, set d=0 on one side of the connection,\n" +
            "                  and d=1 on the other side.\n" +
            "--auth alg      : Authenticate packets with HMAC using message\n" +
            "                  digest algorithm alg (default=SHA1).\n" +
            "                  (usually adds 16 or 20 bytes per packet)\n" +
            "                  Set alg=none to disable authentication.\n" +
            "--cipher alg    : Encrypt packets with cipher algorithm alg\n" +
            "                  (default=BF-CBC).\n" +
            "                  Set alg=none to disable encryption.\n" +
            "--prng alg [nsl] : For PRNG, use digest algorithm alg, and\n" +
            "                   nonce_secret_len=nsl.  Set alg=none to disable PRNG.\n" +
            "--keysize n     : Size of cipher key in bits (optional).\n" +
            "                  If unspecified, defaults to cipher-specific default.\n" +
            "--engine [name] : Enable OpenSSL hardware crypto engine functionality.\n" +
            "--no-replay     : Disable replay protection.\n" +
            "--mute-replay-warnings : Silence the output of replay warnings to log file.\n" +
            "--replay-window n [t]  : Use a replay protection sliding window of size n\n" +
            "                         and a time window of t seconds.\n" +
            "                         Default n=64 t=15\n" +
            "--no-iv         : Disable cipher IV -- only allowed with CBC mode ciphers.\n" +
            "--replay-persist file : Persist replay-protection state across sessions\n" +
            "                  using file.\n" +
            "--test-crypto   : Run a self-test of crypto features enabled.\n" +
            "                  For debugging only.\n" +
            "\n" +
            "TLS Key Negotiation Options:\n" +
            "(These options are meaningful only for TLS-mode)\n" +
            "--tls-server    : Enable TLS and assume server role during TLS handshake.\n" +
            "--tls-client    : Enable TLS and assume client role during TLS handshake.\n" +
            "--key-method m  : Data channel key exchange method.  m should be a method\n" +
            "                  number, such as 1 (default), 2, etc.\n" +
            "--ca file       : Certificate authority file in .pem format containing\n" +
            "                  root certificate.\n" +
            "--capath dir    : A directory of trusted certificates (CAs and CRLs).\n" +
            "--dh file       : File containing Diffie Hellman parameters\n" +
            "                  in .pem format (for --tls-server only).\n" +
            "                  Use \"openssl dhparam -out dh1024.pem 1024\" to generate.\n" +
            "--cert file     : Local certificate in .pem format -- must be signed\n" +
            "                  by a Certificate Authority in --ca file.\n" +
            "--key file      : Local private key in .pem format.\n" +
            "--pkcs12 file   : PKCS#12 file containing local private key, local certificate\n" +
            "                  and optionally the root CA certificate.\n" +
            "--tls-cipher l  : A list l of allowable TLS ciphers separated by : (optional).\n" +
            "                : Use --show-tls to see a list of supported TLS ciphers.\n" +
            "--tls-timeout n : Packet retransmit timeout on TLS control channel\n" +
            "                  if no ACK from remote within n seconds (default=2).\n" +
            "--reneg-bytes n : Renegotiate data chan. key after n bytes sent and recvd.\n" +
            "--reneg-pkts n  : Renegotiate data chan. key after n packets sent and recvd.\n" +
            "--reneg-sec n   : Renegotiate data chan. key after n seconds (default=3600).\n" +
            "--hand-window n : Data channel key exchange must finalize within n seconds\n" +
            "                  of handshake initiation by any peer (default=60).\n" +
            "--tran-window n : Transition window -- old key can live this many seconds\n" +
            "                  after new key renegotiation begins (default=3600).\n" +
            "--single-session: Allow only one session (reset state on restart).\n" +
            "--tls-exit      : Exit on TLS negotiation failure.\n" +
            "--tls-auth f [d]: Add an additional layer of authentication on top of the TLS\n" +
            "                  control channel to protect against DoS attacks.\n" +
            "                  f (required) is a shared-secret passphrase file.\n" +
            "                  The optional d parameter controls key directionality,\n" +
            "                  see --secret option for more info.\n" +
            "--askpass [file]: Get PEM password from controlling tty before we daemonize.\n" +
            "--auth-nocache  : Don't cache --askpass or --auth-user-pass passwords.\n" +
            "--crl-verify crl: Check peer certificate against a CRL.\n" +
            "--tls-verify cmd: Execute shell command cmd to verify the X509 name of a\n" +
            "                  pending TLS connection that has otherwise passed all other\n" +
            "                  tests of certification.  cmd should return 0 to allow\n" +
            "                  TLS handshake to proceed, or 1 to fail.  (cmd is\n" +
            "                  executed as 'cmd certificate_depth X509_NAME_oneline')\n" +
            "--tls-remote x509name: Accept connections only from a host with X509 name\n" +
            "                  x509name. The remote host must also pass all other tests\n" +
            "                  of verification.\n" +
            "--ns-cert-type t: Require that peer certificate was signed with an explicit\n" +
            "                  nsCertType designation t = 'client' | 'server'.\n" +
            "--remote-cert-ku v ... : Require that the peer certificate was signed with\n" +
            "                  explicit key usage, you can specify more than one value.\n" +
            "                  value should be given in hex format.\n" +
            "--remote-cert-eku oid : Require that the peer certificate was signed with\n" +
            "                  explicit extended key usage. Extended key usage can be encoded\n" +
            "                  as an object identifier or OpenSSL string representation.\n" +
            "--remote-cert-tls t: Require that peer certificate was signed with explicit\n" +
            "                  key usage and extended key usage based on RFC3280 TLS rules.\n" +
            "                  t = 'client' | 'server'.\n" +
            "\n" +
            "SSL Library information:\n" +
            "--show-ciphers  : Show cipher algorithms to use with --cipher option.\n" +
            "--show-digests  : Show message digest algorithms to use with --auth option.\n" +
            "--show-engines  : Show hardware crypto accelerator engines (if available).\n" +
            "--show-tls      : Show all TLS ciphers (TLS used only as a control channel).\n" +
            "\n" +
            "Generate a random key (only for non-TLS static key encryption mode):\n" +
            "--genkey        : Generate a random key to be used as a shared secret,\n" +
            "                  for use with the --secret option.\n" +
            "--secret file   : Write key to file.\n" +
            "\n" +
            "Tun/tap config mode (available with linux 2.4+):\n" +
            "--mktun         : Create a persistent tunnel.\n" +
            "--rmtun         : Remove a persistent tunnel.\n" +
            "--dev tunX|tapX : tun/tap device\n" +
            "--dev-type dt   : Device type.  See tunnel options above for details.\n" +
            "--user user     : User to set privilege to.\n" +
            "--group group   : Group to set privilege to.\n")
            .split( "\n" )
    ) );
}
