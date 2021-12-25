#include "shobjidl_core.h"
#include "propkey.h"
#include "jni.h"
#include "string.h"
#include "MediaProps.c"

#define MEDIAPROP_DEBUG 1
#define NULL_INT_VALUE -1                // This is fine because all of the number media property types are VT_UI4, which is unsigned
#define NON_MEDIA_FILE_ERROR -2147467259 // Means 'unknown', "usually" happens when the file is not a media file

#if MEDIAPROP_DEBUG
#include "stdio.h"
#endif


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
        PROPERTYKEY* key = getPropertyKey(propertyKey);

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
        PROPERTYKEY* key = getPropertyKey(propertyKey);

        store->lpVtbl->SetValue(store, key, prop);
        store->lpVtbl->Commit(store);
        store->lpVtbl->Release(store);
    }
}


JNIEXPORT void JNICALL Java_mediaprops_MediaFileUtils_init(JNIEnv* env, jclass clazz) {
    CoInitialize(NULL);
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

    return variant.vt == VT_EMPTY ? NULL : (*env)->NewString(env, (jchar*) variant.pwszVal, wcslen(variant.pwszVal));
}

JNIEXPORT jint JNICALL Java_mediaprops_MediaFileUtils_readIntProperty(JNIEnv* env, jclass clazz, jstring filePath, jint propertyKey) {
    PROPVARIANT variant = getPropertyValue(filePath, propertyKey, env);

    return variant.vt == VT_EMPTY ? NULL_INT_VALUE : variant.intVal;
}

JNIEXPORT jobject JNICALL Java_mediaprops_MediaFileUtils_readAllMediaProperties(JNIEnv* env, jclass clazz, jstring filePath) {
    IPropertyStore* store = createStoreForFile(filePath, env);
    jclass mediaPropertyMapClass = (*env)->FindClass(env, "mediaprops/MediaPropertyMap");
    jclass integerClass = (*env)->FindClass(env, "java/lang/Integer");
    jclass mediaPropertyClass = (*env)->FindClass(env, "mediaprops/MediaProperty");
    jmethodID addFunction = (*env)->GetMethodID(env, mediaPropertyMapClass, "put", "(Lmediaprops/MediaProperty;Ljava/lang/Object;)V");
    jmethodID valueOfFunction = (*env)->GetStaticMethodID(env, integerClass, "valueOf", "(I)Ljava/lang/Integer;");
    jobject result = (*env)->NewObject(env, mediaPropertyMapClass, (*env)->GetMethodID(env, mediaPropertyMapClass, "<init>", "()V"));

    if(store != NULL) {
        DWORD propCount;
        store->lpVtbl->GetCount(store, &propCount);

        for(int i = 0; i < propCount; ++i) {
            PROPERTYKEY key;
            PROPVARIANT value;

            store->lpVtbl->GetAt(store, i, &key);
            store->lpVtbl->GetValue(store, &key, &value);

            char* fieldName = getPropertyFieldName(&key);
            if(fieldName != NULL) {
                jobject mapKey = (*env)->GetStaticObjectField(env, mediaPropertyClass, (*env)->GetStaticFieldID(env, mediaPropertyClass, fieldName, "Lmediaprops/MediaProperty;"));

                switch(value.vt) {
                    case VT_UI2 : (*env)->CallObjectMethod(env, result, addFunction, mapKey, (*env)->CallStaticObjectMethod(env, integerClass, valueOfFunction, value.uiVal)); break;
                    case VT_UI4 : (*env)->CallObjectMethod(env, result, addFunction, mapKey, (*env)->CallStaticObjectMethod(env, integerClass, valueOfFunction, value.ulVal)); break;
                    case VT_UI8 : (*env)->CallObjectMethod(env, result, addFunction, mapKey, (*env)->CallStaticObjectMethod(env, integerClass, valueOfFunction, value.uhVal)); break;
                    case VT_LPWSTR : (*env)->CallObjectMethod(env, result, addFunction, mapKey, (*env)->NewString(env, (jchar*) value.pwszVal, wcslen(value.pwszVal))); break;

                    #if MEDIAPROP_DEBUG
                    default:
                        printf("Property: %s (pid: %d), type: %d\n", fieldName, key.pid, value.vt);
                        (*env)->CallObjectMethod(env, result, addFunction, mapKey, NULL);
                        break;
                    #endif
                }
            }
            #if MEDIAPROP_DEBUG
            else{
                wchar_t guidStr[40] = {0};
                StringFromGUID2(&key.fmtid, guidStr, 40);

                printf("Unknown property field id: %d, name: %ls\n", key.pid, guidStr);
            }
            #endif
        }

        store->lpVtbl->Release(store);
    }

    return result;
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