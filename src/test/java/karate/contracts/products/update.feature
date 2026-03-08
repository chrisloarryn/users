@regression @products @products_update
Feature: Product update contract

  Background:
    * url baseUrl

  Scenario: updating a product preserves the creator and changes the updater
    * def author = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("products-update-author"))' }
    * def editor = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("products-update-editor"))' }
    * def created = call read('classpath:karate/helpers/products/create-product.feature') { token: '#(author.token)', name: 'Original Product', price: 10.00 }
    * def requestBody = buildUpdateProductRequest({ name: 'Updated Product', price: 99.99 })
    Given path 'api', 'products', created.productId
    And headers authHeaders(editor.token)
    And request requestBody
    When method put
    Then status 200
    * match response == productResponseSchema
    * match response.id == created.productId
    * match response.name == requestBody.name
    * match response.price == requestBody.price
    * match response.createdByUserId == author.userId
    * match response.updatedByUserId == editor.userId
