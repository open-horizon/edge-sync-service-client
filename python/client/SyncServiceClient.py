""" SyncServiceClient is a client SDK of the Sync-Service written in Python

    Exported classes: Client
"""

import json
import shutil
import sys
import threading
import time

from UnixSocketSupport import ExtendedPoolManager

import urllib3

class MetaData:
    def __init__(self, _json=None):
        if _json != None:
            self.activation_time = str(_json.get("activationTime", ""))
            self.autodelete = _json.get("autodelete", False)
            self.consumers = _json.get("consumers", 1)
            self.deleted = _json.get("deleted", False)
            self.description = str(_json.get("description", ""))
            self.destination_data_uri = str(_json.get("destinationDataUri", ""))
            self.destination_id = str(_json.get("destinationID", ""))
            self.destination_org_id = str(_json.get("destinationOrgID", ""))
            self.destination_type = str(_json.get("destinationType", ""))
            self.destinations_list = _json.get("destinationsList", [])
            self.do_not_send = _json.get("doNotSend", False)
            self.expiration = str(_json.get("expiration", ""))
            self.inactive = _json.get("inactive", False)
            self.link = str(_json.get("link", ""))
            self.meta_only = _json.get("metaOnly", False)
            self.no_data = _json.get("noData", False)
            self.object_id = str(_json.get("objectID", ""))
            self.object_type = str(_json.get("objectType", ""))
            self.origin_id = str(_json.get("originID", ""))
            self.origin_type = str(_json.get("originType", ""))
            self.source_data_uri = str(_json.get("sourceDataUri", ""))
            self.version = str(_json.get("version", ""))
        else:
            self.activation_time = ""
            self.autodelete = False
            self.consumers = 1
            self.deleted = False
            self.description = ""
            self.destination_id = ""
            self.destination_data_uri = ""
            self.destination_type = ""
            self.destination_org_id = ""
            self.destinations_list = []
            self.do_not_send = False
            self.expiration = ""
            self.inactive = False
            self.link = ""
            self.meta_only = False
            self.no_data = False
            self.object_id = ""
            self.object_type = ""
            self.origin_id = ""
            self.origin_type = ""
            self.source_data_uri = ""
            self.version = ""

    def _dict(self):
        result = {
            "activationTime": self.activation_time, "autodelete": self.autodelete,
            "consumers": self.consumers,            "deleted": self.deleted,
            "description": self.description,        "destinationDataUri": self.destination_data_uri,
            "destinationID": self.destination_id,   "destinationOrgID": self.destination_org_id,
            "destinationType": self.destination_type, 
            "doNotSend": self.do_not_send,          "expiration": self.expiration,
            "inactive": self.inactive,              "link": self.link,
            "metaOnly": self.meta_only,             "noData": self.no_data,
            "objectID": self.object_id,             "objectType": self.object_type,
            "originID": self.origin_id,             "originType": self.origin_type,
            "sourceDataUri": self.source_data_uri,  "version": self.version
        }
        if len(self.destinations_list) != 0:
            result["destinationsList"] = self.destinations_list

        return result


