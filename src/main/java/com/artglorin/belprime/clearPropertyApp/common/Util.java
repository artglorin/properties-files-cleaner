package com.artglorin.belprime.clearPropertyApp.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Created by V.Verminsky on 11.07.2017.
 */
@SuppressWarnings ("WeakerAccess")
public class Util {

    private Util() {
    }

    public static void storeProperties(Properties properties, Path path) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(path.toFile())) {
            properties.store(outputStream, "");
        }
    }

    public static Path getOutputDirectory(String templatePath) throws IOException {
        return Files.createDirectories(Paths.get(templatePath, "cleanup"));
    }

    public static Properties loadProperties(Path path, Charset charset) throws IOException {
        final Properties properties = new Properties();
        try (InputStreamReader inputStream = new InputStreamReader(new FileInputStream(path.toFile()), charset)) {
            properties.load(inputStream);
        }
        return properties;
    }

    public static Properties loadProperties(Path path) throws IOException {
        final Properties result = new Properties();
        try (InputStream outputStream = new FileInputStream(path.toFile())) {
            result.load(outputStream);
        }
        return result;
    }
}
