package com.github.nilscoding.jsinglecolorpng;

/**
 * Some PNG constants
 * @author NilsCoding
 */
public abstract class PNGConstants {
    
    private PNGConstants() { }
    
    /**
     * File Header
     */
    public static final byte[] FILE_HEADER = toByteArray(0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a);
    /**
     * IHDR section
     */
    public static final byte[] SECTION_IHDR = toByteArray('I', 'H', 'D', 'R');
    /**
     * PLTE section
     */
    public static final byte[] SECTION_PLTE = toByteArray('P', 'L', 'T', 'E');
    /**
     * IDAT section
     */
    public static final byte[] SECTION_IDAT = toByteArray('I', 'D', 'A', 'T');
    /**
     * IEND section
     */
    public static final byte[] SECTION_IEND = toByteArray('I', 'E', 'N', 'D');
    /**
     * IEND section fixed length
     */
    public static final byte[] SECTION_IEND_LEN = toByteArray(0, 0, 0, 0);
    /**
     * IEND section fixed checksum
     */
    public static final byte[] SECTION_IEND_CHECKSUM = toByteArray(0xae, 0x42, 0x60, 0x82);
    
    /**
     * Converts an array of int to the corresponding byte array
     * @param values    values to convert
     * @return  resulting byte array
     */
    public static byte[] toByteArray(int... values) {
        if (values == null) {
            return null;
        }
        if (values.length == 0) {
            return new byte[0];
        }
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < bytes.length; i++) {
            byte b = (byte)values[i];
            bytes[i] = b;
        }
        return bytes;
    }
            
}
