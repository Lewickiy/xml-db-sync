package ru.levitsky.service;

import groovy.xml.XmlSlurper;
import groovy.xml.slurpersupport.GPathResult;
import groovy.xml.slurpersupport.NodeChild;
import jakarta.inject.Singleton;
import ru.levitsky.config.AppConfig;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static ru.levitsky.helper.JsonHelper.toJson;

@Singleton
public class XmlParser {

    private final GPathResult xml;

    public XmlParser(AppConfig config) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        SAXParser parser = factory.newSAXParser();
        XmlSlurper slurper = new XmlSlurper(parser);
        this.xml = slurper.parse(config.getCatalogUrl());
    }

    public GPathResult shop() {
        return (GPathResult) xml.getProperty("shop");
    }

    public List<String> getTableNames() {
        List<String> tables = new ArrayList<>();
        for (Object node : shop().children()) {
            NodeChild child = (NodeChild) node;
            if (!child.children().isEmpty()) {
                tables.add(child.name().toLowerCase());
            }
        }
        return tables;
    }

    public List<Map<String, String>> getRows(String tableName) {
        GPathResult table = (GPathResult) shop().getProperty(tableName);
        List<Map<String, String>> rows = new ArrayList<>();

        for (Object obj : table.children()) {
            NodeChild row = (NodeChild) obj;
            Map<String, String> rowMap = new LinkedHashMap<>();

            row.attributes().forEach((k, v) -> rowMap.put(k.toString().toLowerCase(), v.toString()));

            Map<String, String> paramsMap = new LinkedHashMap<>();
            for (Object c : row.children()) {
                NodeChild child = (NodeChild) c;
                if ("param".equals(child.name())) {
                    Object nameAttr = child.attributes().get("name");
                    if (nameAttr != null) {
                        paramsMap.put(nameAttr.toString(), child.text());
                    }
                } else {
                    rowMap.put(child.name().toLowerCase(), child.text());
                }
            }

            if (!paramsMap.isEmpty()) {
                rowMap.put("params", toJson(paramsMap));
            }

            rows.add(rowMap);
        }

        return rows;
    }

    public boolean hasParams(String tableName) {
        GPathResult table = (GPathResult) shop().getProperty(tableName);
        for (Object obj : table.children()) {
            NodeChild row = (NodeChild) obj;
            for (Object c : row.children()) {
                NodeChild child = (NodeChild) c;
                if ("param".equals(child.name())) return true;
            }
        }
        return false;
    }
}
