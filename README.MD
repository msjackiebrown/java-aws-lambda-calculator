# AWS Lambda Calculator

A serverless calculator application built using AWS Lambda and Java 21. This service provides basic arithmetic operations through a REST API endpoint.

## Architecture
```plaintext
                    ┌─────────────────┐
                    │   API Gateway   │
                    │   (HTTP API)    │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │  AWS Lambda     │
                    │  (Java 21)      │
                    └─────────────────┘
```

## Features

- Basic arithmetic operations:
  - Addition
  - Subtraction
  - Multiplication
  - Division
- RESTful API endpoint
- Error handling for invalid operations
- CORS enabled
- Serverless architecture

## Technical Stack

- Java 21
- AWS Lambda
- AWS SAM (Serverless Application Model)
- Maven
- GSON for JSON processing

## API Usage

### Endpoint
POST to the Lambda Function URL

### Request Format
```json
{
    "number1": 10,
    "number2": 5,
    "operation": "add"
}
```

Supported operations:
- `add`
- `subtract`
- `multiply`
- `divide`

### Response Format
```json
{
    "result": 15
}
```

## Development

### Prerequisites
- Java 21
- AWS SAM CLI
- AWS CLI configured with appropriate credentials
- Maven

### Build
```bash
sam build
```

### Local Testing
```bash
sam local invoke
```

### Deploy
```bash
sam deploy
```

## Project Structure

```
.
├── template.yaml           # SAM template
├── samconfig.toml         # SAM configuration
├── CalculatorFunction/    # Lambda function code
│   ├── pom.xml           # Maven configuration
│   └── src/
│       └── main/
│           └── java/
│               └── calculator/
│                   └── Calculator.java
└── events/               # Test events
    └── event.json
```

## Best Practices and Recommendations

1. Error Handling
   - Implement comprehensive error handling for all arithmetic operations
   - Return meaningful error messages for invalid inputs
   - Log errors appropriately using CloudWatch

2. Testing
   - Write unit tests for all calculator operations
   - Include integration tests for API endpoints
   - Use AWS SAM local for testing before deployment

3. Security
   - Enable AWS X-Ray for tracing
   - Implement proper IAM roles and permissions
   - Consider adding API authentication

4. Performance
   - Configure appropriate memory and timeout settings
   - Implement caching if needed
   - Monitor execution times and costs

## Monitoring and Maintenance

### CloudWatch Metrics to Monitor
- Invocation count
- Error rate
- Duration
- Memory usage

### Logging
- Enable detailed CloudWatch logging
- Implement structured logging
- Set up log retention policies

## Contribution Guidelines

1. Fork the repository
2. Create a feature branch
3. Commit changes with clear messages
4. Submit pull request with detailed description

## License

This project is licensed under the MIT License - see the LICENSE file for details.