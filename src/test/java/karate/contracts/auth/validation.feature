@regression @auth
Feature: Authentication validation errors

  Background:
    * url baseUrl
    * configure headers = defaultHeaders
    * def longName = repeat('a', 121)
    * def longEmail = repeat('a', 243) + '@example.com'
    * def longPhoneNumber = repeat('1', 41)
    * def longPhoneCode = repeat('1', 11)

  @auth_register @errors_validation
  Scenario Outline: register rejects missing required fields declared by the contract
    * def requestBody = buildRegisterUserRequest({ email: newEmail('register-missing') })
    * remove requestBody.<fieldPath>
    Given path 'api', 'auth', 'register'
    And request requestBody
    When method post
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Validation failed'
    * match response.status == 400
    * match response.errors contains <expectedError>

    Examples:
      | fieldPath             | expectedError                              |
      | name                  | 'name: must not be blank'                  |
      | email                 | 'email: must not be blank'                 |
      | password              | 'password: must not be blank'              |
      | phones                | 'phones: must not be empty'                |
      | phones[0].number      | 'phones[0].number: must not be blank'      |
      | phones[0].cityCode    | 'phones[0].cityCode: must not be blank'    |
      | phones[0].countryCode | 'phones[0].countryCode: must not be blank' |

  @auth_register @errors_validation
  Scenario Outline: register rejects blank string fields declared by the contract
    * def requestBody = buildRegisterUserRequest({ email: newEmail('register-blank') })
    * set requestBody.<fieldPath> = ''
    Given path 'api', 'auth', 'register'
    And request requestBody
    When method post
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Validation failed'
    * match response.status == 400
    * match response.errors contains <expectedError>

    Examples:
      | fieldPath             | expectedError                              |
      | name                  | 'name: must not be blank'                  |
      | email                 | 'email: must not be blank'                 |
      | password              | 'password: must not be blank'              |
      | phones[0].number      | 'phones[0].number: must not be blank'      |
      | phones[0].cityCode    | 'phones[0].cityCode: must not be blank'    |
      | phones[0].countryCode | 'phones[0].countryCode: must not be blank' |

  @auth_register @errors_validation
  Scenario Outline: register rejects size constraints declared by the contract
    * def requestBody = buildRegisterUserRequest({ email: newEmail('register-size') })
    * set requestBody.<fieldPath> = <invalidValue>
    Given path 'api', 'auth', 'register'
    And request requestBody
    When method post
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Validation failed'
    * match response.status == 400
    * def errorText = response.errors.join(' | ')
    * match errorText contains <expectedFragment>

    Examples:
      | fieldPath             | invalidValue    | expectedFragment                             |
      | name                  | longName        | 'name: size must be between 0 and 120'       |
      | email                 | longEmail       | 'email: size must be between 0 and 254'      |
      | phones[0].number      | longPhoneNumber | 'phones[0].number: size must be between 0 and 40' |
      | phones[0].cityCode    | longPhoneCode   | 'phones[0].cityCode: size must be between 0 and 10' |
      | phones[0].countryCode | longPhoneCode   | 'phones[0].countryCode: size must be between 0 and 10' |

  @auth_register @errors_validation
  Scenario: register rejects invalid email format
    Given path 'api', 'auth', 'register'
    And request buildRegisterUserRequest({ email: 'not-an-email' })
    When method post
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Validation failed'
    * match response.status == 400
    * match response.errors contains 'email: must be a well-formed email address'

  @auth_register @errors_validation
  Scenario Outline: register rejects type coercion for contract fields
    * def requestBody = buildRegisterUserRequest({ email: newEmail('register-coercion') })
    * set requestBody.<fieldPath> = <invalidValue>
    Given path 'api', 'auth', 'register'
    And request requestBody
    When method post
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

  @auth_register @errors_validation
  Scenario: register rejects an empty request body
    Given path 'api', 'auth', 'register'
    And header Content-Type = 'application/json'
    And request ''
    When method post
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Bad Request'
    * match response.status == 400
    * match response.detail == 'Malformed request body.'
    * match response.errors == ['Malformed request body.']

  @auth_register @errors_business
  Scenario: weak passwords return the managed business validation contract
    Given path 'api', 'auth', 'register'
    And request buildRegisterUserRequest({ email: newEmail('weak-password'), password: 'weak' })
    When method post
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Business Rule Violation'
    * match response.status == 400
    * match response.detail == 'Password must contain upper and lower case letters, a number, a special character and at least 8 characters'
    * match response.errors == ['Password must contain upper and lower case letters, a number, a special character and at least 8 characters']

  @auth_login @errors_validation
  Scenario Outline: login rejects missing required fields declared by the contract
    * def requestBody = buildLoginRequest(newEmail('login-missing'), 'StrongPass1!')
    * remove requestBody.<fieldPath>
    Given path 'api', 'auth', 'login'
    And request requestBody
    When method post
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Validation failed'
    * match response.status == 400
    * match response.errors contains <expectedError>

    Examples:
      | fieldPath | expectedError                 |
      | email     | 'email: must not be blank'    |
      | password  | 'password: must not be blank' |

  @auth_login @errors_validation
  Scenario Outline: login rejects blank string fields declared by the contract
    * def requestBody = buildLoginRequest(newEmail('login-blank'), 'StrongPass1!')
    * set requestBody.<fieldPath> = ''
    Given path 'api', 'auth', 'login'
    And request requestBody
    When method post
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Validation failed'
    * match response.status == 400
    * match response.errors contains <expectedError>

    Examples:
      | fieldPath | expectedError                 |
      | email     | 'email: must not be blank'    |
      | password  | 'password: must not be blank' |

  @auth_login @errors_validation
  Scenario: login rejects invalid email format
    Given path 'api', 'auth', 'login'
    And request buildLoginRequest('not-an-email', 'StrongPass1!')
    When method post
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Validation failed'
    * match response.status == 400
    * match response.errors contains 'email: must be a well-formed email address'

  @auth_login @errors_validation
  Scenario: login rejects overlong email values
    Given path 'api', 'auth', 'login'
    And request buildLoginRequest(longEmail, 'StrongPass1!')
    When method post
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Validation failed'
    * match response.status == 400
    * def errorText = response.errors.join(' | ')
    * match errorText contains 'email: size must be between 0 and 254'

  @auth_login @errors_validation
  Scenario Outline: login rejects type coercion for contract fields
    * def requestBody = buildLoginRequest(newEmail('login-coercion'), 'StrongPass1!')
    * set requestBody.<fieldPath> = <invalidValue>
    Given path 'api', 'auth', 'login'
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
      | email     | 123          |
      | password  | 123          |

  @auth_login @errors_validation
  Scenario: login rejects an empty request body
    Given path 'api', 'auth', 'login'
    And header Content-Type = 'application/json'
    And request ''
    When method post
    Then status 400
    * match response == problemDetailSchema
    * match response.title == 'Bad Request'
    * match response.status == 400
    * match response.detail == 'Malformed request body.'
    * match response.errors == ['Malformed request body.']
