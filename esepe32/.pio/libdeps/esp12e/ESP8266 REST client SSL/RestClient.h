#ifndef RestClient_h
#define RestClient_h

#include <Arduino.h>
#include <WiFiClientSecure.h>

class RestClient {

public:
    explicit RestClient(const char* host);
    explicit RestClient(const char* _host, int _port);
    // set ssl to on but do not verify server identity with fingerprint
    explicit RestClient(const char* _host, int _port, bool _ssl);
    // set fingerprint if using SSL, stores the SHA1 fingerprint of the remote site, implicity sets ssl to on
    explicit RestClient(const char* _host, int _port, const char* _fingerprint);

    // Client Setup
    bool dhcp();
    int begin(byte*);

    // Generic HTTP Request
    int request(const char* method, const char* path,
                const char* body, String* response);
    // Set a Request Header
    void setHeader(const char*);
    // Set Content-Type Header
    void setContentType(const char*);
    // Set SSL support on or off
    void setSSL(bool);
    // Set port used
    void setPort(int);

    // GET path
    int get(const char*);
    // GET path and response
    int get(const char*, String*);

    // POST path and body
    int post(const char* path, const char* body);
    // POST path and body and response
    int post(const char* path, const char* body, String*);

    // PATCH path and body
    int patch(const char* path, const char* body);
    // PATCH path and body and response
    int patch(const char* path, const char* body, String*);

    // PUT path and body
    int put(const char* path, const char* body);
    // PUT path and body and response
    int put(const char* path, const char* body, String*);

    // DELETE path
    int del(const char*);
    // DELETE path and body
    int del(const char*, const char*);
    // DELETE path and response
    int del(const char*, String*);
    // DELETE path and body and response
    int del(const char*, const char*, String*);

private:
    WiFiClient client;
    WiFiClientSecure sslClient;
    int readResponse(String*);
    void write(const char*);
    const char* host = nullptr;
    int port = 0;
    int num_headers = 0;
    const char* headers[10] = { nullptr };
    const char* contentType  = nullptr;
    const char* fingerprint = nullptr;
    bool ssl = false;
};

#endif
