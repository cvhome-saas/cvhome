package com.asrevo.cvhome.billing.api.v1;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.billing.commons.dto.PlanView;
import com.asrevo.cvhome.billing.service.PlanCatalogService;

import lombok.RequiredArgsConstructor;

/**
 * The plan catalog.
 *
 * <p>
 * Reachable without a token, by the {@code /api/v1/*&#47;public/**} rule in {@code SecurityConfig}: a pricing page is
 * read by people who have not signed up yet, so requiring one would make the product unsellable. Nothing here is
 * store-scoped, which is why these are the only billing endpoints without a {@code store} parameter and a permission
 * check.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/plan")
@RequiredArgsConstructor
public class PlanCatalogApi {

    private final PlanCatalogService planCatalogService;

    /**
     * Every plan on sale, cheapest first, with its prices and what it grants.
     *
     * @param currency show only prices in this currency; omit for all of them
     */
    @GetMapping("public/plans")
    public List<PlanView> listPlans(@RequestParam(value = "currency", required = false) String currency) {
        return planCatalogService.listActivePlans(currency);
    }

}
