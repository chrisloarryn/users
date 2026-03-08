@regression @users @users_list
Feature: User listing contract

  Background:
    * url baseUrl

  Scenario: listing users returns user contracts for authenticated callers
    * def registered = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("users-list"))' }
    Given path 'api', 'users'
    And headers authHeaders(registered.token)
    When method get
    Then status 200
    * match response == '#[]'
    * def selected = karate.filter(response, function(item){ return item.id == registered.userId })
    * match selected == '#[1]'
    * match selected[0] == userResponseSchema
    * match each selected[0].phones == phoneResponseSchema
    * match selected[0].email == registered.email
    * match selected[0].active == true
