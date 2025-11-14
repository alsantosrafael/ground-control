# Ground Control API - Postman Collection

Complete API collection for Ground Control Feature Flag Management System v1.

## 📦 What's Included

### Collection: `Ground-Control-API-v1.postman_collection.json`

This collection includes:

- **26 API endpoints** with example requests
- **15 example responses** with success and error scenarios
- **Automated tests** for validation
- **Pre-configured variables** for easy setup
- **Comprehensive documentation** for each endpoint

## 🚀 Quick Start

### 1. Import Collection

1. Open Postman
2. Click **Import**
3. Select `Ground-Control-API-v1.postman_collection.json`
4. Collection will appear in your workspace

### 2. Configure Variables

The collection includes pre-configured variables:

| Variable | Default Value | Description |
|----------|---------------|-------------|
| `baseUrl` | `http://localhost:8080` | API base URL (no `/v1` suffix) |
| `flagCode` | `sample-feature` | Sample flag code for testing |
| `userId` | `user123` | Sample user ID for evaluation |

**To modify:**
1. Click on the collection name
2. Go to **Variables** tab
3. Update **Current Value** column
4. Save

### 3. Start the Application

```bash
# Using Gradle
./gradlew bootRun

# Using Docker Compose
docker-compose up

# With specific profile
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 4. Run Your First Request

1. Expand **1. Feature Flags** folder
2. Select **Create Boolean Flag**
3. Click **Send**
4. You should receive a `201 Created` response

## 📚 Collection Structure

### 1. Feature Flags (`/v1/flags`)

**Create Flags:**
- Create Boolean Flag
- Create String Flag
- Create Integer Flag
- Create Percentage Flag

**Read Flags:**
- Get All Flags (Paginated)
- Get Flag by Code
- Get Flags by Codes (Bulk)

**Update Flags:**
- Update Flag
- Change Flag State (Enable/Disable)

**Examples:**
```json
// Create Boolean Flag
POST /v1/flags
{
  "code": "new-checkout",
  "name": "New Checkout Flow",
  "value": true,
  "valueType": "BOOLEAN",
  "enabled": true
}

// Response: 201 Created
{
  "id": 1,
  "code": "new-checkout",
  "enabled": true,
  ...
}
```

### 2. Flag Evaluation (`/v1/evaluations`)

**Evaluate Flags:**
- Global Evaluation (no user context)
- User-Specific Evaluation
- Evaluation with Attributes
- Bulk Flag Evaluation (1-100 flags)

**Examples:**
```json
// Evaluate for user
POST /v1/evaluations/new-checkout
{
  "subjectId": "user123",
  "attributes": {
    "plan": "premium",
    "country": "US"
  }
}

// Response: 200 OK
{
  "enabled": true,
  "value": true,
  "reason": "RULE_MATCH",
  "variant": "premium-users"
}
```

**Bulk Evaluation:**
```json
// Evaluate multiple flags
POST /v1/evaluations/bulk
{
  "flagCodes": ["flag1", "flag2", "flag3"],
  "context": { "subjectId": "user123" }
}

// Response with errors map
{
  "results": {
    "flag1": { /* evaluation */ },
    "flag2": { /* evaluation */ }
  },
  "errors": {
    "flag3": "Flag not found"
  },
  "summary": {
    "requested": 3,
    "successful": 2,
    "failed": 1
  }
}
```

### 3. Health & Monitoring (`/actuator`)

- Health Check
- Application Info
- Prometheus Metrics
- Application Mappings

### 4. API Documentation

- OpenAPI Spec (JSON)
- Swagger UI Access

## 🧪 Example Responses

### Success Responses

The collection includes mock responses for:

- **201 Created** - Flag created successfully
- **200 OK** - Flag retrieved/evaluated
- **204 No Content** - Flag updated/state changed

### Error Responses

Examples of error scenarios:

- **400 Bad Request** - Validation errors
  ```json
  {
    "timestamp": "2025-01-14T10:00:00Z",
    "status": 400,
    "error": "Bad Request",
    "message": "Validation failed: code: Code must be between 2 and 50 characters"
  }
  ```

- **404 Not Found** - Flag doesn't exist
  ```json
  {
    "timestamp": "2025-01-14T10:00:00Z",
    "status": 404,
    "error": "Not Found",
    "message": "Resource not found"
  }
  ```

## ✅ Automated Tests

The collection includes automated tests:

```javascript
// Example test in "Create Boolean Flag"
pm.test("Status code is 201", function () {
    pm.response.to.have.status(201);
});

