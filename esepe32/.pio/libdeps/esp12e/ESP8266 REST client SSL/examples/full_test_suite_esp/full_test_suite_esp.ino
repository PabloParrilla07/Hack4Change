/* RestClient full test suite
 *
 * Every REST method is called.
 *
 * by Mikkel Jeppesen (Duckle)
 * 
 * Meant to call the API provided by: https://github.com/Hal9k-dk/REST-test
 */

#include <ESP8266WiFi.h>
#include "RestClient.h"

int test_delay = 1000; //so we don't spam the API
boolean describe_tests = true;

RestClient client = RestClient("esp-rest-test.herokuapp.com");
//RestClient client  = RestClient("10.0.1.47",5000);


const char ssid[] = "SSID";
const char pass[] = "PASS";

//Setup
void setup() 
{
  Serial.begin(115200);
  delay(10);

  // Connect to Wi-Fi network
  Serial.println();
  Serial.println();
  Serial.print("Connecting to...");
  Serial.println(ssid);

  WiFi.begin(ssid, pass);

  while (WiFi.status() != WL_CONNECTED) 
  {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWi-Fi connected successfully\n");
}

void test_status(int statusCode)
{
  delay(test_delay);
  if(statusCode == 200)
  {
    Serial.print(" OK (");
    Serial.print(statusCode);
    Serial.println(")");
  }
  else
  {
    Serial.print(" FAIL (");
    Serial.print(statusCode);
    Serial.println(")");
  }
}

String response;
void test_response(bool has_body=false, int headers=0)
{
  String body, header1, header2;
  bool ok = response.startsWith(String("OK"));

  if(ok)
  {
    response.remove(0,2);

    if(has_body)
    {
      body = response.substring(0,4);
      response.remove(0,4);
    }

    if(headers > 0)
    {
      header1 = response.substring(0,2);
      response.remove(0,2);
    }

    if(headers > 1)
    {
      header2 = response.substring(0,2);
      response.remove(0,2);
    }

    if (has_body)
    {
      String out = (body == "test") ? " Body(OK): " : " Body(FAIL): ";
      Serial.print(out); Serial.println(body);
    }

    if(headers)
    {
      String out = (header1 == "h1") ? " H1(OK): " : " H1(FAIL): ";
      Serial.print(out); Serial.println(header1);
      if(headers > 1)
      {
        String out = (header2 == "h2") ? " H2(OK): " : " H2(FAIL): ";
        Serial.print(out); Serial.println(header2);
      }
    }
  }
  else
  {
    Serial.print(' '); Serial.println(response);
  }
  response = "";
}

void describe(char * description)
{
  if(describe_tests)
  {
    Serial.println(description);
  }
}

//reusable test variables
char* post_body = "{\"test\":\"test\"}";

void GET_tests()
{
  describe("GET");
  test_status(client.get("/"));

  describe("GET w. response");
  test_status(client.get("/", &response));
  test_response();

  describe("GET w. header");
  client.setHeader("X-Test-Header1: one");
  test_status(client.get("/"));

  describe("GET w. header and response");
  client.setHeader("X-Test-Header1: one");
  test_status(client.get("/", &response));
  test_response(false, 1);

  describe("GET w. 2 headers and response");
  client.setHeader("X-Test-Header1: one");
  client.setHeader("X-Test-Header2: two");
  test_status(client.get("/", &response));
  test_response(false, 2);
}

void POST_tests()
{
    // POST TESTS
  describe("POST w. body");
  client.setContentType("application/json");
  test_status(client.post("/", post_body));

  describe("POST w. body and response");
  client.setContentType("application/json");
  test_status(client.post("/", post_body, &response));
  test_response(true);

  describe("POST w. body and header");
  client.setContentType("application/json");
  client.setHeader("X-Test-Header1: one");
  test_status(client.post("/", post_body));

  describe("POST w. body, header and response");
  client.setContentType("application/json");
  client.setHeader("X-Test-Header1: one");
  test_status(client.post("/", post_body, &response));
  test_response(true, 1);

  describe("POST w. body, 2 headers and response");
  client.setContentType("application/json");
  client.setHeader("X-Test-Header1: one");
  client.setHeader("X-Test-Header2: two");
  test_status(client.post("/", post_body, &response));
  test_response(true, 2);
}

void PUT_tests()
{
  describe("PUT w. body");
  client.setContentType("application/json");
  test_status(client.put("/", post_body));

  describe("PUT w. body and response");
  client.setContentType("application/json");
  test_status(client.put("/", post_body, &response));
  test_response(true);

  describe("PUT w. body and header");
  client.setHeader("X-Test-Header1: one");
  test_status(client.put("/", post_body));

  describe("PUT w. body, header and response");
  client.setHeader("X-Test-Header1: one");
  test_status(client.put("/", post_body, &response));
  test_response(true, 1);

  describe("PUT w. body, 2 headers and response");
  client.setHeader("X-Test-Header1: one");
  client.setHeader("X-Test-Header2: two");
  test_status(client.put("/", post_body, &response));
  test_response(true, 2);
}

void DELETE_tests()
{
  describe("DELETE");
  test_status(client.del("/"));

  describe("DELETE w. body");
  client.setContentType("application/json");
  test_status(client.del("/", post_body));

  describe("DELETE w. body and response");
  client.setContentType("application/json");
  test_status(client.del("/", post_body, &response));
  test_response(true);

  describe("DELETE w. body and header");
  client.setContentType("application/json");
  client.setHeader("X-Test-Header1: one");
  test_status(client.del("/", post_body));

  describe("DELETE w. body, header and response");
  client.setContentType("application/json");
  client.setHeader("X-Test-Header1: one");
  test_status(client.del("/", post_body, &response));
  test_response(true, 1);

  describe("DELETE w. body, 2 headers and response");
  client.setContentType("application/json");
  client.setHeader("X-Test-Header1: one");
  client.setHeader("X-Test-Header2: two");
  test_status(client.del("/", post_body, &response));
  test_response(true, 2);
}


// Run the tests!
void loop()
{
  Serial.println("\nplain tests:");
  client.setSSL(0);
  client.setPort(80);

  GET_tests();
  POST_tests();
  PUT_tests();
  DELETE_tests();


  Serial.println("\nSSL tests:");
  client.setSSL(1);
  client.setPort(443);

  GET_tests();
  POST_tests();
  PUT_tests();
  DELETE_tests();

  Serial.println("\n send any-key to repeat tests");
  while(Serial.available() == 0)
  {;}
  while(Serial.available())
  {
    Serial.read();
  }
}
