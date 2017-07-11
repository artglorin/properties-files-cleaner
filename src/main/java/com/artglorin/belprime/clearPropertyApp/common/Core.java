package com.artglorin.belprime.clearPropertyApp.common;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static com.artglorin.belprime.clearPropertyApp.common.Util.getOutputDirectory;
import static com.artglorin.belprime.clearPropertyApp.common.Util.loadProperties;
import static com.artglorin.belprime.clearPropertyApp.common.Util.storeProperties;

/**
 * Created by V.Verminsky on 10.07.2016.
 */
public class Core implements Runnable {

    private final Properties templateProperties;
    private final File fileWithProcessedProperties;
    private final Charset charset;

    public Core(Properties templateProperties, File fileWithProcessedProperties, Charset charset) {
        this.templateProperties = templateProperties;
        this.fileWithProcessedProperties = fileWithProcessedProperties;
        this.charset = charset;
    }

    @Override
    public void run() {
        try {
            Path cleanupDirectoryPath = getOutputDirectory(fileWithProcessedProperties.getParent());
            final Properties cleaningProperties = loadProperties(fileWithProcessedProperties.toPath(), charset);
            final Properties deletedProperties = new Properties();
            final Path resultOutputFile = Paths.get(cleanupDirectoryPath.toString(), fileWithProcessedProperties.getName());
            final Path forDeletedPropertiesFile = Paths.get(cleanupDirectoryPath.toString(), fileWithProcessedProperties.getName() +".deletedStrings");
            if(cleaningProperties.entrySet().removeIf(entry -> {
                final Object value = templateProperties.get(entry.getKey());
                final boolean remove = value == null || entry.getValue().equals(value);
                if (remove) {
                    deletedProperties.put(entry.getKey(), entry.getValue());
                }
                return remove;
            })) {
                storeProperties(deletedProperties, forDeletedPropertiesFile, charset);
            }
            storeProperties(cleaningProperties, resultOutputFile, charset);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
