@ignore
Feature: Register a user and expose its auth context

  Background:
    * url baseUrl
    * configure headers = defaultHeaders

  Scenario:
    * def requestBody = buildRegisterUserRequest(__arg)
    Given path 'api', 'auth', 'register'
    And request requestBody
    When method post
    Then status 201
    * match response == authResponseSchema
    * match response.user == userResponseSchema
    * match each response.user.phones == phoneResponseSchema
    * match response.user.password == '#notpresent'
    * match response.user.passwordHash == '#notpresent'
    * def auth = response
    * def token = response.accessToken
    * def user = response.user
    * def userId = response.user.id
    * def email = response.user.email
    * def password = requestBody.password
