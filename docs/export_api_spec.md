# Export Dashboard API Specification

## API Overview

The Export Dashboard API is a RESTful API built with Spring Boot 3.2.0 and Java 17 that provides comprehensive export statistics data for South Korea. The API follows Domain-Driven Design (DDD) principles and implements a clean architecture pattern.

### Base Information
- **Base URL**: `http://localhost:8080/api`
- **API Version**: 1.0.0
- **Content Type**: `application/json`
- **Architecture**: Hexagonal Architecture with DDD
- **Framework**: Spring Boot 3.2.0
- **Java Version**: Java 17

## Authentication and Authorization

### Current Security Configuration
The API currently **does not implement authentication or authorization**. All endpoints are publicly accessible.

#### CORS Configuration
- **Allowed Origins**: `*` (all origins)
- **Allowed Methods**: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`
- **Allowed Headers**: `*` (all headers)
- **Credentials**: Allowed
- **Max Age**: 3600 seconds

### Security Considerations
⚠️ **Important**: The current implementation is suitable for development/demo purposes only. For production deployment, consider implementing:
- JWT-based authentication
- Role-based access control (RBAC)
- API rate limiting
- Input validation and sanitization
- HTTPS enforcement

## Architecture Overview

### Domain Model
The API follows DDD principles with the following aggregate roots:
- **Country**: Manages country information and geographic data
- **ProductCategory**: Manages HS code-based product classifications
- **ExportStatistic**: Manages export data with business logic for calculations

### Key Design Patterns
- **Aggregate Root Pattern**: Ensures data consistency within domain boundaries
- **Value Objects**: Immutable objects for domain concepts (Money, Percentage, CountryCode, HsCode)
- **Domain Events**: Event-driven architecture for cross-aggregate communication
- **Repository Pattern**: Data access abstraction
- **Application Services**: Use case orchestration

## API Endpoints

### 1. Countries API

#### GET /countries
Retrieve all active countries.

**Response:**
```json
[
  {
    "id": 1,
    "countryCode": "KOR",
    "nameKo": "한국",
    "nameEn": "Korea",
    "region": "Asia",
    "continent": "Asia",
    "active": true
  }
]
```

#### GET /countries/{id}
Retrieve a specific country by ID.

**Parameters:**
- `id` (path): Country ID (Long)

**Response:**
```json
{
  "id": 1,
  "countryCode": "KOR",
  "nameKo": "한국",
  "nameEn": "Korea",
  "region": "Asia",
  "continent": "Asia",
  "active": true
}
```

**Error Responses:**
- `404 Not Found`: Country not found

#### GET /countries/code/{countryCode}
Retrieve a country by country code.

**Parameters:**
- `countryCode` (path): ISO 3166-1 alpha-3 country code (String)

**Response:** Same as GET /countries/{id}

#### GET /countries/region/{region}
Retrieve countries by region.

**Parameters:**
- `region` (path): Region name (String)

**Response:** Array of country objects

#### GET /countries/continent/{continent}
Retrieve countries by continent.

**Parameters:**
- `continent` (path): Continent name (String)

**Response:** Array of country objects

#### POST /countries
Create a new country.

**Request Body:**
```json
{
  "countryCode": "USA",
  "nameKo": "미국",
  "nameEn": "United States",
  "region": "North America",
  "continent": "North America"
}
```

**Validation Rules:**
- `countryCode`: Required, max 3 characters
- `nameKo`: Required, max 100 characters
- `nameEn`: Required, max 100 characters
- `region`: Optional, max 50 characters
- `continent`: Optional, max 30 characters

**Response:** `201 Created` with created country object

#### PATCH /countries/{id}/activate
Activate a country.

**Parameters:**
- `id` (path): Country ID (Long)

**Response:** `200 OK`

#### PATCH /countries/{id}/deactivate
Deactivate a country.

**Parameters:**
- `id` (path): Country ID (Long)

**Response:** `200 OK`

#### GET /countries/admin/all
Retrieve all countries (including inactive) - Admin endpoint.

**Response:** Array of all country objects

### 2. Export Statistics API

#### GET /export-statistics
Retrieve paginated export statistics.

**Parameters:**
- `page` (query): Page number (default: 0)
- `size` (query): Page size (default: 20)
- `sort` (query): Sort criteria

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "country": {
        "id": 1,
        "countryCode": "USA",
        "nameKo": "미국",
        "nameEn": "United States",
        "region": "North America",
        "continent": "North America",
        "active": true
      },
      "productCategory": {
        "id": 1,
        "hsCode": "8703",
        "hsLevel": 4,
        "nameKo": "승용자동차",
        "nameEn": "Motor cars",
        "parentHsCode": "87",
        "description": "승용자동차 및 기타 자동차",
        "active": true
      },
      "year": 2023,
      "month": 12,
      "exportValueUsd": 1500000.00,
      "currency": "USD",
      "exportWeightKg": 25000.500,
      "exportQuantity": 100.000,
      "quantityUnit": "대",
      "growthRateYoy": 15.25,
      "marketShare": 12.50
    }
  ],
  "pageable": {
    "sort": {
      "sorted": false,
      "unsorted": true,
      "empty": true
    },
    "pageNumber": 0,
    "pageSize": 20,
    "offset": 0,
    "unpaged": false,
    "paged": true
  },
  "totalElements": 100,
  "totalPages": 5,
  "last": false,
  "first": true,
  "numberOfElements": 20,
  "size": 20,
  "number": 0,
  "sort": {
    "sorted": false,
    "unsorted": true,
    "empty": true
  },
  "empty": false
}
```

