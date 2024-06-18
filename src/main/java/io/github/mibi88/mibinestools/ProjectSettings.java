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

import java.awt.ComponentOrientation;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 *
 * @author mibi88
 */
public class ProjectSettings extends JDialog {
    private JTabbedPane tabs;
    private FileList fileList;
    private JPanel buttons;
    
    private JButton applyButton;
    private JButton okButton;
    private JButton cancelButton;

    /**
     * Create a new project settings dialog.
     * @param window The window to use with it.
     */
    public ProjectSettings(Window window) {
        super(window);
        setLayout(new GridBagLayout());
        setSize(320, 240);
        tabs = new JTabbedPane();
        
        fileList = new FileList(window);
        tabs.addTab("Source files", fileList);
        
        buttons = new JPanel();
        
        applyButton = new JButton("Apply");
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                window.saveProjectSettings(ProjectSettings.this);
            }
        });
        okButton = new JButton("Ok");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                window.saveProjectSettings(ProjectSettings.this);
                dispose();
            }
        });
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        buttons.add(applyButton);
        buttons.add(okButton);
        buttons.add(cancelButton);
        
        GridBagConstraints c = new GridBagConstraints();
        
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        add(tabs, c);
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 0;
        add(buttons, c);
        setVisible(true);
    }
    
    /**
     * Get the source files.
     * @return Returns the source files.
     */
    public FileItem[] getSourceFiles() {
        return fileList.getFiles();
    }
}
