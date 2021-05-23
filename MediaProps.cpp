#include "shobjidl_core.h"
#include "propkey.h"
#include "propvarutil.h"
#include "jni.h"
#include "comdef.h"

const int NULL_INT_VALUE = -1;  // This is fine because all of the number media property types are VT_UI4, which is unsigned

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

    if(!SUCCEEDED(res)) {
        jclass unchckdExClass = env->FindClass("java/io/UncheckedIOException");
        jclass fileSysExClass = env->FindClass("java/nio/file/FileSystemException");

        jmethodID unchckdExClassConstructor = env->GetMethodID(unchckdExClass, "<init>", "(Ljava/io/IOException;)V");
        jmethodID fileSysExConstructor = env->GetMethodID(fileSysExClass, "<init>", "(Ljava/lang/String;)V");

        jstring errorMsg = env->NewStringUTF((char*) _com_error(res).ErrorMessage());
        jthrowable fileSysException = (jthrowable) env->NewObject(fileSysExClass, fileSysExConstructor, errorMsg);
        jthrowable unchckdException = (jthrowable) env->NewObject(unchckdExClass, unchckdExClassConstructor, fileSysException);

        env->Throw(unchckdException);

        return nullptr;
    }

    return store;
}

static PROPVARIANT getPropertyValue(jstring filePath, jint propertyKey, JNIEnv* env) {
    IPropertyStore* store = createStoreForFile(filePath, env);
    PROPVARIANT prop;

    if(store != nullptr) {
        store->GetValue(getPropertyKey(propertyKey), &prop);
        store->Release();
    }else{
        prop.vt = VT_EMPTY;
    }

    return prop;
}

static void writePropertyValue(jstring filePath, jint propertyKey, PROPVARIANT* prop, JNIEnv* env) {
    IPropertyStore* store = createStoreForFile(filePath, env);

    if(store != nullptr) {
        store->SetValue(getPropertyKey(propertyKey), *prop);
        store->Commit();
        store->Release();
    }
}


extern "C" JNIEXPORT void JNICALL Java_mediaprops_MediaPropertyUtils_init(JNIEnv* env, jclass clazz) {
    CoInitialize(NULL);
}

extern "C" JNIEXPORT jboolean JNICALL Java_mediaprops_MediaPropertyUtils_hasMediaProperty(JNIEnv* env, jclass clazz, jstring filePath, jint propertyKey) {
    return getPropertyValue(filePath, propertyKey, env).vt == VT_EMPTY ? JNI_FALSE : JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL Java_mediaprops_MediaPropertyUtils_clearMediaProperty(JNIEnv* env, jclass clazz, jstring filePath, jint propertyKey) {
    PROPVARIANT prop;
    prop.vt = VT_EMPTY;

    writePropertyValue(filePath, propertyKey, &prop, env);
}

extern "C" JNIEXPORT jstring JNICALL Java_mediaprops_MediaPropertyUtils_readStringProperty(JNIEnv* env, jclass clazz, jstring filePath, jint propertyKey) {
    PROPVARIANT variant = getPropertyValue(filePath, propertyKey, env);

    return variant.vt == VT_EMPTY ? nullptr : env->NewString((jchar*) variant.pwszVal, wcslen(variant.pwszVal));
}

extern "C" JNIEXPORT jint JNICALL Java_mediaprops_MediaPropertyUtils_readIntProperty(JNIEnv* env, jclass clazz, jstring filePath, jint propertyKey) {
    PROPVARIANT variant = getPropertyValue(filePath, propertyKey, env);

    return variant.vt == VT_EMPTY ? NULL_INT_VALUE : variant.intVal;
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