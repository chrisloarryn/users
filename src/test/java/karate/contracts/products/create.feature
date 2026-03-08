@regression @products @products_create
Feature: Product creation contract

  Background:
    * url baseUrl

  Scenario: creating a product returns the full product contract
    * def author = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("products-create"))' }
    * def requestBody = buildCreateProductRequest({ name: 'Contract Product' })
    Given path 'api', 'products'
    And headers authHeaders(author.token)
    And request requestBody
    When method post
    Then status 201
    * match response == productResponseSchema
    * match response.name == requestBody.name
    * match response.price == requestBody.price
    * match response.createdByUserId == author.userId
    * match response.updatedByUserId == author.userId
