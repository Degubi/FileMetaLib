#include "ShObjIdl.h"
#include "propkey.h"
#include "propvarutil.h"
#include "jni.h"

void updateStringPropertyValue(PROPERTYKEY key, PCWSTR value, IPropertyStore* store) {
    PROPVARIANT prop;
    InitPropVariantFromString(value, &prop);

    store->SetValue(key, prop);
}

PROPERTYKEY getPropertyKey(jint propOrdinal) {
    switch(propOrdinal) {
        case 0: return PKEY_Title;
        case 1: return PKEY_Media_SubTitle;
        case 2: return PKEY_Comment;
        case 3: return PKEY_Author;
    }
}



extern "C" JNIEXPORT void JNICALL Java_degubi_FileMeta_init(JNIEnv* env, jclass clazz) {
    CoInitialize(NULL);
}

extern "C" JNIEXPORT void JNICALL Java_degubi_FileMeta_updateStringProperty(JNIEnv* env, jclass clazz, jstring filePath, jint propertyKey, jstring propertyValue) {
    IPropertyStore* store = NULL;
    const jchar* convertedFilePath = env->GetStringChars(filePath, 0);
    const jchar* convertedPropertyValue = env->GetStringChars(propertyValue, 0);

    SHGetPropertyStoreFromParsingName((wchar_t*) convertedFilePath, NULL, GPS_READWRITE, __uuidof(IPropertyStore), (void**)&store);
    updateStringPropertyValue(getPropertyKey(propertyKey), (wchar_t*) convertedPropertyValue, store);

    store->Commit();
    store->Release();

    env->ReleaseStringChars(filePath, convertedFilePath);
    env->ReleaseStringChars(propertyValue, convertedPropertyValue);
}