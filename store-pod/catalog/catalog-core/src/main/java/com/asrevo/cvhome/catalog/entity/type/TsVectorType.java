package com.asrevo.cvhome.catalog.entity.type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.usertype.UserType;

/**
 * Maps Postgres {@code tsvector} so the column can be named in a Criteria predicate.
 *
 * <p>
 * The value is never meant to travel into Java: the search predicate is a correlated {@code exists} subquery that
 * selects a literal, precisely so no query ever puts a {@code tsvector} in its select list. The text form is
 * readable here anyway, because a mapped attribute needs a type and because a stray read should return something
 * legible rather than blow up.
 * </p>
 */
public class TsVectorType implements UserType<String> {

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
