# Quarkus Product Management System

A reactive REST API application built with Quarkus for managing products with full CRUD operations and additional business features.

## Features

- **CRUD Operations**: Create, Read, Update, Delete products
- **Stock Management**: Check stock availability for specific quantities
- **Product Search**: Search products by name (case-insensitive)
- **Price Sorting**: Get products ordered by price in ascending order
- **Low Stock Alerts**: Find products with stock below specified threshold
- **Reactive Programming**: Built with Quarkus Reactive for high performance
- **Comprehensive Testing**: Unit tests for all endpoints and business logic
- **API Documentation**: Swagger UI integration for easy API exploration
- **Validation**: Input validation with proper error handling

## Technology Stack

- **Framework**: Quarkus 3.6.0
- **Database**: PostgreSQL (with H2 for testing)
- **ORM**: Hibernate Reactive with Panache
- **Reactive**: Mutiny for reactive programming
- **Testing**: JUnit 5, REST Assured
- **Documentation**: OpenAPI/Swagger UI
- **Build Tool**: Maven

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- PostgreSQL (for production)
- Docker (optional, for PostgreSQL)

## Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd quarkus-product-management
```

### 2. Database Setup

#### Option A: Using Docker (Recommended)

```bash
# Start PostgreSQL container
docker run --name postgres-productdb \
  -e POSTGRES_DB=productdb \
  -e POSTGRES_USER=quarkus \
  -e POSTGRES_PASSWORD=quarkus \
  -p 5432:5432 \
  -d postgres:13
```

#### Option B: Local PostgreSQL Installation

1. Install PostgreSQL
2. Create database: `productdb`
3. Create user: `quarkus` with password: `quarkus`
4. Grant privileges to the user

### 3. Run the Application

#### Development Mode

```bash
./mvnw compile quarkus:dev
```

The application will be available at: `http://localhost:8080`

#### Production Mode

```bash
./mvnw clean package
java -jar target/quarkus-app/quarkus-run.jar
```

### 4. Access API Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui
- **OpenAPI Spec**: http://localhost:8080/openapi

## API Endpoints

### Product Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/products` | Create a new product |
| GET | `/api/products` | Get all products |
| GET | `/api/products/{id}` | Get product by ID |
| PUT | `/api/products/{id}` | Update product by ID |
| DELETE | `/api/products/{id}` | Delete product by ID |

### Additional Features

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products/{id}/stock` | Check stock availability |
| GET | `/api/products/sorted-by-price` | Get products sorted by price |
| GET | `/api/products/search` | Search products by name |
| GET | `/api/products/low-stock` | Get low stock products |

### Example API Calls

#### Create a Product

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gaming Laptop",
    "description": "High-performance gaming laptop",
    "price": 1299.99,
    "quantity": 10
  }'
```

#### Get All Products

```bash
curl http://localhost:8080/api/products
```

#### Check Stock Availability

```bash
curl "http://localhost:8080/api/products/1/stock?count=5"
```

#### Get Products Sorted by Price

```bash
curl http://localhost:8080/api/products/sorted-by-price
```

## Testing

### Run Unit Tests

```bash
./mvnw test
```

### Run Integration Tests

```bash
./mvnw verify
```

### Test Coverage

The project includes comprehensive test coverage for:
- All REST API endpoints
- Service layer business logic
- Repository layer operations
- Error handling scenarios
- Edge cases and validation

## Project Structure

```
src/
├── main/
│   ├── java/com/example/productmanagement/
│   │   ├── entity/
│   │   │   └── Product.java              # JPA Entity
│   │   ├── repository/
│   │   │   └── ProductRepository.java   # Data Access Layer
│   │   ├── service/
│   │   │   └── ProductService.java      # Business Logic Layer
│   │   └── resource/
│   │       └── ProductResource.java     # REST API Layer
│   └── resources/
│       └── application.properties       # Configuration
└── test/
    ├── java/com/example/productmanagement/
    │   ├── ProductResourceTest.java     # API Tests
    │   └── service/
    │       └── ProductServiceTest.java  # Service Tests
    └── resources/
        └── application.properties       # Test Configuration
```

## Configuration

### Application Properties

Key configuration options in `src/main/resources/application.properties`:

```properties
# Database
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=quarkus
quarkus.datasource.password=quarkus
quarkus.datasource.reactive.url=postgresql://localhost:5432/productdb

# Hibernate
quarkus.hibernate-orm.database.generation=drop-and-create
quarkus.hibernate-orm.log.sql=true

# Swagger UI
quarkus.swagger-ui.always-include=true
quarkus.swagger-ui.path=/swagger-ui
```

## Development Best Practices

### Code Quality Features

1. **Validation**: Bean validation annotations for input validation
2. **Error Handling**: Proper HTTP status codes and error messages
3. **Documentation**: OpenAPI annotations for API documentation
4. **Testing**: Comprehensive unit and integration tests
5. **Reactive Programming**: Non-blocking operations with Mutiny
6. **Transaction Management**: Proper transaction handling with `@WithTransaction`
7. **Logging**: Structured logging with appropriate log levels

### Performance Optimizations

1. **Reactive Database Operations**: Non-blocking database access
2. **Connection Pooling**: Efficient database connection management
3. **Caching**: Built-in Quarkus caching capabilities
4. **Native Compilation**: Support for GraalVM native compilation

## Docker Support

### Build Docker Image

```bash
./mvnw package -Pnative -Dquarkus.native.container-build=true
docker build -f src/main/docker/Dockerfile.native -t quarkus-product-management .
```

### Run with Docker Compose

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:13
    environment:
      POSTGRES_DB: productdb
      POSTGRES_USER: quarkus
      POSTGRES_PASSWORD: quarkus
    ports:
      - "5432:5432"
  
  app:
    image: quarkus-product-management
    ports:
      - "8080:8080"
    depends_on:
      - postgres
```

## Monitoring and Observability

- **Health Checks**: Built-in health check endpoints
- **Metrics**: Micrometer metrics integration
- **Tracing**: Distributed tracing support
- **Logging**: Structured JSON logging

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

For questions or issues, please create an issue in the repository or contact the development team.
