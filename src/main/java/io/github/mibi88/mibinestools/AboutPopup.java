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

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author mibi88
 */
public class AboutPopup extends JOptionPane {
    String version;
    String artifactId;

    /**
     * Create a new about popup.
     */
    public AboutPopup() {
        super();
        version = "Unknown version";
        artifactId = "Unknown artifactId";
        Properties properties = new Properties();
        try {
            properties.load(this.getClass().getClassLoader()
                    .getResourceAsStream("project.properties"));
            version = properties.getProperty("version");
            artifactId = properties.getProperty("artifactId");
        } catch (IOException ex) {
            Logger.getLogger(AboutPopup.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Show the about popup.
     * @param window The editor window.
     */
    public void show(Window window) {
        showMessageDialog(window, "MibiNESTools\n"
                + "Version: " + version + "\nArtifactId: " + artifactId);
    }
}
