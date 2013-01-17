
BusyBox comes with a NDK build.
See: https://github.com/tias/android-busybox-ndk#readme

This is a minimal busybox build which only includes the 'ip' utility.
The 'ip' utility is used by openvpn to configure the interface.

The build needs to know where the android NDK is installed.
To build busybox call:

	export ANDROID_NDK=/opt/android-ndk-r8b
	build-busybox.sh

