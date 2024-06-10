/*
 * MibiNESTools - Create NES games easily!
 * Copyright (C) 2024  Mibi88
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */
package io.github.mibi88.mibinestools;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

/**
 *
 * @author mibi88
 */
public class Plugin extends Editor {
    URLClassLoader classLoader;
    Object editor;
    Class editorClass;

    /**
     * Load a plugin.
     * @param file The file that contains the plugin properties.
     * @throws Exception Gets thrown if the loading fails.
     */
    public Plugin(File file) throws Exception {
        super("Plugin");
        Properties properties = new Properties();
        properties.load(new FileInputStream(file));
        String folder = properties.getProperty("folder");
        File classFolder = new File(file.getParentFile(), folder);
        classLoader = new URLClassLoader(
                new URL[]{classFolder.toURI().toURL()});
        
        editorClass = classLoader
                .loadClass(properties.getProperty("editorClass"));
        Constructor c = editorClass.getConstructor(JPanel.class);
        editor = c.newInstance(this);
        setEditorName(properties.getProperty("name"));
    }
    
    /**
     * Finish the initialization of the plugin.
     */
    public void init_end() {
        updateTitle();
    }
    
    private Object callMethod(String methodName) {
        try {
            Method method = editorClass.getDeclaredMethod(methodName);
            method.setAccessible(true);
            return method.invoke(editor);
        } catch (Exception ex) {
            Logger.getLogger(Plugin.class.getName()).log(Level.SEVERE,
                    null, ex);
        }
        return null;
    }
    
    private Object callMethod(String methodName, Class<?> type, Object arg) {
        try {
            Method method = editorClass.getDeclaredMethod(methodName,
                    type);
            method.setAccessible(true);
            return method.invoke(editor, arg);
        } catch (Exception ex) {
            Logger.getLogger(Plugin.class.getName()).log(Level.SEVERE,
                    null, ex);
        }
        return null;
    }
    
    /**
     * Create a new file.
     * @return Returns true if the new file was created.
     */
    @Override
    public boolean newFile() {
        if(super.newFile()){
            try {
                return (boolean)callMethod("newFile");
            } catch (Exception ex) {
                Logger.getLogger(Plugin.class.getName()).log(
                        Level.SEVERE, null, ex);
                error();
            }
        }
        return false;
    }
    
    /**
     * Open a file.
     * @param file The file to open.
     * @return Returns true if the file was opened.
     */
    @Override
    public boolean openFile(File file) {
        if(super.openFile(file)){
            try {
                return (boolean)callMethod("openFile", File.class,
                        file);
            } catch (Exception ex) {
                Logger.getLogger(Plugin.class.getName()).log(
                        Level.SEVERE, null, ex);
                error();
            }
        }
        return false;
    }
    
    /**
     * Save the currently opened file.
     */
    @Override
    public void saveFile() {
        callMethod("saveFile");
        super.saveFile();
    }
    
    /**
     * Save the file as.
     * @param file The file to save to.
     */
    @Override
    public void saveAsFile(File file) {
        super.saveAsFile(file);
        callMethod("saveAsFile", File.class, file);
    }
    
    /**
     * Undo the last action.
     */
    @Override
    public void undo() {
        callMethod("undo");
    }
    
    /**
     * Redo the last action.
     */
    @Override
    public void redo() {
        callMethod("redo");
    }
    
    /**
     * Copy some data.
     */
    @Override
    public void copy() {
        callMethod("copy");
    }
    
    /**
     * Cut some data.
     */
    @Override
    public void cut() {
        callMethod("cut");
    }
    
    /**
     * Paste some data.
     */
    @Override
    public void paste() {
        callMethod("paste");
    }
    
    /**
     * Set the scale of the content.
     * @param scale The scale.
     */
    @Override
    public void setScale(int scale) {
        callMethod("setScale", Integer.TYPE, scale);
    }
    
    /**
     * Enable or disable the grid.
     * @param grid True if the grid should be enabled.
     */
    @Override
    public void setGrid(boolean grid) {
        callMethod("setGrid", Boolean.TYPE, grid);
    }
}
