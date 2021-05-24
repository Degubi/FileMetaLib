#include "shobjidl_core.h"
#include "propkey.h"
#include "propvarutil.h"
#include "jni.h"
#include "comdef.h"
#include "string"

static const int NULL_INT_VALUE = -1;                // This is fine because all of the number media property types are VT_UI4, which is unsigned
static const int NON_MEDIA_FILE_ERROR = -2147467259; // Means 'unknown', "usually" happens when the file is not a media file

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

static HRESULT createUncheckedStoreForFile(jstring filePath, JNIEnv* env, void** store) {
    const jchar* convertedFilePath = env->GetStringChars(filePath, JNI_FALSE);
    HRESULT res = SHGetPropertyStoreFromParsingName((PCWSTR) convertedFilePath, NULL, GPS_READWRITE, IID_IPropertyStore, store);

    env->ReleaseStringChars(filePath, convertedFilePath);
    return res;
}

static IPropertyStore* createStoreForFile(jstring filePath, JNIEnv* env) {
    IPropertyStore* store;
    HRESULT res = createUncheckedStoreForFile(filePath, env, (void**) &store);

    if(FAILED(res)) {
        const char* nativePath = env->GetStringUTFChars(filePath, JNI_FALSE);

        if(res == NON_MEDIA_FILE_ERROR) {  // Means 'unknown', "usually" happens when the file is not a media file
            std::string message = "The given file is not a media file: '";

            env->ThrowNew(env->FindClass("mediaprops/exception/NonMediaFileException"), (message + nativePath + "'").c_str());
        }else{
            std::string message = "An IO error happened with the file: '";

            env->ThrowNew(env->FindClass("mediaprops/exception/MediaFileIOException"), (message + nativePath + "', description: " + (char*) _com_error(res).ErrorMessage()).c_str());
        }

        env->ReleaseStringUTFChars(filePath, nativePath);
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


extern "C" JNIEXPORT void JNICALL Java_mediaprops_MediaFileUtils_init(JNIEnv* env, jclass clazz) {
    CoInitialize(NULL);
}

extern "C" JNIEXPORT jboolean JNICALL Java_mediaprops_MediaFileUtils_hasMediaProperty(JNIEnv* env, jclass clazz, jstring filePath, jint propertyKey) {
    return getPropertyValue(filePath, propertyKey, env).vt == VT_EMPTY ? JNI_FALSE : JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_mediaprops_MediaFileUtils_isValidMediaFile(JNIEnv* env, jclass clazz, jstring filePath) {
    IPropertyStore* store;
    jboolean isValid = createUncheckedStoreForFile(filePath, env, (void**) &store) != NON_MEDIA_FILE_ERROR;

    if(isValid) {
        store->Release();
    }

    return isValid;
}

extern "C" JNIEXPORT void JNICALL Java_mediaprops_MediaFileUtils_clearMediaProperty(JNIEnv* env, jclass clazz, jstring filePath, jint propertyKey) {
    PROPVARIANT prop;
    prop.vt = VT_EMPTY;

    writePropertyValue(filePath, propertyKey, &prop, env);
}

extern "C" JNIEXPORT jstring JNICALL Java_mediaprops_MediaFileUtils_readStringProperty(JNIEnv* env, jclass clazz, jstring filePath, jint propertyKey) {
    PROPVARIANT variant = getPropertyValue(filePath, propertyKey, env);

    return variant.vt == VT_EMPTY ? nullptr : env->NewString((jchar*) variant.pwszVal, wcslen(variant.pwszVal));
}

extern "C" JNIEXPORT jint JNICALL Java_mediaprops_MediaFileUtils_readIntProperty(JNIEnv* env, jclass clazz, jstring filePath, jint propertyKey) {
    PROPVARIANT variant = getPropertyValue(filePath, propertyKey, env);

    return variant.vt == VT_EMPTY ? NULL_INT_VALUE : variant.intVal;
}

extern "C" JNIEXPORT void JNICALL Java_mediaprops_MediaFileUtils_writeStringProperty(JNIEnv* env, jclass clazz, jstring filePath, jint propertyKey, jstring propertyValue) {
    PROPVARIANT prop;
    const jchar* convertedValue = env->GetStringChars(propertyValue, JNI_FALSE);

    InitPropVariantFromString((PCWSTR) convertedValue, &prop);
    env->ReleaseStringChars(propertyValue, convertedValue);

    writePropertyValue(filePath, propertyKey, &prop, env);
}

extern "C" JNIEXPORT void JNICALL Java_mediaprops_MediaFileUtils_writeIntProperty(JNIEnv* env, jclass clazz, jstring filePath, jint propertyKey, jint propertyValue) {
    PROPVARIANT prop;
    InitPropVariantFromInt32(propertyValue, &prop);

    writePropertyValue(filePath, propertyKey, &prop, env);
}