class Client:
    _objects_path = "/api/v1/objects/"
    _resend_path  = "/api/v1/resend"

    def __init__(self, service_protocol, service_address, service_port):
        self._service_protocol = service_protocol
        if service_protocol == 'unix':
            self._service_address = "unix:8080"
            self._unix_socket_path = service_address
        elif service_protocol == 'secure-unix':
            self._service_address = "secure-unix:8080"
            self._unix_socket_path = service_address
        else:
            self._service_address = service_address + ":" + str(service_port)
            self._unix_socket_path = ''
        self._app_key = ""
        self._app_secret = ""
        self._app_key_and_secret_set = False
        self.org_id = ""
        self._http_client = self.get_pool_manager()

    def set_ca_certificate(self, cert_pem):
        self._http_client = self.get_pool_manager(ca_certs=cert_pem)

    def set_app_key_and_secret(self, key, secret):
        self._app_key = key
        self._app_secret = secret
        self._app_key_and_secret_set = True

    def start_polling_for_updates(self, object_type, rate, callback):
        url = self._create_object_url(object_type, "", "")
        c = threading.Thread(target=self._poller, args=[url, rate, callback])
        c.setDaemon(True)
        c.start()

    def _poller(self, url, rate, callback):
        firstPoll = True
        while True:
            time.sleep(rate)

            actualUrl = url
            if firstPoll:
                actualUrl = actualUrl + "?received=true"

            try:
                response = self._request_helper("GET", actualUrl)
                if response.status == 200:
                    data = json.loads(response.data.decode('utf-8'))
                    for item in data:
                        callback(MetaData(_json=item))
                if (response.status >= 200 and response.status < 300) or response.status == 404:
                    firstPoll = False
            except:
                (_, exc_value, _) = sys.exc_info() 
                print exc_value

    def fetch_object_data(self, meta_data, writer):
        url = self._create_object_url(meta_data.object_type, meta_data.object_id, "data")
        response = self._request_helper("GET", url, preload_content=False)
        result = response.status == 200
        if result:
            shutil.copyfileobj(response, writer)
        response.release_conn()
        return result
            
    def activate_object(self, meta_data):
        url = self._create_object_url(meta_data.object_type, meta_data.object_id, "activate")
        response = self._request_helper("PUT", url)
        if response.status >= 200 and response.status < 300:
            return True
        else:
            return False
            
    def mark_object_consumed(self, meta_data):
        url = self._create_object_url(meta_data.object_type, meta_data.object_id, "consumed")
        response = self._request_helper("PUT", url)
        if response.status >= 200 and response.status < 300:
            return True
        else:
            return False

    def mark_object_deleted(self, meta_data):
        url = self._create_object_url(meta_data.object_type, meta_data.object_id, "deleted")
        response = self._request_helper("PUT", url)
        if response.status >= 200 and response.status < 300:
            return True
        else:
            return False
            
    def mark_object_received(self, meta_data):
        url = self._create_object_url(meta_data.object_type, meta_data.object_id, "received")
        response = self._request_helper("PUT", url)
        if response.status >= 200 and response.status < 300:
            return True
        else:
            return False

    def update_object(self, meta_data):
        url = self._create_object_url(meta_data.object_type, meta_data.object_id, "")
        payload = {"meta": meta_data._dict()}
        response = self._request_helper("PUT", url,
                                             body=json.dumps(payload).encode('utf-8'),
                                             headers={'Content-Type': 'application/json'})
        if response.status >= 200 and response.status < 300:
            return True
        else:
            return False

    def update_object_data(self, meta_data, reader):
        url = self._create_object_url(meta_data.object_type, meta_data.object_id, "data")
        response = self._request_helper("PUT", url, preload_content=False, body=reader)
        result = response.status == 200
        response.release_conn()
        return result

    def _create_object_url(self, object_type, object_id, command):
        url = self._service_protocol + "://" + self._service_address + Client._objects_path

        if len(self.org_id) != 0:
		    url = url + self.org_id + "/"

        url = url + object_type

        if len(object_id) != 0:
		    url = url + "/" + object_id

        if len(command) != 0:
			url = url + "/" + command

        return url

    def _request_helper(self, method, url, **kwargs):
        arguments = dict()
        for key in kwargs:
            arguments[key] = kwargs[key]

        if self._app_key_and_secret_set:
            auth_header = urllib3.make_headers(basic_auth=self._app_key+ ":"+self._app_secret)
            if 'headers' not in arguments:
                arguments['headers'] = dict()
            for header in auth_header:
                arguments['headers'][header] = auth_header[header]

        return self._http_client.request(method, url, **arguments)

    def get_pool_manager(self, **kwargs):
        arguments = kwargs.copy()
        if self._unix_socket_path != '':
            arguments['unix_socket_path'] = self._unix_socket_path
        return ExtendedPoolManager(**arguments)


class _ClientPoller ():
    pass
