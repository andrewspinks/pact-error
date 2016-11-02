package com.example

import au.com.dius.pact.consumer.PactVerified$
import au.com.dius.pact.consumer.VerificationResult
import au.com.dius.pact.consumer.groovy.PactBuilder
import com.example.client.MyClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

@RunWith(SpringJUnit4ClassRunner)
@SpringBootTest
class PactSpec {

  @Autowired
  private MyClient client

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
      def response = client.createUser("First", "last")
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
      def response = client.createUser("First", "last")
      assert response.getStatusLine().statusCode == 401
    }

    assert result == PactVerified$.MODULE$
  }
}
