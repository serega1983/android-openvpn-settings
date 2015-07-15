# Setting up the CA #

It always takes me longer setting up a CA using easy-rsa as I expect.
It is not hard but for some reason a lot can go wrong.
So here is a little script for Ubuntu 12.04 outlining the
necessary steps.

Just copy and paste it into your shell and it will create the CA, a server and two client certificates.

Execute the last line to create a third client certificate protected by a pass phrase.

You will find the keys and certificates in the keys directory.


```
# Create a writable copy of easy-rsa
cp -a /usr/share/doc/openvpn/examples/easy-rsa/2.0/ easy-rsa
cd easy-rsa

# Initialize the environment script
# You can leave this values blank
cat - >> vars << EOF
export KEY_COUNTRY=
export KEY_PROVINCE=
export KEY_CITY=
export KEY_ORG=
export KEY_EMAIL=
export KEY_CN=
export KEY_NAME=
export KEY_OU=
EOF

# Source the environment
. vars

# Fix a bug
ln -s openssl-1.0.0.cnf openssl.cnf

# Reset the CA
./clean-all

# Generate DH parameters needed by server
./build-dh

# Initialize CA, create one server and 3 client certificates
# Change KEY_CN and KEY_EMAIL as you whish
# KEY_CN must be unqiue within the CA
# Add the --pass option if you want to set a pass phrase on the key

KEY_CN=ca KEY_EMAIL=ca@acme ./pkitool --initca
KEY_CN=server KEY_EMAIL=server@acme ./pkitool --server server
KEY_CN=client1 KEY_EMAIL=$KEY_CN@acme ./pkitool $KEY_CN
KEY_CN=client2 KEY_EMAIL=$KEY_CN@acme ./pkitool $KEY_CN
```

Create certificate protected by pass phrase:
```
KEY_CN=client3 KEY_EMAIL=$KEY_CN@acme ./pkitool --pass $KEY_CN
```


# Server Configuration #

Copy these files from the easy-rsa/keys directory into /etc/openvpn
  * ca.crt
  * server.key
  * server.crt
  * dh1024.pem

Create the file /etc/openvpn/server.conf
```
mode server

dev tun
topology subnet

tls-server
ca   ca.crt
cert server.crt
key  server.key
dh   dh1024.pem
remote-cert-tls client


port 1194

ifconfig 10.0.0.1 255.255.255.0

client-config-dir vpnclients.ccd
```

Push additional routes to the client:

```
push "route 192.168.1.0  255.255.255.0 10.0.0.1"
```

For each client create a file named after its common name in
the subdirectory vpnclients.ccd, e.g. for client1 create a file /etc/openvpn/vpnclients.ccd/client1

```
ifconfig-push 10.0.0.2 255.255.255.0
```

Do this for each client to assign a unique IP address.

Start the server by calling
```
cd /etc/openvpn
openvpn server.conf
```

# Client Configuration #

For client1 copy these files from the easy-rsa/keys directory into a directory client1
  * ca.crt
  * client1.key, but rename it to client.key
  * client1.crt, but rename it to client.crt

For client2 and others pick the correct **.key and**.crt file.
Create a new directory for each client

Create the file client1/client.conf. Edit you servers IP address!
```

dev tun
topology subnet

tls-client
ca   ca.crt
cert client.crt
key  client.key

remote-cert-tls server

remote ***** YOUR SERVERS IP ADDRESS *****
rport 1194

pull
```

Start the client by calling
```
cd client
openvpn client.conf
```

Copy the client directory onto your phone at /sdcard/openvpn