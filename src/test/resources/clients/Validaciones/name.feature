@validations @clients
Feature: Validation Request for Client creations in field "name" - <testName>
        Background:
                * json baseReq = read('classpath:com.chrisloarryn.personclient/integrationTests/data/create_client.json')
                * print baseReq

        Scenario Outline: Validation Request for Client creations in field "name"
            Given path 'clients'
              And request baseReq
                * set baseReq.name = <name>
             When method post
              And match response == <response>
              And match response.status == <status>
             Then match response.name == <name>

        Examples:
                  | testName            | name            | response                                                   | status |
                  | "Name is null"      | null            | { "message": "Name is required" }                          | 400    |
                  | "Name is empty"     | ""              | { "message": "Name is required" }                          | 400    |
                  | "Name is blank"     | " "             | { "message": "Name is required" }                          | 400    |
                  | "Name is too short" | "a"             | { "message": "Name must be between 2 and 100 characters" } | 400    |
                  | "Name is too long"  | "a".repeat(101) | { "message": "Name must be between 2 and 100 characters" } | 400    |
                  | "Name is valid"     | "Chris Loarryn" | { "name": "Chris Loarryn" }                                | 201    |