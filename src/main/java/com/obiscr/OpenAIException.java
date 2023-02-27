package com.obiscr;

/**
 * OpenAIException
 * @author wuzi
 */
public class OpenAIException extends Exception {
    private final String location;
    private final Integer statusCode;
    private final String details;

    public OpenAIException(String location, Integer statusCode, String details) {
        this.location = location;
        this.statusCode = statusCode;
        this.details = details;
    }

    @Override
    public String toString() {
        return "OpenAIException{" +
                "location='" + location + '\'' +
                ", statusCode=" + statusCode +
                ", details='" + details + '\'' +
                '}';
    }
}
