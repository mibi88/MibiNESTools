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
package io.github.mibi88.mibinestools.chr_editor;

import io.github.mibi88.mibinestools.palette_editor.ColorPicker;
import io.github.mibi88.mibinestools.DrawEvent;
import io.github.mibi88.mibinestools.Line;
import io.github.mibi88.mibinestools.Rectangle;
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
    private Line overlayLine;
    private Line renderLine;
    private Rectangle overlayRectangle;
    private Rectangle renderRectangle;
    private int startX, startY;

    /**
     * Create a new tile editor.
     * @param scale The scale of the content.
     * @param palette The palette to use.
     * @param currentColor The currently selected color.
     * @param editor The CHR editor to use this tile editor with.
     */
    public TileEditor(int scale, int[][] palette, byte currentColor,
            CHREditor editor) {
        super(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        
        toolPanel = new ToolPanel(this);
        tileCanvas = new TileCanvas(scale, 8,  8, palette, currentColor);
        initShapes();
        tileCanvas.setEventHandler(new CanvasEvent() {
            @Override
            public void beforeChange(int x, int y) {
                oldData = tileCanvas.getData().clone();
                startX = x;
                startY = y;
            }
            @Override
            public void canvasUpdate(int x, int y, boolean end) {
                switch(toolPanel.getCurrentTool()){
                    case PEN:
                        tileCanvas.setPixel(x, y,
                                tileCanvas.getCurrentColor());
                        break;
                    case LINE:
                        if(end){
                            tileCanvas.clearOverlay();
                            renderLine.drawLine(startX, startY, x,
                                    y);
                        }else{
                            tileCanvas.clearOverlay();
                            overlayLine.drawLine(startX, startY, x,
                                    y);
                        }
                        break;
                    case RECTANGLE:
                        if(end){
                            tileCanvas.clearOverlay();
                            renderRectangle.drawRectangle(startX, startY,
                                    x, y);
                        }else{
                            tileCanvas.clearOverlay();
                            overlayRectangle.drawRectangle(startX, startY,
                                    x, y);
                        }
                        break;
                }
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
    
    private void initShapes() {
        DrawEvent renderEvent = new DrawEvent() {
            @Override
            public void setPixel(int x, int y) {
                tileCanvas.setPixel(x, y, tileCanvas.getCurrentColor());
            }
        };
        DrawEvent overlayEvent = new DrawEvent() {
            @Override
            public void setPixel(int x, int y) {
                tileCanvas.setOverlayPixel(x, y, true);
            }
        };
        overlayLine = new Line(overlayEvent);
        renderLine = new Line(renderEvent);
        overlayRectangle = new Rectangle(overlayEvent);
        renderRectangle = new Rectangle(renderEvent);
    }
    
    /**
     * Reset the tile editor.
     */
    public void reset() {
        tileCanvas.repaint();
    }
    
    /**
     * Increase the scale of the content.
     */
    public void zoomIn() {
        tileCanvas.zoomIn();
        canvasPane.revalidate();
    }
    
    /**
     * Decrease the scale of the content.
     */
    public void zoomOut() {
        tileCanvas.zoomOut();
        canvasPane.revalidate();
    }
    
    /**
     * Load a tile in the tile editor.
     * @param tile The tile to load.
     * @param tx The tile position in the pattern table.
     * @param ty The tile position in the pattern table.
     */
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
    
    /**
     * Set the palette to use to display the tile.
     * @param palette The palette to use to display the tile.
     */
    public void setPalette(int[][] palette) {
        colorPicker.updatePalette(palette);
        tileCanvas.setPalette(palette);
    }
    
    /**
     * Get the tile position on the X axis in the pattern table.
     * @return The position of the tile on the X axis.
     */
    public int getTileX() {
        return tx;
    }
    
    /**
     * Get the tile position on the Y axis in the pattern table.
     * @return The position of the tile on the Y axis.
     */
    public int getTileY() {
        return ty;
    }
    
    /**
     * Get tile that is currently edited in this tile editor
     * @return The tile data.
     */
    public byte[] getTile() {
        return tileCanvas.getData();
    }
}
