#include "RestClient.h"

#ifdef HTTP_DEBUG
#define HTTP_DEBUG_PRINT(string) (Serial.print(string))
#else
#define HTTP_DEBUG_PRINT(string)
#endif

RestClient::RestClient(const char* _host){
    host = _host;
    port = 80;
    ssl = false;
    contentType = "application/x-www-form-urlencoded";
}

RestClient::RestClient(const char* _host, int _port){
    host = _host;
    port = _port;
    ssl = false;
    contentType = "application/x-www-form-urlencoded";
}

bool RestClient::dhcp(){
  byte mac[] = { 0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED };
  if (begin(mac) == 0) {
    Serial.println("Failed to configure Ethernet using DHCP");
    return false;
  }

  //give it time to initialize
  delay(1000);
  return true;
}

RestClient::RestClient(const char* _host, int _port, const char* _fingerprint){
    host = _host;
    port = _port;
    ssl = true;
    fingerprint = _fingerprint;
    contentType = "application/x-www-form-urlencoded";
}

RestClient::RestClient(const char* _host, int _port, bool _ssl) {
    host = _host;
    port = _port;
    ssl = _ssl;
    contentType = "application/x-www-form-urlencoded";
}

// GET path
int RestClient::get(const char* path){
    return request("GET", path, NULL, NULL);
}

//GET path with response
int RestClient::get(const char* path, String* response){
    return request("GET", path, NULL, response);
}

// POST path and body
int RestClient::post(const char* path, const char* body){
    return request("POST", path, body, NULL);
}

// POST path and body with response
int RestClient::post(const char* path, const char* body, String* response){
    return request("POST", path, body, response);
}

// PATCH path and body
int RestClient::patch(const char* path, const char* body){
    return request("PATCH", path, body, NULL);
}

// PATCH path and body with response
int RestClient::patch(const char* path, const char* body, String* response){
    return request("PATCH", path, body, response);
}

// PUT path and body
int RestClient::put(const char* path, const char* body){
    return request("PUT", path, body, NULL);
}

// PUT path and body with response
int RestClient::put(const char* path, const char* body, String* response){
    return request("PUT", path, body, response);
}

// DELETE path
int RestClient::del(const char* path){
    return request("DELETE", path, NULL, NULL);
}

// DELETE path and response
int RestClient::del(const char* path, String* response){
    return request("DELETE", path, NULL, response);
}

// DELETE path and body
int RestClient::del(const char* path, const char* body ){
    return request("DELETE", path, body, NULL);
}

// DELETE path and body with response
int RestClient::del(const char* path, const char* body, String* response){
    return request("DELETE", path, body, response);
}

void RestClient::write(const char* string){

    if(ssl) {
        HTTP_DEBUG_PRINT("\nSSL Print: ");
        HTTP_DEBUG_PRINT(string);
        sslClient.print(string);
    } else {
        HTTP_DEBUG_PRINT("\nHTTP Print: ");
        HTTP_DEBUG_PRINT(string);
        client.print(string);
    }
}

void RestClient::setHeader(const char* header){
    headers[num_headers] = header;
    num_headers++;
}

void RestClient::setContentType(const char* contentTypeValue){
    contentType = contentTypeValue;
}

void RestClient::setSSL(bool _ssl){
    ssl = _ssl;
}

void RestClient::setPort(int _port){
    port = _port;
}

