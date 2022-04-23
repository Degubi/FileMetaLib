#include "shobjidl_core.h"
#include "propkey.h"

#define PKEY_Author_hash                3521196938
#define PKEY_Comment_hash               3521196940
#define PKEY_Copyright_hash             1592722102
#define PKEY_Keywords_hash              3521196939
#define PKEY_Language_hash              3619066863
#define PKEY_Media_SubTitle_hash        152552229
#define PKEY_Media_Year_hash            4169439567
#define PKEY_Title_hash                 3521196936
#define PKEY_Video_Director_hash        1592722134
#define PKEY_Audio_ChannelCount_hash    1332262441
#define PKEY_Audio_EncodingBitrate_hash 1332262438
#define PKEY_Media_AuthorUrl_hash       1592722169
#define PKEY_Media_EncodedBy_hash       1592722173
#define PKEY_Media_Duration_hash        1332262437
#define PKEY_Audio_Format_hash          1332262436
#define PKEY_Audio_SampleRate_hash      1332262439
#define PKEY_Audio_SampleSize_hash      1332262440

static unsigned long hashPropKey(PROPERTYKEY* property) {
    WCHAR keyName[PKEYSTR_MAX];
    PSStringFromPropertyKey(property, keyName, PKEYSTR_MAX);

    unsigned long result = 5381;
    WCHAR* str = keyName;

    for(int c; (c = *str++);) {
        result = ((result << 5) + result) + c;
    }

    return result;
}

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

static char* getPropertyFieldName(PROPERTYKEY* key) {
    switch(hashPropKey(key)) {
        case PKEY_Author_hash:                return "AUTHOR";
        case PKEY_Comment_hash:               return "COMMENT";
        case PKEY_Copyright_hash:             return "COPYRIGHT";
        case PKEY_Keywords_hash:              return "KEYWORDS";
        case PKEY_Language_hash:              return "LANGUAGE";
        case PKEY_Media_SubTitle_hash:        return "SUB_TITLE";
        case PKEY_Media_Year_hash:            return "YEAR";
        case PKEY_Title_hash:                 return "TITLE";
        case PKEY_Video_Director_hash:        return "DIRECTOR";
        case PKEY_Audio_ChannelCount_hash:    return "AUDIO_CHANNEL_COUNT";
        case PKEY_Audio_EncodingBitrate_hash: return "AUDIO_ENCODING_BITRATE";
        case PKEY_Media_AuthorUrl_hash:       return "AUTHOR_URL";
        case PKEY_Media_EncodedBy_hash:       return "ENCODED_BY";
        case PKEY_Media_Duration_hash:        return "DURATION";
        case PKEY_Audio_Format_hash:          return "AUDIO_FORMAT";
        case PKEY_Audio_SampleRate_hash:      return "AUDIO_SAMPLE_RATE";
        case PKEY_Audio_SampleSize_hash:      return "AUDIO_SAMPLE_SIZE";
        default:                              return NULL;
    }
}