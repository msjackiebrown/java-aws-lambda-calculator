package calculator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.util.Map;

public class Calculator implements RequestHandler<Map<String, String>, String> {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public String handleRequest(Map<String, String> event, Context context) {
        try {
            int number1 = Integer.parseInt(event.get("number1"));
            int number2 = Integer.parseInt(event.get("number2"));
            String operation = event.get("operation");

            int result;
            switch (operation) {
                case "add":
                    result = number1 + number2;
                    break;
                case "subtract":
                    result = number1 - number2;
                    break;
                case "multiply":
                    result = number1 * number2;
                    break;
                case "divide":
                    if (number2 == 0) {
                        return "Error: Division by zero is not allowed";
                    }
                    result = number1 / number2;
                    break;
                default:
                    return "Error: Invalid operation";
            }

            JsonObject response = new JsonObject();
            response.addProperty("result", result);
            return GSON.toJson(response);

        } catch (Exception e) {
            JsonObject error = new JsonObject();
            error.addProperty("error", e.getMessage());
            return GSON.toJson(error);
        }
    }
}