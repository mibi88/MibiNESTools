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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 *
 * @author mibi88
 */
public class ToolPanel extends JPanel {
    private final int TOOL_AMOUNT = 4;
    private ButtonGroup buttonGroup;
    private JButton zoomIn;
    private JButton zoomOut;
    private JToggleButton penTool;
    private JToggleButton lineTool;
    
    private TileEditor tileEditor;
    public ToolPanel(TileEditor tileEditor) {
        super();
        setLayout(new GridLayout(TOOL_AMOUNT, 1));
        zoomIn = new JButton("Zoom In");
        zoomOut = new JButton("Zoom Out");
        penTool = new JToggleButton("Pen");
        lineTool = new JToggleButton("Line");
        buttonGroup = new ButtonGroup();
        buttonGroup.add(penTool);
        buttonGroup.add(lineTool);
        add(zoomIn);
        add(zoomOut);
        add(penTool);
        add(lineTool);
        
        this.tileEditor = tileEditor;
        addActions();
    }
    
    private void addActions() {
        zoomIn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tileEditor.zoomIn();
            }
        });
        zoomOut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tileEditor.zoomOut();
            }
        });
    }
}
