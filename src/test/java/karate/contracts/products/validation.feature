@regression @products @products_create @errors_validation
Feature: Product validation errors

  Background:
    * url baseUrl

  Scenario: invalid product payloads return the validation problem detail contract
    * def author = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("products-invalid"))' }
    Given path 'api', 'products'
    And headers authHeaders(author.token)
    And request buildCreateProductRequest({ name: '', price: 0 })
    When method post
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Validation failed'
    * match response.status == 400
    * match response.errors contains 'name: must not be blank'
    * match response.errors contains 'price: must be greater than 0.00'
