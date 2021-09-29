/**
 * Command-line program that decodes a file using Reed-Solomon 4+2.
 *
 * Copyright 2015, Backblaze, Inc.  All rights reserved.
 */

package securestorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;


public class SampleDecoder {
	//changed by Ankit
//	public static final int DATA_SHARDS = 10;
//    public static final int PARITY_SHARDS =  (int) Math.ceil((double)DATA_SHARDS/2.0);
//    public static final int TOTAL_SHARDS = DATA_SHARDS + PARITY_SHARDS;

    StringBuilder sb = new StringBuilder("");
    public static final int BYTES_IN_INT = 4;

    public String decode_file(String fileName,String no_shards,String no_parity) throws IOException {        

        //changed by Ankit
        final int DATA_SHARDS = Integer.parseInt(no_shards);
        final int PARITY_SHARDS =  Integer.parseInt(no_parity);
        final int TOTAL_SHARDS = DATA_SHARDS + PARITY_SHARDS;
        
        final byte [] [] shards = new byte [TOTAL_SHARDS] [];
        final boolean [] shardPresent = new boolean [TOTAL_SHARDS];
        int shardSize = 0;
        int shardCount = 0;
        for (int i = 0; i < TOTAL_SHARDS; i++) {
            File shardFile = new File(
                    "D:/SecureStorageApplication/DownloadedParts/",
                    fileName + "." + i);
            if (shardFile.exists()) 
            {
                shardSize = (int) shardFile.length();
                shards[i] = new byte [shardSize];
                shardPresent[i] = true;
                shardCount += 1;
                InputStream in = new FileInputStream(shardFile);
                in.read(shards[i], 0, shardSize);
                in.close();
//                System.out.println("Read " + shardFile); by Ankit
                sb.append("Read " + shardFile+ "\n");
                shardFile.delete();
            }
        }

        // We need at least DATA_SHARDS to be able to reconstruct the file.
        if (shardCount < DATA_SHARDS) {
//            System.out.println("Not enough shards present"); by Ankit
        	sb.append("Not enough shards present");
            return sb.toString();
        }

        // Make empty buffers for the missing shards.
        for (int i = 0; i < TOTAL_SHARDS; i++) {
            if (!shardPresent[i]) {
                shards[i] = new byte [shardSize];
            }
        }
        
      //including timestamp by Ankit
        //starting timestamp
        long startTime = System.currentTimeMillis();

        // Use Reed-Solomon to fill in the missing shards
        ReedSolomon reedSolomon = new ReedSolomon(DATA_SHARDS, PARITY_SHARDS);
        reedSolomon.decodeMissing(shards, shardPresent, 0, shardSize);
        
        

        // Combine the data shards into one buffer for convenience.
        // (This is not efficient, but it is convenient.)
        byte [] allBytes = new byte [shardSize * DATA_SHARDS];
        for (int i = 0; i < DATA_SHARDS; i++) {
            System.arraycopy(shards[i], 0, allBytes, shardSize * i, shardSize);
        }

        // Extract the file length
        int fileSize = ByteBuffer.wrap(allBytes).getInt();

        // Write the decoded file
        File decodedFile = new File("D:/SecureStorageApplication/FinalFiles/",  fileName);
        OutputStream out = new FileOutputStream(decodedFile);
        out.write(allBytes, BYTES_IN_INT, fileSize);
//        System.out.println("Wrote " + decodedFile);
//        System.out.println("time :" + elapsedTime + " millisecond");
        sb.append("Wrote " + decodedFile + "\n");
        //stopping timestamp
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        
        sb.append("time :" + elapsedTime + " millisecond");
        return sb.toString();
    }
}
