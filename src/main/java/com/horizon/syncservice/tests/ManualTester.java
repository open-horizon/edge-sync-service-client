package com.horizon.syncservice.tests;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.horizon.syncservice.client.SyncServiceClient;
import com.horizon.syncservice.client.SyncServiceMetaData;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class ManualTester {
    private enum Tests {
        HELP, OBJSTATUS, RESEND, SECURITY
    }

    private Tests test;
    private Map<Tests, Options> optionsMap = new HashMap<Tests, Options>();

    private String orgID;
    private String destType;
    private String destID;
    private String objectType;
    private String objectID;
    private String username;
    private boolean remove;
    private String serverProtocol;
    private String serverAddress;
    private String appKey;
    private String appSecret;

    public static void main(String[] argv) {
        ManualTester tester = new ManualTester();
        int rc = tester.parseArgs(argv);
        if (rc == 0) {
            rc = tester.runTests();
        }

        System.exit(rc);
    }

    private int runTests() {

        try {
            switch(test) {
                case HELP:
                    showUsage();
                    return 0;

                case OBJSTATUS:
                    return runObjectStatusTest();

                case RESEND:
                    return runResendTest();

                case SECURITY:
                    return runSecurityTest();
            }

            return 0;
        }
        catch (Exception e) {
            System.err.println("Failed to run test. Exception thrown: " + e.getMessage());
            e.printStackTrace();
            return 99;
        }
    }

    private int runObjectStatusTest() throws Exception {
        SyncServiceClient syncClient = createSyncServiceClient();
        String result = syncClient.getObjectStatus(objectType, objectID);
        System.out.println("The status of " + objectType + ":" + objectID + " is " + result);
        return 0;
    }

    private int runResendTest() throws Exception {
        SyncServiceClient syncClient = createSyncServiceClient();
        syncClient.resend();
        return 0;
    }

    private int runSecurityTest() throws Exception {
        SyncServiceClient syncClient = createSyncServiceClient();

        String[] usernames = new String[1];
        usernames[0] = username;
        if (destType.length() != 0) {
            if (remove) {
                syncClient.removeUsersFromDestinationACL(destType, usernames);
            }
            else {
                syncClient.addUsersToDestinationACL(destType, usernames);
            }
        }
        else if (objectType.length() != 0) {
            if (remove) {
                syncClient.removeUsersFromObjectACL(objectType, usernames);
            }
            else {
                syncClient.addUsersToObjectACL(objectType, usernames);
            }
        }
        else {
            printACLsHelper(syncClient, "destination");
            System.out.println();
            printACLsHelper(syncClient, "object");
        }
        return 0;
    }

    private void printACLsHelper(SyncServiceClient syncClient, String aclType) throws Exception {
        List<String> acls;
        if (aclType.equals("destination")) {
            acls = syncClient.retrieveAllDestinationACLs();
        }
        else {
            acls = syncClient.retrieveAllObjectACLs();
        }

        if (acls.size() > 0) {
            System.out.println(aclType + " ACLs:");
            for (String acl : acls) {
                List<String> usernames;
                if (aclType.equals("destination")) {
                    usernames = syncClient.retrieveDestinationACL(acl);
                }
                else {
                    usernames = syncClient.retrieveObjectACL(acl);
                }

                System.out.print("    " + acl + ":");
                for (int i=0  ;  i < usernames.size()  ;  i++) {
                    if (i % 8 == 0) {
                        System.out.print("\n        " + usernames.get(i));
                    }
                    else {
                        System.out.print(", " + usernames.get(i));
                    }
                }
                System.out.println();
            }
        }
        else {
            System.out.println("No " + aclType + " ACLs exist.");
        }
    }

    private int parseArgs(String[] args) {
        setupOptions();

        if (args.length < 1) {
            System.err.println("You must at least provide a test name");
            showUsage();
            return 1;
        }

        try {
            test = Tests.valueOf(args[0].toUpperCase());
        }
        catch(IllegalArgumentException e) {
            System.err.println("The first argument " + args[0] + " is invalid.");
            showUsage();
            return 1;
        }

        Options options = optionsMap.get(test);
        if (options == null) {
            return 0;
        }

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            orgID = cmd.getOptionValue("orgID", "");
            destType = cmd.getOptionValue("destType", "");
            destID = cmd.getOptionValue("destID", "");
            objectType = cmd.getOptionValue("objectType", "");
            objectID = cmd.getOptionValue("objectID", "");
            username = cmd.getOptionValue("username", "");
            remove = cmd.hasOption("remove");

            serverProtocol = cmd.getOptionValue("serverProtocol", "https");
            serverAddress = cmd.getOptionValue("serverAddress", "localhost:8443");

            appKey = cmd.getOptionValue("appKey", "");
            appSecret = cmd.getOptionValue("appSecret", "");
        }
        catch (ParseException e) {
            System.out.println(e.getMessage());

            return 1;
        }

        return 0;
    }

    private void setupOptions() {
        Options options;

        // objectStatus
        options = new Options();
        addCommonOptions(options);
        options.addOption("org", "orgID", true, "Specify the organization ID of the object (optional)");
        options.addOption("ot", "objectType", true, "Specify the object type of the object");
        options.addOption("oid", "objectID", true, "Specify the object ID of the objects");
        optionsMap.put(Tests.OBJSTATUS, options);

        // resend
        options = new Options();
        addCommonOptions(options);
        optionsMap.put(Tests.RESEND, options);

        // security
        options = new Options();
        addCommonOptions(options);
        options.addOption("org", "orgID", true, "Specify the organization ID of the ACL (optional)");
        options.addOption("dt", "destType", true, "Specify the destination type of the ACL");
        options.addOption("ot", "objectType", true, "Specify the object type of the ACL");
        options.addOption("id", "username", true, "Specify the user name to be added/removed from the ACL");
        options.addOption("remove", "remove", false, "Indicated that the user shoul be removed from the ACL");
        optionsMap.put(Tests.SECURITY, options);
    }

    private void addCommonOptions(Options options) {
        options.addOption("p", "serverProtocol", true, "Specify the protocol of the Cloud Sync Service");
        options.addOption("s", "serverAddress", true, "Specify the address and port of the Cloud Sync Service");
        options.addOption("key", "appKey", true, "Specify the app key to be used when connecting to the Sync Service");
        options.addOption("secret", "appSecret", false, "Specify the app secret to be used when connecting to the Sync Service");
    }

    private SyncServiceClient createSyncServiceClient() throws MalformedURLException {
        return new SyncServiceClient.Builder()
           .withUrl(new URL(serverProtocol + "://" + serverAddress))
           .withAppKeyAndAppSecret(appKey, appSecret)
           .withOrgID(orgID)
           .build();
    }

    private void showUsage() {
        System.out.println("ManualTester help");
        System.out.println("");
        System.out.println("ManualTester objstatus");
        System.out.println("");
        System.out.println("ManualTester resend");
        System.out.println("");
        System.out.println("ManualTester security");
    }
}