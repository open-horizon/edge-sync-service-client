""" Unix Socket support for the Sync Service client

"""


import collections
import functools
import socket

import six
import urllib3
from six.moves import http_client as httplib


# The code below was taken from https://github.com/docker/docker-py/blob/master/docker/transport/unixconn.py
# It is part of the Python APIs for Docker.
#
class UnixHTTPResponse(httplib.HTTPResponse, object):
    def __init__(self, sock, *args, **kwargs):
        super(UnixHTTPResponse, self).__init__(sock, *args, **kwargs)


class UnixHTTPConnection(httplib.HTTPConnection, object):

    def __init__(self, unix_socket_path):
        super(UnixHTTPConnection, self).__init__('http')
        self._unix_socket_path = unix_socket_path

    def connect(self):
        sock = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
        sock.settimeout(60)
        sock.connect(self._unix_socket_path)
        self.sock = sock

    def response_class(self, sock, *args, **kwargs):
        return UnixHTTPResponse(sock, *args, **kwargs)

class UnixHTTPConnectionPool(urllib3.connectionpool.HTTPConnectionPool):
    def __init__(self, host, port, **kwargs):
        super(UnixHTTPConnectionPool, self).__init__('unix', port)
        self._unix_socket_path = kwargs['unix_socket_path']

    def _new_conn(self):
        return UnixHTTPConnection(self._unix_socket_path)

_key_fields = (
    'key_scheme',  # str
    'key_host',  # str
    'key_port',  # int
    'key_timeout',  # int or float or Timeout
    'key_retries',  # int or Retry
    'key_strict',  # bool
    'key_block',  # bool
    'key_source_address',  # str
    'key_key_file',  # str
    'key_cert_file',  # str
    'key_cert_reqs',  # str
    'key_ca_certs',  # str
    'key_ssl_version',  # str
    'key_ca_cert_dir',  # str
    'key_ssl_context',  # instance of ssl.SSLContext or urllib3.util.ssl_.SSLContext
    'key_maxsize',  # int
    'key_headers',  # dict
    'key__proxy',  # parsed proxy url
    'key__proxy_headers',  # dict
    'key_socket_options',  # list of (level (int), optname (int), value (int or str)) tuples
    'key__socks_options',  # dict
    'key_assert_hostname',  # bool or string
    'key_assert_fingerprint',  # str
    'key_server_hostname', #str
    'key_unix_socket_path', #str
)

#: The namedtuple class used to construct keys for the connection pool.
#: All custom key schemes should include the fields in this key at a minimum.
UnixPoolKey = collections.namedtuple('UnixPoolKey', _key_fields)

class ExtendedPoolManager(urllib3.PoolManager):
    def __init__(self, num_pools=10, headers=None, **connection_pool_kw):
        super(ExtendedPoolManager, self).__init__(num_pools=num_pools, headers=headers, **connection_pool_kw)
        self.pool_classes_by_scheme['unix'] = UnixHTTPConnectionPool
        self.key_fn_by_scheme['unix'] = \
            functools.partial(self.key_fn_by_scheme['http'].func, UnixPoolKey)
