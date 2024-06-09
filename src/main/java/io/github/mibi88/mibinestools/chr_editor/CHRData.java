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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;

/**
 * This class stores the pattern table.
 * @author mibi88
 */
public class CHRData {
    byte[][] chrData;
    int chrBanks;
    byte[] rawData;
    
    /**
     * Initialize the data with a new pattern table.
     */
    public CHRData() {
        resetCHRData(2);
        rawData = new byte[2*256*16];
    }
    
    /**
     * Load the pattern table from a file.
     * @param file The file to load the pattern table from.
     * @throws Exception Gets thrown if the loading failed.
     */
    public CHRData(File file) throws Exception {
        FileInputStream fileStream = new FileInputStream(file);
        rawData = new byte[fileStream.available()];
        fileStream.read(rawData);
        loadCHRData(rawData);
        fileStream.close();
    }
    
    /**
     * Save the pattern table to a file.
     * @param file The file to save the pattern table to.
     * @throws Exception Gets thrown on failure.
     */
    public void saveCHRData(File file) throws Exception {
        FileOutputStream fileStream = new FileOutputStream(file);
        fileStream.write(rawData);
        fileStream.close();
    }
    
    /**
     * Load the CHR data from a byte array.
     * @param rawData The byte array that contains the CHR data.
     */
    public void loadCHRData(byte[] rawData) {
        // Each tile is made out of 16 bytes and each bank out of 256 tiles.
        resetCHRData(rawData.length/(256*16));
        for(int i=0;i<chrBanks*256;i++){
            chrData[i] = loadTile(Arrays.copyOfRange(rawData,
                    i*16, i*16+16));
        }
    }
    
    /**
     * Reset the raw CHR data.
     * @param chrBanks The number of banks of the CHR data.
     */
    public void resetRawData(int chrBanks) {
        rawData = new byte[chrBanks*256*16];
    }
    
    /**
     * Reset the CHR data.
     * @param chrBanks The number of banks of the CHR data.
     */
    public void resetCHRData(int chrBanks) {
        chrData = new byte[256*chrBanks][8*8];
        this.chrBanks = chrBanks;
    }
    
    /**
     * Load a tile from the raw CHR data.
     * @param rawTile The tile to load.
     * @return The tile data.
     */
    public byte[] loadTile(byte[] rawTile) {
        byte[] out = new byte[8*8];
        
        for(byte y=0;y<8;y++){
            for(byte x=0;x<8;x++){
                byte byte1 = rawTile[y];
                byte byte2 = rawTile[y+8];
                out[y*8+7-x] = (byte)((byte1&1<<x)>>x|(byte2&1<<x)>>x<<1);
            }
        }
        
        return out;
    }
    
    /**
     * Set a tile in the CHR data.
     * @param tile The tile data.
     * @param tileIndex The position of the tile in the pattern table.
     * @throws Exception Gets thrown if the tile index is out of bounds.
     */
    public void setTile(byte[] tile, int tileIndex) throws Exception {
        if(tileIndex >= 0 && tileIndex < chrData.length){
            if(chrData[tileIndex].length == tile.length){
                chrData[tileIndex] = tile;
                saveTile(tileIndex);
                return;
            }
        }
        throw new Exception("Cannot set tile!");
    }
    
    /**
     * Save a tile as raw CHR data.
     * @param tileIndex The index of the tile in the pattern table.
     */
    public void saveTile(int tileIndex) {
        if(tileIndex >= 0 && tileIndex < chrData.length){
            for(int y=0;y<8;y++){
                byte byte1 = 0x00;
                byte byte2 = 0x00;
                for(int x=0;x<8;x++){
                    byte color = chrData[tileIndex][y*8+x];
                    byte1 |= (color&0b00000001)<<7>>x;
                    byte2 |= (color&0b00000010)<<6>>x;
                }
                rawData[tileIndex*16+y] = byte1;
                rawData[tileIndex*16+8+y] = byte2;
            }
        }
    }
    
    /**
     * Generate a buffered image of the tile.
     * @param tileIndex The index of the tile.
     * @param palette The palette to use.
     * @param scale The scale of the image.
     * @return Returns the BufferedImage.
     */
    public BufferedImage generateTileImage(int tileIndex, int[][] palette,
            int scale) {
        BufferedImage image = new BufferedImage(8*scale, 8*scale,
                BufferedImage.TYPE_INT_RGB);
        if(tileIndex >= 0 && tileIndex < chrBanks*256) {
            for(int y=0;y<8;y++){
                for(int x=0;x<8;x++){
                    int colorNum = chrData[tileIndex][y*8+x]&0b00000011;
                    int color = (palette[colorNum][0] << 16)
                            |(palette[colorNum][1] << 8)|palette[colorNum][2];
                    for(int sy=0;sy<scale;sy++){
                        for(int sx=0;sx<scale;sx++){
                            image.setRGB(x*scale+sx, y*scale+sy, color);
                        }
                    }
                }
            }
        }
        return image;
    }
    
    /**
     * Get the tile data.
     * @param tileIndex The index of the tile.
     * @return The tile data.
     * @throws Exception Gets thrown if the index is out of bounds.
     */
    public byte[] getTile(int tileIndex) throws Exception {
        if(tileIndex >= 0 && tileIndex < chrBanks*256) {
            return chrData[tileIndex];
        }
        throw new Exception("Bad tile index!");
    }
    
    /**
     * Print a tile to the standard output.
     * Use it for debugging only.
     * 
     * @param tileIndex The index of the tile to print.
     */
    public void printTile(int tileIndex) {
        char[] colorChars = {' ', '#', '$', '%'};
        if(tileIndex >= 0 && tileIndex < chrBanks*256) {
            for(int y=0;y<8;y++){
                for(int x=0;x<8;x++){
                    int colorNum = chrData[tileIndex][y*8+x]&0b00000011;
                    System.out.print(colorChars[colorNum]);
                }
                System.out.println();
            }
        }
    }
    
    /**
     * Get the number of CHR banks that compose this pattern table.
     * @return The number of CHR banks.
     */
    public int getChrBanks() {
        return chrBanks;
    }
}
