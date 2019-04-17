package com.horizon.syncservice.client;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;

class RetrofitHelper {
    private final String orgID;
    private final ApiCSS apiCSS;
    private final ApiESS apiESS;

    RetrofitHelper(String orgID, Retrofit retrofit) {
        this.orgID = orgID;
        this.apiCSS = retrofit.create(ApiCSS.class);
        this.apiESS = retrofit.create(ApiESS.class);
    }

    protected Call<List<Destination>> getDestinations() {
        return orgID.equals("") ? apiESS.getDestinations() : apiCSS.getDestinations(orgID);
    }

    protected Call<List<ObjectStatus>> getDestinationObjects(String destType, String destID) {
        return orgID.equals("") ?
                apiESS.getDestinationObjects(destType, destID) :
                apiCSS.getDestinationObjects(orgID, destType, destID);
    }

    protected Call<List<SyncServiceMetaData>> getUpdatedObjects(String objectType, boolean received) {
        return orgID.equals("") ?
                apiESS.getUpdatedObjects(objectType, received) :
                apiCSS.getUpdatedObjects(orgID, objectType, received);
    }

    protected Call<ResponseBody> getObjectData(String objectType, String objectID) {
        return orgID.equals("") ?
                apiESS.getObjectData(objectType, objectID) :
                apiCSS.getObjectData(orgID, objectType, objectID);
    }

    protected Call<List<DestinationStatus>> getObjectDestinations(String objectType, String objectID) {
        return orgID.equals("") ?
                apiESS.getObjectDestinations(objectType, objectID) :
                apiCSS.getObjectDestinations(orgID, objectType, objectID);
    }

    protected Call<String> getObjectStatus(String objectType, String objectID) {
        return orgID.equals("") ?
                apiESS.getObjectStatus(objectType, objectID) :
                apiCSS.getObjectStatus(orgID, objectType, objectID);
    }

    protected Call<Void> putObject(String objectType, String objectID, ObjectPayload payload) {
        return orgID.equals("") ?
                apiESS.putObject(objectType, objectID, payload) :
                apiCSS.putObject(orgID, objectType, objectID, payload);
    }

    protected Call<Void> putObjectData(String objectType, String objectID, RequestBody requestBody) {
        return orgID.equals("") ?
                apiESS.putObjectData(objectType, objectID, requestBody) :
                apiCSS.putObjectData(orgID, objectType, objectID, requestBody);
    }

    protected Call<Void> objectOperation(String objectType, String objectID, String operation) {
        return orgID.equals("") ?
                apiESS.objectOperation(objectType, objectID, operation) :
                apiCSS.objectOperation(orgID, objectType, objectID, operation);
    }

    protected Call<Void> deleteObject(String objectType, String objectID) {
        return orgID.equals("") ?
                apiESS.deleteObject(objectType, objectID) :
                apiCSS.deleteObject(orgID, objectType, objectID);
    }

    protected Call<Void> registerWebHook(String objectType, RequestBody requestBody) {
        return orgID.equals("") ?
                apiESS.registerWebHook(objectType, requestBody) :
                apiCSS.registerWebHook(orgID, objectType, requestBody);
    }

    protected Call<Void> resend() {
        return orgID.equals("") ? apiESS.resend() : apiCSS.resend();
    }

    protected Call<Void> securityUpdate(String aclType, String key, ACLBulkPayload payload) {
        return orgID.equals("") ?
                apiESS.securityUpdate(aclType, key, payload) :
                apiCSS.securityUpdate(aclType, orgID, key, payload);
    }

    protected Call<List<String>> retrieveACL(String aclType, String key) {
        return orgID.equals("") ? apiESS.retrieveACL(aclType, key) : apiCSS.retrieveACL(aclType, orgID, key);
    }

    protected Call<List<String>> retrieveAllACLs(String aclType) {
        return orgID.equals("") ? apiESS.retrieveAllACLs(aclType) : apiCSS.retrieveAllACLs(aclType, orgID);
    }

    interface ApiCSS {

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

        @GET("/api/v1/objects/{orgID}/{objectType}/{objectID}/status")
        Call<String> getObjectStatus(@Path("orgID") String orgID,
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

        @POST("/api/v1/resend")
        Call<Void> resend();

        @PUT("/api/v1/security/{aclType}/{orgID}/{key}")
        Call<Void> securityUpdate(@Path("aclType") String aclType, @Path("orgID") String orgID, @Path("key") String key,
                @Body ACLBulkPayload payload);

        @GET("/api/v1/security/{aclType}/{orgID}/{key}")
        Call<List<String>> retrieveACL(@Path("aclType") String aclType, @Path("orgID") String orgID, @Path("key") String key);

        @GET("/api/v1/security/{aclType}/{orgID}")
        Call<List<String>> retrieveAllACLs(@Path("aclType") String aclType, @Path("orgID") String orgID);
    }

    interface ApiESS {

        @GET("/api/v1/destinations")
        Call<List<Destination>> getDestinations();

        @GET("/api/v1/destinations/{destType}/{destID}/objects")
        Call<List<ObjectStatus>> getDestinationObjects(@Path("destType") String destType, @Path("destID") String destID);

        @GET("/api/v1/objects/{objectType}?received={received}")
        Call<List<SyncServiceMetaData>> getUpdatedObjects(@Path("objectType") String objectType, @Path("received") boolean received);

        @GET("/api/v1/objects/{objectType}/{objectID}/data")
        Call<ResponseBody> getObjectData(@Path("objectType") String objectType,
                @Path("objectID") String objectID);

        @GET("/api/v1/objects/{objectType}/{objectID}/destinations")
        Call<List<DestinationStatus>> getObjectDestinations(@Path("objectType") String objectType, @Path("objectID") String objectID);

        @GET("/api/v1/objects/{objectType}/{objectID}/status")
        Call<String> getObjectStatus(@Path("objectType") String objectType, @Path("objectID") String objectID);

        @PUT("/api/v1/objects/{objectType}/{objectID}")
        Call<Void> putObject(@Path("objectType") String objectType,
                @Path("objectID") String objectID, @Body ObjectPayload payload);

        @PUT("/api/v1/objects/{objectType}/{objectID}/data")
        Call<Void> putObjectData(@Path("objectType") String objectType,
                @Path("objectID") String objectID, @Body RequestBody requestBody);

        @PUT("/api/v1/objects/{objectType}/{objectID}/{operation}")
        Call<Void> objectOperation(@Path("objectType") String objectType,
                @Path("objectID") String objectID, @Path("operation") String operation);

        @DELETE("/api/v1/objects/{objectType}/{objectID}")
        Call<Void> deleteObject(@Path("objectType") String objectType,
                @Path("objectID") String objectID);

        @PUT("/api/v1/objects/{objectType}")
        Call<Void> registerWebHook(@Path("objectType") String objectType,
                @Body RequestBody requestBody);

        @POST("/api/v1/resend")
        Call<Void> resend();

        @PUT("/api/v1/security/{aclType}/{key}")
        Call<Void> securityUpdate(@Path("aclType") String aclType, @Path("key") String key, @Body ACLBulkPayload payload);

        @GET("/api/v1/security/{aclType}/{key}")
        Call<List<String>> retrieveACL(@Path("aclType") String aclType, @Path("key") String key);

        @GET("/api/v1/security/{aclType}")
        Call<List<String>> retrieveAllACLs(@Path("aclType") String aclType);
    }
} 