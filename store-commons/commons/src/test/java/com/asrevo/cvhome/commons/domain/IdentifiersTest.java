package com.asrevo.cvhome.commons.domain;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The id value objects that carry an {@link ObjectId}, and the parsing rules that differ between them.
 *
 * <p>
 * {@link ManagerOrgId} silently yields a null id for anything that is not 24 characters, where {@link PodId} throws.
 * That asymmetry is load-bearing — org ids arrive from request paths and are checked downstream, pod ids are minted
 * internally — and it is the kind of thing a refactor quietly flips, so both halves are asserted.
 * </p>
 */
class IdentifiersTest {

    private static final String HEX = "65f023632bc46470c104b76f";
    private static final String TENANT = "tenant-1";
    private static final String PLATFORM = "platform";
    private static final String LOW = "aaa";
    private static final String HIGH = "bbb";
    private static final String HOST = "shop.example.com";

    @Test
    void aRealmIdCanBeBuiltFromAStoreSoAPodRealmMatchesItsStore() {
        assertThat(RealmId.of(new StoreMerchantId(HEX)).getId()).isEqualTo(HEX);
        assertThat(RealmId.of(TENANT).getId()).isEqualTo(TENANT);
    }

    @Test
    void thePlatformRealmIsAConstant() {
        assertThat(RealmId.PLATFORM.getId()).isEqualTo(PLATFORM);
        assertThat(RealmId.PLATFORM).isEqualTo(RealmId.of(PLATFORM));
    }

    @Test
    void realmIdsSortByTheirValue() {
        assertThat(RealmId.of(LOW).compareTo(RealmId.of(HIGH))).isNegative();
        assertThat(RealmId.of(LOW).compareTo(RealmId.of(LOW))).isZero();
    }

    @Test
    void aPodIdShortensToItsFirstEightCharactersForLogs() {
        assertThat(new PodId(HEX).shorten()).isEqualTo(HEX.substring(0, 8));
    }

    @Test
    void aPodIdWithNoObjectIdShortensToNullRatherThanThrowing() {
        assertThat(new PodId((ObjectId) null).shorten()).isNull();
    }

    @Test
    void podIdsAreMintedDistinct() {
        assertThat(PodId.newId().getId()).isNotEqualTo(PodId.newId().getId());
    }

    @Test
    void aPodIdRejectsAStringThatIsNotObjectIdHex() {
        assertThatThrownBy(() -> new PodId("nope")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aManagerOrgIdParsesTwentyFourCharacterHex() {
        assertThat(new ManagerOrgId(HEX).getId()).isEqualTo(new ObjectId(HEX));
    }

    @Test
    void aManagerOrgIdOfTheWrongLengthYieldsNullRatherThanThrowing() {
        assertThat(new ManagerOrgId("short").getId()).isNull();
        assertThat(new ManagerOrgId("%sextra".formatted(HEX)).getId()).isNull();
        assertThat(new ManagerOrgId((String) null).getId()).isNull();
    }

    @Test
    void managerOrgIdsAreMintedDistinct() {
        assertThat(ManagerOrgId.newId().getId()).isNotEqualTo(ManagerOrgId.newId().getId());
    }

    @Test
    void aDomainMatchesOnlyItsOwnValue() {
        assertThat(new Domain(HOST).matches(HOST)).isTrue();
        assertThat(new Domain(HOST).matches("other.example.com")).isFalse();
    }
}
