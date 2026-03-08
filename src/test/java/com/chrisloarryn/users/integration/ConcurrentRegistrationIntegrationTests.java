package com.chrisloarryn.users.integration;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.chrisloarryn.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class ConcurrentRegistrationIntegrationTests extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void concurrentRegistrationWithTheSameEmailYieldsOneCreationAndOneConflict() throws Exception {
        String email = "race@example.com";
        CountDownLatch startSignal = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Callable<Integer> register = () -> {
                startSignal.await(5, TimeUnit.SECONDS);
                return mockMvc.perform(post("/api/auth/register")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                          "name":"Race User",
                                          "email":"%s",
                                          "password":"StrongPass1!",
                                          "phones":[{"number":"123456789","cityCode":"1","countryCode":"56"}]
                                        }
                                        """.formatted(email)))
                        .andReturn()
                        .getResponse()
                        .getStatus();
            };

            List<Future<Integer>> futures = List.of(executor.submit(register), executor.submit(register));
            startSignal.countDown();

            int firstStatus = futures.get(0).get(10, TimeUnit.SECONDS);
            int secondStatus = futures.get(1).get(10, TimeUnit.SECONDS);

            assertTrue(
                    (firstStatus == 201 && secondStatus == 409) || (firstStatus == 409 && secondStatus == 201),
                    () -> "Expected one 201 and one 409 but got " + firstStatus + " and " + secondStatus);
            assertEquals(1, userRepository.findAll().size());
        } finally {
            executor.shutdownNow();
        }
    }
}
