package com.github.nilscoding.jsinglecolorpng;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;

/**
 * Creator for minimal-sized single-colored PNGs
 * @author NilsCoding
 */
public class MiniPngCreator {
    
    private static final int NULLS_BLOCK_SIZE = 4096;
    
    private MiniPngCreator() { }
    
    /**
     * Creates a single-colored PNG with given color and width/heigth
     * @param r red (0-255)
     * @param g green (0-255)
     * @param b blue (0-255)
     * @param width width
     * @param height    height
     * @param increaseCompat    true to increase compatibility by using 8-bit depth instead of 1-bit
     * @return  image PNG bytes or null on error / invalid size
     */
    public static byte[] createSingleColoredImage(int r, int g, int b, int width, int height, boolean increaseCompat) {
        if ((width < 0) || (height < 0)) {
            return null;
        }
        byte[] data = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream daos = new DataOutputStream(baos);

            daos.write(PNGConstants.FILE_HEADER);
            
            writeIHDRSection(daos, width, height, increaseCompat);
            writePLTESection(daos, r, g, b);
            writeIDATSection(daos, width, height, increaseCompat);
            writeIENDSection(daos);
            
            daos.flush();
            baos.flush();
            data = baos.toByteArray();
        } catch (Exception ex) {
            data = null;
        }
        return data;
    }
    
    /**
     * Writes the IHDR section
     * @param daos  data stream to write to
     * @param width width
     * @param height    height
     * @param increaseCompat    true to increase compatibility by using 8-bit depth instead of 1-bit
     * @throws IOException
     */
    private static void writeIHDRSection(DataOutputStream daos, int width, int height, boolean increaseCompat) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(17);
        buffer.put(PNGConstants.SECTION_IHDR);
        buffer.putInt(width);
        buffer.putInt(height);
        byte bitDepth = (increaseCompat == false) ? (byte)0x01 : (byte)0x08;
        buffer.put(bitDepth); // bit depth
        buffer.put((byte)0x03); // palette
        buffer.put((byte)0x00); // deflate
        buffer.put((byte)0x00); // basic filtering: none
        buffer.put((byte)0x00); // no interlacing
        byte[] bufferData = buffer.array();
        CRC32 crc = new CRC32();
        crc.update(bufferData);
        int checksum = (int)crc.getValue();
        daos.writeInt(0x0d); // block length = buffer length - header length : 17 - 4 = 13
        daos.write(bufferData);
        daos.writeInt(checksum);
    }
    
    /**
     * Writes the PLTE section
     * @param daos  data stream to write to
     * @param r red
     * @param g green
     * @param b blue
     * @throws IOException 
     */
    private static void writePLTESection(DataOutputStream daos, int r, int g, int b) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(7);
        buffer.put(PNGConstants.SECTION_PLTE);
        buffer.put((byte)(0x00ff & r));
        buffer.put((byte)(0x00ff & g));
        buffer.put((byte)(0x00ff & b));
        byte[] bufferData = buffer.array();
        CRC32 crc = new CRC32();
        crc.update(bufferData);
        int checksum = (int)crc.getValue();
        daos.writeInt(0x03); // block length = buffer length - header length: 7 - 4 = 3
        daos.write(bufferData);
        daos.writeInt(checksum);
    }
    
    /**
     * Writes the IDAT section
     * @param daos  data stream to write to
     * @param width width
     * @param height    height
     * @param increaseCompat    true to increase compatibility by using 8-bit depth instead of 1-bit
     * @throws IOException 
     */
    private static void writeIDATSection(DataOutputStream daos, int width, int height, boolean increaseCompat) throws IOException {
        // each pixel is represented by 1 bit, plus one byte for each line (applied filter, which is also 0 here)
        double bitDivider = (increaseCompat == false) ? 8.0 : 1.0;
        int numberOfNulls = (int)Math.ceil((double)width * (double)height / bitDivider) + height;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream deflate = new DeflaterOutputStream(baos);

        // split data into 4k byte blocks for efficiency
        int numberOfBlocks = numberOfNulls / NULLS_BLOCK_SIZE;
        byte[] nullData = new byte[NULLS_BLOCK_SIZE];
        Arrays.fill(nullData, (byte)0);
        for (int i = 0; i < numberOfBlocks; i++) {
            deflate.write(nullData, 0, nullData.length);
        }
        int remainingBytes = numberOfNulls - (numberOfBlocks * NULLS_BLOCK_SIZE);
        if (remainingBytes > 0) {
            byte[] remainingData = new byte[remainingBytes];
            Arrays.fill(remainingData, (byte)0);
            deflate.write(remainingData, 0, remainingData.length);
        }
        
        deflate.flush();
        deflate.finish();
        deflate.close();
        baos.flush();
        byte[] imageData = baos.toByteArray();
        ByteBuffer buffer = ByteBuffer.allocate(imageData.length + 4);
        buffer.put(PNGConstants.SECTION_IDAT);
        buffer.put(imageData);
        byte[] bufferData = buffer.array();
        CRC32 crc = new CRC32();
        crc.update(bufferData);
        int checksum = (int)crc.getValue();
        daos.writeInt(imageData.length); // block length = data length
        daos.write(bufferData);
        daos.writeInt(checksum);
    }
    
    /**
     * Writes the IEND section
     * @param daos  data stream to write to
     * @throws IOException 
     */
    private static void writeIENDSection(DataOutputStream daos) throws IOException {
        daos.write(PNGConstants.SECTION_IEND_LEN);
        daos.write(PNGConstants.SECTION_IEND);
        daos.write(PNGConstants.SECTION_IEND_CHECKSUM);
    }
}
