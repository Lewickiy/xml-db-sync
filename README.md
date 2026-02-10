# ⚠ Notice for Reviewers

Please, after reviewing this project, notify me (representatives of SoftMotion). I will then make the repository private

# XML-DB-Sync (SoftMotion Test Task)

## Description

This project implements a service for parsing an XML catalog and synchronizing data with PostgreSQL.

XML feed:  
[https://expro.ru/bitrix/catalog_export/export_Sai.xml](https://expro.ru/bitrix/catalog_export/export_Sai.xml)

Technologies used:

* Groovy `XmlSlurper` for XML parsing
* PostgreSQL via JDBC
* `offers.vendorCode` is considered unique for UPSERT operations
* Dynamic table and column creation based on XML structure
* Validation with @NotBlank for required configuration fields

A minimal interface is provided via `Application.main()`.

## Features

### Core

```java
String getTableNames();         // Table names from XML

String getTableDDL(String tableName); // DDL to create a table

void update();                  // Update all tables

void update(String tableName);  // Update a specific table
```

### Optional

```java
ArrayList<String> getColumnNames(String tableName); // Dynamic column list

boolean isColumnId(String tableName, String columnName); // Check if column is unique

String getDDLChange(String tableName); // Schema changes (adding new columns)
```

## Database
* `DatabaseService` handles connection lifecycle and queries
* Automatically adds missing columns
* UPSERT using `vendorcode` if present
* `params` column is stored as JSONB when XML contains `<param>` elements

## Configuration
All configuration is done via `application.yml`:

```yaml
micronaut:
  application:
    name: xml-db-sync

datasource:
  default:
    url: ${DB_URL:`jdbc:postgresql://localhost:54322/test`}
    username: ${DB_USER:postgres}
    password: ${DB_PASS:123}
    driver-class-name: org.postgresql.Driver

app:
  catalog-url: https://expro.ru/bitrix/catalog_export/export_Sai.xml
```

Environment variables `DB_URL`, `DB_USER`, and `DB_PASS` can override defaults.

## Run PostgreSQL in Docker

```bash
docker run -d --name test-db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=123 \
  -e POSTGRES_DB=test \
  -p 54322:5432 \
  postgres:17-alpine3.22
```

## Usage

1. Clone repository
2. Start PostgreSQL (see above)
3. Configure DB credentials via environment variables or `application.yml`
4. Run `Application.main()` to sync data

