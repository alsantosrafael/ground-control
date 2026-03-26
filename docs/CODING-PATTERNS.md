# Coding Patterns

Implementation reference for repositories, controllers, services, entities, transactions, and tests in Ground Control (Java + Spring).

---

## Core Principles

- Keep module boundaries explicit and enforceable.
- Prefer clear, intention-revealing code over clever abstractions.
- Keep infrastructure concerns out of domain logic.
- Make failure modes explicit (validation, exceptions, retries, timeouts).
- Optimize for maintainability and observability first.

---

## Import and Package Hygiene

Use explicit imports and stable package structure to keep diffs readable and code review fast.

**Rules:**
- Do not use wildcard imports (`import java.util.*;`, `import org.mockito.*;`)
- Use explicit imports for every type/static helper used
- Remove unused imports
- Keep package names lowercase and domain-oriented (`com.product.ground_control.billing`)
- Group imports in IDE default order and avoid manual reordering noise
- Prefer deterministic formatting to reduce noise in pull requests

```java
// BAD
import java.util.*;
import static org.mockito.Mockito.*;

// GOOD
import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
```

---

## Lean Controller Pattern

Controllers handle HTTP concerns only. Business logic belongs in services.

**Rules:**
- Use `@RestController` + request mapping annotations
- Keep controller methods small and focused
- Validate input with Bean Validation (`@Valid`, `@NotNull`, `@Size`, etc.)
- Map request/response DTOs at the edge
- Do not inject repositories into controllers
- Do not perform business decisions or persistence logic in controllers

```java
@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public List<InvoiceResponse> getUserInvoices(@RequestHeader("X-User-Id") String userId) {
        return invoiceService.getUserInvoices(userId);
    }
}
```

---

## Service Layer Rules

Services orchestrate business workflows and transaction boundaries.

**Rules:**
- Use `@Service` for business components
- Inject collaborators through constructor injection
- Keep methods intention-revealing (business language)
- Keep persistence details encapsulated in repositories
- Prefer domain exceptions over generic runtime exceptions
- Avoid static mutable state

```java
@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional
    public SubscriptionResponse activate(String userId, String planId) {
        var subscription = subscriptionRepository.createPending(userId, planId);
        subscription.activate();
        return SubscriptionResponse.from(subscriptionRepository.save(subscription));
    }
}
```

---

## Repository Pattern and Persistence Encapsulation

Repositories encapsulate JPA-specific querying and mapping concerns.

**Rules:**
- Use `@Repository` and Spring Data interfaces for aggregate access
- Name query methods by business meaning, not ORM mechanics
- Keep `EntityManager`, JPQL, Criteria, and joins out of service classes
- Use custom repository implementations only when query complexity requires it
- Return types should match business intent (`Optional<T>`, paged results, projections)

```java
@Repository
public interface SubscriptionRepository extends JpaRepository<BillingSubscriptionEntity, UUID> {

    Optional<BillingSubscriptionEntity> findByIdAndUserIdAndStatus(
        UUID id,
        String userId,
        SubscriptionStatus status
    );

    List<BillingSubscriptionEntity> findByUserIdOrderByCreatedAtDesc(String userId);
}
```

---

## ORM Leakage Prevention

Do not leak JPA predicates and query mechanics outside repository boundaries.

**Rules:**
- Service code should not construct JPA criteria or query fragments
- Prefer repository methods that encode domain intent
- Keep entity graph/fetch strategies owned by repository methods

```java
public class SubscriptionLookupService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionLookupService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public void lookup(UUID id, String userId) {
        // BAD (service coupled to persistence mechanics)
        subscriptionRepository.findByIdAndUserIdAndStatus(id, userId, SubscriptionStatus.ACTIVE);

        // BETTER (domain intent is clear)
        subscriptionRepository.findActiveByIdForUser(id, userId);
    }
}
```

---

## Transaction Management

Use transactions intentionally and at service boundaries.

**Rules:**
- Annotate write workflows with `@Transactional`
- Prefer `@Transactional(readOnly = true)` for read-only flows
- Keep transaction boundary at orchestration entrypoint (usually service public method)
- Avoid nested transactional behavior unless propagation is explicitly required
- Avoid long-running remote calls inside open transactions

```java
@Service
public class BillingPlanService {

    @Transactional(readOnly = true)
    public BillingPlanResponse getById(UUID id) {
        // read-only path
        return null;
    }

    @Transactional
    public BillingPlanResponse changePrice(UUID id, Money newPrice) {
        // write path, atomic updates
        return null;
    }
}
```

---

## Entity Naming and Module Isolation

Entities and tables must reflect module ownership to avoid collisions and accidental coupling.

**Rules:**
- Prefix table names by module (`billing_subscription`, `identity_user`)
- Keep each module as owner of its persistence schema/tables
- Do not share entities across module boundaries
- Use identifiers/events/APIs for cross-module references (not direct foreign coupling to other module internals)
- Keep JPA entities persistence-focused; avoid exposing them directly in APIs

```java
@Entity
@Table(name = "billing_subscription")
public class BillingSubscriptionEntity {
    // ...fields
}
```

