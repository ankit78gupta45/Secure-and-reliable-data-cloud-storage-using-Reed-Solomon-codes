
package securestorage;

import com.dropbox.core.DbxException;
import com.microsoft.azure.storage.StorageException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.DeflaterSink;


public class SampleEncoder {
//	public static final int DATA_SHARDS = 10;
//    public static final int PARITY_SHARDS =  (int) Math.ceil((double)DATA_SHARDS/2.0);
//    public static final int TOTAL_SHARDS = DATA_SHARDS + PARITY_SHARDS;

    public static final int BYTES_IN_INT = 4;
    public static final String APP_KEY_1 = "";
    public static final String APP_SECRET_1 = "";
    public static final String ACCESS_TOKEN_1 = "";
    public static final String APP_KEY_2 = "";
    public static final String APP_SECRET_2 = "";
    public static final String ACCESS_TOKEN_2 = "";
   
    public StringBuilder sb = new StringBuilder("");

    public String encode_file(String fileName,String no_shards,String no_parity, int isAmazon, int azure, int isDropBox2, int totalParts) throws IOException, DbxException, URISyntaxException, InvalidKeyException, StorageException {
    
        // Parse the command line
//        if (argument.length != 1) {
//            System.out.println("Usage: SampleEncoder <fileName>");
//            return;
//        }
        final File inputFile = new File(fileName);
        if (!inputFile.exists()) {
            System.out.println("Cannot read input file: " + inputFile);
            return null;
        }
      //changed by Ankit
        final int DATA_SHARDS = Integer.parseInt(no_shards);
        final int PARITY_SHARDS =  Integer.parseInt(no_parity);
        final int TOTAL_SHARDS = DATA_SHARDS + PARITY_SHARDS;

        // Get the size of the input file.  (Files bigger that
        // Integer.MAX_VALUE will fail here!)
        final int fileSize = (int) inputFile.length();

        // Figure out how big each shard will be.  The total size stored
        // will be the file size (8 bytes) plus the file.
        final int storedSize = fileSize + BYTES_IN_INT;
        final int shardSize = (storedSize + DATA_SHARDS - 1) / DATA_SHARDS;

        // Create a buffer holding the file size, followed by
        // the contents of the file.
        final int bufferSize = shardSize * DATA_SHARDS;
        final byte [] allBytes = new byte[bufferSize];
        ByteBuffer.wrap(allBytes).putInt(fileSize);
        InputStream in = new FileInputStream(inputFile);
        int bytesRead = in.read(allBytes, BYTES_IN_INT, fileSize);
        if (bytesRead != fileSize) {
            throw new IOException("not enough bytes read");
        }
        in.close();

        // Make the buffers to hold the shards.
        byte [] [] shards = new byte [TOTAL_SHARDS] [shardSize];

        // Fill in the data shards
        for (int i = 0; i < DATA_SHARDS; i++) {
            System.arraycopy(allBytes, i * shardSize, shards[i], 0, shardSize);
        }
        //including timestamp by Ankit
        //starting timestamp
        long startTime;
        long elapsedTime = 0, stopTime;
        // Use Reed-Solomon to calculate the parity.
        ReedSolomon reedSolomon = new ReedSolomon(DATA_SHARDS, PARITY_SHARDS);
        reedSolomon.encodeParity(shards, 0, shardSize);
        
        //DropBoxUpload dbUpload1 = null;
        DropBoxUpload dbUpload2 = null;
        AmazonUtil amazonUtil = new AmazonUtil();
        AzureUtil azureUtil = new AzureUtil();
        
        
        
    
        dbUpload2 = new DropBoxUpload(APP_KEY_2,APP_SECRET_2,ACCESS_TOKEN_2);
        //dbUpload1 = new DropBoxUpload(APP_KEY_1,APP_SECRET_1,ACCESS_TOKEN_1);
        
        //stopping timestamp
        int totalServer =(int) totalParts/(isAmazon+azure+isDropBox2);
        
        ArrayList <Integer> servers = new ArrayList<Integer>();
         int absServer = isAmazon+azure+isDropBox2;
        
                                int countAmazon = 0,countAzure = 0,countDropbox = 0;

        for(int i=0;i<TOTAL_SHARDS;i+=absServer)
        {
            if(isAmazon==1)
            {
               servers.add(0);
               
            }
        }
        for(int i=1;i<TOTAL_SHARDS;i+=absServer)
        {
            if(azure==1)
            {
               servers.add(1);
            }
        }
        for(int i=2;i<TOTAL_SHARDS;i+=absServer)
        {
            if(isDropBox2==1)
            {
               servers.add(2);
            }
        }
        double amazonTime = 0, azureTime = 0, dropBoxTime = 0;

        // Write out the resulting files.
        
        for (int i = 0; i < TOTAL_SHARDS; i++) 
        {
            
            File outputFile = new File("/"+
                    inputFile.getParentFile(),
                    inputFile.getName() + "." + i);
            startTime = System.currentTimeMillis();
            try (OutputStream out = new FileOutputStream(outputFile)) {
                out.write(shards[i]);
            }
            stopTime = System.currentTimeMillis();
            elapsedTime = elapsedTime + stopTime - startTime;  
            
            
        
            
            
        if( null!=servers.get(i))
            switch (servers.get(i)) {
                case 0:
                    try
                    {
                        long start1 = System.currentTimeMillis();
                        amazonUtil.fileN(outputFile.toString());
                        long end1 = System.currentTimeMillis();
                        amazonTime = amazonTime+(end1 - start1);
                        countAmazon++;
                    } catch (IOException ex) {
                        Logger.getLogger(EncryptionAndUpload.class.getName()).log(Level.SEVERE, null, ex);
                    }           break;
                case 1:
                    long start2 = System.currentTimeMillis();
                    azureUtil.AzureUpload(outputFile.toString());
                    long end2 = System.currentTimeMillis();
                        azureTime = azureTime+(end2- start2);
                        countAzure++;
                                break;
                case 2:
                    try
                    {
                        long startA = System.currentTimeMillis();
                        dbUpload2.dropboxUpload(outputFile.toString());
                        long endA = System.currentTimeMillis();
                        dropBoxTime = dropBoxTime+(endA - startA);
                        countDropbox++;
                    }
                    catch (DbxException ex)
                    {
                        Logger.getLogger(EncryptionAndUpload.class.getName()).log(Level.SEVERE, null, ex);
                    }       break;
                default:
                    break;
            }
            
         
        outputFile.delete();

//            System.out.println("wrote " + outputFile);  by Ankit
            sb.append("Uploaded " + outputFile + "\n");
            System.out.println(sb);
        }
        
//        System.out.println("time :" + elapsedTime + " millisecond"); by Ankit
        
        sb.append("time :" + elapsedTime + " millisecond" + inputFile);
        
        double uploadTime = Math.max(Math.max(amazonTime/countAmazon, azureTime/countAzure) ,dropBoxTime/countDropbox );
        
        System.out.println("Encoding Time:"+elapsedTime);
        System.out.println("Uploading Time:"+uploadTime);
        return sb.toString();
    }
}
