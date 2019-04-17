""" Python SDK manual tester """

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
        print "Deleted " + parts[0]
        sync_client.mark_object_deleted(meta_data)
    else:
        file = open("./" + parts[0], "w")
        if sync_client.fetch_object_data(meta_data, file):
            print "Received " + parts[0]
            sync_client.mark_object_consumed(meta_data)

def print_acls_helper(acl_type):
    if acl_type == "destination":
        acls, ok = sync_client.retrieve_all_destination_acls()    
    else:
        acls, ok = sync_client.retrieve_all_object_acls()
    if not ok:
        print "Failed to retrieve all", acl_type, "ACLs"
        return

    if len(acls) != 0:
        print acl_type, "ACLs:"
        
        for acl in acls:
            print "   ", acl + ":"

            if acl_type == "destination":
                usernames, ok = sync_client.retrieve_destination_acl(acl)    
            else:
                usernames, ok = sync_client.retrieve_object_acl(acl)
            if not ok:
                print "Failed to retrieve usernames from", acl_type, acl, "ACL"
                return

            print "       ", usernames
    else:
        print "There are no", acl_type, "ACLs"

def run_security_test():
    if len(dest_type) != 0:
        if remove:
            result = sync_client.remove_users_from_destination_acl(dest_type, [user])
            if not result:
                print "Failed to remove the user from the ACL"
        else:
            result = sync_client.add_users_to_destination_acl(dest_type, [user])
            if not result:
                print "Failed to add the user to the ACL"
    elif len(object_type) != 0:
        if remove:
            result = sync_client.remove_users_from_object_acl(object_type, [user])
            if not result:
                print "Failed to remove the user from the ACL"
        else:
            result = sync_client.add_users_to_object_acl(object_type, [user])
            if not result:
                print "Failed to add the user to the ACL"
    else:
        print_acls_helper("destination")
        print
        print_acls_helper("object")

protocol = "http"
server = "localhost:8080"
org_id = ""
object_type = ""
object_id = ""
dest_type = ""
dest_id = ""
remove = False
user = ""
cert = ""
key = ""
secret = ""
test = sys.argv[1].lower()

opts, args = getopt.getopt(sys.argv[2:], "p:s:", ["org=", "ot=", "oid=", "dt=", "id=", "remove", "user=", "cert=", "key=", "secret="])
for opt, value in opts:
    if opt == "-p":
        protocol = value
    elif opt == "-s":
        server = value
    elif opt == "--org":
        org_id = value
    elif opt == "--ot":
        object_type = value
    elif opt == "--oid":
        object_id = value
    elif opt == "--dt":
        dest_type = value
    elif opt == "--id":
        dest_id = value
    elif opt == "--remove":
        remove = True
    elif opt == "--user":
        user = value
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
            print "An invalid port number of ", parts[1], "was specified"
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


if test == "deleteobject":
    if sync_client.delete_object(object_type, object_id):
        print "Object", object_type+":"+object_id, "deleted"
    else:
        print "Failed to delete object", object_type+":"+object_id

elif test == "deletewebhook":
    if sync_client.delete_webhook(object_type, "http://localhost:1234/webhook"):
        print "Webhook deleted"
    else:
        print "Failed to delete webhook"

elif test == "destinations":
    (results, ok) = sync_client.get_destinations()
    if ok:
        print "Retrieved destinations:"
        print results
    else:
        print "Failed to retrieve destinations"

elif test == "destinationobjects":
    (results, ok) = sync_client.get_destination_objects(dest_type, dest_id)
    if ok:
        print "Retrieved objects for destination:"
        print results
    else:
        print "Failed to retrieve objects for destination"

elif test == "object":
    (result, ok) = sync_client.get_object_metadata(object_type, object_id)
    if ok:
        if result != None:
            print "Retrieved object:"
            print result
        else:
            print "Object not found"
    else:
        print "Failed to retrieve object"

elif test == "objectdestinations":
    (results, ok) = sync_client.get_object_destinations(object_type, object_id)
    if ok:
        print "Retrieved destinations for object:"
        print results
    else:
        print "Failed to retrieve destinations for object"

elif test == "objectstatus":
    (result, ok) = sync_client.get_object_status(object_type, object_id)
    if ok:
        if result != None:
            print "Retrieved object status:", result
        else:
            print "Object not found"
    else:
        print "Failed to retrieve object status"

elif test == "registerwebhook":
    if sync_client.register_webhook(object_type, "http://localhost:1234/webhook"):
        print "Webhook registered"
    else:
        print "Failed to register webhook"

elif test == "resend":
    if sync_client.resend():
        print "Successfully request a resending of all objects"
    else:
        print "Failed to request a resending of all objects"

elif test == "security":
    run_security_test()

else:
    print "An invalid test of", os.argv[1], "was supplid."
