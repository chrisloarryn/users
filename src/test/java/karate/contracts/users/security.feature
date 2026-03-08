@regression @users @users_list @security
Feature: User endpoint security

  Background:
    * url baseUrl
    * configure headers = defaultHeaders

  Scenario: listing users without a token is rejected with an empty unauthorized response
    Given path 'api', 'users'
    When method get
    Then status 401
    * match response == ''
