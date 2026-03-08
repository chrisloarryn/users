@regression @products @products_list
Feature: Product listing contract

  Background:
    * url baseUrl

  Scenario: listing products returns product contracts and includes created resources
    * def author = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("products-list-author"))' }
    * def viewer = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("products-list-viewer"))' }
    * def created = call read('classpath:karate/helpers/products/create-product.feature') { token: '#(author.token)', name: 'Listed Product', price: 22.22 }
    Given path 'api', 'products'
    And headers authHeaders(viewer.token)
    When method get
    Then status 200
    * match response == '#[]'
    * def selected = karate.filter(response, function(item){ return item.id == created.productId })
    * match selected == '#[1]'
    * match selected[0] == productResponseSchema
    * match selected[0].name == 'Listed Product'
    * match selected[0].createdByUserId == author.userId
