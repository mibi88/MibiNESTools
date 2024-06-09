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
    
    int startX, startY;
    
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
        
        overlayLine = new Line(new DrawEvent() {
            @Override
            public void setPixel(int x, int y) {
                nametableViewer.setOverlayPixel(x, y, true);
            }
        });
        
        renderLine = new Line(new DrawEvent() {
            @Override
            public void setPixel(int x, int y) {
                nametableViewer.setTile(x, y);
            }
        });
        
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
                        System.out.println("line");
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
    
    private void addEdit(NametableEditor editor) {
        editor.addEdit(new NametableEdit(editor, oldAttributes, oldNametable,
                nametableViewer.getAttributes(),
                nametableViewer.getNametable()));
    }
    
    public void reset() {
        nametableViewer.reset();
    }
    
    public void setScale(int scale) {
        nametableViewer.setScale(scale);
        nametableViewerPane.revalidate();
    }
    
    public void open(File file) throws IOException {
        nametableViewer.open(file);
    }
    
    public void save(File file) throws IOException {
        nametableViewer.save(file);
    }
    
    public void setCHR(CHRData chrData) {
        nametableViewer.setCHR(chrData);
    }
    
    public void setCHRBank(int chrBank) {
        nametableViewer.setCHRBank(chrBank);
    }
    
    public void setCurrentTile(int currentTile) {
        nametableViewer.setCurrentTile(currentTile);
    }
    
    public void paletteChanged() {
        nametableViewer.repaint();
    }
    
    public Tool getCurrentTool() {
        return nametableToolbar.getCurrentTool();
    }
    
    public void setData(byte[] nametable, byte[] attributes) {
        nametableViewer.setData(nametable, attributes);
    }
    
    public byte[] getSelection() {
        return nametableViewer.getSelection();
    }
    
    public int getSelectionW() {
        return nametableViewer.getSelectionW();
    }
    
    public int getSelectionH() {
        return nametableViewer.getSelectionH();
    }
    
    public void fillSelection(byte[] data, int w, int h) {
        nametableViewer.fillSelection(data, w, h);
    }
    
    public void fillSelection(int tile) {
        nametableViewer.fillSelection(tile);
    }
    
    public byte[] getNametable() {
        return nametableViewer.getNametable();
    }
    
    public byte[] getAttributes() {
        return nametableViewer.getAttributes();
    }
}
