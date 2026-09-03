package com.asrevo.cvhome.sso;

import com.asrevo.cvhome.testsupport.arch.CvhomeArchitectureRules;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * The layering of the SSO server, enforced. It predates the {@code api} convention — its controllers live under
 * {@code web} — so that package is declared as the one legacy location rather than left unchecked.
 */
@AnalyzeClasses(packages = SsoArchitectureTest.DOMAIN, importOptions = ImportOption.DoNotIncludeTests.class)
final class SsoArchitectureTest {

    static final String DOMAIN = "com.asrevo.cvhome.sso";

    @ArchTest
    static final ArchRule API_GOES_THROUGH_SERVICES = CvhomeArchitectureRules.apiDoesNotTouchRepositories(DOMAIN);

    @ArchTest
    static final ArchRule SERVICES_STAY_OFF_THE_WEB = CvhomeArchitectureRules.servicesDoNotDependOnOwnApi(DOMAIN);

    @ArchTest
    static final ArchRule ENTITIES_STAY_BELOW = CvhomeArchitectureRules.entitiesDoNotDependOnServices(DOMAIN);

    @ArchTest
    static final ArchRule CONTROLLERS_IN_WEB = CvhomeArchitectureRules.controllersLiveIn(DOMAIN, "..sso.web..");

    @ArchTest
    static final ArchRule NO_TEST_SUPPORT_IN_PRODUCTION = CvhomeArchitectureRules.noTestSupportInProduction();

    private SsoArchitectureTest() {
    }

}
