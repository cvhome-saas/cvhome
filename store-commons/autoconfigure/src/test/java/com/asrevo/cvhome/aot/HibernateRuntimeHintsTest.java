package com.asrevo.cvhome.aot;

import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.usertype.UserType;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.beans.factory.aot.AotServices;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What Hibernate needs reflectively while it builds the session factory. A missing one fails the service at start.
 */
class HibernateRuntimeHintsTest {

    @Test
    void everyUserTypeUnderOurPackagesIsConstructible() {
        RuntimeHints hints = new RuntimeHints();

        new HibernateRuntimeHints().registerHints(hints, getClass().getClassLoader());

        assertThat(RuntimeHintsPredicates.reflection().onType(StubUserType.class)
                .withMemberCategory(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)).accepts(hints);
    }

    @Test
    void theArrayOfEveryEntitysIdentifierTypeCanBeCreatedInheritedOnesIncluded() {
        RuntimeHints hints = new RuntimeHints();

        new HibernateRuntimeHints().registerHints(hints, getClass().getClassLoader());

        // The multi-id loader binds ids as one SQL array; uaa and cua refused to start without UUID[].
        assertThat(RuntimeHintsPredicates.reflection().onType(UUID[].class)).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection().onType(Long[].class)).accepts(hints);
        assertThat(HibernateRuntimeHints.identifierTypes(StubChildEntity.class)).containsExactly(Long.class);
    }

    @Test
    void aServiceWithoutHibernateGetsNothing() {
        RuntimeHints hints = new RuntimeHints();

        new HibernateRuntimeHints().registerHints(hints, new URLClassLoader(new URL[0], null));

        assertThat(hints.reflection().typeHints()).isEmpty();
    }

    @Test
    void theRegistrarIsDiscoveredByAotProcessingWithoutABean() {
        assertThat(AotServices.factories().load(RuntimeHintsRegistrar.class))
                .anyMatch(HibernateRuntimeHints.class::isInstance);
    }

    /** Stands in for sso-core's UUID-keyed entities. */
    @Entity
    public static class StubEntity {

        @Id
        private UUID id;

    }

    /** Where most pod entities keep their id: on a mapped superclass. */
    @MappedSuperclass
    public static class StubBase {

        @Id
        private Long id;

    }

    /** An entity that declares no id of its own. */
    @Entity
    public static class StubChildEntity extends StubBase {

        private String name;

    }

    /** Stands in for catalog's TsVectorType. */
    public static class StubUserType implements UserType<String> {

        @Override
        public int getSqlType() {
            return Types.OTHER;
        }

        @Override
        public Class<String> returnedClass() {
            return String.class;
        }

        @Override
        public boolean equals(String x, String y) {
            return Objects.equals(x, y);
        }

        @Override
        public int hashCode(String x) {
            return Objects.hashCode(x);
        }

        @Override
        public String nullSafeGet(ResultSet rs, int position, WrapperOptions options) throws SQLException {
            return rs.getString(position);
        }

        @Override
        public void nullSafeSet(PreparedStatement st, String value, int index, WrapperOptions options)
                throws SQLException {
            st.setObject(index, value, Types.OTHER);
        }

        @Override
        public String deepCopy(String value) {
            return value;
        }

        @Override
        public boolean isMutable() {
            return false;
        }

        @Override
        public Serializable disassemble(String value) {
            return value;
        }

        @Override
        public String assemble(Serializable cached, Object owner) {
            return (String) cached;
        }

    }

}
