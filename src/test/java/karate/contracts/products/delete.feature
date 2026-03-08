@regression @products @products_delete @errors_not_found
Feature: Product deletion contract

  Background:
    * url baseUrl

  Scenario: deleting a product returns no content and the product stops existing
    * def author = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("products-delete-author"))' }
    * def created = call read('classpath:karate/helpers/products/create-product.feature') { token: '#(author.token)', name: 'Deleted Product', price: 66.66 }
    Given path 'api', 'products', created.productId
    And headers authHeaders(author.token)
    When method delete
    Then status 204
    * match response == ''

    Given path 'api', 'products', created.productId
    And headers authHeaders(author.token)
    When method get
    Then status 404
    * match response == problemDetailSchema
    * match response.title == 'Not Found'
    * match response.status == 404
    * match response.detail == 'Product not found'
    * match response.errors == ['Product not found']
