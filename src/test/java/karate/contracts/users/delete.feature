@regression @users @users_delete @errors_not_found
Feature: User deletion contract

  Background:
    * url baseUrl

  Scenario: deleting a user returns no content and the user disappears from active reads
    * def registered = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("users-delete"))' }
    * def viewer = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("users-delete-viewer"))' }
    Given path 'api', 'users', registered.userId
    And headers authHeaders(registered.token)
    When method delete
    Then status 204
    * match response == ''

    Given path 'api', 'users', registered.userId
    And headers authHeaders(viewer.token)
    When method get
    Then status 404
    * match response == problemDetailSchema
    * match response.title == 'Not Found'
    * match response.status == 404
    * match response.detail == 'User not found'
    * match response.errors == ['User not found']
