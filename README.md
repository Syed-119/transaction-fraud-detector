# Transaction Fraud Detector

A microservices-based API that analyses credit card transactions and flags potentially fraudulent ones using a machine learning model trained on real-world transaction data.

## Architecture

The project is split into two services, both containerised with Docker Compose:

1. **Spring Boot API (Java)** — the main backend. It exposes REST endpoints, receives transaction data from the client, forwards it to the ML service for fraud analysis, stores the result in a **PostgreSQL** database, and returns the verdict to the client.

**ML Service** — a Flask API serving a pre-trained Random Forest classifier. The model was trained on the [Kaggle Credit Card Fraud Detection dataset](https://www.kaggle.com/datasets/mlg-ulb/creditcardfraud) (284K transactions, 492 fraudulent).

## Tech Stack

| Layer       | Technology                                    |
|-------------|-----------------------------------------------|
| Backend     | Java 21, Spring Boot 3.4, Spring Data JPA     |
| ML Service  | Python 3.11, Flask               |
| Database    | PostgreSQL 16                                  |
| Mapping     | MapStruct                                      |
| Testing     | JUnit 5, Mockito, MockMvc, H2 (in-memory)     |
| DevOps      | Docker, Docker Compose                         |

## Getting Started

### Prerequisites

- Docker
- Python 3.11+ (for model training)
- Java 21+ and Maven (for local development)

### 1. Clone the repository

```bash
git clone https://github.com/Syed-119/transaction-fraud-detector.git
cd transaction-fraud-detector
```

### 2. Train the ML model

Download the [Credit Card Fraud Detection dataset](https://www.kaggle.com/datasets/mlg-ulb/creditcardfraud) and place `creditcard.csv` in the `ml-service/` directory.

```bash
cd ml-service
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
python train-model.py
```

This generates `model.pkl` and `scaler.pkl`.

### 3. Run with Docker Compose

```bash
cd ..
docker-compose up --build
```

The API will be available at `http://localhost:8080`.

## API Endpoints

### Submit a transaction for analysis

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 9999.99,
    "merchantName": "Suspicious Shop",
    "category": "online",
    "cardLast4": "1234"
  }'
```

**Response (201 Created):**

```json
{
  "id": "72ff18dc-1c83-4d69-8888-45f4f4531e7c",
  "amount": 9999.99,
  "merchantName": "Suspicious Shop",
  "category": "online",
  "cardLast4": "1234",
  "transactionTime": "2026-06-07T16:40:01.678295",
  "fraudulent": false,
  "confidenceScore": 1.0,
  "createdAt": "2026-06-07T16:40:01.692223"
}
```

### Get all flagged (fraudulent) transactions

```bash
curl http://localhost:8080/api/transactions/flagged
```

### Get transactions by card number

```bash
curl http://localhost:8080/api/transactions/card/1234
```

### Get a specific transaction by ID

```bash
curl http://localhost:8080/api/transactions/72ff18dc-1c83-4d69-8888-45f4f4531e7c
```

## Running Tests

```bash
cd fraud-api
mvn test
```

Tests cover three layers:

- **Controller** — endpoint behaviour, request validation, HTTP status codes (MockMvc)
- **Service** — business logic, ML client integration, error handling (Mockito)
- **Repository** — custom queries, data persistence (H2 in-memory database)


## How the ML Model Works

The model is a **Random Forest classifier** — an ensemble of 100 decision trees that each vote on whether a transaction is fraudulent. It was trained on 284,000 real credit card transactions with the following approach:

- **StandardScaler** normalises the amount and time features
- **Stratified train/test split** (80/20) preserves the fraud ratio in both sets
- **Balanced class weights** compensate for the heavily imbalanced dataset (since only 0.17% is fraud)
- The model returns a fraud verdict and a confidence score (which is the proportion of trees that agreed)
