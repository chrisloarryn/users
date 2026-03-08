@ignore
Feature: Create a product for an authenticated user

  Background:
    * url baseUrl

  Scenario:
    * def requestBody = buildCreateProductRequest(__arg)
    Given path 'api', 'products'
    And headers authHeaders(__arg.token)
    And request requestBody
    When method post
    Then status 201
    * match response == productResponseSchema
    * def product = response
    * def productId = response.id
