@regression @users @users_update
Feature: User update contract

  Background:
    * url baseUrl

  Scenario: updating a user returns the updated user contract
    * def registered = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("users-update"))' }
    * def requestBody = buildUpdateUserRequest()
    Given path 'api', 'users', registered.userId
    And headers authHeaders(registered.token)
    And request requestBody
    When method put
    Then status 200
    * match response == userResponseSchema
    * match each response.phones == phoneResponseSchema
    * match response.id == registered.userId
    * match response.name == requestBody.name
    * match response.email == requestBody.email
    * match response.phones[0].number == requestBody.phones[0].number
    * match response.active == true
    * match response.password == '#notpresent'
    * match response.passwordHash == '#notpresent'