---

## Cross-Module Communication

Modules communicate through explicit interfaces, events, or HTTP clients.

**Rules:**
- Prefer published module APIs/facades for internal module communication
- Use domain events for asynchronous cross-module updates
- Use HTTP/gRPC clients only for external services or explicit service boundaries
- Do not read/write another module's tables directly

---

## DTOs and Mapping

Keep transport models separate from persistence models.

**Rules:**
- Use request/response DTOs in controllers
- Keep JPA entities out of API contracts
- Centralize mapping (manual mapper or dedicated mapper class)
- Validate requests at the boundary, enforce invariants in domain/service layer

---

## Testing Standards (JUnit 5 + Mockito)

Tests should be focused, deterministic, and behavior-oriented.

### Unit Test Rules

- Name tests by behavior (`shouldActivateSubscriptionWhenPlanIsValid`)
- Mock only collaborators used by the scenario
- Stub only methods actually exercised in that test
- Avoid generic stubbing with broad matchers unless truly needed (`any()` everywhere hides broken behavior)
- Verify only required interactions (avoid over-verification)
- Use `verifyNoMoreInteractions(...)` selectively for critical orchestration tests
- Prefer strict stubbing defaults; avoid `lenient()` unless there is a clear reason
- Prefer real value objects over mocks
- Keep one primary assertion intent per test

```java
@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Test
    void shouldActivateSubscriptionWhenPlanIsValid() {
        var entity = new BillingSubscriptionEntity();
        when(subscriptionRepository.createPending("user-1", "plan-basic")).thenReturn(entity);
        when(subscriptionRepository.save(entity)).thenReturn(entity);

        subscriptionService.activate("user-1", "plan-basic");

        verify(subscriptionRepository).createPending("user-1", "plan-basic");
        verify(subscriptionRepository).save(entity);
        verifyNoMoreInteractions(subscriptionRepository);
    }
}
```

```java
class SubscriptionServiceStubbingExample {

    void setup(SubscriptionRepository subscriptionRepository, BillingSubscriptionEntity entity) {
        // BAD: over-broad stubbing masks incorrect inputs
        when(subscriptionRepository.createPending(anyString(), anyString())).thenReturn(entity);

        // GOOD: precise stubbing validates real behavior
        when(subscriptionRepository.createPending("user-1", "plan-basic")).thenReturn(entity);
    }
}
```

### Integration Test Rules

- Use `@SpringBootTest` only when full context is needed
- Prefer slices when possible (`@WebMvcTest`, `@DataJpaTest`)
- Use Testcontainers for real database integration tests
- Keep test data local to test setup, avoid hidden global state

---

## API and Error Handling

Error responses must be consistent and actionable.

**Rules:**
- Use `@ControllerAdvice` for centralized exception mapping
- Return structured error payloads (code, message, trace/correlation id)
- Avoid leaking internal exception details to clients
- Include correlation ids in logs and response headers when applicable

---

## Static Analysis and Formatting

Keep style and quality checks automated so patterns are enforced, not just documented.

**Rules:**
- Use IDE settings that avoid wildcard imports automatically
- Run formatting and static analysis in CI (for example: Checkstyle/SpotBugs/PMD)
- Fail PR checks on style violations for consistency
- Keep warning suppressions scoped and justified

---

## Naming Conventions

| Category | Convention | Examples |
| --- | --- | --- |
| Packages | lowercase dotted by domain | `com.product.ground_control.billing.service` |
| Classes | PascalCase | `BillingSubscriptionService` |
| Interfaces | PascalCase noun/role | `BillingSubscriptionGateway` |
| Methods | camelCase verb-first | `activateSubscription` |
| Constants | UPPER_SNAKE_CASE | `MAX_RETRY_ATTEMPTS` |
| Endpoints | kebab-case resources | `/api/v1/billing-subscriptions` |
| Tables | snake_case with module prefix | `billing_subscription` |

---

## Common Anti-Patterns

| Anti-Pattern | Fix |
| --- | --- |
| Wildcard imports (`*`) | Use explicit imports only |
| Controller injecting repository | Controller calls service only |
| Business logic in controller | Move business rules to service |
| Service building JPQL/criteria inline | Encapsulate query in repository |
| Returning entities directly from controller | Return response DTOs |
| Broad, unnecessary mocking | Mock only used collaborators |
| Unused stubs in Mockito tests | Remove unnecessary stubbing |
| Over-broad stubbing with `any()` | Use precise argument stubbing for expected behavior |
| Verifying every mock call by default | Verify only behavior that matters |
| Long transaction including remote call | Split workflow and narrow transactional scope |
| Cross-module table access | Use module API/event instead |

---

## Quick Review Checklist

Before opening a PR, validate:

- No wildcard imports
- Controllers remain HTTP-only
- Services hold business orchestration
- Repository APIs are business-meaningful
- Transaction boundaries are explicit
- DTO/entity separation is preserved
- Unit tests mock only what is used
- Unit tests use precise stubbing (avoid blanket `any()`)
- Integration tests cover critical paths