#### GET /export-statistics/{id}
Retrieve a specific export statistic by ID.

**Parameters:**
- `id` (path): Export statistic ID (Long)

**Response:** Export statistic object

#### GET /export-statistics/year/{year}
Retrieve export statistics by year.

**Parameters:**
- `year` (path): Year (Integer)

**Response:** Array of export statistic objects

#### GET /export-statistics/country/{countryId}/year/{year}
Retrieve export statistics by country and year.

**Parameters:**
- `countryId` (path): Country ID (Long)
- `year` (path): Year (Integer)

**Response:** Array of export statistic objects

#### GET /export-statistics/dashboard/{year}
Retrieve dashboard summary for a specific year.

**Parameters:**
- `year` (path): Year (Integer)

**Response:**
```json
{
  "year": 2023,
  "totalExportValue": 15000000000.00,
  "currency": "USD",
  "yearOverYearGrowth": 8.5,
  "totalCountries": 150,
  "totalProducts": 500,
  "topCountries": [
    {
      "countryCode": "USA",
      "countryName": "United States",
      "totalValue": 2500000000.00,
      "marketShare": 16.67
    }
  ],
  "topProducts": [
    {
      "hsCode": "8703",
      "productName": "Motor cars",
      "totalValue": 3000000000.00,
      "marketShare": 20.00
    }
  ],
  "monthlyTrends": [
    {
      "year": 2023,
      "month": 1,
      "totalValue": 1200000000.00
    }
  ]
}
```

#### GET /export-statistics/years
Retrieve available years with export data.

**Response:**
```json
[2020, 2021, 2022, 2023]
```

#### POST /export-statistics
Create a new export statistic record.

**Request Body:**
```json
{
  "countryId": 1,
  "productCategoryId": 1,
  "year": 2023,
  "month": 12,
  "exportValueUsd": 1500000.00,
  "exportWeightKg": 25000.500,
  "exportQuantity": 100.000,
  "quantityUnit": "대"
}
```

**Validation Rules:**
- `countryId`: Required
- `productCategoryId`: Required
- `year`: Required, 1900-2100
- `month`: Required, 1-12
- `exportValueUsd`: Required, >= 0
- `exportWeightKg`: Optional, >= 0
- `exportQuantity`: Optional, >= 0
- `quantityUnit`: Optional, max 20 characters

**Response:** `201 Created` with created export statistic object

## Data Models

### Country
```json
{
  "id": "Long - Primary key",
  "countryCode": "String - ISO 3166-1 alpha-3 code (3 chars)",
  "nameKo": "String - Korean name (max 100 chars)",
  "nameEn": "String - English name (max 100 chars)",
  "region": "String - Geographic region (max 50 chars)",
  "continent": "String - Continent (max 30 chars)",
  "active": "Boolean - Status flag"
}
```

### ProductCategory
```json
{
  "id": "Long - Primary key",
  "hsCode": "String - HS code (max 10 chars)",
  "hsLevel": "Integer - HS classification level",
  "nameKo": "String - Korean name (max 200 chars)",
  "nameEn": "String - English name (max 200 chars)",
  "parentHsCode": "String - Parent HS code (max 10 chars)",
  "description": "String - Detailed description",
  "active": "Boolean - Status flag"
}
```

### ExportStatistic
```json
{
  "id": "Long - Primary key",
  "country": "Country - Foreign key reference",
  "productCategory": "ProductCategory - Foreign key reference",
  "year": "Integer - Export year",
  "month": "Integer - Export month (1-12)",
  "exportValueUsd": "BigDecimal - Export value in USD",
  "currency": "String - Currency code (default: USD)",
  "exportWeightKg": "BigDecimal - Export weight in kilograms",
  "exportQuantity": "BigDecimal - Export quantity",
  "quantityUnit": "String - Unit of quantity measurement",
  "growthRateYoy": "BigDecimal - Year-over-year growth rate (%)",
  "marketShare": "BigDecimal - Market share percentage (%)"
}
```

