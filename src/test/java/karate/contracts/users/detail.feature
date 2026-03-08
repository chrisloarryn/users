@regression @users @users_get_by_id
Feature: User detail contract

  Background:
    * url baseUrl

  Scenario: fetching a user by id returns the full user contract
    * def registered = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("users-get"))' }
    Given path 'api', 'users', registered.userId
    And headers authHeaders(registered.token)
    When method get
    Then status 200
    * match response == userResponseSchema
    * match each response.phones == phoneResponseSchema
    * match response.id == registered.userId
    * match response.email == registered.email
    * match response.active == true
    * match response.password == '#notpresent'
    * match response.passwordHash == '#notpresent'
