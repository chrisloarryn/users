@regression @products @users_products_list
Feature: Nested user product listing contract

  Background:
    * url baseUrl

  Scenario: listing products by user only returns products created by that user
    * def author = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("nested-list-author"))' }
    * def otherUser = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("nested-list-other"))' }
    * def createdByAuthor = call read('classpath:karate/helpers/products/create-product.feature') { token: '#(author.token)', name: 'Author Product', price: 33.33 }
    * def createdByOther = call read('classpath:karate/helpers/products/create-product.feature') { token: '#(otherUser.token)', name: 'Other Product', price: 44.44 }
    Given path 'api', 'users', author.userId, 'products'
    And headers authHeaders(otherUser.token)
    When method get
    Then status 200
    * match response == '#[]'
    * match each response == productResponseSchema
    * def createdByIds = karate.map(response, function(item){ return item.createdByUserId })
    * match each createdByIds == author.userId
    * def responseIds = karate.map(response, function(item){ return item.id })
    * match responseIds contains createdByAuthor.productId
    * assert responseIds.indexOf(createdByOther.productId) == -1
