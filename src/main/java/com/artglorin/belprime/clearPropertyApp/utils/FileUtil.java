package com.artglorin.belprime.clearPropertyApp.utils;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import sun.awt.AppContext;

import java.io.File;
import java.util.List;

/**
 * Created by V.Verminsky on 27.06.2016.
 */
public class FileUtil {

    private static File lastDir;

    private FileUtil() {
    }


    public static File openDialog() {
        final File file = getFileChooser().showOpenDialog(getMainWindow());
        if (file != null) {
            lastDir = file.getParentFile();
        }
        return file;
    }

    public static File openDialog(String description, String extension) {
        final FileChooser fileChooser = getFileChooser();
        fileChooser.getExtensionFilters().add(getFilter(description, extension));
        final File file = fileChooser.showOpenDialog(getMainWindow());
        if (file != null) {
            lastDir = file.getParentFile();
        }
        return file;
    }

    private static Window getMainWindow() {
        return (Window) AppContext.getAppContext().get("mainWindow");
    }

    private static FileChooser getFileChooser() {
        final FileChooser fileChooser = new FileChooser();
        if (lastDir != null) {
            fileChooser.setInitialDirectory(lastDir);
        } else {
            final String filePath = System.getProperty("user.dir");
            fileChooser.setInitialDirectory(new File(filePath));
        }
        return fileChooser;
    }

    public static List<File> openMultiplyDialog() {
        final FileChooser fileChooser = getFileChooser();
        final List<File> files = fileChooser.showOpenMultipleDialog(getMainWindow());
        if (files != null) {
            lastDir = files.get(0).getParentFile();
        }
        return files;
    }

    public static List<File> openMultiplyDialog(String description, String extension) {
        final FileChooser fileChooser = getFileChooser();
        fileChooser.getExtensionFilters().add(getFilter(description, extension));

        final List<File> files = fileChooser.showOpenMultipleDialog(getMainWindow());
        if (files != null) {
            lastDir = files.get(0).getParentFile();
        }
        return files;
    }

    public static List<File> openMultiplyDialog(String description, String extension, File exclude) {
        final FileChooser fileChooser = getFileChooser();
        fileChooser.getExtensionFilters().add(getFilter(description, extension));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(exclude.getName(), exclude.getName()));
        final List<File> files = fileChooser.showOpenMultipleDialog(getMainWindow());
        if (files != null) {
            lastDir = files.get(0).getParentFile();
        }
        return files;
    }

    public static File showSaveDialog() {
        final File file = getFileChooser().showSaveDialog(getMainWindow());
        if (file != null) {
            lastDir = file.getParentFile();
        }
        return file;
    }

    public static File showSaveDialog(String description, String extension) {
        final FileChooser fileChooser = getFileChooser();
        fileChooser.getExtensionFilters().add(getFilter(description, extension));
        final File file = fileChooser.showSaveDialog(getMainWindow());
        if (file != null) {
            lastDir = file.getParentFile();
        }
        return file;
    }

    private static FileChooser.ExtensionFilter getFilter(String description,String extension) {
        return new FileChooser.ExtensionFilter(description, extension);
    }
}
