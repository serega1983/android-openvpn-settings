

This library provides sources and pre compiled binaries for
	- openvpn
	- busybox with applets: ip

NATIVE BINARY BUILD HOWTO:
==========================

To build the sources the android-ndk is necessary.
This project was set up using android-ndk-r8b.
The ndk path should be stored in an env variable:
export ANDROID_NDK=/path/to/ndk

Also add the ndk to $PATH:
export PATH=$PATH:$ANDROID_NDK

Compile openvpn using the command:
	ndk-build

The output will be in libs/armeabi/openvpn-static

Compile busybox:
	cd busybox
	 ./build-busybox.sh

The output will be in libs/armeabi/busybox

