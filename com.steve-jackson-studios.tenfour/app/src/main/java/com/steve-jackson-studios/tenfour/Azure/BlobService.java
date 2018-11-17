package com.steve-jackson-studios.tenfour.Azure;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

/**
 * Created by sjackson on 1/10/2017.
 * BlobService
 */

class BlobService {
    private static final String storageConnectionString =
            "DefaultEndpointsProtocol=http;"
                    + "AccountName=mbcutestfaaa001;"
                    + "AccountKey=W90WE3F2Xt1RTRfwF1FJw4qutdfgzVYR7DTl8ruDBjcwlCZhL6KvzWYcx5rZSUYIXZ1KAD9IAoEMTtKcQSVwhw==";

    public static CloudBlobContainer create(String containerName) {
        if (containerName != null) {
            try {
                CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);
                CloudBlobClient serviceClient = account.createCloudBlobClient();
                CloudBlobContainer container = serviceClient.getContainerReference(containerName.toLowerCase());
                container.createIfNotExists();

                BlobContainerPermissions containerPermissions = new BlobContainerPermissions();
                containerPermissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);
                container.uploadPermissions(containerPermissions);

                return container;
            } catch (StorageException storageException) {
                System.out.print("StorageException encountered: ");
                System.out.println(storageException.getMessage());
                System.exit(-1);
            } catch (Exception e) {
                System.out.print("Exception encountered: ");
                System.out.println(e.getMessage());
                System.exit(-1);
            }
        }

        return null;
    }
}