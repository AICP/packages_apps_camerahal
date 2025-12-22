#include <jni.h>
#include <camera/NdkCameraManager.h>

extern "C"
JNIEXPORT jint JNICALL
Java_com_lineageos_camerahalcheck_NativeCameraProbe_probeCameraServiceNative(JNIEnv*, jobject) {
    ACameraManager* mgr = ACameraManager_create();
    if (!mgr) return -1;
    ACameraIdList* list = nullptr;
    camera_status_t s = ACameraManager_getCameraIdList(mgr, &list);
    int r = (s == ACAMERA_OK && list) ? list->numCameras : -1;
    if (list) ACameraManager_deleteCameraIdList(list);
    ACameraManager_delete(mgr);
    return r;
}
