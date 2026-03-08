package com.chrisloarryn.users.performance;

import java.time.Duration;

final class GatlingSettings {

    private GatlingSettings() {
    }

    static String baseUrl() {
        return System.getProperty("gatling.baseUrl", "http://127.0.0.1:8080");
    }

    static int users() {
        return positiveIntProperty("gatling.users", 5);
    }

    static Duration rampDuration() {
        return Duration.ofSeconds(positiveIntProperty("gatling.rampSeconds", 5));
    }

    static Duration holdDuration() {
        return Duration.ofSeconds(positiveIntProperty("gatling.holdSeconds", 10));
    }

    private static int positiveIntProperty(String propertyName, int defaultValue) {
        return Math.max(1, Integer.getInteger(propertyName, defaultValue));
    }
}
