package com.example.client

import groovy.transform.CompileStatic
import org.apache.http.HttpResponse
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import org.springframework.stereotype.Component

@Component
@CompileStatic
class MyClient {

  String baseUrl = "http://localhost:1234"

  HttpResponse createUser(String firstName, String lastName) {
    Request.Post("${baseUrl}/user")
            .addHeader("Accept", "application/json")
            .addHeader("AUTH-TOKEN", "MyKey")
            .bodyString("{ \"firstName\": \"${firstName}\", \"lastName\": \"${lastName}\" }", ContentType.APPLICATION_JSON)
            .execute().returnResponse()
  }
}
