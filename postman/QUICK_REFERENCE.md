# Ground Control API - Quick Reference

## 🚀 All Endpoints at a Glance

### Feature Flags

```http
# Create Flag
POST /v1/flags
Body: { "code": "flag-name", "name": "...", "value": true, "valueType": "BOOLEAN", "enabled": true }

# Get All Flags (Paginated)
GET /v1/flags?page=0&size=20&sort=updatedAt,desc

# Get Flag by Code
GET /v1/flags/{code}

# Get Multiple Flags
GET /v1/flags/by-codes?codes=flag1&codes=flag2

# Update Flag
PUT /v1/flags/{code}
Body: { "name": "...", "description": "...", "enabled": false }

# Enable/Disable Flag
PATCH /v1/flags/{code}/change-state
Body: { "enabled": true }
```

### Flag Evaluation

```http
# Evaluate Single Flag - Global
POST /v1/evaluations/{code}
Body: { "subjectId": null, "attributes": {} }

# Evaluate Single Flag - User Specific
POST /v1/evaluations/{code}
Body: { "subjectId": "user123", "attributes": { "plan": "premium" } }

# Bulk Evaluation
POST /v1/evaluations/bulk
Body: { "flagCodes": ["flag1", "flag2"], "context": { "subjectId": "user123" } }
```

### Health & Monitoring

```http
# Health Check
GET /actuator/health

# Application Info
GET /actuator/info

# Prometheus Metrics
GET /actuator/prometheus

# API Mappings
GET /actuator/mappings
```

### Documentation

```http
# OpenAPI Spec
GET /v3/api-docs

# Swagger UI
GET /docs.html
```

## 📝 Request Body Templates

### Create Boolean Flag
```json
{
  "code": "new-feature",
  "name": "New Feature",
  "description": "Optional description",
  "value": false,
  "valueType": "BOOLEAN",
  "enabled": true,
  "dueAt": null
}
```

### Create String Flag
```json
{
  "code": "api-endpoint",
  "name": "API Endpoint URL",
  "value": "https://api.example.com",
  "valueType": "STRING",
  "enabled": true
}
```

### Create Integer Flag
```json
{
  "code": "max-retries",
  "name": "Maximum Retry Attempts",
  "value": 3,
  "valueType": "INT",
  "enabled": true
}
```

### Create Percentage Flag
```json
{
  "code": "discount-rate",
  "name": "Discount Percentage",
  "value": 15.5,
  "valueType": "PERCENTAGE",
  "enabled": true
}
```

### Evaluation Context - Simple
```json
{
  "subjectId": "user123",
  "attributes": {}
}
```

### Evaluation Context - Rich Attributes
```json
{
  "subjectId": "user123",
  "attributes": {
    "plan": "premium",
    "country": "US",
    "creditScore": 750,
    "betaTester": true,
    "organizationSize": 500,
    "email": "user@example.com",
    "accountAge": 365
  }
}
```

### Bulk Evaluation Request
```json
{
  "flagCodes": [
    "feature-1",
    "feature-2",
    "feature-3"
  ],
  "context": {
    "subjectId": "user123",
    "attributes": {
      "plan": "premium"
    }
  }
}
```

### Update Flag
```json
{
  "name": "Updated Name",
  "description": "Updated description",
  "enabled": false,
  "dueAt": "2025-12-31T23:59:59Z"
}
```

### Change Flag State
```json
{
  "enabled": true
}
```

## 📦 Response Examples

### Flag Object
```json
{
  "id": 1,
  "code": "new-feature",
  "name": "New Feature",
  "description": "Feature description",
  "value": false,
  "valueType": "BOOLEAN",
  "enabled": true,
  "dueAt": null,
  "rolloutRules": [],
  "createdAt": "2025-01-14T10:00:00Z",
  "updatedAt": "2025-01-14T10:00:00Z"
}
```

### Evaluation Result
```json
{
  "enabled": true,
  "value": true,
  "valueType": "BOOLEAN",
  "variant": "premium-users",
  "evaluationType": "DEFAULT",
  "reason": "RULE_MATCH"
}
```

**Reason Values:**
- `DEFAULT` - No rules matched, using default value
- `RULE_MATCH` - Matched a rollout rule
- `FLAG_DISABLED` - Flag is disabled
- `FLAG_EXPIRED` - Flag has expired

