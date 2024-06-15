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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author mibi88
 */
public class ClosableTab extends JPanel {
    private JTabbedPane tabbedPane;
    private Editor editor;
    private CloseEvent event;

    /**
     * Create a new closable tab.
     * @param tabbedPane The tabbed pane to use with this tab.
     * @param editor The editor used with this tab.
     */
    public ClosableTab(JTabbedPane tabbedPane, Editor editor) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setOpaque(false);
        this.tabbedPane = tabbedPane;
        this.editor = editor;
        JLabel label = new JLabel() {
            @Override
            public String getText() {
                String text = "Unknown title";
                int index = tabbedPane.indexOfTabComponent(
                        ClosableTab.this);
                if(index >= 0){
                    text = tabbedPane.getTitleAt(index);
                }
                setText(text);
                return text;
            }
        };
        label.setBorder(BorderFactory
                .createEmptyBorder(0, 0, 0, 5));
        add(label);
        JButton closeButton = new JButton(getIcon("cross.png"));
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeTab();
            }
        });
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setPreferredSize(new Dimension(16, 16));
        closeButton.setBorder(BorderFactory
                .createEmptyBorder(3, 0, 0, 0));
        closeButton.setFocusable(false);
        add(closeButton);
    }
    
    /**
     * Set the event handler to handle tab closing.
     * @param event The event handler.
     */
    public void setEventHandler(CloseEvent event) {
        this.event = event;
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
    
    private void closeTab() {
        if(!editor.getFileSaved()){
            String fileName = editor.getFileName();
            JOptionPane askToQuit = new JOptionPane();
            int selected = askToQuit.showConfirmDialog(this,
                            fileName + " is not saved!\n"
                                    + "Do you really want to close it?",
                            "Unsaved changes",
                            JOptionPane.YES_NO_OPTION);
            if(selected != JOptionPane.OK_OPTION
                    || askToQuit.getValue() == null){
                return;
            }
        }
        int index = tabbedPane.indexOfTabComponent(ClosableTab.this);
        if(index >= 0){
            tabbedPane.remove(index);
            if(event != null){
                event.tabClosed(index);
                event.tabClosed(editor);
            }
        }
    }
}
