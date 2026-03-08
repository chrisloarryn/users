@regression @products @products_get_by_id
Feature: Product detail contract

  Background:
    * url baseUrl

  Scenario: fetching a product by id returns the full product contract
    * def author = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("products-get-author"))' }
    * def viewer = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("products-get-viewer"))' }
    * def created = call read('classpath:karate/helpers/products/create-product.feature') { token: '#(author.token)', name: 'Fetched Product', price: 18.75 }
    Given path 'api', 'products', created.productId
    And headers authHeaders(viewer.token)
    When method get
    Then status 200
    * match response == productResponseSchema
    * match response.id == created.productId
    * match response.name == 'Fetched Product'
    * match response.price == 18.75
    * match response.createdByUserId == author.userId
