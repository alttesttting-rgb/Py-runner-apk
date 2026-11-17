"""Best-effort compatibility layer for scripts which import tls_client.
Uses requests; does not implement browser TLS fingerprints/impersonation.
"""
import warnings
import requests

warnings.warn("PinkPy: tls_client compatibility mode uses requests; TLS impersonation is unavailable on Android.", RuntimeWarning)

class Session:
    def __init__(self, client_identifier=None, random_tls_extension_order=False, **kwargs):
        self._session = requests.Session()
        self.headers = self._session.headers
        self.cookies = self._session.cookies
        self.proxies = {}
        self.timeout = kwargs.pop("timeout", 30)

    def request(self, method, url, **kwargs):
        timeout = kwargs.pop("timeout_seconds", kwargs.pop("timeout", self.timeout))
        proxy = kwargs.pop("proxy", None)
        if proxy:
            kwargs["proxies"] = {"http": proxy, "https": proxy}
        return self._session.request(method, url, timeout=timeout, **kwargs)

    def get(self, url, **kwargs): return self.request("GET", url, **kwargs)
    def options(self, url, **kwargs): return self.request("OPTIONS", url, **kwargs)
    def head(self, url, **kwargs): return self.request("HEAD", url, **kwargs)
    def post(self, url, **kwargs): return self.request("POST", url, **kwargs)
    def put(self, url, **kwargs): return self.request("PUT", url, **kwargs)
    def patch(self, url, **kwargs): return self.request("PATCH", url, **kwargs)
    def delete(self, url, **kwargs): return self.request("DELETE", url, **kwargs)
    def close(self): self._session.close()
