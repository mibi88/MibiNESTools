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

/**
 *
 * @author mibi88
 */
public class Rectangle {
    private DrawEvent event;

    /**
     * Draw a rectangle.
     * @param event The DrawEvent to use to draw the rectangle.
     */
    public Rectangle(DrawEvent event) {
        this.event = event;
    }
    
    /**
     * Draw a rectangle.
     * @param x1 The starting position of the rectangle.
     * @param y1 The starting position of the rectangle.
     * @param x2 The end position of the rectangle.
     * @param y2 The end position of the rectangle.
     */
    public void drawRectangle(int x1, int y1, int x2, int y2) {
        int ix = x1 < x2 ? 1 : -1;
        int iy = y1 < y2 ? 1 : -1;
        for(int y=y1;y!=y2+iy;y+=iy){
            for(int x=x1;x!=x2+ix;x+=ix){
                event.setPixel(x, y);
            }
        }
    }
}