### Bulk Evaluation Response
```json
{
  "results": {
    "feature-1": {
      "enabled": true,
      "value": true,
      "reason": "DEFAULT"
    },
    "feature-2": {
      "enabled": true,
      "value": "blue",
      "reason": "RULE_MATCH"
    }
  },
  "errors": {
    "feature-3": "Flag not found"
  },
  "summary": {
    "requested": 3,
    "successful": 2,
    "failed": 1
  }
}
```

### Paginated Response
```json
{
  "content": [ /* array of flags */ ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": { "sorted": true }
  },
  "totalElements": 45,
  "totalPages": 3,
  "last": false,
  "first": true,
  "numberOfElements": 20
}
```

### Error Response
```json
{
  "timestamp": "2025-01-14T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: code: Code is required"
}
```

## 🔍 HTTP Status Codes

| Code | Meaning | When It Happens |
|------|---------|-----------------|
| 200 | OK | Successful GET request |
| 201 | Created | Successful POST (create) |
| 204 | No Content | Successful PUT/PATCH (update) |
| 400 | Bad Request | Validation failed |
| 404 | Not Found | Flag doesn't exist |
| 409 | Conflict | Illegal state (e.g., duplicate code) |
| 500 | Server Error | Unexpected error |

## ✅ Validation Rules

### Flag Code
- **Length:** 2-50 characters
- **Pattern:** `^[a-zA-Z0-9_-]+$`
- **Valid:** `new-feature`, `theme_color`, `feature-123`
- **Invalid:** `new feature` (space), `new@feature` (special char)

### Flag Name
- **Length:** 3-100 characters
- **Required:** Yes

### Description
- **Length:** Max 500 characters
- **Required:** No

### Subject ID
- **Length:** Max 100 characters
- **Required:** No (use null for global evaluation)

### Attributes
- **Max Entries:** 50
- **Types:** string, number, boolean, array, date

### Bulk Evaluation
- **Min Flags:** 1
- **Max Flags:** 100

## 🎯 Common Use Cases

### 1. Kill Switch (Emergency Disable)
```bash
# Disable feature immediately
PATCH /v1/flags/problematic-feature/change-state
{ "enabled": false }
```

### 2. A/B Testing
```bash
# Evaluate for user with context
POST /v1/evaluations/new-ui
{
  "subjectId": "user123",
  "attributes": { "variant": "B" }
}
```

### 3. Premium Feature
```bash
# Create flag for premium users
POST /v1/flags
{
  "code": "advanced-analytics",
  "name": "Advanced Analytics",
  "value": false,
  "valueType": "BOOLEAN",
  "enabled": true
}

# Evaluate with plan attribute
POST /v1/evaluations/advanced-analytics
{
  "subjectId": "user123",
  "attributes": { "plan": "premium" }
}
```

### 4. Geographic Rollout
```bash
# Evaluate with country attribute
POST /v1/evaluations/new-feature
{
  "subjectId": "user123",
  "attributes": { "country": "US" }
}
```

### 5. Dashboard Initialization
```bash
# Fetch all flags at once
POST /v1/evaluations/bulk
{
  "flagCodes": ["feature-1", "feature-2", "feature-3", "theme", "config"],
  "context": { "subjectId": "user123" }
}
```

## 🧪 Testing Commands (cURL)

```bash
# Create flag
curl -X POST http://localhost:8080/v1/flags \
  -H "Content-Type: application/json" \
  -d '{"code":"test-flag","name":"Test","value":true,"valueType":"BOOLEAN","enabled":true}'

# Get flag
curl http://localhost:8080/v1/flags/test-flag

# Evaluate flag
curl -X POST http://localhost:8080/v1/evaluations/test-flag \
  -H "Content-Type: application/json" \
  -d '{"subjectId":"user123","attributes":{}}'

# Health check
curl http://localhost:8080/actuator/health
```

## 🔗 Quick Links

- **Swagger UI:** http://localhost:8080/docs.html
- **Health Check:** http://localhost:8080/actuator/health
- **Metrics:** http://localhost:8080/actuator/prometheus
- **OpenAPI Spec:** http://localhost:8080/v3/api-docs

## 💡 Pro Tips

1. **Use bulk evaluation** for dashboard initialization (single request vs multiple)
2. **Enable pagination** for large flag lists (default: 20 items per page)
3. **Use subjectId** for deterministic percentage rollouts (same user = same result)
4. **Set dueAt** for time-based flags (auto-disable after date)
5. **Use variant names** in rules to track which rule matched
6. **Monitor cache hit rates** via Prometheus metrics
7. **Use health checks** in load balancers and orchestrators

---

**Version:** 1.0.1
**API Base:** `/v1`
**Need Help?** Check the full documentation at http://localhost:8080/docs.html
