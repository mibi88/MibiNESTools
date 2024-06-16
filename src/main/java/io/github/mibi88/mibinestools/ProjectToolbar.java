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

import io.github.mibi88.mibinestools.chr_editor.ToolPanel;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 *
 * @author mibi88
 */
public class ProjectToolbar extends JToolBar {
    private JButton runButton;
    private JButton buildButton;
    private JButton cleanButton;
    private JButton settingsButton;

    /**
     * Create a new project toolbar.
     */
    public ProjectToolbar() {
        super("Project");
        setFloatable(false);
        setRollover(true);
        
        runButton = new JButton(getIcon("run.png"));
        runButton.setToolTipText("Run");
        buildButton = new JButton(getIcon("build.png"));
        buildButton.setToolTipText("Build");
        cleanButton = new JButton(getIcon("clean.png"));
        cleanButton.setToolTipText("Clean");
        settingsButton = new JButton(getIcon("settings.png"));
        settingsButton.setToolTipText("Project settings");
        
        add(runButton);
        add(buildButton);
        add(cleanButton);
        addSeparator();
        add(settingsButton);
    }
    
    private ImageIcon getIcon(String image) {
        try {
            return new ImageIcon(ImageIO
                    .read(ClassLoader.getSystemResource(image)));
        } catch (IOException ex) {
            Logger.getLogger(ToolPanel.class.getName()).log(
                    Level.SEVERE, null, ex);
            return new ImageIcon();
        }
    }
}
