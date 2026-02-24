package com.pm.billingservice.grpc;

import billing.BillingResponse;
import billing.BillingServiceGrpc;
import com.pm.billingservice.BillingServiceApplication;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class BillingGrpcService extends BillingServiceGrpc.BillingServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(BillingGrpcService.class);

    @Override
    public void createBillingAccount(billing.BillingRequest billingRequest,
                                      StreamObserver<BillingResponse> responseObserver){
        //streamobserver - helps get multiple responses, accept back and forth communication
        log.info("createBillingAccount request received : {}", billingRequest.toString());

        //Business Logic
        BillingResponse response = BillingResponse.newBuilder()
                .setAccountId("12345")
                .setStatus("ACTIVE").build();

        responseObserver.onNext(response);
        //better for real time analytics
        responseObserver.onCompleted();

    }


}
