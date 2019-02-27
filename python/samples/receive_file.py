""" Python equivalent of the Go receive_file """

import getopt
import os
import sys

if sys.path[0] != '':
    sys.path.insert(0, sys.path[0]+"/../")

from client import SyncServiceClient

def receive_callback(meta_data):
    parts = meta_data.object_id.split("@")
    if meta_data.deleted:
        os.remove("./" + parts[0])
        print "Deleted " + parts[0]
        sync_client.mark_object_deleted(meta_data)
    else:
        file = open("./" + parts[0], "w")
        if sync_client.fetch_object_data(meta_data, file):
            print "Received " + parts[0]
            sync_client.mark_object_consumed(meta_data)

sync_client = SyncServiceClient.Client("https", "localhost", 6002)
# sync_client = SyncServiceClient.Client("unix", "../persist/sync/ess.sock", 8080)
# sync_client = SyncServiceClient.Client("secure-unix", "../persist/sync/ess.sock", 8080)

sync_client.set_ca_certificate("../persist/sync/certs/cert.pem")

key = ""
secret = ""
opts, args = getopt.getopt(sys.argv[1:], "k:s:")
for opt, value in opts:
    if opt == "-k":
        key = value
    if opt == "-s":
        secret = value
if key != "":
    sync_client.set_app_key_and_secret(key, secret)


sync_client.start_polling_for_updates("send-file", 5, receive_callback)

user_input = raw_input("Press enter to exit\n")

sys.exit(0)