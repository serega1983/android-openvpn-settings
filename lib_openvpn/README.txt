

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


Add binaries to APK:
====================
As the binaries are no shared libraries they are included
in the APK as raw resources. Later on the device these raw
resources are extraced to the APP local file system.

cp libs/armeabi/busybox res/raw/busybox
cp libs/armeabi/openvpn-static res/raw/openvpn

hg commit -m 'Update busybox and openvpn binaries' res/raw/busybox res/raw/openvpn


Links and Resources:
- Read more about ARM ABI versions on android and how to build binaries 
  http://stackoverflow.com/questions/5089783/producing-optimised-ndk-code-for-multiple-architectures

