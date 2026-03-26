# Integration Patterns

Patterns for integrating external services in Ground Control using Java, Spring Boot, and Spring Modulith.

---

## Outbound Client Encapsulation

**Core principle**: Outbound clients own all protocol details. Services only orchestrate business operations.

**Client owns:**
- URLs and endpoint paths
- Authentication headers and tokens
- Request/response mapping
- Timeout, retry, and error translation

**Service knows:**
- Business operations only
- No HTTP path/auth/header details

### Pattern 1: In-memory/Fake Client (local development and tests)

```java
@Component
@Profile({"local", "test"})
public class FakePaymentGatewayClient implements PaymentGatewayClient {

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        return PaymentResult.success("PAY-" + System.currentTimeMillis());
    }
}
```

### Pattern 2: HTTP Client Adapter (production REST APIs)

```java
@Component
@Profile("!test")
public class HttpRatingClient implements RatingClient {

    private final RestClient restClient;
    private final RatingProperties properties;

    public HttpRatingClient(RestClient.Builder builder, RatingProperties properties) {
        this.properties = properties;
        this.restClient = builder
            .baseUrl(properties.baseUrl())
            .defaultHeader("Authorization", "Bearer " + properties.token())
            .build();
    }

    @Override
    public Optional<BigDecimal> getRating(String title) {
        try {
            var response = restClient.get()
                .uri("/rating/{title}", title)
                .retrieve()
                .body(RatingResponse.class);
            return Optional.ofNullable(response).map(RatingResponse::rating);
        } catch (RestClientException ex) {
            throw new ExternalDependencyException("Rating API request failed", ex);
        }
    }
}
```

### Pattern 3: JDK HttpClient Adapter (when low-level HTTP control is needed)

```java
@Component
public class HttpClientInvoiceGateway implements InvoiceGatewayClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(3))
        .build();

    @Override
    public InvoiceGatewayResponse createInvoice(InvoiceGatewayRequest request) {
        // Keep low-level HTTP details encapsulated in the adapter.
        return InvoiceGatewayResponse.accepted("INV-123");
    }
}
```

### Service usage

```java
@Service
public class PaymentService {

    private final PaymentGatewayClient paymentGatewayClient;
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentGatewayClient paymentGatewayClient, PaymentRepository paymentRepository) {
        this.paymentGatewayClient = paymentGatewayClient;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Payment pay(Invoice invoice) {
        var result = paymentGatewayClient.processPayment(new PaymentRequest(invoice.id(), invoice.totalAmount()));
        return paymentRepository.save(Payment.from(invoice, result.transactionId()));
    }
}
```

---

## Dependency Injection Patterns

### Default: direct injection

Use when there is one concrete provider.

```java
@Service
public class TaxService {

    private final EasyTaxClient easyTaxClient;

    public TaxService(EasyTaxClient easyTaxClient) {
        this.easyTaxClient = easyTaxClient;
    }
}
```

### Interface-based injection

Use when provider replacement is required (vendor swap, feature flags, environment switch).

```java
public interface VideoSummaryAdapter {
    String generateSummary(String videoUrl);
}

@Component
@Primary
class GeminiVideoSummaryAdapter implements VideoSummaryAdapter {
    @Override
    public String generateSummary(String videoUrl) {
        return "summary";
    }
}

@Component
@ConditionalOnProperty(name = "video.provider", havingValue = "openai")
class OpenAiVideoSummaryAdapter implements VideoSummaryAdapter {
    @Override
    public String generateSummary(String videoUrl) {
        return "summary";
    }
}
```

---

## Spring Modulith Integration Rules

Use explicit module APIs and events for inter-module communication.

**Rules:**
- Do not access another module's repositories/entities directly
- Expose module capabilities through published interfaces
- Publish domain events for async communication
- Keep module boundaries aligned with business capabilities

```java
@NamedInterface("billing-api")
package com.product.ground_control.billing.api;
```

```java
public record SubscriptionActivatedEvent(UUID subscriptionId, String userId) {}

@Service
public class BillingService {

    private final ApplicationEventPublisher events;

    public BillingService(ApplicationEventPublisher events) {
        this.events = events;
    }

    @Transactional
    public void activateSubscription(UUID subscriptionId, String userId) {
        events.publishEvent(new SubscriptionActivatedEvent(subscriptionId, userId));
    }
}

@Component
class NotificationListener {

    @ApplicationModuleListener
    void on(SubscriptionActivatedEvent event) {
        // Notify user without coupling to billing internals.
    }
}
```

---

## Structured Logging and Correlation

**Rules:**
- Log structured key-value context
- Include correlation/request id when available
- Use consistent operation names across modules
- Never log secrets or sensitive PII

