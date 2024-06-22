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

import java.io.File;

/**
 *
 * @author mibi88
 */
public class FileItem {
    File folder;
    File file;

    /**
     * Create a new file item. It represents a file in a file list.
     * @param file The file.
     * @param folder The folder that contains this file.
     */
    public FileItem(File file, File folder) {
        this.folder = folder;
        this.file = file;
    }
    
    /**
     * Get the relative path to this file.
     * @return Returns the relative path.
     */
    @Override
    public String toString() {
        return folder.toURI().relativize(file.toURI()).getPath();
    }
    
    /**
     * Get the file.
     * @return Returns the file.
     */
    public File getFile() {
        return file;
    }
}
