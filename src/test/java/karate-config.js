function fn() {
    karate.configure('ssl', true);
    karate.configure('logPrettyRequest', true);

    let env = karate.env;

    karate.log('karate.env system property was:', env);

    if (!env) {
        env = 'dev';
    }

    let config = {
        env: env
    }

    if (env == 'local') {
        config.baseUrl = 'http://localhost:8080/api';
    } else if (env == 'dev') {
        config.baseUrl = 'https://person-service-dev.herokuapp.com';
    } else if (env == 'qa') {
        config.baseUrl = 'https://person-service-qa.herokuapp.com';
    } else if (env == 'prod') {
        config.baseUrl = 'https://person-service-prod.herokuapp.com';
    }

    karate.configure('connectTimeout', 5000);
    karate.configure('readTimeout', 5000);

    karate.log('karate configs:', config);

    return config;
}