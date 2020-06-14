//
// Created by kevin on 19-8-20.
//

#ifndef MYAPPLICATION2_LOG_H
#define MYAPPLICATION2_LOG_H
#include <android/log.h>
#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"jxc",FORMAT,##__VA_ARGS__);

#endif //MYAPPLICATION2_LOG_H
