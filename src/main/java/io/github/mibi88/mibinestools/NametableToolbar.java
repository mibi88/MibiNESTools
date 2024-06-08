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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

/**
 *
 * @author mibi88
 */
public class NametableToolbar extends JToolBar {
    private JToggleButton selection;
    private JToggleButton color;
    private JToggleButton pen;
    private JToggleButton rectangle;
    private JToggleButton line;
    private ButtonGroup buttonGroup;
    private Tool currentTool;
    public NametableToolbar() {
        super("Tools", JToolBar.VERTICAL);
        setFloatable(false);
        setRollover(true);
        
        currentTool = Tool.SELECTION;
        selection = new JToggleButton(getIcon("selection.png"));
        selection.setToolTipText("Selection");
        color = new JToggleButton(getIcon("color.png"));
        color.setToolTipText("Color");
        pen = new JToggleButton(getIcon("pen.png"));
        pen.setToolTipText("Pen");
        rectangle = new JToggleButton(getIcon("rectangle.png"));
        rectangle.setToolTipText("Rectangle");
        line = new JToggleButton(getIcon("line.png"));
        line.setToolTipText("Line");
        buttonGroup = new ButtonGroup();
        buttonGroup.add(selection);
        buttonGroup.add(color);
        buttonGroup.add(pen);
        buttonGroup.add(rectangle);
        buttonGroup.add(line);
        add(selection);
        add(color);
        add(pen);
        add(rectangle);
        add(line);
        selection.setSelected(true);
        addActions();
    }
    
    private void addActions() {
        selection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentTool = Tool.SELECTION;
            }
        });
        color.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentTool = Tool.COLOR;
            }
        });
        pen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentTool = Tool.PEN;
            }
        });
        rectangle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentTool = Tool.RECTANGLE;
            }
        });
        line.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentTool = Tool.LINE;
            }
        });
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
    
    public Tool getCurrentTool() {
        return currentTool;
    }
}
