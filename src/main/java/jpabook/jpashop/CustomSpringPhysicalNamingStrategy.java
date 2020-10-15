package jpabook.jpashop;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import java.util.Locale;

/**
 * Created by frenchline707@gmail.com on 2020-10-14
 * Blog : http://frenchline707.tistory.com
 * Github : http://github.com/frenchLineCigar
 */

public class CustomSpringPhysicalNamingStrategy implements PhysicalNamingStrategy {

    @Override
    public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return apply(name, jdbcEnvironment);
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return applyForSchema(name, jdbcEnvironment);
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return applyForTable(name, jdbcEnvironment);
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return applyForSequence(name, jdbcEnvironment);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return applyForColumn(name, jdbcEnvironment);
    }

    private Identifier apply(Identifier name, JdbcEnvironment jdbcEnvironment) {
        if (name == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder(name.getText().replace('.', '_'));
        for (int i = 1; i < builder.length() - 1; i++) {
            if (isUnderscoreRequired(builder.charAt(i - 1), builder.charAt(i), builder.charAt(i + 1))) {
                builder.insert(i++, '_');
            }
        }
        return getIdentifier(builder.toString(), name.isQuoted(), jdbcEnvironment);
    }

    /* For toPhysicalTableName */
    private Identifier applyForTable(Identifier name, JdbcEnvironment jdbcEnvironment) {
        StringBuilder builder = convertUnderscore(name);
        if (builder == null) return null;
        return getIdentifier("jpa_table_" + builder.toString(), name.isQuoted(), jdbcEnvironment);
    }

    /* For toPhysicalSchemaName */
    private Identifier applyForSchema(Identifier name, JdbcEnvironment jdbcEnvironment) {
        StringBuilder builder = convertUnderscore(name);
        if (builder == null) return null;
        return getIdentifier("jpa_schema_" + builder.toString(), name.isQuoted(), jdbcEnvironment);
    }

    /* For toPhysicalSequenceName */
    private Identifier applyForSequence(Identifier name, JdbcEnvironment jdbcEnvironment) {
        StringBuilder builder = convertUnderscore(name);
        if (builder == null) return null;
        return getIdentifier("jpa_seq_" + builder.toString(), name.isQuoted(), jdbcEnvironment);
    }

    /* For toPhysicalColumnName */
    private Identifier applyForColumn(Identifier name, JdbcEnvironment jdbcEnvironment) {
        StringBuilder builder = convertUnderscore(name);
        if (builder == null) return null;
        return getIdentifier("jpa_col_" + builder.toString(), name.isQuoted(), jdbcEnvironment);
    }

    /* 언더 스코어 변환 공통 코드 */
    private StringBuilder convertUnderscore(Identifier name) {
        if (name == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder(name.getText().replace('.', '_'));
        for (int i = 1; i < builder.length() - 1; i++) {
            if (isUnderscoreRequired(builder.charAt(i - 1), builder.charAt(i), builder.charAt(i + 1))) {
                builder.insert(i++, '_');
            }
        }
        return builder;
    }

    /**
     * Get an identifier for the specified details. By default this method will return an
     * identifier with the name adapted based on the result of
     * {@link #isCaseInsensitive(JdbcEnvironment)}
     * @param name the name of the identifier
     * @param quoted if the identifier is quoted
     * @param jdbcEnvironment the JDBC environment
     * @return an identifier instance
     */
    protected Identifier getIdentifier(String name, boolean quoted, JdbcEnvironment jdbcEnvironment) {
        if (isCaseInsensitive(jdbcEnvironment)) {
            name = name.toLowerCase(Locale.ROOT);
        }
        return new Identifier(name, quoted);
    }

    /**
     * Specify whether the database is case sensitive.
     * @param jdbcEnvironment the JDBC environment which can be used to determine case
     * @return true if the database is case insensitive sensitivity
     */
    protected boolean isCaseInsensitive(JdbcEnvironment jdbcEnvironment) {
        return true;
    }

    private boolean isUnderscoreRequired(char before, char current, char after) {
        return Character.isLowerCase(before) && Character.isUpperCase(current) && Character.isLowerCase(after);
    }

}