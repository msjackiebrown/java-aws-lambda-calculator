package calculator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;

public class Calculator implements RequestHandler<Map<String, String>, String> {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = Logger.getLogger(Calculator.class.getName());
    private static final String[] VALID_OPERATIONS = {"+", "-", "*", "/"};

    public String handleRequest(Map<String, String> event, Context context) {
        LOGGER.info("Received event: " + event);
        
        try {
            // Check if this is a direct invocation or from an API Gateway event
            String body = event.get("body");
            if (body != null) {
                // This is from API Gateway, parse the body
                try {
                    JsonObject jsonRequest = JsonParser.parseString(body).getAsJsonObject();
                    return processCalculation(jsonRequest);
                } catch (JsonSyntaxException e) {
                    LOGGER.warning("Invalid JSON in request body: " + e.getMessage());
                    return errorResponse("Invalid JSON format in request body");
                }
            } else {
                // Handle direct invocation with parameters in event
                if (event.containsKey("a") && event.containsKey("b") && event.containsKey("op")) {
                    // Using documented API format
                    try {
                        double a = Double.parseDouble(event.get("a"));
                        double b = Double.parseDouble(event.get("b"));
                        String op = event.get("op");
                        
                        validateOperation(op);
                        return calculateResult(a, b, op);
                    } catch (NumberFormatException e) {
                        LOGGER.warning("Invalid number format: " + e.getMessage());
                        return errorResponse("Parameters 'a' and 'b' must be valid numbers");
                    } catch (IllegalArgumentException e) {
                        LOGGER.warning("Validation error: " + e.getMessage());
                        return errorResponse(e.getMessage());
                    }
                } else if (event.containsKey("number1") && event.containsKey("number2") && event.containsKey("operation")) {
                    // Using legacy format for backward compatibility
                    try {
                        double number1 = Double.parseDouble(event.get("number1"));
                        double number2 = Double.parseDouble(event.get("number2"));
                        String operation = event.get("operation");
                        
                        // Convert legacy operation format to new
                        String op = convertOperation(operation);
                        validateOperation(op);
                        return calculateResult(number1, number2, op);
                    } catch (NumberFormatException e) {
                        LOGGER.warning("Invalid number format: " + e.getMessage());
                        return errorResponse("Parameters 'number1' and 'number2' must be valid numbers");
                    } catch (IllegalArgumentException e) {
                        LOGGER.warning("Validation error: " + e.getMessage());
                        return errorResponse(e.getMessage());
                    }
                } else {
                    LOGGER.warning("Missing required parameters");
                    return errorResponse("Missing required parameters. Please provide 'a', 'b', and 'op' or 'number1', 'number2', and 'operation'");
                }
            }
        } catch (NumberFormatException e) {
            LOGGER.severe("Invalid number format: " + e.getMessage());
            return errorResponse("Invalid number format: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.severe("Unexpected error: " + e.getMessage());
            return errorResponse("Error processing request: " + e.getMessage());
        }
    }
    
    private String processCalculation(JsonObject jsonRequest) {
        try {
            // Validate required fields exist
            if (!jsonRequest.has("a") || !jsonRequest.has("b") || !jsonRequest.has("op")) {
                LOGGER.warning("Missing required JSON parameters");
                return errorResponse("Missing required parameters. Please provide 'a', 'b', and 'op'");
            }
            
            // Validate number formats
            try {
                double a = jsonRequest.get("a").getAsDouble();
                double b = jsonRequest.get("b").getAsDouble();
                String op = jsonRequest.get("op").getAsString();
                
                validateOperation(op);
                return calculateResult(a, b, op);
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid number in JSON: " + e.getMessage());
                return errorResponse("Parameters 'a' and 'b' must be valid numbers");
            }
        } catch (Exception e) {
            LOGGER.severe("Error processing JSON request: " + e.getMessage());
            return errorResponse("Error processing JSON request: " + e.getMessage());
        }
    }
    
    private void validateOperation(String op) {
        if (!Arrays.asList(VALID_OPERATIONS).contains(op)) {
            throw new IllegalArgumentException(
                "Invalid operation '" + op + "'. Supported operations are '+', '-', '*', '/'");
        }
    }
    
    private String calculateResult(double a, double b, String op) {
        double result;
        long startTime = System.currentTimeMillis();
        
        switch (op) {
            case "+":
                result = a + b;
                break;
            case "-":
                result = a - b;
                break;
            case "*":
                result = a * b;
                break;
            case "/":
                if (Math.abs(b) < 1e-10) {  // Better check for close to zero
                    LOGGER.warning("Division by zero attempt");
                    return errorResponse("Division by zero is not allowed");
                }
                result = a / b;
                break;
            default:
                // This shouldn't happen due to validateOperation, but kept as a safety
                return errorResponse("Invalid operation. Supported operations are '+', '-', '*', '/'");
        }
        
        long executionTime = System.currentTimeMillis() - startTime;
        
        // Create response with metadata (as suggested in Issue #6)
        JsonObject response = new JsonObject();
        response.addProperty("result", result);
        response.addProperty("operation", op);
        response.addProperty("operand1", a);
        response.addProperty("operand2", b);
        response.addProperty("executionTime", executionTime);
        
        LOGGER.info("Calculation complete: " + a + " " + op + " " + b + " = " + result);
        return GSON.toJson(response);
    }
    
    private String convertOperation(String legacyOp) {
        switch (legacyOp) {
            case "add":
                return "+";
            case "subtract":
                return "-";
            case "multiply":
                return "*";
            case "divide":
                return "/";
            default:
                return legacyOp;
        }
    }
    
    private String errorResponse(String message) {
        JsonObject error = new JsonObject();
        error.addProperty("error", message);
        error.addProperty("timestamp", System.currentTimeMillis());
        return GSON.toJson(error);
    }
}