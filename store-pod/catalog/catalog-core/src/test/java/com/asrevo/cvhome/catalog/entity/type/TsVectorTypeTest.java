package com.asrevo.cvhome.catalog.entity.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import org.hibernate.type.descriptor.WrapperOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The mapping exists so a Criteria predicate can name the column. It is never meant to carry a value into
 * Java, but it must behave if something reads one — and it must bind as {@code OTHER}, because a
 * {@code tsvector} sent as a string is rejected by the driver.
 */
@ExtendWith(MockitoExtension.class)
class TsVectorTypeTest {

    private static final String DOCUMENT = "'run':1A 'shoe':2A";

    private final TsVectorType type = new TsVectorType();

    @Mock
    private ResultSet resultSet;

    @Mock
    private PreparedStatement statement;

    @Test
    void bindsAsOtherRatherThanAsAString() throws Exception {
        type.nullSafeSet(statement, DOCUMENT, 1, (WrapperOptions) null);

        verify(statement).setObject(1, DOCUMENT, Types.OTHER);
    }

    @Test
    void readsTheTextForm() throws Exception {
        when(resultSet.getString(3)).thenReturn(DOCUMENT);

        assertThat(type.nullSafeGet(resultSet, 3, (WrapperOptions) null)).isEqualTo(DOCUMENT);
    }

    @Test
    void describesItselfAsAnOpaqueStringColumn() {
        assertThat(type.getSqlType()).isEqualTo(Types.OTHER);
        assertThat(type.returnedClass()).isEqualTo(String.class);
        assertThat(type.isMutable()).isFalse();
    }

    @Test
    void valueSemanticsAreTheStringsOwn() {
        assertThat(type.equals(DOCUMENT, DOCUMENT)).isTrue();
        assertThat(type.equals(DOCUMENT, "'other':1")).isFalse();
        assertThat(type.equals(null, null)).isTrue();
        assertThat(type.hashCode(DOCUMENT)).isEqualTo(DOCUMENT.hashCode());
        assertThat(type.deepCopy(DOCUMENT)).isSameAs(DOCUMENT);
        assertThat(type.assemble(type.disassemble(DOCUMENT), null)).isEqualTo(DOCUMENT);
    }
}
