/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package securestorage;

import com.amazonaws.services.cloudfront.model.Paths;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileDirectory;
import com.microsoft.azure.storage.file.CloudFileShare;
import com.microsoft.azure.storage.file.ListFileItem;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.util.ArrayList;

/**
 *
 * @author Ankit Gupta
 */
public class AzureUtil {
    //SCURITY CREDENTIALS FOR MICROSOFT AZURE
    public static final String storageConnectionString =
    "DefaultEndpointsProtocol=https;AccountName=securestorage12345;AccountKey=;EndpointSuffix=core.windows.net";
    public static final String shareName = "filestorage";
    CloudFileClient fileClient;
    CloudStorageAccount storageAccount;
    CloudFileShare share;
    public AzureUtil() throws URISyntaxException, InvalidKeyException, StorageException
    {
        storageAccount = CloudStorageAccount.parse(storageConnectionString);
        fileClient = storageAccount.createCloudFileClient();
        share = fileClient.getShareReference(shareName);
    }
    
    public void AzureUpload(String fileName) throws StorageException, URISyntaxException, IOException 
    {
       CloudFileDirectory rootDir = share.getRootDirectoryReference();
       //String filePath = fileName;
       File file = new File(fileName);
       CloudFile cloudFile = rootDir.getFileReference(file.getName());
       cloudFile.uploadFromFile(file.getPath());
    }
    public ArrayList AzureDirectory(String file) throws StorageException, URISyntaxException, URISyntaxException 
    {
        Path p = java.nio.file.Paths.get(file);
        String fileName = p.getFileName().toString();
        CloudFileDirectory rootDir = share.getRootDirectoryReference();
        ArrayList<String> azureFiles = new ArrayList<String>();
        for ( ListFileItem fileItem : rootDir.listFilesAndDirectories() ) {
            String str = fileItem.getUri().getRawPath().toString();
            
            
            if(fileName.equalsIgnoreCase(str.substring(13,13+fileName.length()))) //LENGTH SHOULD BE DECIDED BY (FILESHARE NAME+2)
                
                    {
                        //sb.append(i+"\n");
                        azureFiles.add(str.substring(13,str.length()));
                    }
            
                        //System.out.println(str.substring(16,16+fileName.length())+" "+ fileName);
        }
        return azureFiles;
    }
    
    public void azureDownload(String file) throws StorageException, URISyntaxException, IOException
    {
        Path p = java.nio.file.Paths.get(file);
        String fileName = p.getFileName().toString();
        CloudFileDirectory rootDir = share.getRootDirectoryReference();
        CloudFile cloudFile = rootDir.getFileReference(fileName);
        FileOutputStream outputStream = new FileOutputStream("D:/SecureStorageApplication/DownloadedParts/"+fileName);
        //File f = new File("C://"+fileName);
        cloudFile.download(outputStream);
        cloudFile.delete();
    }
}
