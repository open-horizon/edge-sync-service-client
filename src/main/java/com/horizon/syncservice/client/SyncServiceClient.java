package com.horizon.syncservice.client;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.StringBuffer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Interceptor;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Client for Sync-Service
 * 
 * Note: Use the class SyncServiceClient.Builder to create a SyncServiceClient instance.
 */
public class SyncServiceClient {

    private static final String CLASS_NAME = SyncServiceClient.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    public static final String DEFAULT_HTTP_PROTOCOL = "http";
    public static final String DEFAULT_HTTP_HOST = "localhost";
    public static final int DEFAULT_HTTP_PORT = 8080;

    private static final String DESTINATION_ACL = "destinations";
	private static final String OBJECT_ACL = "objects";

    private final String orgID;
    private final RetrofitHelper api;

    private SyncServiceClient(URL url, String orgID, ObjectMapper mapper, OkHttpClient httpClient) {
        this.orgID = orgID;
        Retrofit retrofit = new Retrofit.Builder().baseUrl(url.toExternalForm())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .client(httpClient)
                .build();
        this.api = new RetrofitHelper(orgID, retrofit);
    }

    /**
     * Get the list of destinations in the organization of the sync client.
     * 
     * @return A List of {@link Destination Destination} class objects.
     * @throws SyncServiceException If an error was returned from the Sync Service.
     * @throws IOException If an I/O error occurred.
     */
    public List<Destination> getDestinations()
            throws SyncServiceException, IOException {
        final String METHOD = "getDestinations";
        try {
            Response<List<Destination>> response = api.getDestinations().execute();
            if (response.isSuccessful()) {
                List<Destination> result = response.body();
                return result;
            } else if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) {
                return new ArrayList<Destination>();
            } else {
                String message = String.format("Failed to get the list of destinations for %s. Error: %s", orgID,
                        response.errorBody().string());
                throw new SyncServiceException(message);
            }
        } catch (IOException ex) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD, "IOException from getDestinations", ex);
            throw ex;
        }
    }

    /**
     * Get the list of objects at a destination.
     * 
     * @param destType The destination type of the destination whose list of objects is to be returned.
     * @param destID The destination ID of the destination whose list of objects is to be returned.
     * @return A List of {@link ObjectStatus ObjectStatus} class objects, which contain information
     *                   about the objects at the specified destination.
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     */
    public List<ObjectStatus> getDestinationObjects(String destType, String destID)
            throws SyncServiceException, IOException {
        final String METHOD = "getDestinationObjects";
        try {
            Response<List<ObjectStatus>> response = api.getDestinationObjects(destType, destID).execute();
            if (response.isSuccessful()) {
                List<ObjectStatus> result = response.body();
                return result;
            } else if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) {
                return new ArrayList<ObjectStatus>();
            } else {
                String message = String.format("Failed to get the list of objects for the destination %s:%s:%s. Error: %s",
                        orgID, destType, destID, response.errorBody().string());
                throw new SyncServiceException(message);
            }
        } catch (IOException ex) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD, "IOException from getDestinationObjects", ex);
            throw ex;
        }
    }

    /**
     * Create/update an object. Only object metadata is sent in this request
     * 
     * <p>In the provided {@link SyncServiceMetaData SyncServiceMetaData} object, the application must provide
     *      the ObjectID and ObjectType which uniquely identify the object.
     * <p>When creating/updating an object in the CSS the application must also provide either destinationID
     *      and destinationType or destinationsList.
     * <p>All other fields in {@link SyncServiceMetaData SyncServiceMetaData} are optional and if not
     *      specified will take the default values.
     * 
     * @param metaData specifies the object's metadata.
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     */
    public void updateObject(SyncServiceMetaData metaData) throws SyncServiceException, IOException {
        final String METHOD = "updateObject";
        try {
            ObjectPayload payload = new ObjectPayload(metaData);
            Response<Void> response = api.putObject(metaData.getObjectType(), metaData.getObjectID(), payload)
                    .execute();
            if (!response.isSuccessful()) {
                String message = String.format("Failed to update the object %s:%s:%s. Error: %s", orgID,
                        metaData.getObjectType(), metaData.getObjectID(), response.errorBody().string());
                throw new SyncServiceException(message);
            }
        } catch (IOException ex) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD, "IOException from updateObject", ex);
            throw ex;
        }
    }

    /**
     * Metadata of updated objects of the specified objectType are retrieved.
     * 
     * @param objectType specifies the type of objects whose updates are queried for.
     * @param received If <code>true</code>, objects marked as received, will be included in the results.
     * @return A List of {@link SyncServiceMetaData SyncServiceMetaData} class objects, one for each
     *               updated object.
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     */
    public List<SyncServiceMetaData> getUpdatedObjects(String objectType, boolean received)
            throws SyncServiceException, IOException {
        final String METHOD = "getUpdatedObjects";
        try {
            Response<List<SyncServiceMetaData>> response = api.getUpdatedObjects(objectType, received).execute();
            if (response.isSuccessful()) {
                List<SyncServiceMetaData> metaData = response.body();
                return metaData;
            } else if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) {
                return new ArrayList<SyncServiceMetaData>();
            } else {
                String message = String.format("Failed to get the list of updated objects for %s:%s. Error: %s", orgID,
                        objectType, response.errorBody().string());
                throw new SyncServiceException(message);
            }
        } catch (IOException ex) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD, "IOException from getUpdatedObjects", ex);
            throw ex;
        }
    }

    /**
     * Get information about the destinations to which an object was sent
     * 
     * @param objectType The object type of the object whose destination information will be retrieved.
     * @param objectID The object ID of the object whose destination information will be retrieved.
     * @return A List of {@link DestinationStatus DestinationStatus} class objects, each of which has
     *                 information about a destination to which the object was sent.
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     */
    public List<DestinationStatus> getObjectDestinations(String objectType, String objectID)
            throws SyncServiceException, IOException {
        final String METHOD = "getObjectDestinations";
        try {
            Response<List<DestinationStatus>> response = api.getObjectDestinations(objectType, objectID).execute();
            if (response.isSuccessful()) {
                List<DestinationStatus> result = response.body();
                return result;
            } else if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) {
                return new ArrayList<DestinationStatus>();
            } else {
                String message = String.format("Failed to get the destinations of the object %s:%s%s. Error: %s",
                        orgID, objectType, objectID, response.errorBody().string());
                throw new SyncServiceException(message);
            }
        } catch (IOException ex) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD, "IOException from getObjectDestinations", ex);
            throw ex;
        }
    }

    /**
     * Get the status of an object that was sent
     * 
     * @param objectType The object type of the object whose status will be retrieved.
     * @param objectID The object ID of the object whose status will be retrieved.
     * @return A String indicating the status of the object. See the class {@link ObjectStatus ObjectStatus} 
     *                 for the list of possible values.
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     */
    public String getObjectStatus(String objectType, String objectID)
            throws SyncServiceException, IOException {
        final String METHOD = "getObjectStatus";
        try {
            Response<String> response = api.getObjectStatus(objectType, objectID).execute();
            if (response.isSuccessful()) {
                String result = response.body();
                return result;
            } else if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) {
                return "";
            } else {
                String message = String.format("Failed to get the status of the object %s:%s%s. Error: %s",
                        orgID, objectType, objectID, response.errorBody().string());
                throw new SyncServiceException(message);
            }
        } catch (IOException ex) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD, "IOException from getObjectStatus", ex);
            throw ex;
        }
    }

    /**
     * Update an object's data from an array of bytes.
     * 
     * @param metaData The metadata of the object whose data is to be updated.
     * @param input The new data for the object.
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     */
    public void updateObjectData(SyncServiceMetaData metaData, byte[] input)
            throws SyncServiceException, IOException {
        final String METHOD = "updateObjectData";
        try {
            RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"), input);
            Response<Void> response = api.putObjectData(metaData.getObjectType(), metaData.getObjectID(), body)
                    .execute();
            if (!response.isSuccessful()) {
                String message = String.format("Failed to update the object data %s:%s:%s. Error: %s", orgID,
                        metaData.getObjectType(), metaData.getObjectID(), response.errorBody().string());
                throw new SyncServiceException(message);
            }
        } catch (IOException ex) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD, "IOException from updateObjectData", ex);
            throw ex;
        }
    }

    /**
     * Update an object's data from a file.
     *
     * @param metaData The metadata of the object whose data is to be updated.
     * @param input A file containing the new data for the object.
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     */
    public void updateObjectData(SyncServiceMetaData metaData, File input)
            throws SyncServiceException, IOException {
        final String METHOD = "updateObjectData";
        try {
            RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"), input);
            Response<Void> response = api.putObjectData(metaData.getObjectType(), metaData.getObjectID(), body)
                    .execute();
            if (!response.isSuccessful()) {
                String message = String.format("Failed to update the object %s:%s:%s. Error: %s", orgID,
                        metaData.getObjectType(), metaData.getObjectID(), response.errorBody().string());
                throw new SyncServiceException(message);
            }
        } catch (IOException ex) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD, "IOException from updateObjectData", ex);
            throw ex;
        }
    }

    /**
     * Fetch an object's data and return it as a byte array.
     *
     * @param metaData The metadata of the object whose data is to be fetched.
     * @return The object's data
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     */
    public byte[] fetchObjectData(SyncServiceMetaData metaData) throws SyncServiceException, IOException {
        final String METHOD = "fetchObjectData";
        try {
            Response<ResponseBody> response = api.getObjectData( metaData.getObjectType(), metaData.getObjectID())
                    .execute();
            if (!response.isSuccessful()) {
                String message = String.format("Failed to delete the object %s:%s:%s. Error: %s", orgID,
                        metaData.getObjectType(), metaData.getObjectID(), response.errorBody().string());
                response.raw().close();
                throw new SyncServiceException(message);
            }
            return response.body().bytes();
        } catch (IOException ex) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD, "IOException from fetchObjectData", ex);
            throw ex;
        }
    }

    /**
     * Fetch an object's data and write it to a file.
     *
     * @param metaData The metadata of the object whose data is to be fetched.
     * @param file The File object of the file to which the object's data will be written.
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     */
    public void fetchObjectData(SyncServiceMetaData metaData, File file)
            throws SyncServiceException, IOException {
        final String METHOD = "fetchObjectData";
        try {
            Response<ResponseBody> response = api.getObjectData(metaData.getObjectType(), metaData.getObjectID())
                    .execute();
            if (!response.isSuccessful()) {
                String message = String.format("Failed to delete the object %s:%s:%s. Error: %s", orgID,
                        metaData.getObjectType(), metaData.getObjectID(), response.errorBody().string());
                response.raw().close();
                throw new SyncServiceException(message);
            }
            FileOutputStream fileOutStream = new FileOutputStream(file);
            InputStream dataStream = response.body().byteStream();
            byte[] buffer = new byte[4096];
            while (true) {
                int length = dataStream.read(buffer);
                if (length == -1) {
                    break;
                }
                fileOutStream.write(buffer, 0, length);
            }
            fileOutStream.close();

            response.raw().close();
        } catch (IOException ex) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD, "IOException from fetchObjectData", ex);
            throw ex;
        }
    }

    /**
     * Delete an object.
     *
     * @param metaData The metadata of the object that will be deleted.
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     */
    public void deleteObject(SyncServiceMetaData metaData) throws SyncServiceException, IOException {
        final String METHOD = "deleteObject";
        try {
            Response<Void> response = api.deleteObject(metaData.getObjectType(), metaData.getObjectID())
                    .execute();
            if (!response.isSuccessful()) {
                String message = String.format("Failed to delete the object %s:%s:%s. Error: %s", orgID,
                        metaData.getObjectType(), metaData.getObjectID(), response.errorBody().string());
                throw new SyncServiceException(message);
            }
        } catch (IOException ex) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD, "IOException from deleteObject", ex);
            throw ex;
        }
    }

    /**
     * Activate an object.
     *
     * @param metaData The metadata of the object that will be activated.
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     */
    public void activateObject(SyncServiceMetaData metaData) throws SyncServiceException, IOException {
        operationHelper(metaData, "activate");
    }

    /**
     * Mark an object as having been consumed.
     *
     * @param metaData The metadata of the object that will be marked as having been consumed.
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     */
    public void markObjectConsumed(SyncServiceMetaData metaData)
            throws SyncServiceException, IOException {
        operationHelper(metaData, "consumed");
    }

    /**
     * Mark an object as having been deleted.
     *
     * @param metaData The metadata of the object that will be marked as having been deleted.
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     */
    public void markObjectDeleted(SyncServiceMetaData metaData) throws SyncServiceException, IOException {
        operationHelper(metaData, "deleted");
    }

    /**
     * Mark an object as having been received.
     *
     * @param metaData The metadata of the object that will be marked as having been received.
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     */
    public void markObjectReceived(SyncServiceMetaData metaData) throws SyncServiceException, IOException {
        operationHelper(metaData, "received");
    }

    /**
     * Perform an "operation" on an object.
     *
     * @param metaData The metadata of the object on which the operation will be performed.
     * @param operation The operation to perform.
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     */
    private void operationHelper(SyncServiceMetaData metaData, String operation)
            throws SyncServiceException, IOException {
        final String METHOD = "operationHelper";
        try {
            Response<Void> response = api
                    .objectOperation(metaData.getObjectType(), metaData.getObjectID(), operation).execute();
            if (!response.isSuccessful()) {
                String message = String.format("Failed to perform the operation %s the object %s:%s:%s. Error: %s",
                        operation, orgID, metaData.getObjectType(), metaData.getObjectID(),
                        response.errorBody().string());
                throw new SyncServiceException(message);
            }
        } catch (IOException ex) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD, "IOException from operationHelper", ex);
            throw ex;
        }
    }

    /**
     * Register a Webhook callback when an object is updated.
     *
     * @param objectType The object type on which to register a web hook.
     * @param url The URL of the web hook to be registered.
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     */
    public void registerWebHook(String objectType, URL url) throws SyncServiceException, IOException {
        webHookHelper("register", objectType, url);
    }

    /**
     * Delete a Webhook callback.
     *
     * @param objectType The object type whose web hook will be deleted.
     * @param url The URL of the webhook to delete.
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     */
    public void deleteWebHook(String objectType, URL url) throws SyncServiceException, IOException {
        webHookHelper("delete", objectType, url);
    }

    /**
     * A helper method for managing Webhook callbacks, used when an object is updated.
     *
     * @param operation The operation being performed (delete/register)
     * @param objectType The object on whom a web hook is being managed.
     * @param url The URL of the web hook being managed.
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     */
    private void webHookHelper(String operation, String objectType, URL url)
            throws SyncServiceException, IOException {
        final String METHOD = "webHookHelper";
        try {
            StringBuffer body = new StringBuffer(400);
            body.append("{\n");
            body.append("   \"action\": \"").append(operation).append("\",\n");
            body.append("   \"url\": \"").append(url.toString()).append("\"\n");
            body.append("}\n");

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),
                    body.toString().getBytes(Charset.forName("utf-8")));
            Response<Void> response = api.registerWebHook(objectType, requestBody).execute();
            if (!response.isSuccessful()) {
                String message = String.format("Failed to %s the webhook for %s:%s. Error: %s", operation, orgID, objectType,
                        response.errorBody().string());
                throw new SyncServiceException(message);
            }
        } catch (IOException ex) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD, "IOException from webHookHelper", ex);
            throw ex;
        }
    }

    /**
     * Resend requests that all objects in the Sync Service be resent to an ESS.
     * Used by an ESS to ask the CSS to resend it all the objects (supported only for ESS to CSS requests).
     * An application only needs to use this API in case the data it previously obtained from the ESS was lost.
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     */
    public void resend() throws SyncServiceException, IOException {
        final String METHOD = "resend";
        try {
            Response<Void> response = api.resend().execute();
            if (!response.isSuccessful()) {
                String message = String.format("Failed to request all objects to be resent. Error: %s",
                        response.errorBody().string());
                throw new SyncServiceException(message);
            }
        } catch (IOException ex) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD, "IOException from resend", ex);
            throw ex;
        }
    }

    /**
     * AddUsersToDestinationACL adds users to an ACL protecting a destination type.
     * 
     * For more information on the sync service's security model see:
     * <a href="https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security">https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security</a>
     * 
     * @param destType The destination type whose ACL is having usernames added to it.
     * @param usernames An array of the usernames to add to the specified ACL.
     * 
     * Note: Adding the first user to such an ACL automatically creates it.
     * 
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     * 
     * Note: This API is for use with a CSS only.
     */
    public void addUsersToDestinationACL(String destType, String[] usernames) throws SyncServiceException, IOException {
        modifySecurityHelper(true, DESTINATION_ACL, destType, usernames);
    }

    /**
     * RemoveUsersFromDestinationACL removes users from an ACL protecting a destination type.
     * 
     * For more information on the sync service's security model see:
     * <a href="https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security">https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security</a>
     * 
     * @param destType The destination type whose ACL is having usernames removed from it..
     * @param usernames An array of the usernames to remove from the specified ACL.
     * 
     * Note: Removing the last user from such an ACL automatically deletes it.
     * 
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     * 
     * Note: This API is for use with a CSS only.
     */
    public void removeUsersFromDestinationACL(String destType, String[] usernames) throws SyncServiceException, IOException {
	    modifySecurityHelper(false, DESTINATION_ACL, destType, usernames);
    }

    /**
     * RetrieveDestinationACL retrieves the list of users with access to a destination type protected by an ACL.
     * 
     * For more information on the sync service's security model see:
     * <a href="https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security">https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security</a>
     * 
     * @param destType The destination type from whose ACL the list of usernames should be retrieved.
     * 
     * @return A list of strings representing the usernames with access to the destination type protected by the ACL in question.
     * 
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     * 
     * Note: This API is for use with a CSS only.
     */
    public List<String> retrieveDestinationACL(String destType) throws SyncServiceException, IOException {
	    return retrieveACLHelper(DESTINATION_ACL, destType);
    }

    /**
     * RetrieveAllDestinationACLs retrieves the list of destination ACLs in the organization.
     * 
     * For more information on the sync service's security model see:
     * <a href="https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security">https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security</a>
     * 
     * @return A list of strings representing the destination types for which ACLs exist.
     * 
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     * 
     * Note: This API is for use with a CSS only.
     */
    public List<String> retrieveAllDestinationACLs() throws SyncServiceException, IOException {
	    return retrieveACLHelper(DESTINATION_ACL, "");
    }

    /**
     * AddUsersToObjectACL adds users to an ACL protecting an object type.
     * 
     * For more information on the sync service's security model see:
     * <a href="https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security">https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security</a>
     * 
     * @param objectType The object type whose ACL is having usernames added to it.
     * @param usernames An array of the usernames to add to the specified ACL.
     * 
     * Note: Adding the first user to such an ACL automatically creates it.
     * 
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     */
    public void addUsersToObjectACL(String objectType, String[] usernames) throws SyncServiceException, IOException {
	    modifySecurityHelper(true, OBJECT_ACL, objectType, usernames);
    }

    /**
     * RemoveUsersFromObjectACL removes users from an ACL protecting an object type.
     * 
     * For more information on the sync service's security model see:
     * <a href="https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security">https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security</a>
     * 
     * @param objectType The object type whose ACL is having usernames removed from it.
     * @param usernames An array of the usernames to remove from the specified ACL.
     * 
     * Note: Removing the last user from such an ACL automatically deletes it.
     * 
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     */
    public void removeUsersFromObjectACL(String objectType, String[] usernames) throws SyncServiceException, IOException {
	    modifySecurityHelper(false, OBJECT_ACL, objectType, usernames);
    }

    /**
     * RetrieveObjectACL retrieves the list of users with access to an object type protected by an ACL.
     * 
     * For more information on the sync service's security model see:
     * <a href="https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security">https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security</a>
     * 
     * @param objectType The object type from whose ACL the list of usernames should be retrieved.
     * 
     * @return A list of strings representing the usernames with access to the object type protected by the ACL in question.
     * 
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     */
    public List<String> retrieveObjectACL(String objectType) throws SyncServiceException, IOException {
	    return retrieveACLHelper(OBJECT_ACL, objectType);
    }

    /**
     * RetrieveAllObjectACLs retrieves the list of object ACLs in the organization.
     * 
     * For more information on the sync service's security model see:
     * <a href="https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security">https://github.ibm.com/edge-sync-service-dev/edge-sync-service#security</a>
     * 
     * @return A list of strings representing the object types for which ACLs exist.
     * 
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     */
    public List<String> retrieveAllObjectACLs() throws SyncServiceException, IOException {
	    return retrieveACLHelper(OBJECT_ACL, "");
    }

    /**
     * modifySecurityHelper is helper for modifying ACLs.
     * 
     * @param key       The key of the ACL that is having usernames added/removed to/from it.
     * @param usernames An array of the usernames to add/remove to/from the specified ACL.
     * 
     * @throws SyncServiceException If an error was returned from the Sync Server.
     * @throws IOException If an I/O error occurred.
     */
    protected void modifySecurityHelper(boolean add, String aclType, String key, String[] usernames) throws SyncServiceException, IOException {
        final String METHOD = "modifySecurityHelper";
        String mesageInsert = add ? "to" : "from";
        try {
            ACLBulkPayload payload = new ACLBulkPayload(add ? "add" : "remove", usernames);
            Response<Void> response = api.securityUpdate(aclType, key, payload).execute();
            if (!response.isSuccessful()) {
                String message = String.format("Failed to %s usernames %s the %s ACL %s. Error: %s", 
                        payload.getAction(), mesageInsert, aclType, key, response.errorBody().string());
                throw new SyncServiceException(message);
            }
        } catch (IOException ex) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD, "IOException from modifySecurityHelper", ex);
            throw ex;
        }
    }

    /**
     * retrieveACLHelper is a helper for retrieving ACL information
     */
    private List<String> retrieveACLHelper(String aclType, String key) throws SyncServiceException, IOException {
        final String METHOD = "getUpdatedObjects";
        try {
            Call<List<String>> call;
            if (key.equals("")) {
                call = api.retrieveAllACLs(aclType);
            }
            else {
                call = api.retrieveACL(aclType, key);
            }

            Response<List<String>> response = call.execute();            
            if (response.isSuccessful()) {
                List<String> metaData = response.body();
                return metaData;
            } else if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) {
                return new ArrayList<String>();
            } else {
                String message = key.equals("") ?
                            String.format("Failed to get the %s ACL for %s. Error: %s",
                                            aclType, key, response.errorBody().string()) :
                            String.format("Failed to get all of the %s ACLs. Error: %s",
                                            response.errorBody().string());
                throw new SyncServiceException(message);
            }
        } catch (IOException ex) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD, "IOException from getUpdatedObjects", ex);
            throw ex;
        }
    }

    /** The builder class used to create an instance of {@link SyncServiceClient SyncServiceClient} object */
    public static class Builder {
        private URL url;
        private Interceptor authInterceptor;
        private String orgID;
        private SSLContext sslContext;
        private X509TrustManager trustManager;
        private HostnameVerifier hostnameVerifier;
        private Long connectTimeoutMillis;
        private Long readTimeoutMillis;
        private Long writeTimeoutMillis;
        private static String NEGATIVEVALUE = "Negative value";
        private Interceptor interceptor;

        public Builder() {
            try {
                url = new URL(DEFAULT_HTTP_PROTOCOL, DEFAULT_HTTP_HOST, DEFAULT_HTTP_PORT, "");
                orgID = "";
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Sets the base URL that will be used by the built {@link SyncServiceClient SyncServiceClient} object.
         * @param url The URL to use as the SyncServiceClient's base URL.
         * @return this
         */
        public Builder withUrl(URL url) {
            this.url = url;
            return this;
        }

        /**
         * Set the app key and app secret to be used by the built {@link SyncServiceClient SyncServiceClient}
         *             object.
         * <p>The app key and app secret are used to authenticate with the Sync Service that the client is
         * communicating with. The exact details of the app key and app secret depend on the Sync Service's configuration.
         * @param appKey The app key.
         * @param appSecret The app secret.
         * @return this
         */
        public Builder withAppKeyAndAppSecret(String appKey, String appSecret) {
            authInterceptor = new BasicAuthInterceptor(appKey, appSecret);
            return this;
        }

        /**
         * Set the organization ID to be used by the built {@link SyncServiceClient SyncServiceClient} object.
         * @param orgID The organization ID.
         * @return this
         */
        public Builder withOrgID(String orgID) {
            this.orgID = orgID;
            return this;
        }

        /**
         * Set the SSLContext to be used by the built {@link SyncServiceClient SyncServiceClient} object.
         * @param sslContext The SSLContext.
         * @return this
         */
        public Builder withSslContext(SSLContext sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        /**
         * Set the X509TrustManager to be used by the built {@link SyncServiceClient SyncServiceClient} object.
         * @param trustManager The X509TrustManager.
         * @return this
         */
        public Builder withTrustManager(X509TrustManager trustManager) {
            this.trustManager = trustManager;
            return this;
        }

        /**
         * Set the HostnameVerifier to be used by the built {@link SyncServiceClient SyncServiceClient} object.
         * @param hostnameVerifier The HostnameVerifier.
         * @return this
         */
        public Builder withHostnameVerifier(HostnameVerifier hostnameVerifier) {
            this.hostnameVerifier = hostnameVerifier;
            return this;
        }

        /**
         * Set the connect timeout to be used by the built {@link SyncServiceClient SyncServiceClient} object.
         * @param timeoutMillis The connect timeout in milli-seconds.
         * @return this
         */
        public Builder withConnectTimeoutMillis(long timeoutMillis) {
            checkArgument(timeoutMillis >= 0, NEGATIVEVALUE);
            this.connectTimeoutMillis = timeoutMillis;
            return this;
        }

        /**
         * Set the read timeout to be used by the built {@link SyncServiceClient SyncServiceClient} object.
         * @param timeoutMillis The read timeout in milli-seconds.
         * @return this
         */
        public Builder withReadTimeoutMillis(long timeoutMillis) {
            checkArgument(timeoutMillis >= 0, NEGATIVEVALUE);
            this.readTimeoutMillis = timeoutMillis;
            return this;
        }

        /**
         * Set the write timeout to be used by the built {@link SyncServiceClient SyncServiceClient} object.
         * @param timeoutMillis The write timeout in milli-seconds.
         * @return this
         */
        public Builder withWriteTimeoutMillis(long timeoutMillis) {
            checkArgument(timeoutMillis >= 0, NEGATIVEVALUE);
            this.writeTimeoutMillis = timeoutMillis;
            return this;
        }

        /**
         * Add a {@link okhttp3.Interceptor Interceptor} to the {@link okhttp3.OkHttpClient OkHttpClient}
         *            used by the built {@link SyncServiceClient SyncServiceClient} object.
         * @param interceptor The Interceptor to add.
         * @return this
         */
        public Builder withInterceptor(Interceptor interceptor) {
            this.interceptor = interceptor;
            return this;
        }

        /**
         * Actually build the {@link SyncServiceClient SyncServiceClient} object.
         * @return A {@link SyncServiceClient SyncServiceClient} object
         */
        public SyncServiceClient build() {
            OkHttpClient okHttpClient = createOkHttpClient();

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new Jdk8Module());
            return new SyncServiceClient(url, orgID, mapper, okHttpClient);
        }

        /**
         * Helper method to build the {@link okhttp3.OkHttpClient OkHttpClient} used by the built
         *       {@link SyncServiceClient SyncServiceClient} object
         * @return A {@link okhttp3.OkHttpClient OkHttpClient}
         */
        private OkHttpClient createOkHttpClient() {
            final OkHttpClient.Builder builder = new OkHttpClient.Builder();

            if (authInterceptor != null) {
                builder.addInterceptor(authInterceptor);
            }

            if (sslContext != null && trustManager != null) {
                builder.sslSocketFactory(sslContext.getSocketFactory(), trustManager);
            } else if (sslContext != null) {
                builder.sslSocketFactory(sslContext.getSocketFactory());
            }

            if (hostnameVerifier != null) {
                builder.hostnameVerifier(hostnameVerifier);
            }

            if (connectTimeoutMillis != null) {
                builder.connectTimeout(connectTimeoutMillis, TimeUnit.MILLISECONDS);
            }

            if (readTimeoutMillis != null) {
                builder.readTimeout(readTimeoutMillis, TimeUnit.MILLISECONDS);
            }

            if (writeTimeoutMillis != null) {
                builder.writeTimeout(writeTimeoutMillis, TimeUnit.MILLISECONDS);
            }

            if (interceptor != null) {
                builder.addInterceptor(interceptor);
            }

            return builder.build();
        }
    }
}
