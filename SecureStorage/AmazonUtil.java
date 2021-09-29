
/*
 * UploadObjectSingleOperation.java
 *
 * Created on April 15, 2017, 12:35 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 * created by Ankit Gupta
 */

package securestorage;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import java.io.File;
import java.io.IOException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

public class AmazonUtil {
    
    public static int decideServer(String s)
    {
		int result;
		result=Integer.parseInt(s.substring(s.length()-1));
		return result;
    }
	private final static String bucketName= "securestorage1234";
	private static String keyName;
        BasicAWSCredentials awsCreds;
        private static File inputFile;
	private static String uploadFileName;
        public AmazonS3 s3client;
        
        public AmazonUtil()
        {
            
            
            //Amazon AWS Credentials
            //Update at Lines 61
            awsCreds = new BasicAWSCredentials("", 
                                            "");
             s3client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                              .withRegion("ap-south-1").build(); 
        }
        public void fileN(String fileName) throws IOException  
        {   
             uploadFileName=fileName;
             fileName=fileName.replace("\\","@");
             String temp=fileName.substring(0,fileName.lastIndexOf("@")+1);
             fileName=fileName.replaceFirst(temp,"");
             
             keyName=fileName;
            
             inputFile = new File(uploadFileName);
             if (!inputFile.exists()) 
             {
                System.out.println("Cannot read input file: " + inputFile);         
             }         
            try 
            {           
                System.out.println("Uploading a new object to S3 from a file\n");
                s3client.putObject(new PutObjectRequest(bucketName, keyName, inputFile));
            } 
            catch (AmazonServiceException ase) 
            {
                System.out.println("Caught an AmazonServiceException, which " +
            		"means your request made it " +
                    "to Amazon S3, but was rejected with an error response" +
                    " for some reason.");
                System.out.println("Error Message:    " + ase.getMessage());
                System.out.println("HTTP Status Code: " + ase.getStatusCode());
                System.out.println("AWS Error Code:   " + ase.getErrorCode());
                System.out.println("Error Type:       " + ase.getErrorType());
                System.out.println("Request ID:       " + ase.getRequestId());
            } 
            catch (AmazonClientException ace) 
            {
                System.out.println("Caught an AmazonClientException, which " +
            		"means the client encountered " +
                    "an internal error while trying to " +
                    "communicate with S3, " +
                    "such as not being able to access the network.");
                System.out.println("Error Message: " + ace.getMessage());
            }
        }
        public ArrayList<String> getFiles(String fileName)
        {
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
    .withBucketName(bucketName)
    .withPrefix(fileName);
ObjectListing objectListing;
ArrayList <String> filesInDirectory = new ArrayList<String>();

do {
        objectListing = s3client.listObjects(listObjectsRequest);
        for (S3ObjectSummary objectSummary : 
            objectListing.getObjectSummaries()) {
            //System.out.println(objectSummary.getKey()
            filesInDirectory.add(objectSummary.getKey());
                    
        }
        listObjectsRequest.setMarker(objectListing.getNextMarker());
} while (objectListing.isTruncated());
return filesInDirectory;
        }
        public void downLoadBucketFile(String fileName) throws IOException
        {
            //BasicAWSCredentials awsCreds = new BasicAWSCredentials("AKIAJTKDK7XNJWEPK57A",  "MLmVlLPwtW8ivHc+Gu+y96elHGvhv17Eb6/HTM+n");
            s3client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withRegion("ap-south-1").build();
            //S3Object s3object = s3client.getObject(new GetObjectRequest(bucketName, fileName));
            //displayTextInputStream(s3object.getObjectContent(),fileName);
            
            
            //BufferedReader reader = new BufferedReader(new InputStreamReader(
            //s3object.getObjectContent()));
            File file = new File("D:/SecureStorageApplication/DownloadedParts/"+fileName); 
            ObjectMetadata object = s3client.getObject(new GetObjectRequest(bucketName, fileName), file);

            /*
while (true) {          
     String line = reader.readLine();           
     if (line == null)
          break;            

     writer.write(line + "\n");
}

writer.close();
*/

            s3client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
        }
}