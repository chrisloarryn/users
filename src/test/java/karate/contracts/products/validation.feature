@regression @products @products_create @errors_validation
Feature: Product validation errors

  Background:
    * url baseUrl
    * configure headers = defaultHeaders
    * def longProductName = repeat('a', 121)

  @products_create @errors_validation
  Scenario Outline: creating a product rejects missing required fields declared by the contract
    * def author = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("products-invalid"))' }
    * def requestBody = buildCreateProductRequest()
    * remove requestBody.<fieldPath>
    Given path 'api', 'products'
    And headers authHeaders(author.token)
    And request requestBody
    When method post
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Validation failed'
    * match response.status == 400
    * match response.errors contains <expectedError>

    Examples:
      | fieldPath | expectedError              |
      | name      | 'name: must not be blank'  |
      | price     | 'price: must not be null'  |

  @products_create @errors_validation
  Scenario: creating a product rejects blank names
    * def author = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("products-create-blank"))' }
    Given path 'api', 'products'
    And headers authHeaders(author.token)
    And request buildCreateProductRequest({ name: '' })
    When method post
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Validation failed'
    * match response.status == 400
    * match response.errors contains 'name: must not be blank'

  @products_create @errors_validation
  Scenario Outline: creating a product rejects size and numeric constraints declared by the contract
    * def author = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("products-create-constraints"))' }
    * def requestBody = buildCreateProductRequest()
    * set requestBody.<fieldPath> = <invalidValue>
    Given path 'api', 'products'
    And headers authHeaders(author.token)
    And request requestBody
    When method post
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Validation failed'
    * match response.status == 400
    * def errorText = response.errors.join(' | ')
    * match errorText contains <expectedFragment>

    Examples:
      | fieldPath | invalidValue    | expectedFragment                         |
      | name      | longProductName | 'name: size must be between 0 and 120'   |
      | price     | 0               | 'price: must be greater than 0.00'       |
      | price     | -1              | 'price: must be greater than 0.00'       |
      | price     | 10.123          | 'numeric value out of bounds'            |
      | price     | 12345678901.00  | 'numeric value out of bounds'            |

  @products_create @errors_validation
  Scenario Outline: creating a product rejects type coercion for contract fields
    * def author = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("products-create-coercion"))' }
    * def requestBody = buildCreateProductRequest()
    * set requestBody.<fieldPath> = <invalidValue>
    Given path 'api', 'products'
    And headers authHeaders(author.token)
    And request requestBody
    When method post
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Bad Request'
    * match response.status == 400
    * match response.detail == 'Malformed request body.'
    * match response.errors == ['Malformed request body.']

    Examples:
      | fieldPath | invalidValue |
      | name      | 123          |
      | price     | '12.34'      |

  @products_create @errors_validation
  Scenario: creating a product rejects an empty request body
    * def author = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("products-create-empty-body"))' }
    Given path 'api', 'products'
    And headers authHeaders(author.token)
    And header Content-Type = 'application/json'
    And request ''
    When method post
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Bad Request'
    * match response.status == 400
    * match response.detail == 'Malformed request body.'
    * match response.errors == ['Malformed request body.']

  @products_update @errors_validation
  Scenario Outline: updating a product rejects missing required fields declared by the contract
    * def author = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("products-update-missing"))' }
    * def created = call read('classpath:karate/helpers/products/create-product.feature') { token: '#(author.token)', name: 'Original Product', price: 10.00 }
    * def requestBody = buildUpdateProductRequest()
    * remove requestBody.<fieldPath>
    Given path 'api', 'products', created.productId
    And headers authHeaders(author.token)
    And request requestBody
    When method put
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Validation failed'
    * match response.status == 400
    * match response.errors contains <expectedError>

    Examples:
      | fieldPath | expectedError              |
      | name      | 'name: must not be blank'  |
      | price     | 'price: must not be null'  |

  @products_update @errors_validation
  Scenario: updating a product rejects blank names
    * def author = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("products-update-blank"))' }
    * def created = call read('classpath:karate/helpers/products/create-product.feature') { token: '#(author.token)', name: 'Original Product', price: 10.00 }
    Given path 'api', 'products', created.productId
    And headers authHeaders(author.token)
    And request buildUpdateProductRequest({ name: '' })
    When method put
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Validation failed'
    * match response.status == 400
    * match response.errors contains 'name: must not be blank'

  @products_update @errors_validation
  Scenario Outline: updating a product rejects size and numeric constraints declared by the contract
    * def author = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("products-update-constraints"))' }
    * def created = call read('classpath:karate/helpers/products/create-product.feature') { token: '#(author.token)', name: 'Original Product', price: 10.00 }
    * def requestBody = buildUpdateProductRequest()
    * set requestBody.<fieldPath> = <invalidValue>
    Given path 'api', 'products', created.productId
    And headers authHeaders(author.token)
    And request requestBody
    When method put
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Validation failed'
    * match response.status == 400
    * def errorText = response.errors.join(' | ')
    * match errorText contains <expectedFragment>

    Examples:
      | fieldPath | invalidValue    | expectedFragment                         |
      | name      | longProductName | 'name: size must be between 0 and 120'   |
      | price     | 0               | 'price: must be greater than 0.00'       |
      | price     | -1              | 'price: must be greater than 0.00'       |
      | price     | 10.123          | 'numeric value out of bounds'            |
      | price     | 12345678901.00  | 'numeric value out of bounds'            |

  @products_update @errors_validation
  Scenario Outline: updating a product rejects type coercion for contract fields
    * def author = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("products-update-coercion"))' }
    * def created = call read('classpath:karate/helpers/products/create-product.feature') { token: '#(author.token)', name: 'Original Product', price: 10.00 }
    * def requestBody = buildUpdateProductRequest()
    * set requestBody.<fieldPath> = <invalidValue>
    Given path 'api', 'products', created.productId
    And headers authHeaders(author.token)
    And request requestBody
    When method put
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Bad Request'
    * match response.status == 400
    * match response.detail == 'Malformed request body.'
    * match response.errors == ['Malformed request body.']

    Examples:
      | fieldPath | invalidValue |
      | name      | 123          |
      | price     | '12.34'      |

  @products_update @errors_validation
  Scenario: updating a product rejects an empty request body
    * def author = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("products-update-empty-body"))' }
    * def created = call read('classpath:karate/helpers/products/create-product.feature') { token: '#(author.token)', name: 'Original Product', price: 10.00 }
    Given path 'api', 'products', created.productId
    And headers authHeaders(author.token)
    And header Content-Type = 'application/json'
    And request ''
    When method put
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Bad Request'
    * match response.status == 400
    * match response.detail == 'Malformed request body.'
    * match response.errors == ['Malformed request body.']
