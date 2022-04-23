package mediaprops;

import static mediaprops.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import mediaprops.exception.*;
import org.junit.jupiter.api.*;

public class MediaPropertyReadTests {

    @Test
    public void testReadFromFileInUse() throws IOException {
        try(var hihi = Files.newOutputStream(mp4Path, StandardOpenOption.WRITE)) {
            var exception = assertThrows(MediaFileIOException.class, () -> MediaFileUtils.readProperty(mp4Path, MediaProperty.COMMENT));

            assertEquals("An IO error happened with the file: '" + executionFolder + "\\src\\test\\resources\\test.mp4', description: The process cannot access the file because it is being used by another process.\r\n", exception.getMessage());
        }
    }

    @Test
    public void testReadFromNonExistentFile() {
        var exception = assertThrows(FileDoesNotExistException.class, () -> MediaFileUtils.readProperty(nonExistingPath, MediaProperty.COMMENT));

        assertEquals("File doesn't exist: '" + executionFolder + "\\src\\test\\resources\\test.lol'", exception.getMessage());
    }

    @Test
    public void testReadFromNonMediaFile() {
        var exception = assertThrows(NonMediaFileException.class, () -> MediaFileUtils.readProperty(txtPath, MediaProperty.COMMENT));

        assertEquals("The given file is not a media file: '" + executionFolder + "\\src\\test\\resources\\test.txt'\r\n", exception.getMessage());
    }

    @Test
    public void testValidMediaFile() {
        assertTrue(MediaFileUtils.isMediaFile(mp4Path));
    }

    @Test
    public void testInvalidMediaFile() {
        assertFalse(MediaFileUtils.isMediaFile(txtPath));
    }

    @Test
    public void testDoesntHaveProperty() {
        assertFalse(MediaFileUtils.hasProperty(mp4Path, MediaProperty.LANGUAGE));
    }

    @Test
    public void testNonExistingStringPropertyRead() {
        assertEquals(Optional.empty(), MediaFileUtils.readProperty(mp4Path, MediaProperty.COPYRIGHT));
    }

    @Test
    public void testNonExistingStringIntegerRead() {
        assertEquals(Optional.empty(), MediaFileUtils.readProperty(mp4Path, MediaProperty.YEAR));
    }
}