/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.relational;

import java.util.function.Predicate;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Randall Hauch
 */
public class SelectorsTest {

    private Predicate<TableId> filter;

    @Test
    public void shouldCreateFilterWithAllLists() {
        filter = Selectors.tableSelector()
                          .includeDatabases("connector_test")
                          .excludeDatabases("")
                          .includeTables("")
                          .excludeTables("")
                          .build();
        assertAllowed(filter, "connector_test", "A");
        assertAllowed(filter, "connector_test", "B");
        assertNotAllowed(filter, "other_test", "A");
        assertNotAllowed(filter, "other_test", "B");
    }

    @Test
    public void shouldCreateFilterWithDatabaseWhitelistAndTableWhitelist() {
        filter = Selectors.tableSelector()
                          .includeDatabases("db1,db2")
                          .includeTables("db1\\.A,db1\\.B,db2\\.C")
                          .build();

        assertAllowed(filter, "db1", "A");
        assertAllowed(filter, "db1", "B");
        assertNotAllowed(filter, "db1", "D");
        assertNotAllowed(filter, "db1", "E");
        assertNotAllowed(filter, "db1", "F");

        assertAllowed(filter, "db2", "C");
        assertNotAllowed(filter, "db2", "G");
        assertNotAllowed(filter, "db2", "H");

        assertNotAllowed(filter, "db3", "A");
        assertNotAllowed(filter, "db4", "A");
    }

    @Test
    public void shouldCreateFilterWithDatabaseWhitelistAndTableBlacklist() {
        filter = Selectors.tableSelector()
                          .includeDatabases("db1,db2")
                          .excludeTables("db1\\.A,db1\\.B,db2\\.C")
                          .build();

        assertNotAllowed(filter, "db1", "A");
        assertNotAllowed(filter, "db1", "B");
        assertAllowed(filter, "db1", "D");
        assertAllowed(filter, "db1", "E");
        assertAllowed(filter, "db1", "F");

        assertNotAllowed(filter, "db2", "C");
        assertAllowed(filter, "db2", "G");
        assertAllowed(filter, "db2", "H");

        assertNotAllowed(filter, "db3", "A");
        assertNotAllowed(filter, "db4", "A");
    }

    @Test
    public void shouldCreateFilterWithDatabaseBlacklistAndTableWhitelist() {
        filter = Selectors.tableSelector()
                          .excludeDatabases("db3,db4")
                          .includeTables("db1\\.A,db1\\.B,db2\\.C")
                          .build();

        assertAllowed(filter, "db1", "A");
        assertAllowed(filter, "db1", "B");
        assertNotAllowed(filter, "db1", "D");
        assertNotAllowed(filter, "db1", "E");
        assertNotAllowed(filter, "db1", "F");

        assertAllowed(filter, "db2", "C");
        assertNotAllowed(filter, "db2", "G");
        assertNotAllowed(filter, "db2", "H");

        assertNotAllowed(filter, "db3", "A");
        assertNotAllowed(filter, "db4", "A");
    }

    @Test
    public void shouldCreateFilterWithDatabaseBlacklistAndTableBlacklist() {
        filter = Selectors.tableSelector()
                          .excludeDatabases("db3,db4")
                          .excludeTables("db1\\.A,db1\\.B,db2\\.C")
                          .build();

        assertNotAllowed(filter, "db1", "A");
        assertNotAllowed(filter, "db1", "B");
        assertAllowed(filter, "db1", "D");
        assertAllowed(filter, "db1", "E");
        assertAllowed(filter, "db1", "F");

        assertNotAllowed(filter, "db2", "C");
        assertAllowed(filter, "db2", "G");
        assertAllowed(filter, "db2", "H");

        assertNotAllowed(filter, "db3", "A");
        assertNotAllowed(filter, "db4", "A");
    }

    @Test
    public void shouldCreateFilterWithNoDatabaseFilterAndTableWhitelist() {
        filter = Selectors.tableSelector()
                          .includeTables("db1\\.A,db1\\.B,db2\\.C")
                          .build();

        assertAllowed(filter, "db1", "A");
        assertAllowed(filter, "db1", "B");
        assertNotAllowed(filter, "db1", "D");
        assertNotAllowed(filter, "db1", "E");
        assertNotAllowed(filter, "db1", "F");

        assertAllowed(filter, "db2", "C");
        assertNotAllowed(filter, "db2", "G");
        assertNotAllowed(filter, "db2", "H");

        assertNotAllowed(filter, "db3", "A");
        assertNotAllowed(filter, "db4", "A");
    }

    @Test
    public void shouldCreateFilterWithNoDatabaseFilterAndTableBlacklist() {
        filter = Selectors.tableSelector()
                          .excludeTables("db1\\.A,db1\\.B,db2\\.C")
                          .build();

        assertNotAllowed(filter, "db1", "A");
        assertNotAllowed(filter, "db1", "B");
        assertAllowed(filter, "db1", "D");
        assertAllowed(filter, "db1", "E");
        assertAllowed(filter, "db1", "F");

        assertNotAllowed(filter, "db2", "C");
        assertAllowed(filter, "db2", "G");
        assertAllowed(filter, "db2", "H");

        assertAllowed(filter, "db3", "A");
        assertAllowed(filter, "db4", "A");
    }

    @Test
    public void shouldCreateFilterWithDatabaseWhitelistAndNoTableFilter() {
        filter = Selectors.tableSelector()
                          .includeDatabases("db1,db2")
                          .build();

        assertAllowed(filter, "db1", "A");
        assertAllowed(filter, "db2", "A");
        assertNotAllowed(filter, "db3", "A");
        assertNotAllowed(filter, "db4", "A");
    }

    @Test
    public void shouldCreateFilterWithDatabaseBlacklistAndNoTableFilter() {
        filter = Selectors.tableSelector()
                          .excludeDatabases("db1,db2")
                          .build();

        assertNotAllowed(filter, "db1", "A");
        assertNotAllowed(filter, "db2", "A");
        assertAllowed(filter, "db3", "A");
        assertAllowed(filter, "db4", "A");
    }

    protected void assertAllowed(Predicate<TableId> filter, String dbName, String tableName) {
        TableId id = new TableId(dbName, null, tableName);
        assertThat(filter.test(id)).isTrue();
    }

    protected void assertNotAllowed(Predicate<TableId> filter, String dbName, String tableName) {
        TableId id = new TableId(dbName, null, tableName);
        assertThat(filter.test(id)).isFalse();
    }

}
