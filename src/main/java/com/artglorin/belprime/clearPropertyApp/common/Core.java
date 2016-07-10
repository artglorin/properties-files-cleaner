package com.artglorin.belprime.clearPropertyApp.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Scanner;

/**
 * Created by V.Verminsky on 10.07.2016.
 */
public class Core implements Runnable {

    private final Properties properties;

    private final File processed;
    private final Charset charset;

    public Core(Properties properties, File processed, Charset charset) {
        this.properties = properties;
        this.processed = processed;
        this.charset = charset;
    }

    @Override
    public void run() {
        try {
            Path cleanupDirectoryPath = Files.createDirectories(Paths.get(processed.getParent(), "cleanup"));
            Logger logger = (Logger) LogManager.getLogger("FileHandler: " + processed.getName());
            FileAppender fa = FileAppender.createAppender(cleanupDirectoryPath.toString() + File.separator + processed.getName() + ".log", "false", "false", "File", "true",
                    "false", "false", "4000", PatternLayout.createDefaultLayout(), null, "false", null, null);
            logger.addAppender(fa);
            fa.start();


            final File tempFile = createTempFile(processed);
            try (Scanner scanner = new Scanner(new InputStreamReader(new FileInputStream(processed), charset));
                 OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempFile), charset)) {
                boolean nextLineWriteAsIs = false;
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (nextLineWriteAsIs) {
                        nextLineWriteAsIs = writeLine(writer, line, logger);
                        continue;
                    }
                    if (isNeedWriteString(line)) {
                        nextLineWriteAsIs = writeLine(writer, line, logger);
                    } else {
                        logger.error("Delete line: {}", line);
                    }
                }
            } catch (IOException e) {
                logger.error(e);
            }
            try {
                final Path file = Paths.get(cleanupDirectoryPath.toString(), processed.getName());
                Files.deleteIfExists(file);
                Files.copy(tempFile.toPath(), file);
                Files.deleteIfExists(tempFile.toPath());
            } catch (IOException e) {
                logger.error(e);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean writeLine(OutputStreamWriter writer, String line, Logger logger) throws IOException {
        boolean nextLineWriteAsIs;
        logger.info("Write line: {}", line);
        writer.write(line + System.lineSeparator());
        nextLineWriteAsIs = line.endsWith("\\") && !line.endsWith("\\\\");
        return nextLineWriteAsIs;
    }


    private boolean isNeedWriteString(String string) {
        string = string.trim();
        return string.isEmpty() || string.startsWith("#") || (string.contains("=") && properties.containsKey(getKey(string)));

    }

    public String getKey(String line) {
        return line.substring(0, line.indexOf("="));
    }


    private File createTempFile(File file) {
        File temp = null;
        try {
            temp = File.createTempFile(file.getName(), ".tmp");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return temp;
    }

    private Properties loadProperties(File template) {
        final Properties properties = new Properties();
        try (InputStreamReader inputStream = new InputStreamReader(new FileInputStream(template), "Windows-1251")) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

}
