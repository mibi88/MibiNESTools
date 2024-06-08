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

import javax.swing.undo.AbstractUndoableEdit;

/**
 *
 * @author mibi88
 */
public class NametableEdit extends AbstractUndoableEdit {
    private NametableEditor editor;
    private byte[] oldAttributes, oldNametable;
    private byte[] newAttributes, newNametable;
    public NametableEdit(NametableEditor editor, byte[] oldAttributes,
            byte[] oldNametable, byte[] newAttributes, byte[] newNametable) {
        super();
        this.editor = editor;
        this.oldAttributes = oldAttributes.clone();
        this.oldNametable = oldNametable.clone();
        this.newAttributes = newAttributes.clone();
        this.newNametable = newNametable.clone();
    }
    
    @Override
    public void undo() {
        super.undo();
        editor.setData(oldNametable, oldAttributes);
        editor.fileEdited();
    }
    
    @Override
    public void redo() {
        super.redo();
        editor.setData(newNametable, newAttributes);
        editor.fileEdited();
    }
}
