
package com.lgcns.sce.nfc;

/**
 * @class AppExec
 *
 * Utility class for handling byte array objects.
 */
public class ByteUtils {

    // Convert integer to byte array
    public static final byte[] intToByteArray(int value) {
        return new byte[] {
                (byte) (value >>> 24), 
                (byte) (value >>> 16),
                (byte) (value >>> 8), 
                (byte) value
        };
    }

    // Convert byte array to integer
    public static final int byteArrayToInt(byte[] b) {
        return (b[0] << 24) + 
               ((b[1] & 0xFF) << 16) + 
               ((b[2] & 0xFF) << 8) + 
               (b[3] & 0xFF);
    }
    // Convert long to byte array 
    public static final byte[] longToByteArray(long value) {
        return new byte[] {                
                (byte) (value >>> 56),
                (byte) (value >>> 48), 
                (byte) (value >>> 40),
                (byte) (value >>> 32), 
                (byte) (value >>> 24), 
                (byte) (value >>> 16),
                (byte) (value >>> 8), 
                (byte) value
        };
    }
 
    // Convert byte array to long  
    public static final long byteArrayToLong(byte[] b) {
        return (long)(b[0] << 56) + 
               ((long)(b[1] & 0xFF) << 48) + 
               ((long)(b[2] & 0xFF) << 40) + 
               ((long)(b[3] & 0xFF) << 32)+
               ((long)(b[4] & 0xFF) << 24) + 
               ((long)(b[5] & 0xFF) << 16) + 
               ((long)(b[6] & 0xFF) << 8) + 
               ((long)b[7] & 0xFF);
    }
    
    // Merge byte arrays
    public static final byte[] byteMerge(byte[][] inp)
    {
        int length = 0;
        if (inp==null) return null;
        for(int inx = 0 ; inx<inp.length ; inx++)
        {
            if(inp[inx]==null) return null;
            length+=inp[inx].length;
        }
        
        int position = 0;
        byte[] merge = new byte[length];
        for(int inx = 0; inx<inp.length ; inx++)
        {
            for(int jnx = 0 ; jnx < inp[inx].length ; jnx++)
            {
                merge[position++] = inp[inx][jnx];
            }
        }
        
        return merge;
    }
    
    // Split data with different split length.  
    public static final byte[][] byteSplit(byte[] inp , int... splitLength)
    {   
        if (inp==null || splitLength==null) return null;
        
        int position = 0;
        byte spilit_data[][] = new byte[splitLength.length][];
        for(int inx=0 ; inx < splitLength.length ; inx++)
        {
            spilit_data[inx] = new byte[splitLength[inx]];
            for(int jnx=0 ; jnx < splitLength[inx] ; jnx++)
            {
                try
                {
                    spilit_data[inx][jnx] = inp[position++];
                }
                catch(ArrayIndexOutOfBoundsException e)
                {
                    return null;
                }
            }
        }
        
        return spilit_data;
    }
 
}
