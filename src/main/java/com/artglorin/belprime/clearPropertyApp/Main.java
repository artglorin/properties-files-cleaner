package com.artglorin.belprime.clearPropertyApp;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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
        final AnchorPane anchorPane = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/main.fxml"));
        primaryStage.setScene(new Scene(anchorPane));
        primaryStage.show();
    }
}