```java
@Service
public class ContentPublishingService {

    private static final Logger log = LoggerFactory.getLogger(ContentPublishingService.class);

    public void publish(String contentId, String actor) {
        log.info("content_publish_start contentId={} actor={} module=content", contentId, actor);
        try {
            // business operation
            log.info("content_publish_success contentId={} module=content", contentId);
        } catch (Exception ex) {
            log.error("content_publish_failure contentId={} module=content", contentId, ex);
            throw ex;
        }
    }
}
```

---

## Metrics and Health Checks

### Metrics (Micrometer)

```java
@Service
public class SubscriptionService {

    private final Counter createdCounter;
    private final Timer createTimer;

    public SubscriptionService(MeterRegistry registry) {
        this.createdCounter = Counter.builder("billing.subscription.creates")
            .description("Total subscriptions created")
            .register(registry);
        this.createTimer = Timer.builder("billing.subscription.create.duration")
            .description("Subscription create duration")
            .register(registry);
    }

    public void createSubscription() {
        createTimer.record(() -> {
            // create subscription
            createdCounter.increment();
        });
    }
}
```

### Health indicator

```java
@Component
public class BillingHealthIndicator implements HealthIndicator {

    private final Supplier<Boolean> dbProbe;
    private final Supplier<Boolean> outboundProbe;

    public BillingHealthIndicator(Supplier<Boolean> dbProbe, Supplier<Boolean> outboundProbe) {
        this.dbProbe = dbProbe;
        this.outboundProbe = outboundProbe;
    }

    @Override
    public Health health() {
        Map<String, Boolean> checks = Map.of(
            "database", dbProbe.get(),
            "outbound", outboundProbe.get()
        );

        boolean allUp = checks.values().stream().allMatch(Boolean::booleanValue);

        if (allUp) {
            return Health.up().withDetail("module", "billing").build();
        }
        return Health.down()
            .withDetail("module", "billing")
            .withDetail("database", checks.get("database") ? "ok" : "failed")
            .withDetail("outbound", checks.get("outbound") ? "ok" : "degraded")
            .build();
    }
}
```

---

## Resilience: Timeouts, Retries, Circuit Breakers

Prefer Spring Retry + Resilience4j for external calls.

```java
@Component
public class ExternalMovieRatingClient {

    @Retryable(
        retryFor = ExternalDependencyException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 300, multiplier = 2.0)
    )
    @CircuitBreaker(name = "movieRating", fallbackMethod = "fallbackRating")
    public RatingResponse getMovieRating(String title) {
        // timeout should be configured on underlying HTTP client
        throw new ExternalDependencyException("Temporary failure");
    }

    public RatingResponse fallbackRating(String title, Throwable throwable) {
        return new RatingResponse(null, "fallback", "rating temporarily unavailable");
    }
}
```

**Per-call checklist:**
- Timeout configured
- Retry with exponential backoff
- Circuit breaker configured
- Fallback/degraded response defined
- Exceptions translated to domain-safe errors

---

## Event Patterns

Prefer durable brokers for production async workflows.

| Option | Best For | Notes |
| --- | --- | --- |
| Kafka | High throughput, replay | Use `spring-kafka` with explicit topic contracts |
| SQS | AWS-native queueing | Use Spring Cloud AWS integration |
| In-memory | Local development only | Never for production delivery guarantees |

Define explicit payload contracts.

```java
public record VideoProcessingRequested(
    UUID videoId,
    String url,
    String contentId,
    Instant requestedAt
) {}
```

```java
@Component
class VideoProcessingPublisher {

    private final ApplicationEventPublisher events;

    VideoProcessingPublisher(ApplicationEventPublisher events) {
        this.events = events;
    }

    void publish(UUID videoId, String url, String contentId) {
        events.publishEvent(new VideoProcessingRequested(videoId, url, contentId, Instant.now()));
    }
}
```

---

## Security Rules for Integrations

### Credentials
- Never hardcode API keys
- Load credentials via `@ConfigurationProperties` or environment variables
- Rotate secrets and keep access scoped

### Logging
- Never log tokens, secrets, card data, or raw PII
- Redact identifiers where required by policy

### Webhooks
- Validate provider signatures before processing payloads
- Reject unsigned or invalid requests with `400`
- Apply rate limiting and idempotency handling

```java
@PostMapping("/webhooks/stripe")
public ResponseEntity<Void> handleStripeWebhook(
    @RequestHeader("Stripe-Signature") String signature,
    @RequestBody String rawBody
) {
    webhookVerifier.verify(signature, rawBody);
    return ResponseEntity.ok().build();
}
```

**Security checklist per integration:**
- [ ] Credentials come from secure configuration
- [ ] HTTP client TLS verification enabled
- [ ] Sensitive fields excluded/redacted in logs
- [ ] Webhook signatures verified
- [ ] Idempotency strategy defined for retries/replays
- [ ] Cross-module access uses APIs/events, never direct DB

