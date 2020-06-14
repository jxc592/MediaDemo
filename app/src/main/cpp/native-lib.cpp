#include <jni.h>
#include <string>

#include <android/native_window_jni.h>
#include <unistd.h>
#include <aaudio/AAudio.h>
#include "log.h"
#include "ffmpeg.h"




extern "C" JNIEXPORT jstring JNICALL
Java_com_example_myapplication_util_NativeLib_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "click me to play frames";

    return env->NewStringUTF(hello.c_str());
}



extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_myapplication_util_NativeLib_parserAlbumArt(JNIEnv *env, jobject instance,jstring _path){
    uint8_t* data;
    int length = 0;

    getAlbumArt(env->GetStringUTFChars(_path,0),&data,&length);

    if(length <= 0) {
        LOGE("jxc parserAlbumArt error,no albumart");
        return NULL;
    }
    LOGE("jxc parserAlbumArt size :%d data length %d" ,length,data[0]);
    jbyteArray c_result = env->NewByteArray(length);
    jbyte buf[length];
    for(int i=0; i<length; i++){
        buf[i] = data[i];
    }

    //4. 赋值
    env->SetByteArrayRegion(c_result, 0, length, buf);

    return c_result;

};


