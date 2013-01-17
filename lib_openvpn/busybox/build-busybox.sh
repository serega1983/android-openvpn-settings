
if [ -z "$ANDROID_NDK" ] ; then
	echo '$ANDROID_NDK' must point to an android NDK installation
	exit -1
fi

cp config vanilla/.config
export PATH="$PATH:$ANDROID_NDK/toolchains/arm-linux-androideabi-4.4.3/prebuilt/linux-x86/bin/"
sed -i "s|/opt/android-ndk|$ANDROID_NDK|" vanilla/.config
cd vanilla
make
[ -d ../../libs/armeabi ] || mkdir -p ../../libs/armeabi
cp busybox ../../libs/armeabi
