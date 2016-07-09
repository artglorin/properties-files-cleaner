package com.artglorin.belprime.clearPropertyApp.controller;

import com.artglorin.belprime.clearPropertyApp.utils.FileUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;
import rx.Subscriber;
import rx.internal.schedulers.CachedThreadScheduler;
import rx.internal.util.RxThreadFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by V.Verminsky on 06.07.2016.
 */
public class MainController {
    private static final Logger logger = LogManager.getLogger(MainController.class);

    @FXML
    private ListView<String> processedList;

    @FXML
    private Label templateLabel;

    @FXML
    private TextArea log;
    private File template;
    private List<File> processList;

    @FXML
    private void initialize() {

    }


    @FXML
    private void loadTemplate() {
        template = FileUtil.openDialog("Property files", "*.properties");
        if (template != null) {
            templateLabel.setText(template.getName());
//            AppContext.getAppContext().put("template", template);
        }

    }


    @FXML
    private void loadProcessed() {
        processList = FileUtil.openMultiplyDialog("Property files", "*.properties");
        if (processList != null) {

            processedList.getItems().clear();
            if (processList.contains(template)){
                processList = new ArrayList<>(processList);
                processList.remove(template);
                logger.info("remove from list template file: " + template.getName());
            }
            processedList.getItems().addAll(processList.stream().map(File::getName).collect(Collectors.toList()));
//            AppContext.getAppContext().put("processedList", processList);
        }
    }

    @FXML
    private void process() {

//        final Properties properties = loadProperties(template);
        Observable.from(processList)
                .subscribeOn(new CachedThreadScheduler(new RxThreadFactory("process")))
                .subscribe(it -> new FileHandler(loadProperties(template), it));
//        processList.forEach(file -> {
//            createTempFile(file);
//        });
    }


    private static File createTempFile(File file) {
        File temp = null;
        try {
            temp = File.createTempFile(file.getName(), ".tmp");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return temp;
    }

    private static Properties loadProperties(File template) {
        final Properties properties = new Properties();
        try (InputStreamReader inputStream = new InputStreamReader(new FileInputStream(template), "Windows-1251")) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    private static class FileHandler {

        public final org.apache.logging.log4j.Logger LOG;
        private final KeyChecker keyChecker;

        FileHandler(Properties properties, File processed) {
            keyChecker = new KeyChecker(properties);
            LOG = LogManager.getLogger("FileHandler: " + processed.getName());


            final File tempFile = createTempFile(processed);
            try (Scanner scanner = new Scanner(new InputStreamReader(new FileInputStream(processed), "Windows-1251"));
                 OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempFile), "Windows-1251")) {
                boolean nextLineWriteAsIs = false;
                while (scanner.hasNextLine()) {

                    String line ;
//                    boolean readnext = false;
//                    do {

                    line = scanner.nextLine();
                    if (nextLineWriteAsIs) {
                        writer.write(line + System.lineSeparator());
                        LOG.info("Write line: {}", line);
                        nextLineWriteAsIs =   line.endsWith("\\") && !line.endsWith("\\\\");
                        continue;
                    }
//                        if (readnext){
//                            line = line.substring(0, line.lastIndexOf("\\"));
//                        }
//                    } while (readnext && scanner.hasNextLine());
                    if (keyChecker.test(line)) {
                        nextLineWriteAsIs =  line.endsWith("\\") && !line.endsWith("\\\\");
                        // Пустую строку и строку с комментарием пишем в файл
                        writer.write(line + System.lineSeparator());
                        LOG.info("Write line: {}", line);
                    } else {
                        LOG.error("Delete line: {}", line);
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            }
            try {
                final Path path = Files.createDirectories(Paths.get(processed.getParent(), "processed"));
                final Path file = Paths.get(path.toString(), processed.getName());
                Files.deleteIfExists(file);
                Files.copy(tempFile.toPath(), file);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }




    private static class StringChecker extends Subscriber<String> {
        private final KeyChecker keyChecker;
        private StringWriter writer = new StringWriter();

        public StringChecker(Properties properties) {
            keyChecker = new KeyChecker(properties);
        }

        @Override
        public void onCompleted() {
            writer.onCompleted();
        }

        @Override
        public void onError(Throwable throwable) {
            writer.onError(throwable);
        }

        @Override
        public void onNext(String s) {
            if (keyChecker.test(s)) {
                // Пустую строку и строку с комментарием пишем в файл
                writer.onNext(s);
//                LOG.info("Write line: {}", s);
            } else {
//                LOG.error("Delete line: {}", s);
            }

        }
    }


    private static class KeyChecker implements Predicate<String> {

        private final Properties properties;
        private final KeyExtractor keyExtractor = new KeyExtractor();

        public KeyChecker(Properties properties) {
            this.properties = properties;
        }


        @Override
        public boolean test(String s) {
            s = s.trim();
            return s.isEmpty() || s.startsWith("#") || (s.contains("=") && properties.containsKey(keyExtractor.getKey(s)));
        }
    }

    private static class KeyExtractor {
        public String getKey(String line) {
            return line.substring(0, line.indexOf("="));
        }
    }


    private static class StringWriter extends Subscriber<String> {

        private FileWriter writer;

        public StringWriter() {
            try {
                writer = new FileWriter(File.createTempFile("", ""));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onCompleted() {
//            this.
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(Throwable throwable) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onNext(String s) {
            try {
                writer.write(s);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
