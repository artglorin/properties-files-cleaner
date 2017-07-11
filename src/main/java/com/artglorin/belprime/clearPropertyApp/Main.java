package com.artglorin.belprime.clearPropertyApp;


import com.artglorin.belprime.clearPropertyApp.controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by V.Verminsky on 06.07.2016.
 */
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        @SuppressWarnings("ConstantConditions")
        final FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/main.fxml"));
        final AnchorPane anchorPane = loader.load();
        primaryStage.setScene(new Scene(anchorPane));
        final MainController controller = loader.getController();
        anchorPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                controller.getList().getItems().removeAll(controller.getList().getSelectionModel().getSelectedItems());
            }
        });
        primaryStage.show();
    }
}
