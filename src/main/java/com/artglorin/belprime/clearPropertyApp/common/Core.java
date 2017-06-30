package com.artglorin.belprime.clearPropertyApp.common;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by V.Verminsky on 10.07.2016.
 */
public class Core implements Runnable {

    private final Properties templateProperties;
    private static final Pattern pattern = Pattern.compile("\\\\+$");
    private final File outputFile;
    private final Charset charset;

    public Core(Properties templateProperties, File outputFile, Charset charset) {
        this.templateProperties = templateProperties;
        this.outputFile = outputFile;
        this.charset = charset;
    }

    @Override
    public void run() {
        try {
            Path cleanupDirectoryPath = getOutputDirectory(outputFile.getParent());
            final Path writeFile = Paths.get(cleanupDirectoryPath.toString(), outputFile.getName());
            final Path deleteFile = Paths.get(cleanupDirectoryPath.toString(), outputFile.getName() +".deletedStrings");
            try (Scanner scanner = new Scanner(new InputStreamReader(new FileInputStream(outputFile), charset));
                 OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(writeFile.toFile()), charset);
                 OutputStreamWriter deleted = new OutputStreamWriter(new FileOutputStream(deleteFile.toFile()), charset)) {
                boolean nextLineWriteAsIs = false;
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (nextLineWriteAsIs) {
                        nextLineWriteAsIs = writeLine(writer, line);
                        continue;
                    }
                    if (isNeedWriteString(line)) {
                        nextLineWriteAsIs = writeLine(writer, line);
                    } else {
                        deleted.write(line + System.lineSeparator());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Path getOutputDirectory(String templatePath) throws IOException {
        return Files.createDirectories(Paths.get(templatePath, "cleanup"));
    }

    private boolean writeLine(OutputStreamWriter writer, String line) throws IOException {
        boolean nextLineWriteAsIs = false;
        writer.write(line + System.lineSeparator());
        final Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            nextLineWriteAsIs = matcher.group().length() % 2 != 0;
        }
        return nextLineWriteAsIs;
    }


    private boolean isNeedWriteString(String string) {
        string = string.trim();
        return string.isEmpty() || string.startsWith("#") || (string.contains("=") && templateProperties.containsKey(getKey(string)));

    }

    private String getKey(String line) {
        return line.substring(0, line.indexOf("="));
    }
}
