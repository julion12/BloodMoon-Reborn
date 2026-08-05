package org.spectralmemories.bloodmoon.config;

import org.bukkit.World;
import org.junit.jupiter.api.Test;
import org.spectralmemories.bloodmoon.ConfigReader;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorldConfigRegistryTest {
    @Test void registeredWorldIsResolvedByUuidRatherThanWorldObjectIdentity() {
        UUID id = UUID.randomUUID();
        World initiallyLoaded = world(id);
        World reloadedInstance = world(id);
        ConfigReader config = mock(ConfigReader.class);
        WorldConfigRegistry registry = new WorldConfigRegistry();

        registry.put(initiallyLoaded, config);

        assertSame(config, registry.get(reloadedInstance));
    }

    @Test void unloadRemovesTheWorldAndAllowsFreshRegistration() {
        UUID id = UUID.randomUUID();
        World first = world(id);
        World reloaded = world(id);
        ConfigReader oldConfig = mock(ConfigReader.class);
        ConfigReader newConfig = mock(ConfigReader.class);
        WorldConfigRegistry registry = new WorldConfigRegistry();

        registry.put(first, oldConfig);
        assertSame(oldConfig, registry.remove(first));
        assertNull(registry.get(reloaded));
        registry.put(reloaded, newConfig);

        assertAll(() -> assertSame(newConfig, registry.get(first)),
                () -> assertEquals(1, registry.values().size()));
    }

    @Test void nullWorldAndNullReaderAreSafe() {
        WorldConfigRegistry registry = new WorldConfigRegistry();
        assertAll(() -> assertNull(registry.get(null)),
                () -> assertNull(registry.remove(null)),
                () -> assertNull(registry.put(null, mock(ConfigReader.class))),
                () -> assertNull(registry.put(world(UUID.randomUUID()), null)));
    }

    private static World world(UUID id) {
        World world = mock(World.class);
        when(world.getUID()).thenReturn(id);
        return world;
    }
}
