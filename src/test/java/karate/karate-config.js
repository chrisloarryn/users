function fn() {
  var UUID = Java.type('java.util.UUID');
  var config = {};

  config.baseUrl = karate.properties['karate.baseUrl'] || 'http://127.0.0.1:8080';
  config.defaultHeaders = { Accept: 'application/json' };
  config.uuidPattern = '^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$';
  config.isoInstantPattern = '^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{1,9})?Z$';
  config.jwtPattern = '^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$';

  config.uuid = '#regex ' + config.uuidPattern;
  config.isoInstant = '#regex ' + config.isoInstantPattern;
  config.jwt = '#regex ' + config.jwtPattern;

  config.phoneResponseSchema = {
    number: '#string',
    cityCode: '#string',
    countryCode: '#string',
    createdAt: config.isoInstant
  };

  config.userResponseSchema = {
    id: config.uuid,
    name: '#string',
    email: '#string',
    phones: '#[]',
    createdAt: config.isoInstant,
    updatedAt: config.isoInstant,
    lastLoginAt: config.isoInstant,
    active: '#boolean'
  };

  config.authResponseSchema = {
    tokenType: '#string',
    accessToken: config.jwt,
    expiresAt: config.isoInstant,
    user: '#object'
  };

  config.productResponseSchema = {
    id: config.uuid,
    name: '#string',
    price: '#number',
    createdAt: config.isoInstant,
    updatedAt: config.isoInstant,
    createdByUserId: config.uuid,
    updatedByUserId: config.uuid
  };

  config.problemDetailSchema = {
    type: '#string',
    title: '#string',
    status: '#number',
    detail: '#string',
    instance: '##string',
    errors: '#[] #string'
  };

  config.newEmail = function(prefix) {
    return prefix + '+' + UUID.randomUUID() + '@example.com';
  };

  config.newName = function(prefix) {
    return prefix + '-' + UUID.randomUUID();
  };

  config.repeat = function(value, count) {
    return new Array(count + 1).join(value);
  };

  config.basePhones = function() {
    return [
      {
        number: '123456789',
        cityCode: '1',
        countryCode: '56'
      }
    ];
  };

  config.authHeaders = function(token) {
    return {
      Accept: 'application/json',
      Authorization: 'Bearer ' + token
    };
  };

  config.buildRegisterUserRequest = function(overrides) {
    var uuid = Java.type('java.util.UUID').randomUUID();
    var payload = {
      name: 'Karate User',
      email: 'karate-user+' + uuid + '@example.com',
      password: 'StrongPass1!',
      phones: [
        {
          number: '123456789',
          cityCode: '1',
          countryCode: '56'
        }
      ]
    };
    if (overrides) {
      for (var key in overrides) {
        if (Object.prototype.hasOwnProperty.call(overrides, key) && overrides[key] !== null && overrides[key] !== undefined) {
          payload[key] = overrides[key];
        }
      }
    }
    return payload;
  };

  config.buildLoginRequest = function(email, password) {
    return {
      email: email,
      password: password
    };
  };

  config.buildUpdateUserRequest = function(overrides) {
    var uuid = Java.type('java.util.UUID').randomUUID();
    var payload = {
      name: 'Updated Karate User',
      email: 'updated-user+' + uuid + '@example.com',
      password: 'NewStrong1!',
      phones: [
        {
          number: '999999999',
          cityCode: '2',
          countryCode: '56'
        }
      ]
    };
    if (overrides) {
      for (var key in overrides) {
        if (Object.prototype.hasOwnProperty.call(overrides, key) && overrides[key] !== null && overrides[key] !== undefined) {
          payload[key] = overrides[key];
        }
      }
    }
    return payload;
  };

  config.buildCreateProductRequest = function(overrides) {
    var uuid = Java.type('java.util.UUID').randomUUID();
    var payload = {
      name: 'karate-product-' + uuid,
      price: 12.34
    };
    if (overrides) {
      for (var key in overrides) {
        if (Object.prototype.hasOwnProperty.call(overrides, key) && overrides[key] !== null && overrides[key] !== undefined) {
          payload[key] = overrides[key];
        }
      }
    }
    return payload;
  };

  config.buildUpdateProductRequest = function(overrides) {
    var uuid = Java.type('java.util.UUID').randomUUID();
    var payload = {
      name: 'updated-product-' + uuid,
      price: 99.99
    };
    if (overrides) {
      for (var key in overrides) {
        if (Object.prototype.hasOwnProperty.call(overrides, key) && overrides[key] !== null && overrides[key] !== undefined) {
          payload[key] = overrides[key];
        }
      }
    }
    return payload;
  };

  return config;
}
