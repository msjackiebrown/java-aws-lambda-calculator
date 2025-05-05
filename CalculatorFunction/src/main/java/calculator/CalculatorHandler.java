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

import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

public class CalculatorHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = Logger.getLogger(CalculatorHandler.class.getName());
    
    private final CalculatorService calculatorService;
    
    public CalculatorHandler() {
        this.calculatorService = new CalculatorService();
    }
    
    // For testing purposes - allows injecting a mock service
    CalculatorHandler(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        LOGGER.info("Received API Gateway event: " + event);
        
        try {
            String result;
            
            // Get the request body
            String body = event.getBody();
            if (body != null && !body.isEmpty()) {
                // This is a POST request with JSON body
                try {
                    JsonObject jsonRequest = JsonParser.parseString(body).getAsJsonObject();
                    result = processCalculation(jsonRequest);
                } catch (JsonSyntaxException e) {
                    LOGGER.warning("Invalid JSON in request body: " + e.getMessage());
                    result = calculatorService.errorResponse("Invalid JSON format in request body");
                }
            } else {
                // Handle query parameters (GET request)
                Map<String, String> queryParams = event.getQueryStringParameters();
                if (queryParams != null) {
                    result = processQueryParams(queryParams);
                } else {
                    LOGGER.warning("No query parameters or request body found");
                    result = calculatorService.errorResponse("No calculation parameters provided");
                }
            }
            
            // Create response with appropriate headers
            APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
            response.setStatusCode(200);
            response.setBody(result);
            
            // Set CORS headers
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Access-Control-Allow-Origin", "*");
            headers.put("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
            response.setHeaders(headers);
            
            return response;
        } catch (Exception e) {
            LOGGER.severe("Unexpected error: " + e.getMessage());
            
            // Create error response
            APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
            response.setStatusCode(500);
            response.setBody(calculatorService.errorResponse("Error processing request: " + e.getMessage()));
            
            // Set CORS headers
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Access-Control-Allow-Origin", "*");
            response.setHeaders(headers);
            
            return response;
        }
    }
    
    private String processQueryParams(Map<String, String> params) {
        // First try the documented API format
        if (params.containsKey("a") && params.containsKey("b") && params.containsKey("op")) {
            try {
                double a = Double.parseDouble(params.get("a"));
                double b = Double.parseDouble(params.get("b"));
                String op = params.get("op");
                
                calculatorService.validateOperation(op);
                return calculatorService.calculateResult(a, b, op);
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid number format: " + e.getMessage());
                return calculatorService.errorResponse("Parameters 'a' and 'b' must be valid numbers");
            } catch (IllegalArgumentException e) {
                LOGGER.warning("Validation error: " + e.getMessage());
                return calculatorService.errorResponse(e.getMessage());
            }
        }
        
        // Try legacy format
        if (params.containsKey("number1") && params.containsKey("number2") && params.containsKey("operation")) {
            try {
                double number1 = Double.parseDouble(params.get("number1"));
                double number2 = Double.parseDouble(params.get("number2"));
                String operation = params.get("operation");
                
                // Convert legacy operation format to new
                String op = calculatorService.convertOperation(operation);
                calculatorService.validateOperation(op);
                return calculatorService.calculateResult(number1, number2, op);
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid number format: " + e.getMessage());
                return calculatorService.errorResponse("Parameters 'number1' and 'number2' must be valid numbers");
            } catch (IllegalArgumentException e) {
                LOGGER.warning("Validation error: " + e.getMessage());
                return calculatorService.errorResponse(e.getMessage());
            }
        }
        
        LOGGER.warning("Missing required parameters");
        return calculatorService.errorResponse("Missing required parameters. Please provide 'a', 'b', and 'op' or 'number1', 'number2', and 'operation'");
    }
    
    private String processCalculation(JsonObject jsonRequest) {
        try {
            // Validate required fields exist
            if (!jsonRequest.has("a") || !jsonRequest.has("b") || !jsonRequest.has("op")) {
                LOGGER.warning("Missing required JSON parameters");
                return calculatorService.errorResponse("Missing required parameters. Please provide 'a', 'b', and 'op'");
            }
            
            // Validate number formats
            try {
                double a = jsonRequest.get("a").getAsDouble();
                double b = jsonRequest.get("b").getAsDouble();
                String op = jsonRequest.get("op").getAsString();
                
                calculatorService.validateOperation(op);
                return calculatorService.calculateResult(a, b, op);
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid number in JSON: " + e.getMessage());
                return calculatorService.errorResponse("Parameters 'a' and 'b' must be valid numbers");
            }
        } catch (Exception e) {
            LOGGER.severe("Error processing JSON request: " + e.getMessage());
            return calculatorService.errorResponse("Error processing JSON request: " + e.getMessage());
        }
    }
}