## Error Handling

### Standard Error Response Format
```json
{
  "timestamp": "2023-12-01T10:30:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/countries",
  "details": [
    {
      "field": "countryCode",
      "message": "국가 코드는 필수입니다."
    }
  ]
}
```

### HTTP Status Codes
- `200 OK`: Successful request
- `201 Created`: Resource successfully created
- `400 Bad Request`: Invalid request data or validation errors
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

### Common Validation Errors
- Missing required fields
- Invalid data formats
- Constraint violations (length, range)
- Foreign key constraints
- Business rule violations

## Caching Strategy

### Redis Caching Configuration
The API implements Redis-based caching with the following cache configurations:

#### Cache Categories and TTL
- **Dashboard Summary**: 30 minutes
- **Countries/Product Categories**: 1 hour
- **Regions/Continents**: 1 day
- **Export Statistics**: 15 minutes
- **Yearly Trends**: 15 minutes
- **Filtered Data**: 1 hour

#### Cache Keys
- `dashboard-summary::{year}`
- `countries::active`
- `countries-by-region::{region}`
- `countries-by-continent::{continent}`
- `product-categories::active`
- `export-statistics::{criteria}`

## Performance Considerations

### Database Optimization
- **Indexes**: Strategically placed on frequently queried columns
- **Connection Pooling**: Configured for optimal database connections
- **Query Optimization**: Efficient JPA queries with proper fetch strategies

### API Performance
- **Pagination**: Implemented for large datasets (default page size: 20)
- **Lazy Loading**: JPA entities use lazy loading to minimize data transfer
- **Caching**: Redis caching reduces database load for frequently accessed data

### Rate Limiting
⚠️ **Not Currently Implemented**: Consider implementing rate limiting for production use.

## Monitoring and Health Checks

### Actuator Endpoints
- `GET /api/actuator/health`: Application health status
- `GET /api/actuator/info`: Application information
- `GET /api/actuator/metrics`: Application metrics

### Health Check Response
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "7.0.0"
      }
    }
  }
}
```

## API Usage Examples

### Retrieve Dashboard Data
```bash
curl -X GET "http://localhost:8080/api/export-statistics/dashboard/2023" \
  -H "Content-Type: application/json"
```

### Create New Export Statistic
```bash
curl -X POST "http://localhost:8080/api/export-statistics" \
  -H "Content-Type: application/json" \
  -d '{
    "countryId": 1,
    "productCategoryId": 1,
    "year": 2023,
    "month": 12,
    "exportValueUsd": 1500000.00,
    "exportWeightKg": 25000.500,
    "exportQuantity": 100.000,
    "quantityUnit": "대"
  }'
```

### Get Countries by Region
```bash
curl -X GET "http://localhost:8080/api/countries/region/Asia" \
  -H "Content-Type: application/json"
```

## Development and Configuration

### Environment Profiles
- **Development**: `dev` profile with debug logging and database auto-update
- **Production**: `prod` profile with minimal logging and validation-only database mode

### Configuration Properties
```yaml
# Database Configuration
spring.datasource.url: jdbc:postgresql://localhost:5432/export_dashboard
spring.datasource.username: ${DB_USERNAME:export_user}
spring.datasource.password: ${DB_PASSWORD:export_password}

# Redis Configuration
spring.data.redis.host: ${REDIS_HOST:localhost}
spring.data.redis.port: ${REDIS_PORT:6379}

# Server Configuration
server.port: 8080
server.servlet.context-path: /api
```

## External Dependencies

The API does not currently integrate with external services. All data is managed internally within the PostgreSQL database. Future enhancements might include:

- **External Trade Data APIs**: Integration with government trade statistics APIs
- **Currency Exchange APIs**: Real-time exchange rate services
- **Notification Services**: Email/SMS notifications for threshold alerts
- **Analytics Services**: External business intelligence platforms

## Future Enhancements

### Planned Features
1. **Authentication & Authorization**: JWT-based security implementation
2. **Real-time Data**: WebSocket support for live data updates
3. **Advanced Analytics**: Machine learning-based trend prediction
4. **Data Import/Export**: Bulk data operations via CSV/Excel
5. **Audit Logging**: Comprehensive audit trail for all operations
6. **API Versioning**: Backward-compatible API versioning strategy

### Security Roadmap
1. **API Gateway**: Centralized security and routing
2. **OAuth2 Integration**: Enterprise authentication support
3. **Rate Limiting**: Request throttling and DDoS protection
4. **Input Validation**: Enhanced security validation
5. **HTTPS Enforcement**: SSL/TLS termination