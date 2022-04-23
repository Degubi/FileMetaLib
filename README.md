# JMedia-Properties
[![Github issues](https://img.shields.io/github/issues/Degubi/JMedia-Properties?label=Issues&style=plastic&logo=github)](https://github.com/Degubi/JMedia-Properties/issues)
[![Dependencies](https://img.shields.io/badge/Dependencies-0-green?style=plastic&logo=Java)](https://github.com/Degubi/JMedia-Properties/blob/master/pom.xml)
- Java wrapper library around Windows Media File Metadata Properties
- Useful for editing e.g the year or author attributes of a media file (e.g mp3/mp4 files)

## What are Windows Media File Metadata Properties?

- Windows docs: https://docs.microsoft.com/en-us/windows/win32/medfound/metadata-properties-for-media-files
- Basically these properties in the file details menu:
<br><br><img src = "https://winaero.com/blog/wp-content/uploads/2014/02/Windows-Properties.png">

## Installation

**Maven dependency:** (via Github Packages)

```xml
<repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/Degubi/jmedia-properties</url>
</repository>

<dependency>
    <groupId>degubi</groupId>
    <artifactId>jmedia-properties</artifactId>
    <version>1.0.2</version>
</dependency>
```


**Jar file:**
<br><br>
Jar file downloads are available under 'Packages'

## Usage
All operations are found as static utilities in a single class: MediaFileUtils<br>
All supported media properties are found in an enum like class: MediaProperty

```java
var file = Path.of("myFile.mp4");
var targetFile = Path.of("targetFile.mp4");

// Returns the 'author' field's value wrapped in an optional
MediaFileUtils.readProperty(file, MediaProperty.AUTHOR);

// Returns true if the file has a 'language' property
MediaFileUtils.hasProperty(file, MediaProperty.LANGUAGE);

// Returns true if the given file is a valid media file
MediaFileUtils.isMediaFile(file);

// Write 2021 into the 'year' field of the file
MediaFileUtils.writeProperty(file, MediaProperty.YEAR, 2021);

// Copies the year field from 'file' to 'targetFile'
MediaFileUtils.copyProperty(file, targetFile, MediaProperty.YEAR);

// Clears the file's 'title' property
MediaFileUtils.clearPropery(file, MediaProperty.TITLE);
```

## Contributing

Feedback, bug reports and enhancements are always welcome.
