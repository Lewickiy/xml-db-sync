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

A minimal interface is provided via `main()`.

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
3. Configure DB credentials (in `Main.java`)
4. Run `Main.main()` to sync data