// The mother- generic request method.
//
int RestClient::request(const char* method, const char* path,
                        const char* body, String* response){

    HTTP_DEBUG_PRINT("HTTP: connect\n");

    if (ssl) {
        if (fingerprint)
        {
            sslClient.setFingerprint(fingerprint);
        }
        else
        {
            sslClient.setInsecure();
        }
        
        if(!sslClient.connect(host, port)){
            HTTP_DEBUG_PRINT("HTTPS Connection failed\n");
            return 0;
        }
    } else {
        if(!client.connect(host, port)){
            HTTP_DEBUG_PRINT("HTTP Connection failed\n");
            return 0;
        }
    }


    HTTP_DEBUG_PRINT("HTTP: connected\n");
    HTTP_DEBUG_PRINT("REQUEST: \n");

    String request = String(method) + " " + String(path) + " HTTP/1.1\r\n";
    for(int i=0; i<num_headers; i++){
        request += String(headers[i]) + "\r\n";
    }
    request += "Host: " + String(host) +  ":" + String(port) + "\r\n";
    request += "Connection: close\r\n";
    if(body != NULL){
        char contentLength[30];
        sprintf(contentLength, "Content-Length: %d\r\n", strlen(body));
        request += String(contentLength);

        request += "Content-Type: " + String(contentType) + "\r\n";
    }
    request += "\r\n";

    if(body != NULL){
        request += String(body);
        request += "\r\n\r\n";
    }
    delay(0);
    write(request.c_str());

    HTTP_DEBUG_PRINT("\nEND REQUEST\n");
    //make sure you write all those bytes.
    //delay(1000);

    HTTP_DEBUG_PRINT("HTTP: call readResponse\n");
    int statusCode = readResponse(response);
    HTTP_DEBUG_PRINT("HTTP: return readResponse\n");

    //cleanup
    HTTP_DEBUG_PRINT("HTTP: stop client\n");
    num_headers = 0;
    if(ssl){
        sslClient.stop();
    } else {
        client.stop();
    }
    delay(50);
    HTTP_DEBUG_PRINT("HTTP: client stopped\n");

    return statusCode;

}

int RestClient::readResponse(String* response) {

    // an http request ends with a blank line
    boolean currentLineIsBlank = true;
    boolean httpBody = false;
    boolean inStatus = false;

    char statusCode[4];
    int i = 0;
    int code = 0;

    void* http_client;
    if(ssl) {
        HTTP_DEBUG_PRINT("HTTP: Connect: " + String(sslClient.connected()) + " Available: " + String(sslClient.available()) + "\n");
        while (sslClient.connected()) {
            HTTP_DEBUG_PRINT(".");
            delay(0);

            if (sslClient.available()) {
                HTTP_DEBUG_PRINT(",");
                delay(0);
                char c = sslClient.read();
                HTTP_DEBUG_PRINT(c);

                if(c == ' ' && !inStatus){
                    inStatus = true;
                }

                if(inStatus && i < 3 && c != ' '){
                    statusCode[i] = c;
                    i++;
                }
                if(i == 3){
                    statusCode[i] = '\0';
                    code = atoi(statusCode);
                }

                if(httpBody){
                    //only write response if its not null
                    if(response != NULL) response->concat(c);
                }
                else
                {
                    if (c == '\n' && currentLineIsBlank) {
                        httpBody = true;
                    }

                    if (c == '\n') {
                        // your starting a new line
                        currentLineIsBlank = true;
                    }
                    else if (c != '\r') {
                        // you've gotten a character on the current line
                        currentLineIsBlank = false;
                    }
                }
            }
        }
        HTTP_DEBUG_PRINT("HTTPS client closed \n");
    }else {
        while (client.connected() || client.available()) {
            delay(0);

            if (client.available()) {
                delay(0);
                char c = client.read();

                if(c == ' ' && !inStatus){
                    inStatus = true;
                }

                if(inStatus && i < 3 && c != ' '){
                    statusCode[i] = c;
                    i++;
                }
                if(i == 3){
                    statusCode[i] = '\0';
                    code = atoi(statusCode);
                }

                if(httpBody){
                    // only write response if its not null
                    if(response != NULL) response->concat(c);
                }
                else
                {
                    if (c == '\n' && currentLineIsBlank) {
                        httpBody = true;
                    }

                    if (c == '\n') {
                        // your starting a new line
                        currentLineIsBlank = true;
                    }
                    else if (c != '\r') {
                        // you've gotten a character on the current line
                        currentLineIsBlank = false;
                    }
                }
            }
        }
        while (client.available()) {
            char c = client.read();
            if (httpBody)
                if (response)
                    response->concat(c);
        }
    }

    HTTP_DEBUG_PRINT("HTTP: return readResponse3\n");
    return code;
}
