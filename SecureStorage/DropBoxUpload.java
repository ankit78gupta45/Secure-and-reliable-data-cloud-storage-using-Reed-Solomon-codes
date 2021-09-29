/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package securestorage;

import static com.amazonaws.services.cloudformation.model.ResourceAttribute.Metadata;
import com.dropbox.core.*;
import com.sun.xml.internal.ws.api.addressing.WSEndpointReference.Metadata;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;
/**
 *
 * @author Ankit Gupta
 */
public class DropBoxUpload {

    DbxClient client;
    
    public DropBoxUpload(String APP_KEY, String APP_SECRET, String ACCESS_TOKEN) throws DbxException
    {
        DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);
        DbxRequestConfig config = new DbxRequestConfig(
            "SecureStorage/1.0", Locale.getDefault().toString());
        DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);
        client = new DbxClient(config, ACCESS_TOKEN);
        System.out.println("Linked account: " + client.getAccountInfo().displayName);
    }
    public void dropboxUpload(String fileName) throws IOException, DbxException
    {
        File inputFile = new File(fileName);
        FileInputStream inputStream = new FileInputStream(inputFile);
        try {
            DbxEntry.File uploadedFile;
            Path p = Paths.get(fileName);
            String file = p.getFileName().toString();
            uploadedFile = client.uploadFile("/"+file,
            DbxWriteMode.add(), inputFile.length(), inputStream);
            System.out.println("Uploaded: " + uploadedFile.toString());
        } 
        finally {
            inputStream.close();
        }
    }
    public void dropboxDownload(String fileName) throws DbxException, FileNotFoundException, IOException
    {
        Path p = Paths.get(fileName);
        String file = p.getFileName().toString();
        FileOutputStream outputStream = new FileOutputStream("D:/SecureStorageApplication/DownloadedParts/"+file);
        try {
            DbxEntry.File downloadedFile = client.getFile("/"+file, null,outputStream);
            //System.out.println("Metadata: " + downloadedFile.toString());
           
            //client.delete(downloadedFile.toString());
        } finally {
            outputStream.close();
        }
    }
    public ArrayList<String> dropBoxListFiles() throws DbxException, FileNotFoundException, IOException
    {
        DbxEntry.WithChildren listing = client.getMetadataWithChildren("/");
        ArrayList <String> filesInDirectory = new ArrayList<String>();
        //System.out.println("Files in the root path:");
        for(DbxEntry child : listing.children) 
        {
            //System.out.println("	" + child.name);
            filesInDirectory.add(child.name);
        }
        return filesInDirectory;
    }
    
    
}
