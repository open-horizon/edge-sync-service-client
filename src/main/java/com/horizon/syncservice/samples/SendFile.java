package com.horizon.syncservice.samples;

import com.horizon.syncservice.client.SyncServiceClient;
import com.horizon.syncservice.client.SyncServiceMetaData;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.net.URL;

public class SendFile {
    private String orgID;
    private String destType;
    private String destID;
    private String fileName;
    private String serverProtocol;
    private String serverAddress;
    private String appKey;
    private String appSecret;

    public final static void main(String[] args) {
        SendFile sender = new SendFile();
        sender.parseArgs(args);
        sender.send();
    }

    private void send() {
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                System.out.format("The file %s does not exist.\n", fileName);
                return;
            }
            if (!file.isFile()) {
                System.out.format("The file %s is not a regular file.\n", fileName);
                return;
            }
            if (!file.canRead()) {
                System.out.format("The file %s can not be read.\n", fileName);
                return;
            }

            String[] parts = fileName.split("/");

            SyncServiceMetaData metaData = new SyncServiceMetaData();
            metaData.setObjectType("send-file");
            metaData.setObjectID(parts[parts.length-1] + "@-" + destType + "-" + destID);
            metaData.setDestType(destType);
            metaData.setDestID(destID);
            metaData.setVersion("0.0.1");

            SyncServiceClient syncClient =
                 new SyncServiceClient.Builder()
                    .withUrl(new URL(serverProtocol + "://" + serverAddress))
                    .withAppKeyAndAppSecret(appKey, appSecret)
                    .withOrgID(orgID)
                    .build();

            syncClient.updateObject(metaData);

            syncClient.updateObjectData(metaData, file);

            System.out.format("File %s sent\n", fileName);
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            System.err.format("Failed to send the file %s to %s:%s:%s. Error: %s\n",
                                fileName, orgID, destType, destID, cause.getMessage());
		}
    }

    private void parseArgs(String[] args) {
        Options options = new Options();

        options.addOption("org", "orgID", true, "Specify the organization ID of the destination to send the file to (optional)");

        options.addOption("dt", "destType", true, "Specify the destination type to send the file to.");

        options.addOption("id", "destID", true, "Specify the destination ID to send the file to");

        options.addOption("f", "fileName", true, "Specify the file to send");
        options.getOption("fileName").setRequired(true);

        options.addOption("h", "help", false, "Display usage information.");

        options.addOption("p", "serverProtocol", true, "Specify the protocol of the Cloud Sync Service");

        options.addOption("s", "serverAddress", true, "Specify the address and port of the Cloud Sync Service");

        options.addOption("key", "appKey", true, "Specify the app key to be used when connecting to the Sync Service");
        options.addOption("secret", "appSecret", false, "Specify the app secret to be used when connecting to the Sync Service");

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("help")) {
                formatter.printHelp("send-file", options);
                System.exit(0);
            }
    
            orgID = cmd.getOptionValue("orgID", "");
            destType = cmd.getOptionValue("destType", "");
            destID = cmd.getOptionValue("destID", "");
            fileName = cmd.getOptionValue("fileName");

            serverProtocol = cmd.getOptionValue("serverProtocol", "https");
            serverAddress = cmd.getOptionValue("serverAddress", "localhost:8443");

            appKey = cmd.getOptionValue("appKey", "");
            appSecret = cmd.getOptionValue("appSecret", "");

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("send-file", options);

            System.exit(1);
        }

	//cert           = flag.String("cert", "", "Specifiy the file containing the server's CA certificate");
    }
}