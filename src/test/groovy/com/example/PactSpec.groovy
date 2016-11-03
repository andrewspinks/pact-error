package com.example

import au.com.dius.pact.consumer.PactVerified$
import au.com.dius.pact.consumer.VerificationResult
import au.com.dius.pact.consumer.groovy.PactBuilder
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import org.junit.Before
import org.junit.Test

class PactSpec {

  @Before
  void setup() {
//    sleep(4000) /* work around what seems to be a race condition in pact :-( */
  }

  @Test
  void 'a request to create user'() {

    PactBuilder legacyWebappService = new PactBuilder()
    legacyWebappService {
      serviceConsumer 'MyService'
      hasPactWith 'LegacyService'
      port 1234
      given("Auth token valid")
      uponReceiving('a request to create a user')
      withAttributes(
              method: 'post',
              path: '/user',
              headers: ['Accept': regexp(~/.*application\/json.*/, 'application/json'), 'AUTH-TOKEN': "MyKey"]
      )
      withBody {
        firstName "First"
        lastName "last"
      }
      willRespondWith(
              status: 200,
              headers: ['Content-Type': regexp(~/.*application\/json.*/, 'application/json')])
      withBody {
        status true
        data {
          someStuff "here"
        }
      }
    }

    VerificationResult result = legacyWebappService.run {
      def response = Request.Post("http://localhost:1234/user")
              .addHeader("Accept", "application/json")
              .addHeader("AUTH-TOKEN", "MyKey")
              .bodyString("{ \"firstName\": \"First\", \"lastName\": \"last\" }", ContentType.APPLICATION_JSON)
              .execute().returnResponse()
      assert response.getStatusLine().statusCode == 200
    }

    assert result == PactVerified$.MODULE$
  }

  @Test
  void 'a request to create user with invalid auth token'() {
    PactBuilder legacyWebappService = new PactBuilder()
    legacyWebappService {
      serviceConsumer 'MyService'
      hasPactWith 'LegacyService'
      port 1234
      given("Aut htoken invalid")
      uponReceiving('a request to create a user with an invalid auth token')
      withAttributes(
              method: 'post',
              path: '/user',
              headers: ['Accept': regexp(~/.*application\/json.*/, 'application/json'), 'AUTH-TOKEN': "MyKey"]
      )
      withBody {
        firstName "First"
        lastName "last"
      }
      willRespondWith(status: 401)
    }

    VerificationResult result = legacyWebappService.run {
      def response = Request.Post("http://localhost:1234/user")
              .addHeader("Accept", "application/json")
              .addHeader("AUTH-TOKEN", "MyKey")
              .bodyString("{ \"firstName\": \"First\", \"lastName\": \"last\" }", ContentType.APPLICATION_JSON)
              .execute().returnResponse()
      assert response.getStatusLine().statusCode == 401
    }

    assert result == PactVerified$.MODULE$
  }
}
