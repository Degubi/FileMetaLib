#include "shobjidl_core.h"
#include "propkey.h"
#include "propvarutil.h"
#include "jni.h"
// #include "comdef.h"

static PROPERTYKEY getPropertyKey(jint propOrdinal) {
    switch(propOrdinal) {
        case 0: return PKEY_Author;
        case 1: return PKEY_Comment;
        case 2: return PKEY_Copyright;
        case 3: return PKEY_Keywords;
        case 4: return PKEY_Language;
        case 5: return PKEY_Media_SubTitle;
        case 6: return PKEY_Media_Year;
        case 7: return PKEY_Title;
    }
}

static IPropertyStore* createStoreForFile(jstring filePath, JNIEnv* env) {
    IPropertyStore* store;
    const jchar* convertedFilePath = env->GetStringChars(filePath, JNI_FALSE);
    HRESULT res = SHGetPropertyStoreFromParsingName((PCWSTR) convertedFilePath, NULL, GPS_READWRITE, __uuidof(IPropertyStore), (void**)&store);

    env->ReleaseStringChars(filePath, convertedFilePath);

    /*if(!SUCCEEDED(res)) {
        _com_error err = _com_error(res);
        LPCTSTR errMsg = err.ErrorMessage();
    }*/

    return store;
}

static PROPVARIANT getPropertyValue(jstring filePath, jint propertyKey, JNIEnv* env) {
    IPropertyStore* store = createStoreForFile(filePath, env);
    PROPVARIANT variant;

    store->GetValue(getPropertyKey(propertyKey), &variant);
    store->Release();

    return variant;
}

static void writePropertyValue(jstring filePath, jint propertyKey, PROPVARIANT* prop, JNIEnv* env) {
    IPropertyStore* store = createStoreForFile(filePath, env);
    store->SetValue(getPropertyKey(propertyKey), *prop);
    store->Commit();
    store->Release();
}


extern "C" JNIEXPORT void JNICALL Java_mediaprops_MediaPropertyUtils_init(JNIEnv* env, jclass clazz) {
    CoInitialize(NULL);
}

extern "C" JNIEXPORT jboolean JNICALL Java_mediaprops_MediaPropertyUtils_hasMediaProperty(JNIEnv* env, jclass clazz, jstring filePath, jint propertyKey) {
    return getPropertyValue(filePath, propertyKey, env).vt == VT_EMPTY ? JNI_FALSE : JNI_TRUE;
}

extern "C" JNIEXPORT jstring JNICALL Java_mediaprops_MediaPropertyUtils_readStringProperty(JNIEnv* env, jclass clazz, jstring filePath, jint propertyKey) {
    PROPVARIANT variant = getPropertyValue(filePath, propertyKey, env);

    return variant.vt == VT_EMPTY ? nullptr : env->NewString((jchar*) variant.pwszVal, wcslen(variant.pwszVal));
}

extern "C" JNIEXPORT jint JNICALL Java_mediaprops_MediaPropertyUtils_readIntProperty(JNIEnv* env, jclass clazz, jstring filePath, jint propertyKey) {
    PROPVARIANT variant = getPropertyValue(filePath, propertyKey, env);

    return variant.vt == VT_EMPTY ? -1 : variant.intVal;
}

extern "C" JNIEXPORT void JNICALL Java_mediaprops_MediaPropertyUtils_writeStringProperty(JNIEnv* env, jclass clazz, jstring filePath, jint propertyKey, jstring propertyValue) {
    PROPVARIANT prop;
    const jchar* convertedValue = env->GetStringChars(propertyValue, JNI_FALSE);

    InitPropVariantFromString((PCWSTR) convertedValue, &prop);
    env->ReleaseStringChars(propertyValue, convertedValue);

    writePropertyValue(filePath, propertyKey, &prop, env);
}

extern "C" JNIEXPORT void JNICALL Java_mediaprops_MediaPropertyUtils_writeIntProperty(JNIEnv* env, jclass clazz, jstring filePath, jint propertyKey, jint propertyValue) {
    PROPVARIANT prop;
    InitPropVariantFromInt32(propertyValue, &prop);

    writePropertyValue(filePath, propertyKey, &prop, env);
}