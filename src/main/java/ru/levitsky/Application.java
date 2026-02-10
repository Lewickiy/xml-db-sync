package ru.levitsky;

import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.Micronaut;
import ru.levitsky.service.DatabaseService;
import ru.levitsky.service.XmlCatalogService;

public class Application {
    public static void main(String[] args) {
        try (ApplicationContext context = Micronaut.run(Application.class, args)) {
            XmlCatalogService xmlService = context.getBean(XmlCatalogService.class);
            DatabaseService dbService = context.getBean(DatabaseService.class);

            for (String table : xmlService.getTableNames()) {
                xmlService.update(table, dbService);
            }
            System.out.println("Готово");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
