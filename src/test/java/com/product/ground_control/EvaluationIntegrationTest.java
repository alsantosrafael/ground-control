package com.product.ground_control;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.product.ground_control.toggles.domain.dto.EvalRequest;
import com.product.ground_control.toggles.domain.dto.EvalResponse;
import com.product.ground_control.toggles.api.grpc.EvaluationServiceGrpc;
import com.product.ground_control.toggles.domain.FeatureType;
import com.product.ground_control.toggles.application.configuration.CacheConfig;
import com.product.ground_control.toggles.domain.entity.Toggle;
import com.product.ground_control.analytics.infrastructure.repository.EventRepository;
import com.product.ground_control.toggles.infrastructure.repository.ToggleRepository;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.devh.boot.grpc.server.config.GrpcServerProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.concurrent.TimeUnit;
import static org.awaitility.Awaitility.await;

/**
 * End-to-end integration test covering REST + gRPC evaluation and async analytics ingestion.
 * Lives at the root package level to legitimately access internals from both the toggles
 * and analytics modules — this is an intentional cross-cutting test boundary.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "grpc.server.port=0",
    "spring.cache.type=caffeine",
    "spring.flyway.enabled=false"
})
class EvaluationIntegrationTest {

    @LocalServerPort
    private int port;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private GrpcServerProperties grpcServerProperties;

    @Autowired
    private EventRepository eventRepository;

    @MockitoBean
    private ToggleRepository toggleRepository;

    private ManagedChannel channel;
    private EvaluationServiceGrpc.EvaluationServiceBlockingStub stub;

    @BeforeEach
    void setUp() {
        cacheManager.getCache(CacheConfig.FEATURE_RULES_CACHE).clear();
        eventRepository.deleteAll();

        int port = grpcServerProperties.getPort();
        channel = ManagedChannelBuilder.forAddress("localhost", port)
                .usePlaintext()
                .build();
        stub = EvaluationServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDown() {
        if (channel != null) {
            channel.shutdown();
        }
    }

    @Test
    void shouldEvaluateViaRestAndGrpcWithCaching() throws Exception {
        String key = "multi_protocol_feature";
        Toggle toggle = new Toggle(
            UUID.randomUUID(),
            key,
            FeatureType.BOOLEAN,
            List.of(),
            "true",
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(toggleRepository.findByKey(key)).thenReturn(Optional.of(toggle));

        // 1. Call REST
        EvalRequest restRequest = new EvalRequest(key, Map.of());
        ResponseEntity<EvalResponse> restResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/rules/eval", restRequest, EvalResponse.class);

        assertThat(restResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(restResponse.getBody().result()).isEqualTo("true");
        assertThat(restResponse.getBody().type()).isEqualTo("BOOLEAN");
        assertThat(restResponse.getBody().featureKey()).isEqualTo(key);

        verify(toggleRepository, times(1)).findByKey(key);

        // 2. Call gRPC (should hit cache)
        com.product.ground_control.toggles.api.grpc.EvalRequest grpcRequest = com.product.ground_control.toggles.api.grpc.EvalRequest.newBuilder()
                .setFeatureKey(key)
                .build();
        com.product.ground_control.toggles.api.grpc.EvalResponse grpcResponse = stub.evaluate(grpcRequest);

        assertThat(grpcResponse.getResult()).isEqualTo("true");
        assertThat(grpcResponse.getType()).isEqualTo("BOOLEAN");
        assertThat(grpcResponse.getFeatureKey()).isEqualTo(key);

        // Verify repository was NOT called again (cached)
        verify(toggleRepository, times(1)).findByKey(key);

        // 3. Verify analytics events were persisted for both evaluation calls (reliable async ingestion)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            var events = eventRepository.findAll();
            assertThat(events).anySatisfy(event -> {
                assertThat(event.getFeatureKey().getValue()).isEqualTo(key);
                assertThat(event.getVariation().getVariant()).isEqualTo("true");
            });
            // Both REST and gRPC evaluations should each produce one event
            assertThat(events).hasSizeGreaterThanOrEqualTo(2);
        });
    }

    @Test
    void shouldReturnNotFoundWhenFeatureMissing() throws Exception {
        String key = "missing_feature";
        when(toggleRepository.findByKey(anyString())).thenReturn(Optional.empty());

        // REST should return 404
        EvalRequest restRequest = new EvalRequest(key, Map.of());
        try {
            restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/rules/eval", restRequest, EvalResponse.class);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
