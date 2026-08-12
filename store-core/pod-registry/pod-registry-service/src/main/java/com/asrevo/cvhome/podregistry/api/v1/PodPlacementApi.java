package com.asrevo.cvhome.podregistry.api.v1;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.podregistry.commons.dto.PlacementDecision;
import com.asrevo.cvhome.podregistry.commons.dto.PlacementRequest;
import com.asrevo.cvhome.podregistry.commons.errors.NoEligiblePodException;
import com.asrevo.cvhome.podregistry.service.PodPlacementService;
import com.asrevo.cvhome.podregistry.services.placement.IPodPlacementService;

import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Where a new store should be created.
 *
 * <p>
 * Service-to-service only. Tenancy asks on behalf of an organization while it is still deciding whether the store
 * can be created, so there is no store to scope a permission against and no human ever calls this — which is why
 * {@code STORE-CORE.POD.PLACEMENT} checks the {@code store_core} scope and ignores its target, exactly as billing's
 * {@code QUOTA-CHECK} does.
 * </p>
 */
@RestController
@RequestMapping("api/v1/pod/private")
@AllArgsConstructor
@Tag(name = "Pod placement", description = "Where a new store should be created")
@Slf4j
public class PodPlacementApi implements IPodPlacementService {

    private final PodPlacementService placementService;

    /**
     * A refusal is an error status rather than a 200 carrying a verdict, unlike billing's quota check.
     *
     * <p>
     * The difference is what the caller can do with it. Billing's refusal is routine and has a reason worth showing
     * an org ("you have three unpaid stores"), so it is a normal answer. Having nowhere at all to put a store is an
     * operational fault — someone has to drain, resize or add a pod — so it travels as a 422 with a code, and the
     * caller aborts rather than renders it.
     * </p>
     */
    @Override
    @PostMapping("placement")
    @PreAuthorize("hasPermission(null,'PodId','STORE-CORE.POD.PLACEMENT')")
    public PlacementDecision place(@RequestBody PlacementRequest request) throws NoEligiblePodException {
        PlacementDecision decision = placementService.place(request);
        log.info("Placed a new store for org {} on pod {} ({})",
                request.org() == null ? null : request.org().id(), decision.podId(), decision.reason());
        return decision;
    }

}
