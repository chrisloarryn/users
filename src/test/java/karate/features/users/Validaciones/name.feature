@validations @users
Feature: Validation Request for User creations in field "name" - <testName>
        Background:
            Given url baseUrl
                * json baseReq = read('classpath:karate/data/create_user.json')
                * print baseReq

        Scenario Outline: Validation Request for Client creations in field "name"
            Given path 'users'
              And request baseReq
                * set baseReq.nombre = <name>
             When method post
             # And match response == <response>
              And match response.status == <status>
          # Then match response.name == <name>

        Examples:
          | testName        | name            | response                    | status |
          | "Name is valid" | "Chris Loarryn" | { "name": "Chris Loarryn" } | 201    |
          |                 |                 |                             |        |