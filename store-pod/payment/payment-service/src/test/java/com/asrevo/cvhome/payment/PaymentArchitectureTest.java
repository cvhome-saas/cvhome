package com.asrevo.cvhome.payment;

import com.asrevo.cvhome.testsupport.arch.CvhomeArchitectureRules;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * The layering of the payment domain, enforced. Copy this class into a new service and change {@link #DOMAIN} — the
 * rules themselves live in {@link CvhomeArchitectureRules}.
 */
@AnalyzeClasses(packages = "com.asrevo.cvhome.payment", importOptions = ImportOption.DoNotIncludeTests.class)
final class PaymentArchitectureTest {

    static final String DOMAIN = "com.asrevo.cvhome.payment";

    @ArchTest
    static final ArchRule API_GOES_THROUGH_SERVICES = CvhomeArchitectureRules.apiDoesNotTouchRepositories(DOMAIN);

    @ArchTest
    static final ArchRule SERVICES_STAY_OFF_THE_WEB = CvhomeArchitectureRules.servicesDoNotDependOnOwnApi(DOMAIN);

    @ArchTest
    static final ArchRule ENTITIES_STAY_BELOW = CvhomeArchitectureRules.entitiesDoNotDependOnServices(DOMAIN);

    /**
     * payment still serves its shopper auth endpoints from {@code controller.v1.auth}; the deviation is declared so no
     * new controller location can appear.
     */
    @ArchTest
    static final ArchRule CONTROLLERS_IN_API =
            CvhomeArchitectureRules.controllersLiveIn(DOMAIN, "com.asrevo.cvhome.payment.controller..");

    @ArchTest
    static final ArchRule NO_TEST_SUPPORT_IN_PRODUCTION = CvhomeArchitectureRules.noTestSupportInProduction();

    private PaymentArchitectureTest() {
    }

}
