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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author mibi88
 */
public class TileEditor extends JPanel {
    private ToolPanel toolPanel;
    private JScrollPane canvasPane;
    private TileCanvas tileCanvas;
    private ColorPicker colorPicker;
    private int tx, ty;
    private byte[] oldData;
    public TileEditor(int scale, int[][] palette, byte currentColor,
            CHREditor editor) {
        super(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        
        toolPanel = new ToolPanel(this);
        tileCanvas = new TileCanvas(scale, 8,  8, palette, currentColor);
        tileCanvas.setEventHandler(new CanvasEvent() {
            @Override
            public void beforeChange() {
                oldData = tileCanvas.getData().clone();
            }
            @Override
            public void canvasUpdate(boolean end) {
                editor.updateTile(tileCanvas.getData(), tx, ty);
                if(end){
                    editor.addEdit(new CHREdit(editor, oldData,
                            tileCanvas.getData(), tx, ty));
                }
            }
        });
        canvasPane = new JScrollPane(tileCanvas);
        canvasPane.revalidate();
        colorPicker = new ColorPicker(palette, tileCanvas);
        
        c.fill = GridBagConstraints.VERTICAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0.90;
        add(toolPanel, c);
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.75;
        c.weighty = 0.75;
        add(canvasPane, c);
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 0.25;
        add(colorPicker, c);
    }
    
    public void reset() {
        tileCanvas.repaint();
    }
    
    public void zoomIn() {
        tileCanvas.zoomIn();
        canvasPane.revalidate();
    }
    
    public void zoomOut() {
        tileCanvas.zoomOut();
        canvasPane.revalidate();
    }
    
    public void loadTile(byte[] tile, int tx, int ty) {
        try {
            tileCanvas.loadData(tile);
            this.tx = tx;
            this.ty = ty;
        } catch (Exception ex) {
            Logger.getLogger(TileEditor.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }
    
    public void setPalette(int[][] palette) {
        colorPicker.updatePalette(palette);
        tileCanvas.setPalette(palette);
    }
    
    public int getTileX() {
        return tx;
    }
    
    public int getTileY() {
        return ty;
    }
    
    public byte[] getTile() {
        return tileCanvas.getData();
    }
}
