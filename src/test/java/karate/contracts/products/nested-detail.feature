@regression @products @users_products_get_by_id @errors_not_found
Feature: Nested user product detail contract

  Background:
    * url baseUrl

  Scenario: fetching a user product by id returns the nested product contract
    * def author = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("nested-get-author"))' }
    * def viewer = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("nested-get-viewer"))' }
    * def created = call read('classpath:karate/helpers/products/create-product.feature') { token: '#(author.token)', name: 'Nested Product', price: 55.55 }
    Given path 'api', 'users', author.userId, 'products', created.productId
    And headers authHeaders(viewer.token)
    When method get
    Then status 200
    * match response == productResponseSchema
    * match response.id == created.productId
    * match response.name == 'Nested Product'
    * match response.createdByUserId == author.userId

  Scenario: fetching a product under the wrong user returns a managed not found contract
    * def author = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("wrong-owner-author"))' }
    * def otherUser = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("wrong-owner-other"))' }
    * def created = call read('classpath:karate/helpers/products/create-product.feature') { token: '#(author.token)', name: 'Owner Product', price: 77.77 }
    Given path 'api', 'users', otherUser.userId, 'products', created.productId
    And headers authHeaders(otherUser.token)
    When method get
    Then status 404
    * match response == problemDetailSchema
    * match response.title == 'Not Found'
    * match response.status == 404
    * match response.detail == 'Product not found for user'
    * match response.errors == ['Product not found for user']
