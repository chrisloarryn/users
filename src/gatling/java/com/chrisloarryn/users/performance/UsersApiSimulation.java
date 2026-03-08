package com.chrisloarryn.users.performance;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.forAll;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class UsersApiSimulation extends Simulation {

    private static final Duration USER_THINK_TIME = Duration.ofMillis(250);

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(GatlingSettings.baseUrl())
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Gatling users-service");

    private final ScenarioBuilder usersFlow = scenario("users_api_flow")
            .exec(this::seedUser)
            .exec(http("register_user")
                    .post("/api/auth/register")
                    .body(StringBody("""
                            {
                              "name":"#{name}",
                              "email":"#{email}",
                              "password":"#{password}",
                              "phones":[{"number":"123456789","cityCode":"1","countryCode":"56"}]
                            }
                            """))
                    .asJson()
                    .check(status().is(201))
                    .check(jsonPath("$.user.id").saveAs("userId")))
            .pause(USER_THINK_TIME)
            .exec(http("login_user")
                    .post("/api/auth/login")
                    .body(StringBody("""
                            {
                              "email":"#{email}",
                              "password":"#{password}"
                            }
                            """))
                    .asJson()
                    .check(status().is(200))
                    .check(jsonPath("$.accessToken").saveAs("accessToken")))
            .pause(USER_THINK_TIME)
            .exec(http("list_users")
                    .get("/api/users")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200)))
            .exec(http("get_user_by_id")
                    .get("/api/users/#{userId}")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
                    .check(jsonPath("$.id").isEL("#{userId}")))
            .pause(USER_THINK_TIME)
            .exec(http("update_user")
                    .put("/api/users/#{userId}")
                    .header("Authorization", "Bearer #{accessToken}")
                    .body(StringBody("""
                            {
                              "name":"#{updatedName}",
                              "email":"#{updatedEmail}",
                              "password":"#{updatedPassword}",
                              "phones":[{"number":"999999999","cityCode":"2","countryCode":"56"}]
                            }
                            """))
                    .asJson()
                    .check(status().is(200))
                    .check(jsonPath("$.email").isEL("#{updatedEmail}")))
            .pause(USER_THINK_TIME)
            .exec(http("create_product")
                    .post("/api/products")
                    .header("Authorization", "Bearer #{accessToken}")
                    .body(StringBody("""
                            {
                              "name":"#{productName}",
                              "price":#{productPrice}
                            }
                            """))
                    .asJson()
                    .check(status().is(201))
                    .check(jsonPath("$.id").saveAs("productId"))
                    .check(jsonPath("$.createdByUserId").isEL("#{userId}"))
                    .check(jsonPath("$.updatedByUserId").isEL("#{userId}")))
            .exec(http("list_products")
                    .get("/api/products")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200)))
            .exec(http("get_product_by_id")
                    .get("/api/products/#{productId}")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
                    .check(jsonPath("$.id").isEL("#{productId}"))
                    .check(jsonPath("$.createdByUserId").isEL("#{userId}")))
            .exec(http("get_products_by_user")
                    .get("/api/users/#{userId}/products")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200)))
            .exec(http("get_user_product_by_id")
                    .get("/api/users/#{userId}/products/#{productId}")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
                    .check(jsonPath("$.id").isEL("#{productId}"))
                    .check(jsonPath("$.createdByUserId").isEL("#{userId}")))
            .pause(USER_THINK_TIME)
            .exec(http("update_product")
                    .put("/api/products/#{productId}")
                    .header("Authorization", "Bearer #{accessToken}")
                    .body(StringBody("""
                            {
                              "name":"#{updatedProductName}",
                              "price":#{updatedProductPrice}
                            }
                            """))
                    .asJson()
                    .check(status().is(200))
                    .check(jsonPath("$.id").isEL("#{productId}"))
                    .check(jsonPath("$.name").isEL("#{updatedProductName}"))
                    .check(jsonPath("$.updatedByUserId").isEL("#{userId}")))
            .pause(USER_THINK_TIME)
            .exec(http("delete_product")
                    .delete("/api/products/#{productId}")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(204)))
            .exec(http("delete_user")
                    .delete("/api/users/#{userId}")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(204)));

    {
        setUp(usersFlow.injectOpen(
                        rampUsers(GatlingSettings.users()).during(GatlingSettings.rampDuration()),
                        constantUsersPerSec(GatlingSettings.users()).during(GatlingSettings.holdDuration())))
                .protocols(httpProtocol)
                .assertions(
                        global().failedRequests().count().is(0L),
                        global().responseTime().percentile4().lt(1500),
                        forAll().successfulRequests().percent().is(100.0));
    }

    private Session seedUser(Session session) {
        String uniqueSuffix = System.currentTimeMillis() + "-" + ThreadLocalRandom.current().nextInt(1_000_000);
        return session
                .set("name", "Gatling User " + uniqueSuffix)
                .set("email", "gatling-" + uniqueSuffix + "@example.com")
                .set("password", "StrongPass1!")
                .set("updatedName", "Updated User " + uniqueSuffix)
                .set("updatedEmail", "updated-" + uniqueSuffix + "@example.com")
                .set("updatedPassword", "NewStrong1!")
                .set("productName", "Gatling Product " + uniqueSuffix)
                .set("productPrice", "12.34")
                .set("updatedProductName", "Updated Product " + uniqueSuffix)
                .set("updatedProductPrice", "99.99");
    }
}
