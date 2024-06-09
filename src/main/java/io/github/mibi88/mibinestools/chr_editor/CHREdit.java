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

import javax.swing.undo.AbstractUndoableEdit;

/**
 * This class stores an action made in the CHR editor.
 * @author mibi88
 */
public class CHREdit extends AbstractUndoableEdit {
    private byte[] tileData;
    private byte[] newData;
    private int tx, ty;
    private CHREditor editor;

    /**
     * Create a new edit.
     * @param editor The CHR editor used.
     * @param tileData The data of the tile before the edition.
     * @param newData The new tile data.
     * @param tx The position of the tile in the pattern table.
     * @param ty The position of the tile in the pattern table.
     */
    public CHREdit(CHREditor editor, byte[] tileData, byte[] newData, int tx,
            int ty) {
        super();
        this.tileData = tileData.clone();
        this.newData = newData.clone();
        this.tx = tx;
        this.ty = ty;
        this.editor = editor;
    }
    
    /**
     * Undo this action.
     */
    @Override
    public void undo() {
        super.undo();
        editor.updateTile(tileData, tx, ty);
    }
    
    /**
     * Redo this action.
     */
    @Override
    public void redo() {
        super.redo();
        editor.updateTile(newData, tx, ty);
    }
}
