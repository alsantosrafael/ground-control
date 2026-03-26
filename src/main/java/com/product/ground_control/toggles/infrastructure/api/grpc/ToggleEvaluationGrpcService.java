package com.product.ground_control.toggles.infrastructure.api.grpc;

import com.product.ground_control.toggles.api.grpc.EvalRequest;
import com.product.ground_control.toggles.api.grpc.EvalResponse;
import com.product.ground_control.toggles.api.grpc.EvaluationServiceGrpc;
import com.product.ground_control.toggles.application.services.ToggleService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * gRPC Service for high-performance rule evaluation.
 */
@GrpcService
@RequiredArgsConstructor
public class ToggleEvaluationGrpcService extends EvaluationServiceGrpc.EvaluationServiceImplBase {

    private final ToggleService toggleService;

    @Override
    public void evaluate(EvalRequest request, StreamObserver<EvalResponse> responseObserver) {
        String key = request.getFeatureKey();
        Map<String, String> context = request.getContextMap();

        toggleService.evaluate(key, context).ifPresentOrElse(
            result -> {
                EvalResponse response = EvalResponse.newBuilder()
                    .setResult(result.value())
                    .setType(result.type())
                    .setFeatureKey(result.featureKey())
                    .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            },
            () -> responseObserver.onError(Status.NOT_FOUND
                .withDescription("Feature flag not found: " + key)
                .asRuntimeException())
        );
    }
}
