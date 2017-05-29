//
// Created by cmina on 2017-03-13.
//

#include <jni.h>
#include "opencv2/core/core.hpp"
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <stdio.h>
#include <sstream>
#include <string>
#include <opencv2/opencv.hpp>
#include <android/log.h>

#define LOG_TAG "Native_Lib_JNI"

#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))


int const max_lowThreshold = 100;

int ratio = 3;

int kernel_size = 3;

int lowThreshold = 10;


using namespace std;
using namespace cv;

int toGray(Mat img, Mat &gray);

extern "C" {

JNIEXPORT jint JNICALL Java_com_example_cmina_mycastcast_activity_CameraFilterActivity_convertNativeGray(JNIEnv *, jobject,
                                                                          jlong addrRgba,
                                                                          jlong addrGray);

JNIEXPORT jint JNICALL Java_com_example_cmina_mycastcast_activity_CameraFilterActivity_convertNativeGray(JNIEnv *, jobject,
                                                                          jlong addrRgba,
                                                                          jlong addrGray) {

    Mat &mRgb = *(Mat *) addrRgba;
    Mat &mGray = *(Mat *) addrGray;

    int conv;
    jint retVal;

    conv = toGray(mRgb, mGray);
    retVal = (jint) conv;

    return retVal;

}

JNIEXPORT jint JNICALL Java_com_example_cmina_mycastcast_util_CameraView_convertNativeGray(JNIEnv *, jobject,
                                                                           jlong addrRgba,
                                                                           jlong addrGray);

JNIEXPORT jint JNICALL Java_com_example_cmina_mycastcast_util_CameraView_convertNativeGray(JNIEnv *, jobject,
                                                                           jlong addrRgba,
                                                                           jlong addrGray) {

    Mat &mRgb = *(Mat *) addrRgba;
    Mat &mGray = *(Mat *) addrGray;

    int conv;
    jint retVal;

    conv = toGray(mRgb, mGray);
    retVal = (jint) conv;

    return retVal;

}





JNIEXPORT jboolean JNICALL Java_com_example_cmina_mycastcast_activity_CameraFilterActivity_nativeCanny(JNIEnv *env, jobject instance, long iAddr) {

    cv::Mat *blur = (cv::Mat *) iAddr;
    cv::Canny(*blur, *blur, 80, 100, 3);
    return true;
}

JNIEXPORT jboolean JNICALL Java_com_example_cmina_mycastcast_util_CameraView_nativeCanny(JNIEnv *env, jobject instance, long iAddr) {

    cv::Mat *blur = (cv::Mat *) iAddr;
    cv::Canny(*blur, *blur, 80, 100, 3);
    return true;
}

}


int toGray(Mat img, Mat &gray) {
    cvtColor(img, gray, CV_RGBA2GRAY); // Assuming RGBA input

    if (gray.rows == img.rows && gray.cols == img.cols) {
        return (1);
    }
    return (0);
}

