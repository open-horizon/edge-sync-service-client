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
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.Interceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Client for Sync-Service
 */
public class SyncServiceClient {

    private static final String CLASS_NAME = SyncServiceClient.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    public static final String DEFAULT_HTTP_PROTOCOL = "http";
    public static final String DEFAULT_HTTP_HOST = "localhost";
    public static final int DEFAULT_HTTP_PORT = 8080;

    private final String orgID;
    private final Api api;

    private SyncServiceClient(URL url, String orgID, ObjectMapper mapper, OkHttpClient httpClient) {
        this.orgID = orgID;
        Retrofit retrofit = new Retrofit.Builder().baseUrl(url.toExternalForm())
                .addConverterFactory(JacksonConverterFactory.create(mapper)).client(httpClient).build();
        this.api = retrofit.create(Api.class);
    }

    /***
     * Get the list of destinations in the org of the sync client
     * 
     * @return List<Destination>
     * @throws SyncServiceException
     * @throws IOException
     */
    public List<Destination> getDestinations()
            throws SyncServiceException, IOException {
        final String METHOD = "getDestinations";
        try {
            Response<List<Destination>> response = api.getDestinations(orgID).execute();
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

    /***
     * Get the list of objects at a destination
     * 
     * @param destType
     * @param destID
     * @return List<ObjectStatus>
     * @throws SyncServiceException
     * @throws IOException
     */
    public List<ObjectStatus> getDestinationObjects(String destType, String destID)
            throws SyncServiceException, IOException {
        final String METHOD = "getDestinationObjects";
        try {
            Response<List<ObjectStatus>> response = api.getDestinationObjects(orgID, destType, destID).execute();
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

    /***
     * Update an object. Only object metadata is sent in this request
     * 
     * @param metaData
     * @return
     * @throws SyncServiceException
     * @throws IOException
     */
    public void updateObject(SyncServiceMetaData metaData) throws SyncServiceException, IOException {
        final String METHOD = "updateObject";
        try {
            ObjectPayload payload = new ObjectPayload(metaData);
            Response<Void> response = api.putObject(orgID, metaData.getObjectType(), metaData.getObjectID(), payload)
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

    /***
     * Metadata of updated objects of the specified objectType are retrieved
     * 
     * @param objectType
     * @param received
     * @return List<SyncServiceMetaData>
     * @throws SyncServiceException
     * @throws IOException
     */
    public List<SyncServiceMetaData> getUpdatedObjects(String objectType, boolean received)
            throws SyncServiceException, IOException {
        final String METHOD = "getUpdatedObjects";
        try {
            Response<List<SyncServiceMetaData>> response = api.getUpdatedObjects(orgID, objectType, received).execute();
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

    /***
     * Get information about the destinations to which an object was sent
     * 
     * @param objectType
     * @param objectID
     * @return List<DestinationStatus>
     * @throws SyncServiceException
     * @throws IOException
     */
    public List<DestinationStatus> getObjectDestinations(String objectType, String objectID)
            throws SyncServiceException, IOException {
        final String METHOD = "getObjectDestinations";
        try {
            Response<List<DestinationStatus>> response = api.getObjectDestinations(orgID, objectType, objectID).execute();
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
     * Update an object's data from an array of bytes
     * 
     * @param metaData
     * @param input
     * @return
     * @throws SyncServiceException
     * @throws IOException
     */
    public void updateObjectData(SyncServiceMetaData metaData, byte[] input)
            throws SyncServiceException, IOException {
        final String METHOD = "updateObjectData";
        try {
            RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"), input);
            Response<Void> response = api.putObjectData(orgID, metaData.getObjectType(), metaData.getObjectID(), body)
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
     * Update an object's data from a file
     *
     * @param metaData
     * @param input
     * @return
     * @throws SyncServiceException
     * @throws IOException
     */
    public void updateObjectData(SyncServiceMetaData metaData, File input)
            throws SyncServiceException, IOException {
        final String METHOD = "updateObjectData";
        try {
            RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"), input);
            Response<Void> response = api.putObjectData(orgID, metaData.getObjectType(), metaData.getObjectID(), body)
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
     * Fetch an object's data and return it as a byte array
     *
     * @param metaData
     * @return byte[]
     * @throws SyncServiceException
     * @throws IOException
     */
    public byte[] fetchObjectData(SyncServiceMetaData metaData) throws SyncServiceException, IOException {
        final String METHOD = "fetchObjectData";
        try {
            Response<ResponseBody> response = api.getObjectData(orgID, metaData.getObjectType(), metaData.getObjectID())
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
     * Fetch an object's data and write it to a file
     *
     * @param metaData
     * @param file
     * @return void
     * @throws SyncServiceException
     * @throws IOException
     */
    public void fetchObjectData(SyncServiceMetaData metaData, File file)
            throws SyncServiceException, IOException {
        final String METHOD = "fetchObjectData";
        try {
            Response<ResponseBody> response = api.getObjectData(orgID, metaData.getObjectType(), metaData.getObjectID())
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

    /***
     * Delete an object
     *
     * @param metaData
     * @return
     * @throws SyncServiceException
     * @throws IOException
     */
    public void deleteObject(SyncServiceMetaData metaData) throws SyncServiceException, IOException {
        final String METHOD = "deleteObject";
        try {
            Response<Void> response = api.deleteObject(orgID, metaData.getObjectType(), metaData.getObjectID())
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

    /***
     * Activate an object
     *
     * @param metaData
     * @return
     * @throws SyncServiceException
     * @throws IOException
     */
    public void activateObject(SyncServiceMetaData metaData) throws SyncServiceException, IOException {
        operationHelper(metaData, "activate");
    }

    /***
     * Mark an object as being consumed
     *
     * @param metaData
     * @return
     * @throws SyncServiceException
     * @throws IOException
     */
    public void markObjectConsumed(SyncServiceMetaData metaData)
            throws SyncServiceException, IOException {
        operationHelper(metaData, "consumed");
    }

    /***
     * Mark an object as being deleted
     *
     * @param metaData
     * @return
     * @throws SyncServiceException
     * @throws IOException
     */
    public void markObjectDeleted(SyncServiceMetaData metaData) throws SyncServiceException, IOException {
        operationHelper(metaData, "deleted");
    }

    /***
     * Mark an object as being received
     *
     * @param metaData
     * @return
     * @throws SyncServiceException
     * @throws IOException
     */
    public void markObjectReceived(SyncServiceMetaData metaData) throws SyncServiceException, IOException {
        operationHelper(metaData, "received");
    }

    /***
     * Perform an "operation" on an object
     *
     * @param metaData
     * @param operation
     * @return
     * @throws SyncServiceException
     * @throws IOException
     */
    private void operationHelper(SyncServiceMetaData metaData, String operation)
            throws SyncServiceException, IOException {
        final String METHOD = "operationHelper";
        try {
            Response<Void> response = api
                    .objectOperation(orgID, metaData.getObjectType(), metaData.getObjectID(), operation).execute();
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

    /***
     * Register a Webhook callback when an object is updated
     *
     * @param objectType
     * @return
     * @throws SyncServiceException
     * @throws IOException
     */
    public void registerWebHook(String objectType, URL url) throws SyncServiceException, IOException {
        webHookHelper("register", objectType, url);
    }

    /***
     * Delete a Webhook callback
     *
     * @param objectType
     * @return
     * @throws SyncServiceException
     * @throws IOException
     */
    public void deleteWebHook(String objectType, URL url) throws SyncServiceException, IOException {
        webHookHelper("delete", objectType, url);
    }

    /***
     * Register a Webhook callback when an object is updated
     *
     * @param objectType
     * @return
     * @throws SyncServiceException
     * @throws IOException
     */
    private void webHookHelper(String operation, String objectType, URL url)
            throws SyncServiceException, IOException {
        final String METHOD = "registerWebHook";
        try {
            StringBuffer body = new StringBuffer(400);
            body.append("{\n");
            body.append("   \"action\": \"").append(operation).append("\",\n");
            body.append("   \"url\": \"").append(url.toString()).append("\"\n");
            body.append("}\n");

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),
                    body.toString().getBytes(Charset.forName("utf-8")));
            Response<Void> response = api.registerWebHook(orgID, objectType, requestBody).execute();
            if (!response.isSuccessful()) {
                String message = String.format("Failed to register the webhook for %s:%s. Error: %s", orgID, objectType,
                        response.errorBody().string());
                throw new SyncServiceException(message);
            }
        } catch (IOException ex) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD, "IOException from registerWebHook", ex);
            throw ex;
        }
    }

    interface Api {

        @GET("/api/v1/destinations/{orgID}")
        Call<List<Destination>> getDestinations(@Path("orgID") String orgID);

        @GET("/api/v1/destinations/{orgID}/{destType}/{destID}/objects")
        Call<List<ObjectStatus>> getDestinationObjects(@Path("orgID") String orgID,
                @Path("destType") String destType, @Path("destID") String destID);

        @GET("/api/v1/objects/{orgID}/{objectType}?received={received}")
        Call<List<SyncServiceMetaData>> getUpdatedObjects(@Path("orgID") String orgID,
                @Path("objectType") String objectType, @Path("received") boolean received);

        @GET("/api/v1/objects/{orgID}/{objectType}/{objectID}/data")
        Call<ResponseBody> getObjectData(@Path("orgID") String orgID, @Path("objectType") String objectType,
                @Path("objectID") String objectID);

        @GET("/api/v1/objects/{orgID}/{objectType}/{objectID}/destinations")
        Call<List<DestinationStatus>> getObjectDestinations(@Path("orgID") String orgID,
                @Path("objectType") String objectType, @Path("objectID") String objectID);

        @PUT("/api/v1/objects/{orgID}/{objectType}/{objectID}")
        Call<Void> putObject(@Path("orgID") String orgID, @Path("objectType") String objectType,
                @Path("objectID") String objectID, @Body ObjectPayload payload);

        @PUT("/api/v1/objects/{orgID}/{objectType}/{objectID}/data")
        Call<Void> putObjectData(@Path("orgID") String orgID, @Path("objectType") String objectType,
                @Path("objectID") String objectID, @Body RequestBody requestBody);

        @PUT("/api/v1/objects/{orgID}/{objectType}/{objectID}/{operation}")
        Call<Void> objectOperation(@Path("orgID") String orgID, @Path("objectType") String objectType,
                @Path("objectID") String objectID, @Path("operation") String operation);

        @DELETE("/api/v1/objects/{orgID}/{objectType}/{objectID}")
        Call<Void> deleteObject(@Path("orgID") String orgID, @Path("objectType") String objectType,
                @Path("objectID") String objectID);

        @PUT("/api/v1/objects/{orgID}/{objectType}")
        Call<Void> registerWebHook(@Path("orgID") String orgID, @Path("objectType") String objectType,
                @Body RequestBody requestBody);
    }

    public static class Builder {
        private URL url;
        private BasicAuthInterceptor authInterceptor;
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
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        public Builder withUrl(URL url) {
            this.url = url;
            return this;
        }

        public Builder withAppKeyAndAppSecret(String appKey, String appSecret) {
            authInterceptor = new BasicAuthInterceptor(appKey, appSecret);
            return this;
        }

        public Builder withOrgID(String orgID) {
            this.orgID = orgID;
            return this;
        }

        public Builder withSslContext(SSLContext sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        public Builder withTrustManager(X509TrustManager trustManager) {
            this.trustManager = trustManager;
            return this;
        }

        public Builder withHostnameVerifier(HostnameVerifier hostnameVerifier) {
            this.hostnameVerifier = hostnameVerifier;
            return this;
        }

        public Builder withConnectTimeoutMillis(long timeoutMillis) {
            checkArgument(timeoutMillis >= 0, NEGATIVEVALUE);
            this.connectTimeoutMillis = timeoutMillis;
            return this;
        }

        public Builder withReadTimeoutMillis(long timeoutMillis) {
            checkArgument(timeoutMillis >= 0, NEGATIVEVALUE);
            this.readTimeoutMillis = timeoutMillis;
            return this;
        }

        public Builder withWriteTimeoutMillis(long timeoutMillis) {
            checkArgument(timeoutMillis >= 0, NEGATIVEVALUE);
            this.writeTimeoutMillis = timeoutMillis;
            return this;
        }

        public Builder withInterceptor(Interceptor interceptor) {
            this.interceptor = interceptor;
            return this;
        }

        public SyncServiceClient build() {
            OkHttpClient okHttpClient = createOkHttpClient();

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new Jdk8Module());
            return new SyncServiceClient(url, orgID, mapper, okHttpClient);
        }

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
