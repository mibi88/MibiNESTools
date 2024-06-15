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
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import javax.swing.JOptionPane;

/**
 *
 * @author mibi88
 */
public class PluginLoader {

    /**
     * Load a plugin.
     * @param file The properties file of the plugin.
     * @param window The window used with this plugin.
     * @return The Editor class of the plugin.
     * @throws Exception Thrown on failure.
     */
    public static Class loadPlugin(File file, Window window) throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream(file));
        String targetVersion = properties.getProperty("targetVersion");
        if(!getVersion().equals(targetVersion)){
            JOptionPane.showMessageDialog(window,
                    "This plugin can't be loaded!\n"
                    + "Version " + targetVersion + " required.");
            throw new Exception("Bad version!");
        }
        String folder = properties.getProperty("folder");
        File classFolder = new File(file.getParentFile(), folder);
        URLClassLoader classLoader = new URLClassLoader(
                new URL[]{classFolder.toURI().toURL()});
        
        Class editorClass = classLoader
                .loadClass(properties.getProperty("editorClass"));
        return editorClass;
    }
    
    private static String getVersion() throws IOException {
        Properties properties = new Properties();
        properties.load(PluginLoader.class.getClassLoader()
                .getResourceAsStream("project.properties"));
        return properties.getProperty("version");
    }
}
