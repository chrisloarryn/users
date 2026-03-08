@regression @auth @auth_register
Feature: Register contract

  Background:
    * url baseUrl
    * configure headers = defaultHeaders

  Scenario: register returns the complete auth contract and hides persistence fields
    * def requestBody = buildRegisterUserRequest({ name: 'Jane Contract', email: newEmail('register-user').toLowerCase() })
    Given path 'api', 'auth', 'register'
    And request requestBody
    When method post
    Then status 201
    * match response == authResponseSchema
    * match response.tokenType == 'Bearer'
    * match response.user == userResponseSchema
    * match each response.user.phones == phoneResponseSchema
    * match response.user.email == requestBody.email
    * match response.user.active == true
    * match response.user.password == '#notpresent'
    * match response.user.passwordHash == '#notpresent'
