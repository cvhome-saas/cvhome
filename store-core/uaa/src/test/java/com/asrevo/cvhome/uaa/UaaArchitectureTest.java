package com.asrevo.cvhome.uaa;

import com.asrevo.cvhome.testsupport.arch.CvhomeArchitectureRules;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * The layering of uaa, enforced. uaa predates the {@code api} convention — its controllers live under {@code web} —
 * so that package is declared as the one legacy location rather than left unchecked.
 */
@AnalyzeClasses(packages = UaaArchitectureTest.DOMAIN, importOptions = ImportOption.DoNotIncludeTests.class)
final class UaaArchitectureTest {

    static final String DOMAIN = "com.asrevo.cvhome.uaa";

    @ArchTest
    static final ArchRule API_GOES_THROUGH_SERVICES = CvhomeArchitectureRules.apiDoesNotTouchRepositories(DOMAIN);

    @ArchTest
    static final ArchRule SERVICES_STAY_OFF_THE_WEB = CvhomeArchitectureRules.servicesDoNotDependOnOwnApi(DOMAIN);

    @ArchTest
    static final ArchRule ENTITIES_STAY_BELOW = CvhomeArchitectureRules.entitiesDoNotDependOnServices(DOMAIN);

    @ArchTest
    static final ArchRule CONTROLLERS_IN_WEB = CvhomeArchitectureRules.controllersLiveIn(DOMAIN, "..uaa.web..");

    @ArchTest
    static final ArchRule NO_TEST_SUPPORT_IN_PRODUCTION = CvhomeArchitectureRules.noTestSupportInProduction();

    private UaaArchitectureTest() {
    }

}
