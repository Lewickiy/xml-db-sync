package ru.levitsky;

import ru.levitsky.service.DatabaseService;
import ru.levitsky.service.XmlCatalogService;

public class Main {
    public static void main(String[] args) throws Exception {

        XmlCatalogService xml = new XmlCatalogService(
                "https://expro.ru/bitrix/catalog_export/export_Sai.xml"
        );

        //TODO (production): externalize DB URL, username, and password instead of hardcoding
        DatabaseService db = new DatabaseService(
                "jdbc:postgresql://localhost:54322/test",
                "postgres",
                "123"
        );

        for (String table : xml.getTableNames()) {
            db.execute(xml.getTableDDL(table));
            xml.update(table, db);
        }

        System.out.println("Completed");
    }
}
