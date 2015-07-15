#OpenVPN Settings FAQ

# FAQ #

Q: How do I get the OpenVPN configuration file on the device?

A: Use the USB cable and mount the phone like a USB Stick, then copy your config file into the /openvpn directory.


&lt;hr/&gt;



Q: Is there a context menu per configuration entry?

A: Yes, just touch the configuration entry a little longer.


&lt;hr/&gt;



Q: How do I enable the log file?

A: Tap configuration entry long to open the context menu, then choose 'Preferences'. In the following screen you can enable logging.


&lt;hr/&gt;



Q: Where is the OpenVPN log file?

A: It will be stored on the sdcard in the /openvpn directory under the name of the configuration with the extension changed to .log.


&lt;hr/&gt;



Q: Where do I find a TUN module for my phone.

A: In Android 4 and higher the TUN module comes with the device. Older android versions require you to get a custom TUN module. You can try the TUN Installer from the market. If it doesn't have a module for your device you need to find one online.


&lt;hr/&gt;



Q: Can I use a TAP device?

A: Yes you can, but it might send unnecessary broadcast traffic to your phone and drain the bettary quicker than a TUN device.


&lt;hr/&gt;



Q: Why do I need busybox?

A: Android comes with its own implementaion of ifconfig and route which are incompatible with openvpn. Busybox ifconfig and route work just fine.


&lt;hr/&gt;



Q: The tunnel comes up but no IP address is set or routes are missing.

A: Most likely openvpn uses the wrong ifconfig or route command. See '
Why do I need busybox?'


&lt;hr/&gt;