pm.test("Response has flag code", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.code).to.eql(pm.variables.get("flagCode"));
});
```

**To run all tests:**
1. Click on collection
2. Click **Run** button
3. Select all requests
4. Click **Run Ground Control API v1**

## 🎯 Common Workflows

### Workflow 1: Create and Evaluate a Flag

1. **Create Boolean Flag** → `201 Created`
2. **Get Flag by Code** → `200 OK`
3. **Evaluate Flag - User Specific** → `200 OK`

### Workflow 2: Bulk Operations

1. **Create Multiple Flags** (Boolean, String, Int)
2. **Get Flags by Codes (Bulk)** → Get all at once
3. **Bulk Flag Evaluation** → Evaluate all at once

### Workflow 3: Feature Rollout

1. **Create Boolean Flag** (disabled)
2. **Create Percentage Rollout Rule** (25%)
3. **Evaluate Flag** for different users
4. **Increase Percentage** to 50%, then 100%
5. **Change Flag State** to fully enabled

## 🔧 Environment Setup

### Local Development

```javascript
// Environment Variables
{
  "baseUrl": "http://localhost:8080",
  "flagCode": "sample-feature",
  "userId": "user123"
}
```

### Production Environment

```javascript
{
  "baseUrl": "https://api.yourcompany.com",
  "flagCode": "production-feature",
  "userId": "prod-user-001"
}
```

## 📖 Additional Resources

- **API Documentation:** http://localhost:8080/docs.html
- **OpenAPI Spec:** http://localhost:8080/v3/api-docs
- **Health Check:** http://localhost:8080/actuator/health
- **Prometheus Metrics:** http://localhost:8080/actuator/prometheus

## 🆕 API Changes in v1.0.1

- ✅ All endpoints now use `/v1/` prefix for versioning
- ✅ Enhanced input validation with detailed error messages
- ✅ Improved error responses with structured format
- ✅ Bulk evaluation returns errors map and summary
- ✅ Security improvements: ReDoS protection, env vars
- ✅ Constants extracted for better maintainability

## 🤝 Contributing

To add new endpoints to this collection:

1. Test the endpoint manually
2. Add request to appropriate folder
3. Include example request body
4. Add success and error response examples
5. Write automated tests
6. Update this README

## 📝 Notes

- **API Versioning:** All endpoints use `/v1/` prefix
- **Validation:** Requests are validated at API boundary
- **Error Format:** Consistent error response structure
- **Pagination:** Default 20 items, sort by `updatedAt` desc
- **Bulk Limits:** Max 100 flags per bulk evaluation

## 🐛 Troubleshooting

### Issue: "Connection refused"
**Solution:** Make sure the application is running on `http://localhost:8080`

### Issue: "404 Not Found" on all requests
**Solution:** Check that you're using the `/v1/` prefix in URLs

### Issue: "Validation failed" errors
**Solution:** Check request body against validation rules:
- Flag code: 2-50 chars, alphanumeric + hyphens/underscores
- Name: 3-100 chars
- SubjectId: max 100 chars
- Attributes: max 50 entries

### Issue: Tests failing
**Solution:** Update collection variables to match your environment

---

**Version:** 1.0.1
**Last Updated:** 2025-01-14
**Maintained By:** Ground Control Team
