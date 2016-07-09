package com.artglorin.belprime.clearPropertyApp.utils;

import javafx.scene.control.TextArea;

/**
 * Created by V.Verminsky on 26.06.2016.
 */
public class Logger {

    final private TextArea textArea;

    public Logger(TextArea textArea) {
        this.textArea = textArea;
    }


    public void error(String text) {
        textArea.appendText(text + "\n");
    }

    public void clear(){
        textArea.clear();
    }

    public void info(String text, boolean newString) {
        if (newString) {
            textArea.appendText(text + "\n");
        } else {
            textArea.appendText(text);
        }
    }

    public void info(String s) {
        info(s, true);
    }
}
