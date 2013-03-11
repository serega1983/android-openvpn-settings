#!/bin/bash

#!/bin/bash

if [ ! -x $ANDROID_SDK_HOME/tools/android ]; then
	echo android too not found
	echo did you set '$ANDROID_SDK_HOME'?
	exit -1
fi



#target=android-4
#$ANDROID_SDK_HOME/tools/android update project --subprojects --target $target --path . || exit -1


# build main application
#ant clean
#ant debug || exit 1

# build unit tests
cd test
ant clean
ant debug || exit 1

# start emulator, run tests, kill emulator
export ANDROID_SERIAL=emulator-5582

echo Starting emulator
$ANDROID_SDK_HOME/tools/emulator-arm -avd android-2.2 -netspeed full -netdelay none -no-window -port 5582 &
echo Waiting for emulator
$ANDROID_SDK_HOME/platform-tools/adb wait-for-device

ant uninstall
ant debug install test

$ANDROID_SDK_HOME/platform-tools/adb emu kill

