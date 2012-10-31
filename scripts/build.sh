#!/bin/bash

# build main application
ant clean
ant debug || exit 1

# build unit tests
cd test
ant clean
ant debug || exit 1

# start emulator, run tests, kill emulator
export ANDROID_SERIAL=emulator-5582

echo Starting emulator
$ANDROID_SDK_HOME/tools/emulator-arm -avd android-1.6-normal -netspeed full -netdelay none -no-window -port 5582 &
echo Waiting for emulator
$ANDROID_SDK_HOME/platform-tools/adb wait-for-device

ant uninstall
ant debug install test

$ANDROID_SDK_HOME/platform-tools/adb emu kill

