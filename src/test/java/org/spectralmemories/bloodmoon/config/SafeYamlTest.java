package org.spectralmemories.bloodmoon.config;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.error.YAMLException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SafeYamlTest {
    @Test void readsOrdinaryMappings() {
        Object loaded = SafeYaml.create().load("enabled: true\ncount: 3\n");

        assertInstanceOf(Map.class, loaded);
        assertEquals(Boolean.TRUE, ((Map<?, ?>) loaded).get("enabled"));
        assertEquals(3, ((Map<?, ?>) loaded).get("count"));
    }

    @Test void rejectsArbitraryJavaTypeTags() {
        assertThrows(YAMLException.class,
                () -> SafeYaml.create().load("value: !!java.net.URL [\"https://example.invalid\"]\n"));
    }

    @Test void rejectsDuplicateKeys() {
        assertThrows(YAMLException.class,
                () -> SafeYaml.create().load("value: first\nvalue: second\n"));
    }
}
