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
package io.github.mibi88.mibinestools.nametable_editor;

import io.github.mibi88.mibinestools.DrawEvent;
import io.github.mibi88.mibinestools.Line;
import io.github.mibi88.mibinestools.Rectangle;
import io.github.mibi88.mibinestools.Tool;
import io.github.mibi88.mibinestools.chr_editor.CHRData;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author mibi88
 */
public class NametablePane extends JPanel {
    private NametableViewer nametableViewer;
    private JScrollPane nametableViewerPane;
    private NametableToolbar nametableToolbar;
    
    private byte[] oldAttributes;
    private byte[] oldNametable;
    
    private Line overlayLine;
    private Line renderLine;
    private Rectangle overlayRectangle;
    private Rectangle renderRectangle;
    
    int startX, startY;
    
    /**
     * Create a nametable pane.
     * @param editor The editor to create it for.
     */
    public NametablePane(NametableEditor editor){
        super(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        nametableToolbar = new NametableToolbar();
        nametableViewer = new NametableViewer(editor.getCHRData(),
                editor.getPaletteEditor(), editor.getScale(),
                true);
        nametableViewerPane = new JScrollPane(nametableViewer);
        
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.VERTICAL;
        c.weighty = 1;
        c.weightx = 0;
        add(nametableToolbar, c);
        c.gridx++;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        add(nametableViewerPane, c);
        
        initShapes();
        
        nametableViewer.setEventHandler(new NametableViewerEvent() {
            @Override
            public void beforeChange(int tx, int ty) {
                oldNametable = nametableViewer.getNametable().clone();
                oldAttributes = nametableViewer.getAttributes().clone();
                startX = tx;
                startY = ty;
                nametableViewer.clearOverlay();
                return;
            }
            
            @Override
            public void tileChanged(int tx, int ty, boolean end) {
                switch(getCurrentTool()){
                    case SELECTION:
                        if(tx == startX && ty == startY){
                            nametableViewer.setSelection(startX,
                                    startY, tx+1, ty+1);
                        }else{
                            nametableViewer.setSelection(startX
                                    +(tx < startX ? 1 : 0),
                                    startY+(ty < startY ? 1 : 0),
                                    tx < startX ? tx : tx+1,
                                    ty < startY ? ty : ty+1);
                        }
                        break;
                    case COLOR:
                        nametableViewer.setPalette(tx, ty,
                                editor.getPaletteEditor()
                                        .getCurrentPaletteIndex());
                        break;
                    case PEN:
                        nametableViewer.setTile(tx, ty);
                        break;
                    case LINE:
                        if(end){
                            nametableViewer.clearOverlay();
                            renderLine.drawLine(startX, startY,
                                    tx, ty);
                        }else{
                            nametableViewer.clearOverlay();
                            overlayLine.drawLine(startX, startY,
                                    tx, ty);
                            nametableViewer.repaint();
                        }
                        break;
                    case RECTANGLE:
                        if(end){
                            nametableViewer.clearOverlay();
                            renderRectangle.drawRectangle(startX, startY,
                                    tx, ty);
                        }else{
                            nametableViewer.clearOverlay();
                            overlayRectangle.drawRectangle(startX, startY,
                                    tx, ty);
                            nametableViewer.repaint();
                        }
                        break;
                }
                if(getCurrentTool() != Tool.SELECTION){
                    if(end){
                        addEdit(editor);
                    }
                    editor.fileEdited();
                }
            }
        });
    }
    
    private void initShapes() {
        DrawEvent overlayEvent = new DrawEvent() {
            @Override
            public void setPixel(int x, int y) {
                nametableViewer.setOverlayPixel(x, y, true);
            }
        };
        DrawEvent renderEvent = new DrawEvent() {
            @Override
            public void setPixel(int x, int y) {
                nametableViewer.setTile(x, y);
            }
        };
        
        overlayLine = new Line(overlayEvent);
        renderLine = new Line(renderEvent);
        overlayRectangle = new Rectangle(overlayEvent);
        renderRectangle = new Rectangle(renderEvent);
    }
    
    private void addEdit(NametableEditor editor) {
        editor.addEdit(new NametableEdit(editor, oldAttributes, oldNametable,
                nametableViewer.getAttributes(),
                nametableViewer.getNametable()));
    }
    
    /**
     * Reset the nametable pane.
     */
    public void reset() {
        nametableViewer.reset();
    }
    
    /**
     * Set the scale of the content.
     * @param scale The scale.
     */
    public void setScale(int scale) {
        nametableViewer.setScale(scale);
        nametableViewerPane.revalidate();
    }
    
    /**
     * Open a nametable from a file.
     * @param file The file to open the nametable from.
     * @throws IOException Gets thrown on failure.
     */
    public void open(File file) throws IOException {
        nametableViewer.open(file);
    }
    
    /**
     * Save the nametable to a file.
     * @param file The file to open the nametable from.
     * @throws IOException Gets thrown on failure.
     */
    public void save(File file) throws IOException {
        nametableViewer.save(file);
    }
    
    /**
     * Set the CHR data to use.
     * @param chrData The CHR data to use.
     */
    public void setCHR(CHRData chrData) {
        nametableViewer.setCHR(chrData);
    }
    
    /**
     * Set the number of the CHR bank that should be used to display the
     * nametable.
     * @param chrBank The number of the CHR bank.
     */
    public void setCHRBank(int chrBank) {
        nametableViewer.setCHRBank(chrBank);
    }
    
    /**
     * Set the tile to use for drawing on the nametable.
     * @param currentTile The tile to use for drawing.
     */
    public void setCurrentTile(int currentTile) {
        nametableViewer.setCurrentTile(currentTile);
    }
    
    /**
     * Update the nametable viewer after changing the palette.
     */
    public void paletteChanged() {
        nametableViewer.repaint();
    }
    
    /**
     * Get the currently selected tool.
     * @return Returns the selected tool.
     */
    public Tool getCurrentTool() {
        return nametableToolbar.getCurrentTool();
    }
    
    /**
     * Load a nametable from byte arrays.
     * @param nametable The nametable.
     * @param attributes The attribute table.
     */
    public void setData(byte[] nametable, byte[] attributes) {
        nametableViewer.setData(nametable, attributes);
    }
    
    /**
     * Get the content of the selection.
     * @return The selected tiles.
     */
    public byte[] getSelection() {
        return nametableViewer.getSelection();
    }
    
    /**
     * Get the width of the selection.
     * @return The width of the selection.
     */
    public int getSelectionW() {
        return nametableViewer.getSelectionW();
    }
    
    /**
     * Get the height of the selection.
     * @return The height of the selection.
     */
    public int getSelectionH() {
        return nametableViewer.getSelectionH();
    }
    
    /**
     * Fill the selection with data.
     * @param data The data to load into the selection.
     * @param w The width of the area to load.
     * @param h The height of the area to load.
     */
    public void fillSelection(byte[] data, int w, int h) {
        nametableViewer.fillSelection(data, w, h);
    }
    
    /**
     * Fill the selection with a tile.
     * @param tile The tile to fill the selection with.
     */
    public void fillSelection(int tile) {
        nametableViewer.fillSelection(tile);
    }
    
    /**
     * Get the nametable data.
     * @return The nametable data.
     */
    public byte[] getNametable() {
        return nametableViewer.getNametable();
    }
    
    /**
     * Get the attribute table.
     * @return The attribute table data.
     */
    public byte[] getAttributes() {
        return nametableViewer.getAttributes();
    }
    
    /**
     * Set if the grid should be displayed.
     * @param grid True if the grid should be drawn.
     */
    public void setGrid(boolean grid) {
        nametableViewer.setGrid(grid);
    }
}
