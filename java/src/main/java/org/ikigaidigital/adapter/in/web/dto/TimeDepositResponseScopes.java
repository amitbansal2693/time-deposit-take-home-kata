package org.ikigaidigital.adapter.in.web.dto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Field scopes for {@link TimeDepositResponse}, defined in an XML file (similar to a DTO definition)
 * and exposed through the type-safe {@link Scope} enum. Each scope lists attributes, may extend another
 * scope, or set {@code allAttributes="true"} to include every field. The file is read and validated at
 * startup: scope names must match the {@link Scope} constants and attribute names must match the record
 * components, so a mistake fails fast on boot rather than at request time.
 */
@Component
public class TimeDepositResponseScopes {

    private final Map<String, Method> accessors;
    private final Map<Scope, Set<String>> attributesByScope;

    public TimeDepositResponseScopes(@Value("${time-deposit.api.response-scopes-file}") Resource definitionFile) {
        this.accessors = accessors();
        Element root = parse(definitionFile).getDocumentElement();
        if (!"dto".equals(root.getTagName())) {
            throw new IllegalStateException("Root element must be <dto> in " + definitionFile);
        }
        Map<String, Element> definitions = new LinkedHashMap<>();
        for (Element scope : children(root, "scope")) {
            definitions.put(scope.getAttribute("name"), scope);
        }
        Map<String, Set<String>> resolvedByName = new LinkedHashMap<>();
        for (String name : definitions.keySet()) {
            resolve(name, definitions, resolvedByName, new HashSet<>());
        }
        Map<Scope, Set<String>> byScope = new EnumMap<>(Scope.class);
        for (Scope scope : Scope.values()) {
            Set<String> attributes = resolvedByName.get(scope.definitionName());
            if (attributes == null) {
                throw new IllegalStateException("Scope '" + scope.definitionName()
                        + "' is declared in the Scope enum but missing from " + definitionFile);
            }
            byScope.put(scope, attributes);
        }
        this.attributesByScope = Collections.unmodifiableMap(byScope);
    }

    /** Projects each response onto the attributes of the scope, keeping the scope's attribute order. */
    public List<Map<String, Object>> project(List<TimeDepositResponse> body, Scope scope) {
        Set<String> attributes = attributesByScope.get(scope);
        List<Map<String, Object>> result = new ArrayList<>(body.size());
        for (TimeDepositResponse item : body) {
            result.add(toMap(item, attributes));
        }
        return result;
    }

    private Map<String, Object> toMap(TimeDepositResponse item, Set<String> attributes) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (String attribute : attributes) {
            try {
                map.put(attribute, accessors.get(attribute).invoke(item));
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Cannot read attribute '" + attribute + "'", e);
            }
        }
        return map;
    }

    private Set<String> resolve(String name, Map<String, Element> definitions,
                                Map<String, Set<String>> resolved, Set<String> inProgress) {
        Set<String> known = resolved.get(name);
        if (known != null) {
            return known;
        }
        Element definition = definitions.get(name);
        if (definition == null) {
            throw new IllegalStateException("Scope '" + name + "' is referenced but not defined");
        }
        if (!inProgress.add(name)) {
            throw new IllegalStateException("Cyclic extends involving scope '" + name + "'");
        }
        Set<String> attributes = new LinkedHashSet<>();
        if (Boolean.parseBoolean(definition.getAttribute("allAttributes"))) {
            attributes.addAll(accessors.keySet());
        }
        String parent = definition.getAttribute("extends");
        if (!parent.isBlank()) {
            attributes.addAll(resolve(parent, definitions, resolved, inProgress));
        }
        for (Element attribute : children(definition, "attribute")) {
            String attributeName = attribute.getAttribute("name");
            if (!accessors.containsKey(attributeName)) {
                throw new IllegalStateException("'" + attributeName + "' in scope '" + name
                        + "' is not a TimeDepositResponse attribute");
            }
            attributes.add(attributeName);
        }
        inProgress.remove(name);
        Set<String> unmodifiable = Collections.unmodifiableSet(attributes);
        resolved.put(name, unmodifiable);
        return unmodifiable;
    }

    private static Map<String, Method> accessors() {
        Map<String, Method> result = new LinkedHashMap<>();
        for (RecordComponent component : TimeDepositResponse.class.getRecordComponents()) {
            result.put(component.getName(), component.getAccessor());
        }
        return Collections.unmodifiableMap(result);
    }

    private static Document parse(Resource resource) {
        try (InputStream in = resource.getInputStream()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            return factory.newDocumentBuilder().parse(in);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new IllegalStateException("Cannot read response scope definitions: " + resource, e);
        }
    }

    private static List<Element> children(Element parent, String tagName) {
        List<Element> result = new ArrayList<>();
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i) instanceof Element element && tagName.equals(element.getTagName())) {
                result.add(element);
            }
        }
        return result;
    }
}

