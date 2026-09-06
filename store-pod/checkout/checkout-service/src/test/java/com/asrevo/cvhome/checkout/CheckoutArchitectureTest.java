package com.asrevo.cvhome.checkout;

import com.asrevo.cvhome.testsupport.arch.CvhomeArchitectureRules;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * The layering of the checkout domain, enforced — all five rules, no declared deviation. The previous checkout
 * needed two; the rewrite was the chance to clear them.
 */
@AnalyzeClasses(packages = "com.asrevo.cvhome.checkout", importOptions = ImportOption.DoNotIncludeTests.class)
final class CheckoutArchitectureTest {

    static final String DOMAIN = "com.asrevo.cvhome.checkout";

    @ArchTest
    static final ArchRule API_GOES_THROUGH_SERVICES = CvhomeArchitectureRules.apiDoesNotTouchRepositories(DOMAIN);

    @ArchTest
    static final ArchRule SERVICES_STAY_OFF_THE_WEB = CvhomeArchitectureRules.servicesDoNotDependOnOwnApi(DOMAIN);

    @ArchTest
    static final ArchRule ENTITIES_STAY_BELOW = CvhomeArchitectureRules.entitiesDoNotDependOnServices(DOMAIN);

    @ArchTest
    static final ArchRule CONTROLLERS_IN_API = CvhomeArchitectureRules.controllersLiveInApi(DOMAIN);

    @ArchTest
    static final ArchRule NO_TEST_SUPPORT_IN_PRODUCTION = CvhomeArchitectureRules.noTestSupportInProduction();

    private CheckoutArchitectureTest() {
    }

}
