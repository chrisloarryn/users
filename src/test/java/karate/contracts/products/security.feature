@regression @products @products_list @security
Feature: Product endpoint security

  Background:
    * url baseUrl
    * configure headers = defaultHeaders

  Scenario: product endpoints reject unauthenticated callers with empty responses
    Given path 'api', 'products'
    When method get
    Then status 401
    * match response == ''

    Given path 'api', 'products'
    And request buildCreateProductRequest({ name: 'Unauthorized Product' })
    When method post
    Then status 401
    * match response == ''
