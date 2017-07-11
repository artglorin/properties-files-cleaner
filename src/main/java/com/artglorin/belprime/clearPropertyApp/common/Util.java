package com.artglorin.belprime.clearPropertyApp.common;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
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

    public static void storeProperties(Properties properties, Path path, Charset utf8) throws IOException {
        try (Writer outputStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path.toFile()), utf8))) {
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
