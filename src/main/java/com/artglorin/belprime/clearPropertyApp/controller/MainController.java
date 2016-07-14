package com.artglorin.belprime.clearPropertyApp.controller;

import com.artglorin.belprime.clearPropertyApp.common.Core;
import javafx.application.Platform;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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

import static com.artglorin.javaFxUtil.JavaFxFileDialogUtil.openDialog;
import static com.artglorin.javaFxUtil.JavaFxFileDialogUtil.openMultiplyDialog;

/**
 * Created by V.Verminsky on 06.07.2016.
 */
public class MainController {

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
        final File file = openDialog("Property files", "*.properties");
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
        List<File> files = openMultiplyDialog("Property files", "*.properties");
        if (files != null) {
            processedList.getItems().clear();
            if (files.contains(template.getValue())){
                files = new ArrayList<>(files);
                files.remove(template.getValue());
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
        final ProgressIndicator  progressBar = new ProgressIndicator(-1);

        final BorderPane borderPane = new BorderPane();
        borderPane.setCenter(progressBar);
        borderPane.backgroundProperty().setValue(Background.EMPTY);
        Stage stage = new Stage(StageStyle.TRANSPARENT);
        final Scene scene = new Scene(borderPane, 200, 200);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
        final float increment = 1f / processedList.getItems().size();
        final Properties progress = loadProperties(template.getValue(), templateEncoding.getValue());
        final Charset value = processListEncoding.getValue();
        FloatProperty property = new SimpleFloatProperty(0);
        Observable.from(processedList.getItems())
                .subscribeOn(new NewThreadScheduler(new RxThreadFactory("process")))
                .doOnNext(file -> {
                    property.setValue(property.add(increment).get());
                    Platform.runLater(() -> progressBar.setProgress(property.floatValue()));
                })
                .doOnCompleted(() -> {
                    lockButtons = false;
                    Platform.runLater(stage::close);
                })
                .subscribe(file -> {
                    new Core(progress, file, value).run();
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
