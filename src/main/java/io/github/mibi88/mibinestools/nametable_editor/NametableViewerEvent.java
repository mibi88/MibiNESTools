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

/**
 * Methods that get called when the nametable should be edited.
 * @author mibi88
 */
public interface NametableViewerEvent {

    /**
     * Gets called before the nametable is edited.
     * @param tx The position of the tile that is at the mouse cursor position.
     * @param ty The position of the tile that is at the mouse cursor position.
     */
    public void beforeChange(int tx, int ty);

    /**
     * Gets called when the nametable should be edited.
     * @param tx The position of the tile that is at the mouse cursor position.
     * @param ty The position of the tile that is at the mouse cursor position.
     * @param end If the mouse buttons got released.
     */
    public void tileChanged(int tx, int ty, boolean end);
}
