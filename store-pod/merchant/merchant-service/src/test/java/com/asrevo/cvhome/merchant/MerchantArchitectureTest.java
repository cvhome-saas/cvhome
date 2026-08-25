package com.asrevo.cvhome.merchant;

import com.asrevo.cvhome.testsupport.arch.CvhomeArchitectureRules;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * The layering of the merchant domain, enforced. Copy this class into a new service and change {@link #DOMAIN} — the
 * rules themselves live in {@link CvhomeArchitectureRules}.
 */
@AnalyzeClasses(packages = "com.asrevo.cvhome.merchant", importOptions = ImportOption.DoNotIncludeTests.class)
final class MerchantArchitectureTest {

    static final String DOMAIN = "com.asrevo.cvhome.merchant";

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

    private MerchantArchitectureTest() {
    }

}
