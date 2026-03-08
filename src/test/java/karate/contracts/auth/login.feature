@regression @auth @auth_login
Feature: Login contract

  Background:
    * url baseUrl
    * configure headers = defaultHeaders

  Scenario: login returns the full auth contract for an existing user
    * def registered = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("login-user"))' }
    * def requestBody = buildLoginRequest(registered.email, registered.password)
    Given path 'api', 'auth', 'login'
    And request requestBody
    When method post
    Then status 200
    * match response == authResponseSchema
    * match response.tokenType == 'Bearer'
    * match response.user == userResponseSchema
    * match each response.user.phones == phoneResponseSchema
    * match response.user.id == registered.userId
    * match response.user.email == registered.email
    * match response.user.password == '#notpresent'
    * match response.user.passwordHash == '#notpresent'

  @errors_auth
  Scenario: login with invalid credentials returns the managed problem detail contract
    * def registered = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("invalid-login"))' }
    Given path 'api', 'auth', 'login'
    And request buildLoginRequest(registered.email, 'WrongPass1!')
    When method post
    Then status 401
    * match response == problemDetailSchema
    * match response.title == 'Unauthorized'
    * match response.status == 401
    * match response.detail == 'Invalid email or password'
    * match response.errors == ['Invalid email or password']
