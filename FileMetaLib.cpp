#include "shobjidl_core.h"
#include "propkey.h"
#include "propvarutil.h"
#include "jni.h"
// #include "comdef.h"

PROPERTYKEY getPropertyKey(jint propOrdinal) {
    switch(propOrdinal) {
        case 0: return PKEY_Title;
        case 1: return PKEY_Media_SubTitle;
        case 2: return PKEY_Comment;
        case 3: return PKEY_Author;
    }
}



extern "C" JNIEXPORT void JNICALL Java_filemetalib_FileMeta_init(JNIEnv* env, jclass clazz) {
    CoInitialize(NULL);
}

extern "C" JNIEXPORT jstring JNICALL Java_filemetalib_FileMeta_readStringProperty(JNIEnv* env, jclass clazz, jstring filePath, jint propertyKey) {
    IPropertyStore* store = NULL;
    const jchar* convertedFilePath = env->GetStringChars(filePath, JNI_FALSE);

    HRESULT res = SHGetPropertyStoreFromParsingName((PCWSTR) convertedFilePath, NULL, GPS_READWRITE, __uuidof(IPropertyStore), (void**)&store);
    env->ReleaseStringChars(filePath, convertedFilePath);

    // TODO: Propagate errors
    /*if(!res) {
        _com_error err = _com_error(res);
        LPCTSTR errMsg = err.ErrorMessage();
    }*/

    PROPVARIANT variant;
    store->GetValue(getPropertyKey(propertyKey), &variant);

    jstring value = variant.vt == VT_EMPTY ? nullptr : env->NewString((jchar*) variant.pwszVal, wcslen(variant.pwszVal));

    store->Release();
    return value;
}

extern "C" JNIEXPORT void JNICALL Java_filemetalib_FileMeta_writeStringProperty(JNIEnv* env, jclass clazz, jstring filePath, jint propertyKey, jstring propertyValue) {
    PROPVARIANT prop;
    const jchar* convertedPropertyValue = env->GetStringChars(propertyValue, JNI_FALSE);

    InitPropVariantFromString((PCWSTR) convertedPropertyValue, &prop);
    env->ReleaseStringChars(propertyValue, convertedPropertyValue);

    IPropertyStore* store;
    const jchar* convertedFilePath = env->GetStringChars(filePath, JNI_FALSE);

    HRESULT res = SHGetPropertyStoreFromParsingName((PCWSTR) convertedFilePath, NULL, GPS_READWRITE, __uuidof(IPropertyStore), (void**)&store);
    env->ReleaseStringChars(filePath, convertedFilePath);

    // TODO: Propagate errors
    /*if(!res) {
        _com_error err = _com_error(res);
        LPCTSTR errMsg = err.ErrorMessage();
    }*/

    store->SetValue(getPropertyKey(propertyKey), prop);
    store->Commit();
    store->Release();
}