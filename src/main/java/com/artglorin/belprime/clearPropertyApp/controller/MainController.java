package com.artglorin.belprime.clearPropertyApp.controller;

import com.artglorin.belprime.clearPropertyApp.common.Core;
import com.artglorin.belprime.clearPropertyApp.utils.FileUtil;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by V.Verminsky on 06.07.2016.
 */
public class MainController {
    private static final Logger logger = LogManager.getLogger(MainController.class);

    private File template;

    @FXML
    private ListView<File> processedList;

    @FXML
    private ComboBox<Charset> templateEncoding;

    @FXML
    private ComboBox<Charset> processListEncoding;

    @FXML
    private Label templateLabel;

    @FXML
    private TextArea log;

    @FXML
    private void initialize() {
        processedList.setCellFactory(param -> new ListCell<File>(){
                    @Override
                    protected void updateItem(File item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item.getName());
                        } else {
                            setText(null);
                        }
                    }
                }
        );
        final Charset defaultCharset = Charset.forName("UTF-8");
        final List<Charset> charsetList = Arrays.asList(Charset.forName("IBM00858"),
                Charset.forName("IBM437"),
                Charset.forName("IBM775"),
                Charset.forName("IBM850"),
                Charset.forName("IBM852"),
                Charset.forName("IBM855"),
                Charset.forName("IBM857"),
                Charset.forName("IBM862"),
                Charset.forName("IBM866"),
                Charset.forName("ISO-8859-1"),
                Charset.forName("ISO-8859-2"),
                Charset.forName("ISO-8859-4"),
                Charset.forName("ISO-8859-5"),
                Charset.forName("ISO-8859-7"),
                Charset.forName("ISO-8859-9"),
                Charset.forName("ISO-8859-13"),
                Charset.forName("ISO-8859-15"),
                Charset.forName("KOI8-R"),
                Charset.forName("KOI8-U"),
                Charset.forName("US-ASCII"),
                defaultCharset,
                Charset.forName("UTF-16"),
                Charset.forName("UTF-16BE"),
                Charset.forName("UTF-16LE"),
                Charset.forName("UTF-32"),
                Charset.forName("UTF-32BE"),
                Charset.forName("UTF-32LE"),
                Charset.forName("x-UTF-32BE-BOM"),
                Charset.forName("x-UTF-32LE-BOM"),
                Charset.forName("windows-1250"),
                Charset.forName("windows-1251"),
                Charset.forName("windows-1252"),
                Charset.forName("windows-1253"),
                Charset.forName("windows-1254"),
                Charset.forName("windows-1257"),
                Charset.forName("UnicodeBig"),
                Charset.forName("x-IBM737"),
                Charset.forName("x-IBM874"),
                Charset.forName("x-UTF-16LE-BOM")
        );

        templateEncoding.getItems().addAll(charsetList);
        templateEncoding.getSelectionModel().select(defaultCharset);
        processListEncoding.getItems().addAll(charsetList);
        processListEncoding.getSelectionModel().select(defaultCharset);
        log.setEditable(false);
    }

    @FXML
    private void loadTemplate() {
        template = FileUtil.openDialog("Property files", "*.properties");
        if (template != null) {
            templateLabel.setText(template.getName());
        }
    }


    @FXML
    private void loadProcessed() {
        List<File> files = FileUtil.openMultiplyDialog("Property files", "*.properties");
        if (files != null) {

            processedList.getItems().clear();
            if (files.contains(template)){
                files = new ArrayList<>(files);
                files.remove(template);
                logger.info("remove from list template file: " + template.getName());
            }
            processedList.getItems().addAll(files);
        }
    }

    @FXML
    private void process() {
        final Properties properties = loadProperties(template, templateEncoding.getValue());
        final Charset value = processListEncoding.getValue();
        processedList.getItems().stream().parallel()
                .forEach(file -> new Core(properties, file, value).run());
    }

    private static Properties loadProperties(File template, Charset charset) {
        final Properties properties = new Properties();
        try (InputStreamReader inputStream = new InputStreamReader(new FileInputStream(template), charset)) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

}
