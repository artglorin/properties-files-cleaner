package com.artglorin.belprime.clearPropertyApp.controller;

import com.artglorin.belprime.clearPropertyApp.common.Core;
import com.artglorin.belprime.clearPropertyApp.utils.FileUtil;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;
import rx.internal.schedulers.NewThreadScheduler;
import rx.internal.util.RxThreadFactory;

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

    private ObjectProperty<File> template = new SimpleObjectProperty<>();

    @FXML
    private ListView<File> processedList;

    @FXML
    private Button processListButton;

    @FXML
    private Button processButton;

    @FXML
    private ComboBox<Charset> templateEncoding;

    @FXML
    private ComboBox<Charset> processListEncoding;

    @FXML
    private Label templateLabel;

    private boolean lockButtons;


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
        processListButton.disableProperty().bind(template.isNull());
        templateLabel.setText("Файл образец ещё не выбран");
        processedList.setPlaceholder(new Label("Файлы для обработки не выбраны."));
    }

    @FXML
    private void loadTemplate() {
        if (lockButtons) {
            return;
        }
        final File file = FileUtil.openDialog("Property files", "*.properties");
        if (file != null) {
            template.setValue(file);
            templateLabel.setText("Образец файла свойств: " + template.getValue().getName());
        }
    }

    @FXML
    private void loadProcessed() {
        if (lockButtons) {
            return;
        }
        List<File> files = FileUtil.openMultiplyDialog("Property files", "*.properties");
        if (files != null) {
            processedList.getItems().clear();
            if (files.contains(template.getValue())){
                files = new ArrayList<>(files);
                files.remove(template.getValue());
                logger.info("remove from list template file: " + template.getName());
            }
            processedList.getItems().addAll(files);
            processButton.disableProperty().setValue(false);
        }
    }

    @FXML
    private void process() {
        if (lockButtons) {
            return;
        }
        lockButtons = true;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Началась обработка");
        alert.setHeaderText(null);
        alert.show();
        final Properties properties = loadProperties(template.getValue(), templateEncoding.getValue());
        final Charset value = processListEncoding.getValue();
        IntegerProperty property = new SimpleIntegerProperty(processedList.getItems().size());
        Observable.from(processedList.getItems())
                .subscribeOn(new NewThreadScheduler(new RxThreadFactory("process")))
                .doOnNext(file -> {
                    property.setValue(property.subtract(1).get());
                    Platform.runLater(() -> {
                        alert.setContentText(property.getValue().toString());
                    });
                })
                .doOnCompleted(() -> {
                    lockButtons = false;
                    Platform.runLater(alert::close);
                })
                .subscribe(file -> {
                    new Core(properties, file, value).run();
                });
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
