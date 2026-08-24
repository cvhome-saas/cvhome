package com.asrevo.cvhome.tenancy;

import com.asrevo.cvhome.testsupport.arch.CvhomeArchitectureRules;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * The layering of the tenancy domain, enforced. Copy this class into a new service and change {@link #DOMAIN} — the
 * rules themselves live in {@link CvhomeArchitectureRules}.
 */
@AnalyzeClasses(packages = "com.asrevo.cvhome.tenancy", importOptions = ImportOption.DoNotIncludeTests.class)
final class TenancyArchitectureTest {

    static final String DOMAIN = "com.asrevo.cvhome.tenancy";

    @ArchTest
    static final ArchRule API_GOES_THROUGH_SERVICES = CvhomeArchitectureRules.apiDoesNotTouchRepositories(DOMAIN);

    @ArchTest
    static final ArchRule SERVICES_STAY_OFF_THE_WEB = CvhomeArchitectureRules.servicesDoNotDependOnOwnApi(DOMAIN);

    @ArchTest
    static final ArchRule ENTITIES_STAY_BELOW = CvhomeArchitectureRules.entitiesDoNotDependOnServices(DOMAIN);

    /**
     * tenancy predates the {@code ..api..} convention and keeps its controllers under {@code manager.controller};
     * the deviation is declared here so no <em>new</em> location can appear.
     */
    @ArchTest
    static final ArchRule CONTROLLERS_IN_API =
            CvhomeArchitectureRules.controllersLiveIn(DOMAIN,
                    "com.asrevo.cvhome.tenancy.manager.controller..", "com.asrevo.cvhome.tenancy.controller..");

    @ArchTest
    static final ArchRule NO_TEST_SUPPORT_IN_PRODUCTION = CvhomeArchitectureRules.noTestSupportInProduction();

    private TenancyArchitectureTest() {
    }

}
