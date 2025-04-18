# AWS Lambda Calculator

A serverless calculator application built using AWS Lambda and Java 21. This service provides basic arithmetic operations through a Lambda Function URL.

## Architecture
```plaintext
                    ┌─────────────────┐
                    │  Lambda URL     │
                    │  (HTTPS)        │
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
- Lambda Function URL endpoint (no API Gateway required)
- Error handling for invalid operations
- CORS enabled
- Serverless architecture

## Technical Stack

- Java 21
- AWS Lambda
- AWS Lambda Function URLs
- AWS SAM (Serverless Application Model)
- Maven
- GSON for JSON processing

## API Usage

### Endpoint
POST to the Lambda Function URL (available in CloudFormation outputs after deployment)

### Request Format
```json
{
    "a": 10,
    "b": 5,
    "op": "+"
}
```

Supported operations:
- `+` (addition)
- `-` (subtraction)
- `*` (multiplication)
- `/` (division)

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