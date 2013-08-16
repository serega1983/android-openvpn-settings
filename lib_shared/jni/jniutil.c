#include <stdlib.h>
#include <errno.h>
#include <android/log.h>
#include "de_schaeuffelhut_android_openvpn_shared_util_JniUtil.h"

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    __android_log_write(ANDROID_LOG_DEBUG,"openvpn", "Loading jniutil native library, compiled on " __DATE__ " " __TIME__ );
    return JNI_VERSION_1_2;
}

void JNI_OnUnload(JavaVM *vm, void *reserved) {
    __android_log_write(ANDROID_LOG_DEBUG,"openvpn", "Unloading jniutil native library, compiled on " __DATE__ " " __TIME__ );
}

/* Copied from AOSP dalvik/libcore/luni/src/main/native/org_apache_harmony_luni_platform_OSFileSystem.cpp
 * function harmony_io_closeImpl()
 */
JNIEXPORT jint JNICALL Java_de_schaeuffelhut_android_openvpn_shared_util_JniUtil_closeImpl
  (JNIEnv *env, jclass jniutil, jint fd)
{
    jint result;

    for (;;) {
        result = (jint) close(fd);

        if ((result != -1) || (errno != EINTR)) {
            break;
        }

        /*
         * If we didn't break above, that means that the close() call
         * returned due to EINTR. We shield Java code from this
         * possibility by trying again.
         */
    }

    return result;
}
