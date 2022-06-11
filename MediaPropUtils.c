#include "shobjidl_core.h"
#include "propkey.h"
#include "jni.h"
#include "string.h"

#define NULL_INT_VALUE -1                // This is fine because all of the number media property types are VT_UI4, which is unsigned
#define NON_MEDIA_FILE_ERROR -2147467259 // Means 'unknown', "usually" happens when the file is not a media file


static const PROPERTYKEY* getPropertyKey(jint propOrdinal) {
    switch(propOrdinal) {
        case 0:  return &PKEY_Author;
        case 1:  return &PKEY_Comment;
        case 2:  return &PKEY_Copyright;
        case 3:  return &PKEY_Keywords;
        case 4:  return &PKEY_Language;
        case 5:  return &PKEY_Media_SubTitle;
        case 6:  return &PKEY_Media_Year;
        case 7:  return &PKEY_Title;
        case 8:  return &PKEY_Video_Director;
        case 9:  return &PKEY_Audio_ChannelCount;
        case 10: return &PKEY_Audio_EncodingBitrate;
        case 11: return &PKEY_Media_AuthorUrl;
        case 12: return &PKEY_Media_EncodedBy;
        case 13: return &PKEY_Media_Duration;
        case 14: return &PKEY_Audio_Format;
        case 15: return &PKEY_Audio_SampleRate;
        case 16: return &PKEY_Audio_SampleSize;
        default: return NULL;
    }
}

static HRESULT createUncheckedStoreForFile(jstring filePath, JNIEnv* env, IPropertyStore** store) {
    const jchar* convertedFilePath = (*env)->GetStringChars(env, filePath, JNI_FALSE);
    HRESULT res = SHGetPropertyStoreFromParsingName((PCWSTR) convertedFilePath, NULL, GPS_READWRITE, &IID_IPropertyStore, store);

    (*env)->ReleaseStringChars(env, filePath, convertedFilePath);
    return res;
}

static IPropertyStore* createStoreForFile(jstring filePath, JNIEnv* env) {
    IPropertyStore* store;
    HRESULT res = createUncheckedStoreForFile(filePath, env, &store);

    if(FAILED(res)) {
        const char* nativePath = (*env)->GetStringUTFChars(env, filePath, JNI_FALSE);

        if(res == NON_MEDIA_FILE_ERROR) {  // Means 'unknown', "usually" happens when the file is not a media file
            char message[128] = "The given file is not a media file: '";
            strcat(message, nativePath);
            strcat(message, "'\r\n");

            (*env)->ThrowNew(env, (*env)->FindClass(env, "mediaprops/exception/NonMediaFileException"), message);
        }else{
            TCHAR* errorText;
            FormatMessage(FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_IGNORE_INSERTS, NULL, res, MAKELANGID(LANG_ENGLISH, SUBLANG_ENGLISH_US), (LPTSTR) &errorText, 0, NULL);

            char message[128] = "An IO error happened with the file: '";
            strcat(message, nativePath);
            strcat(message, "', description: ");
            strcat(message, (char*) errorText);

            LocalFree(errorText);
            (*env)->ThrowNew(env, (*env)->FindClass(env, "mediaprops/exception/MediaFileIOException"), message);
        }

        (*env)->ReleaseStringUTFChars(env, filePath, nativePath);
        return NULL;
    }

    return store;
}

static PROPVARIANT getPropertyValue(jstring filePath, jint propertyKey, JNIEnv* env) {
    IPropertyStore* store = createStoreForFile(filePath, env);
    PROPVARIANT prop;

    if(store != NULL) {
        const PROPERTYKEY* key = getPropertyKey(propertyKey);

        store->lpVtbl->GetValue(store, key, &prop);
        store->lpVtbl->Release(store);
    }else{
        prop.vt = VT_EMPTY;
    }

    return prop;
}

static void writePropertyValue(jstring filePath, jint propertyKey, PROPVARIANT* prop, JNIEnv* env) {
    IPropertyStore* store = createStoreForFile(filePath, env);

    if(store != NULL) {
        const PROPERTYKEY* key = getPropertyKey(propertyKey);

        store->lpVtbl->SetValue(store, key, prop);
        store->lpVtbl->Commit(store);
        store->lpVtbl->Release(store);
    }
}

static jstring getArrayPropertyStringElement(PROPVARIANT variant, JNIEnv* env) {
    LPWSTR firstElement = variant.calpwstr.pElems[0];

    return (*env)->NewString(env, (jchar*) firstElement, wcslen(firstElement));
}


JNIEXPORT void JNICALL Java_mediaprops_MediaFileUtils_init(JNIEnv* env, jclass clazz) {
    CoInitializeEx(NULL, COINIT_MULTITHREADED);
}

JNIEXPORT jboolean JNICALL Java_mediaprops_MediaFileUtils_hasMediaProperty(JNIEnv* env, jclass clazz, jstring filePath, jint propertyKey) {
    return getPropertyValue(filePath, propertyKey, env).vt == VT_EMPTY ? JNI_FALSE : JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_mediaprops_MediaFileUtils_isValidMediaFile(JNIEnv* env, jclass clazz, jstring filePath) {
    IPropertyStore* store;
    jboolean isValid = createUncheckedStoreForFile(filePath, env, &store) != NON_MEDIA_FILE_ERROR;

    if(isValid) {
        store->lpVtbl->Release(store);
    }

    return isValid;
}

JNIEXPORT void JNICALL Java_mediaprops_MediaFileUtils_clearMediaProperty(JNIEnv* env, jclass clazz, jstring filePath, jint propertyKey) {
    PROPVARIANT prop = { .vt = VT_EMPTY };

    writePropertyValue(filePath, propertyKey, &prop, env);
}

JNIEXPORT jstring JNICALL Java_mediaprops_MediaFileUtils_readStringProperty(JNIEnv* env, jclass clazz, jstring filePath, jint propertyKey) {
    PROPVARIANT variant = getPropertyValue(filePath, propertyKey, env);

    return variant.vt == VT_EMPTY ? NULL :
           (variant.vt & VT_VECTOR) != 0 ? getArrayPropertyStringElement(variant, env) :
                                           (*env)->NewString(env, (jchar*) variant.pwszVal, wcslen(variant.pwszVal));
}

JNIEXPORT jint JNICALL Java_mediaprops_MediaFileUtils_readIntProperty(JNIEnv* env, jclass clazz, jstring filePath, jint propertyKey) {
    PROPVARIANT variant = getPropertyValue(filePath, propertyKey, env);

    return variant.vt == VT_EMPTY ? NULL_INT_VALUE : variant.intVal;
}

JNIEXPORT void JNICALL Java_mediaprops_MediaFileUtils_writeStringProperty(JNIEnv* env, jclass clazz, jstring filePath, jint propertyKey, jstring propertyValue) {
    const jchar* convertedValue = (*env)->GetStringChars(env, propertyValue, JNI_FALSE);
    PROPVARIANT prop = { .vt = VT_LPWSTR, .pwszVal = (LPWSTR) convertedValue };

    writePropertyValue(filePath, propertyKey, &prop, env);
    (*env)->ReleaseStringChars(env, propertyValue, convertedValue);
}

JNIEXPORT void JNICALL Java_mediaprops_MediaFileUtils_writeIntProperty(JNIEnv* env, jclass clazz, jstring filePath, jint propertyKey, jint propertyValue) {
    PROPVARIANT prop = { .vt = VT_I4, .lVal = propertyValue };

    writePropertyValue(filePath, propertyKey, &prop, env);
}