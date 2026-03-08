@regression @auth
Feature: Authentication validation errors

  Background:
    * url baseUrl
    * configure headers = defaultHeaders

  @auth_register @errors_validation
  Scenario: register bean validation failures return the validation problem detail contract
    Given path 'api', 'auth', 'register'
    And request buildRegisterUserRequest({ name: '', email: 'not-an-email', phones: [] })
    When method post
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Validation failed'
    * match response.status == 400
    * match response.detail == '#string'
    * match response.errors contains 'name: must not be blank'
    * match response.errors contains 'email: must be a well-formed email address'

  @auth_register @errors_business
  Scenario: weak passwords return the managed business validation contract
    Given path 'api', 'auth', 'register'
    And request buildRegisterUserRequest({ email: newEmail('weak-password'), password: 'weak' })
    When method post
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Business Rule Violation'
    * match response.status == 400
    * match response.detail == 'Password must contain upper and lower case letters, a number, a special character and at least 8 characters'
    * match response.errors == ['Password must contain upper and lower case letters, a number, a special character and at least 8 characters']
