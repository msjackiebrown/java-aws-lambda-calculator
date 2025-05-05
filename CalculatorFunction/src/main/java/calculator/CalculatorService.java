package calculator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.logging.Logger;

public class CalculatorService {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = Logger.getLogger(CalculatorService.class.getName());
    private static final String[] VALID_OPERATIONS = {"+", "-", "*", "/"};

    public void validateOperation(String op) {
        if (!Arrays.asList(VALID_OPERATIONS).contains(op)) {
            throw new IllegalArgumentException(
                "Invalid operation '" + op + "'. Supported operations are '+', '-', '*', '/'");
        }
    }
    
    public String calculateResult(double a, double b, String op) {
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
        
        // Create response with metadata
        JsonObject response = new JsonObject();
        response.addProperty("result", result);
        response.addProperty("operation", op);
        response.addProperty("operand1", a);
        response.addProperty("operand2", b);
        response.addProperty("executionTime", executionTime);
        
        LOGGER.info("Calculation complete: " + a + " " + op + " " + b + " = " + result);
        return GSON.toJson(response);
    }
    
    public String convertOperation(String legacyOp) {
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
    
    public String errorResponse(String message) {
        JsonObject error = new JsonObject();
        error.addProperty("error", message);
        error.addProperty("timestamp", System.currentTimeMillis());
        return GSON.toJson(error);
    }
}