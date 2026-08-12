package com.asrevo.cvhome.merchant.api;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.asrevo.cvhome.errors.RemoteServiceTimeoutException;
import com.asrevo.cvhome.errors.RemoteServiceUnavailableException;
import com.asrevo.cvhome.errors.UnmappedRemoteFailureException;

@HttpExchange("/api/v1")
public interface MerchantStorePodClient {

    /**
     * Creates the store inside this pod.
     *
     * <p>
     * The failures are named because the caller acts on the difference between them. A pod that refused is a
     * verdict and is recorded as failed provisioning; a pod that never answered decided nothing, and the store is
     * left mid-flight for the outbox to retry. Without these clauses both arrive wrapped in the unchecked carrier
     * and the caller cannot tell them apart — which is how a timeout came to be recorded as a rejection.
     * </p>
     *
     * @throws UnmappedRemoteFailureException   the pod answered with an error. This API publishes no error
     *                                          catalog, so its codes arrive untyped but still carry the pod's own
     *                                          code and status
     * @throws RemoteServiceUnavailableException the pod could not be reached at all
     * @throws RemoteServiceTimeoutException     the pod was reachable but did not answer in time — the store may
     *                                           or may not have been created
     */
    @PostExchange("/private/store")
    Void create(@RequestBody Map<Object, Object> dto)
            throws UnmappedRemoteFailureException, RemoteServiceUnavailableException, RemoteServiceTimeoutException;

    @GetExchange("private/store")
    Map<String, Object> getStore(@RequestParam("store") String store);

}
