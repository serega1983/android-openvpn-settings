
if [ -z "$ANDROID_NDK_R3" ] ; then
	echo '$ANDROID_NDK_R3' must point to an android-ndk-r3 installation
	echo 'Get it at: http://dl.google.com/android/ndk/android-ndk-r3-linux-x86.zip'
	exit -1
fi

cd vanilla
make mrproper
cd ..

egrep -v 'CONFIG_CROSS_COMPILER_PREFIX|CONFIG_SYSROOT|CONFIG_EXTRA_CFLAGS' config > vanilla/.config

# when building with android-ndk-r3
export PATH="$PATH:$ANDROID_NDK_R3/build/prebuilt/linux-x86/arm-eabi-4.4.0/bin"
cat - >> vanilla/.config << EOF 
CONFIG_CROSS_COMPILER_PREFIX="arm-eabi-"
CONFIG_SYSROOT="$ANDROID_NDK_R3/build/platforms/android-3/arch-arm"
CONFIG_EXTRA_CFLAGS="-DANDROID -D__ANDROID__ -DSK_RELEASE -nostdlib -march=armv5te -msoft-float -mfloat-abi=softfp  -fpic -fno-short-enums -fgcse-after-reload -frename-registers"
EOF

## when building with android-ndk-r8b
#export PATH="$PATH:$ANDROID_NDK/toolchains/arm-linux-androideabi-4.4.3/prebuilt/linux-x86/bin/"
#cat - >> vanilla/.config << EOF 
#CONFIG_CROSS_COMPILER_PREFIX="arm-linux-androideabi-"
#CONFIG_SYSROOT="$ANDROID_NDK/platforms/android-9/arch-arm"
#CONFIG_EXTRA_CFLAGS="-DANDROID -D__ANDROID__ -DSK_RELEASE -nostdlib -march=armv7-a -msoft-float -mfloat-abi=softfp -mfpu=neon -mthumb -mthumb-interwork -fpic -fno-short-enums -fgcse-after-reload -frename-registers"
#EOF

cd vanilla
make

[ -d ../../libs/armeabi ] || mkdir -p ../../libs/armeabi
cp busybox ../../libs/armeabi

echo Resulting busybo should be at
ls -l ../../libs/armeabi/busybox

