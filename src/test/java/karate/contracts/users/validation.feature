@regression @users @users_update @errors_validation
Feature: User update validation errors

  Background:
    * url baseUrl
    * configure headers = defaultHeaders
    * def longName = repeat('a', 121)
    * def longEmail = repeat('a', 243) + '@example.com'
    * def longPhoneNumber = repeat('1', 41)
    * def longPhoneCode = repeat('1', 11)

  Scenario Outline: updating a user rejects missing required fields declared by the contract
    * def registered = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("users-update-missing"))' }
    * def requestBody = buildUpdateUserRequest()
    * remove requestBody.<fieldPath>
    Given path 'api', 'users', registered.userId
    And headers authHeaders(registered.token)
    And request requestBody
    When method put
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Validation failed'
    * match response.status == 400
    * match response.errors contains <expectedError>

    Examples:
      | fieldPath             | expectedError                              |
      | name                  | 'name: must not be blank'                  |
      | email                 | 'email: must not be blank'                 |
      | phones                | 'phones: must not be null'                 |
      | phones[0].number      | 'phones[0].number: must not be blank'      |
      | phones[0].cityCode    | 'phones[0].cityCode: must not be blank'    |
      | phones[0].countryCode | 'phones[0].countryCode: must not be blank' |

  Scenario Outline: updating a user rejects blank string fields declared by the contract
    * def registered = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("users-update-blank"))' }
    * def requestBody = buildUpdateUserRequest()
    * set requestBody.<fieldPath> = ''
    Given path 'api', 'users', registered.userId
    And headers authHeaders(registered.token)
    And request requestBody
    When method put
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Validation failed'
    * match response.status == 400
    * match response.errors contains <expectedError>

    Examples:
      | fieldPath             | expectedError                              |
      | name                  | 'name: must not be blank'                  |
      | email                 | 'email: must not be blank'                 |
      | phones[0].number      | 'phones[0].number: must not be blank'      |
      | phones[0].cityCode    | 'phones[0].cityCode: must not be blank'    |
      | phones[0].countryCode | 'phones[0].countryCode: must not be blank' |

  Scenario Outline: updating a user rejects size constraints declared by the contract
    * def registered = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("users-update-size"))' }
    * def requestBody = buildUpdateUserRequest()
    * set requestBody.<fieldPath> = <invalidValue>
    Given path 'api', 'users', registered.userId
    And headers authHeaders(registered.token)
    And request requestBody
    When method put
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Validation failed'
    * match response.status == 400
    * def errorText = response.errors.join(' | ')
    * match errorText contains <expectedFragment>

    Examples:
      | fieldPath             | invalidValue    | expectedFragment                                   |
      | name                  | longName        | 'name: size must be between 0 and 120'             |
      | email                 | longEmail       | 'email: size must be between 0 and 254'            |
      | phones[0].number      | longPhoneNumber | 'phones[0].number: size must be between 0 and 40'  |
      | phones[0].cityCode    | longPhoneCode   | 'phones[0].cityCode: size must be between 0 and 10' |
      | phones[0].countryCode | longPhoneCode   | 'phones[0].countryCode: size must be between 0 and 10' |

  Scenario: updating a user rejects invalid email format
    * def registered = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("users-update-email"))' }
    * def requestBody = buildUpdateUserRequest({ email: 'not-an-email' })
    Given path 'api', 'users', registered.userId
    And headers authHeaders(registered.token)
    And request requestBody
    When method put
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Validation failed'
    * match response.status == 400
    * match response.errors contains 'email: must be a well-formed email address'

  Scenario Outline: updating a user rejects type coercion for contract fields
    * def registered = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("users-update-coercion"))' }
    * def requestBody = buildUpdateUserRequest()
    * set requestBody.<fieldPath> = <invalidValue>
    Given path 'api', 'users', registered.userId
    And headers authHeaders(registered.token)
    And request requestBody
    When method put
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Bad Request'
    * match response.status == 400
    * match response.detail == 'Malformed request body.'
    * match response.errors == ['Malformed request body.']

    Examples:
      | fieldPath             | invalidValue |
      | name                  | 123          |
      | email                 | 123          |
      | password              | 123          |
      | phones                | 123          |
      | phones[0].number      | 123          |
      | phones[0].cityCode    | 123          |
      | phones[0].countryCode | 123          |

  Scenario: updating a user rejects an empty request body
    * def registered = call read('classpath:karate/helpers/auth/register-user.feature') { email: '#(newEmail("users-update-empty-body"))' }
    Given path 'api', 'users', registered.userId
    And headers authHeaders(registered.token)
    And header Content-Type = 'application/json'
    And request ''
    When method put
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Bad Request'
    * match response.status == 400
    * match response.detail == 'Malformed request body.'
    * match response.errors == ['Malformed request body.']
