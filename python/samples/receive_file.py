""" Python equivalent of the Go receive_file """

import getopt
import os
import sys
import time

if sys.path[0] != '':
    sys.path.insert(0, sys.path[0]+"/../")

from client import SyncServiceClient

def receive_callback(meta_data):
    parts = meta_data.object_id.split("@")
    if meta_data.deleted:
        os.remove("./" + parts[0])
        print ("Deleted " + parts[0])
        sync_client.mark_object_deleted(meta_data)
    else:
        file = open("./" + parts[0], "w")
        if sync_client.fetch_object_data(meta_data, file):
            print ("Received " + parts[0])
            sync_client.mark_object_consumed(meta_data)

protocol = "http"
server = "localhost:8080"
org_id = ""
cert = ""
key = ""
secret = ""
opts, args = getopt.getopt(sys.argv[1:], "p:s:", ["org=", "cert=", "key=", "secret="])
for opt, value in opts:
    if opt == "-p":
        protocol = value
    elif opt == "-s":
        server = value
    elif opt == "--org":
        org_id = value
    elif opt == "--cert":
        cert = value
    elif opt == "--key":
        key = value
    elif opt == "--secret":
        secret = value

if protocol != "unix" and protocol != "secure-unix":
    parts = server.split(":")
    if parts[0] != "":
        host = parts[0]
    if parts[1] != "":
        if parts[1].isdigit():
            port = int(parts[1])
        else:
            print ("An invalid port number of ", parts[1], "was specified")
            sys.exit(99)
else:
    host = server
    port = 8080

sync_client = SyncServiceClient.Client(protocol, host, port)

if org_id != "":
    sync_client.set_org_id(org_id)
if cert != "":
    sync_client.set_ca_certificate(cert)
if key != "":
    sync_client.set_app_key_and_secret(key, secret)


sync_client.start_polling_for_updates("send-file", 5, receive_callback)

user_input = input("Press enter to exit\n")
    
sync_client.stop_polling_for_updates()
time.sleep(5)

sys.exit(0)
