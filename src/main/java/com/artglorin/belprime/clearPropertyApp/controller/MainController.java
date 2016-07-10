package com.artglorin.belprime.clearPropertyApp.controller;

import com.artglorin.belprime.clearPropertyApp.common.Core;
import com.artglorin.belprime.clearPropertyApp.utils.FileUtil;
import javafx.fxml.FXML;
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
        final Properties properties = loadProperties(template);
        processedList.getItems().stream().parallel()
                .forEach(file -> new Core(properties, file, Charset.forName("Windows-1251")).run());
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

}
