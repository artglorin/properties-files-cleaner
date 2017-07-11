package com.artglorin.belprime.clearPropertyApp.controller;

import com.artglorin.belprime.clearPropertyApp.common.Core;
import com.artglorin.belprime.clearPropertyApp.common.Util;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import rx.Observable;
import rx.internal.schedulers.NewThreadScheduler;
import rx.internal.util.RxThreadFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.artglorin.belprime.clearPropertyApp.common.Util.loadProperties;
import static com.artglorin.javaFxUtil.JfxFileDialogUtil.openDialog;
import static com.artglorin.javaFxUtil.JfxFileDialogUtil.openMultiplyDialog;

/**
 * Created by V.Verminsky on 06.07.2016.
 */
public class MainController {

    private static final String PROPERTY_EXTENSION = ".properties";
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
        lockButtons = true;
        final File file = openDialog("Property files", "*" + PROPERTY_EXTENSION);
        if (file != null) {
            template.setValue(file);
            templateLabel.setText("Образец файла свойств: " + template.getValue().getName());
            final String templateName = file.getName();
            final int startExtensionIndex = templateName.lastIndexOf(PROPERTY_EXTENSION);
            if (startExtensionIndex > 0) {
                final String propertyName = templateName.substring(0, startExtensionIndex);
                Optional.ofNullable(file.getParentFile().listFiles((dir, name) -> name.startsWith(propertyName + "_") && name.endsWith(PROPERTY_EXTENSION)))
                        .map(Arrays::asList).ifPresent(this::loadFilesToProcess);
            }

        }
        lockButtons = false;
    }

    @FXML
    private void loadProcessed() {
        if (lockButtons) {
            return;
        }
        lockButtons = true;
        List<File> files = openMultiplyDialog("Property files", "*" + PROPERTY_EXTENSION);
        loadFilesToProcess(files);
        lockButtons = false;
    }

    private void loadFilesToProcess(List<File> files) {
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
    private void process() throws IOException {
        if (lockButtons) {
            return;
        }
        lockButtons = true;
        final GridPane pane;
        ProgressIndicator  progressBar;
        //noinspection ConstantConditions
        pane = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/progress.fxml"));
        progressBar = (ProgressIndicator) pane.lookup("#pid");
        final Scene scene = new Scene(pane, 200, 200);
        Stage stage = new Stage(StageStyle.TRANSPARENT);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        final Window owner = processButton.getScene().getWindow();
        stage.setX(owner.getX() + owner.getWidth() / 2 - scene.getWidth() / 2);
        stage.setY(owner.getY() + owner.getHeight() / 2 - scene.getHeight() / 2);
        stage.show();
        final float increment = 1f / processedList.getItems().size();
        try {
            doStuff(progressBar, stage, increment, loadProperties(template.getValue().toPath(), templateEncoding.getValue()),
                    processListEncoding.getValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doStuff(ProgressIndicator progressBar,
                         Stage stage,
                         float increment,
                         Properties templatePropertyFile,
                         Charset value) {
        final SimpleFloatProperty progressPercent = new SimpleFloatProperty(0);
        Observable.from(processedList.getItems())
                  .subscribeOn(new NewThreadScheduler(new RxThreadFactory("process")))
                  .doOnNext(file -> {
                      progressPercent.setValue(progressPercent.add(increment).get());
                      Platform.runLater(() -> progressBar.setProgress(progressPercent.floatValue()));
                  })
                  .doOnCompleted(() -> {
                      lockButtons = false;
                      Platform.runLater(stage::close);
                  })
                  .doOnTerminate(() -> {
                      try {
                          Files.copy(template.getValue().toPath(),
                                     Paths.get(Util.getOutputDirectory(template.getValue().getParent()).toString(), template.getValue().getName()));
                      } catch (IOException e) {
                          e.printStackTrace();
                      }
                  })
                  .subscribe(file -> {
                      final Properties threadInstance = new Properties();
                      threadInstance.putAll(templatePropertyFile.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                      new Core(threadInstance, file, value).run();
                  });
    }

}
