package com.steve-jackson-studios.tenfour.Azure;

import android.util.Log;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by sjackson on 1/10/2017.
 * CloudMedia
 */

public class CloudMedia {

    private static final String TAG = "CloudMedia";
    private static final String MEDIA_BASE_URL = "https://mbcutestfaaa001.blob.core.windows.net/";
    private static HashMap<String, CloudBlobContainer> CONTAINERS = new HashMap<String, CloudBlobContainer>();

    private CloudMedia() {
    }

    public static String getQualifiedFilename() {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date());
        return ("MAMBO" + timeStamp + ".jpg");
    }

    public static String getQualifiedFilename(String filePath) {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date());
        String extension = (filePath.toLowerCase().endsWith("gif")) ? ".gif" : ".jpg";
        return ("MAMBO" + timeStamp + extension);
    }

    public static String getLink(String containerName, String fileName) throws URISyntaxException, StorageException {
        containerName = containerName.toLowerCase();
        CloudBlobContainer container = getContainer(containerName);
        if (container != null) {
            for (ListBlobItem blobItem : container.listBlobs()) {
                // If the item is a blob, not a virtual directory
                if (blobItem instanceof CloudBlockBlob) {
                    return blobItem.getUri().toString();
                }
            }
        }
        return null;
    }

    public static void saveItem(String containerName, String fileName, String filePath) {
        containerName = containerName.toLowerCase();
        CloudBlobContainer container = getContainer(containerName);
        if (container != null) {
            try {
                CloudBlockBlob blob = container.getBlockBlobReference(fileName);
                File sourceFile = new File(filePath);
                File destinationFile = new File(sourceFile.getParentFile(), fileName + ".tmp");
                blob.downloadToFile(destinationFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void uploadItem(String containerName, String fileName, String filePath) {
        containerName = containerName.toLowerCase();
        CloudBlobContainer container = getContainer(containerName);
        if (container != null) {
            try {
                CloudBlockBlob blob = container.getBlockBlobReference(fileName);
                File sourceFile = new File(filePath);
                blob.upload(new FileInputStream(sourceFile), sourceFile.length());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String uploadFile(String containerName, File sourceFile, String fileName) {
        //Log.d(TAG, "uploadFile( FILENAME = " + fileName + ")");
        containerName = containerName.toLowerCase();
        CloudBlobContainer container = getContainer(containerName);
        if (container != null) {
            try {
                CloudBlockBlob blob = container.getBlockBlobReference(fileName);
                blob.upload(new FileInputStream(sourceFile), sourceFile.length());
                return generateFileUrl(containerName, fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String copyAvatar(URI sourceFile, String userName) {
        CloudBlobContainer container = getContainer("useravatars");
        if (container != null) {
            try {
                CloudBlockBlob sourceBlob = new CloudBlockBlob(sourceFile);
                CloudBlockBlob userBlob = container.getBlockBlobReference(userName);
                userBlob.startCopy(sourceBlob);
                return generateFileUrl("useravatars", userName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String createAvatar(File sourceFile, String userName) {
        CloudBlobContainer userContainer = getContainer("useravatars");
        if (userContainer != null) {
            try {
                CloudBlockBlob userBlob = userContainer.getBlockBlobReference(userName);
                userBlob.upload(new FileInputStream(sourceFile), sourceFile.length());
                return generateFileUrl("useravatars", userName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static String generateFileUrl(String containerName, String fileName) {
        return MEDIA_BASE_URL + containerName + "/" + fileName;
    }

    private static CloudBlobContainer getContainer(String name) {
        CloudBlobContainer container;
        if (CONTAINERS.get(name) == null) {
            container = BlobService.create(name);
            if (container != null) {
                CONTAINERS.put(name, container);
            }
        } else {
            container = CONTAINERS.get(name);
        }
        return container;
    }
}
