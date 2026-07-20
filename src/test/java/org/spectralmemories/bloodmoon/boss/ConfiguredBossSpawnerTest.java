package org.spectralmemories.bloodmoon.boss;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class ConfiguredBossSpawnerTest {
    private static final UUID VANILLA_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID MYTHIC_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private static SpawnedBossResult vanilla() {
        return SpawnedBossResult.success(BossModeResolver.Mode.VANILLA, VANILLA_ID, "The Tough One", true);
    }

    private static SpawnedBossResult mythic() {
        return SpawnedBossResult.success(BossModeResolver.Mode.MYTHICMOBS, MYTHIC_ID, "Infernal King", false);
    }

    @Test void vanillaModeSpawnsVanillaWithConfiguredNameAndBar() {
        SpawnedBossResult result = spawn("VANILLA", true, true, true, ConfiguredBossSpawnerTest::vanilla,
                ConfiguredBossSpawnerTest::mythic);
        assertAll(() -> assertTrue(result.success()),
                () -> assertEquals(BossModeResolver.Mode.VANILLA, result.actualMode()),
                () -> assertEquals(VANILLA_ID, result.entityUuid()),
                () -> assertEquals("The Tough One", result.displayName()),
                () -> assertTrue(result.vanillaBossBarCreated()),
                () -> assertFalse(result.fallbackUsed()));
    }

    @Test void vanillaModeNeverAttemptsMythic() {
        AtomicInteger calls = new AtomicInteger();
        spawn("VANILLA", true, true, true, ConfiguredBossSpawnerTest::vanilla,
                () -> { calls.incrementAndGet(); return mythic(); });
        assertEquals(0, calls.get());
    }

    @Test void mythicModeUsesDisplayUuidAndNoVanillaBar() {
        SpawnedBossResult result = spawn("MYTHICMOBS", true, true, true,
                ConfiguredBossSpawnerTest::vanilla, ConfiguredBossSpawnerTest::mythic);
        assertAll(() -> assertTrue(result.success()),
                () -> assertEquals(BossModeResolver.Mode.MYTHICMOBS, result.actualMode()),
                () -> assertEquals(MYTHIC_ID, result.entityUuid()),
                () -> assertEquals("Infernal King", result.displayName()),
                () -> assertFalse(result.vanillaBossBarCreated()),
                () -> assertFalse(result.fallbackUsed()));
    }

    @Test void successfulMythicNeverCreatesVanillaEntity() {
        AtomicInteger calls = new AtomicInteger();
        spawn("MYTHICMOBS", true, true, true,
                () -> { calls.incrementAndGet(); return vanilla(); }, ConfiguredBossSpawnerTest::mythic);
        assertEquals(0, calls.get());
    }

    @Test void failedMythicFallsBackOnceToVanillaWhenEnabled() {
        AtomicInteger calls = new AtomicInteger();
        SpawnedBossResult result = spawn("MYTHICMOBS", true, true, true,
                () -> { calls.incrementAndGet(); return vanilla(); }, SpawnedBossResult::failed);
        assertAll(() -> assertEquals(1, calls.get()),
                () -> assertTrue(result.success()),
                () -> assertEquals(BossModeResolver.Mode.VANILLA, result.actualMode()),
                () -> assertEquals("The Tough One", result.displayName()),
                () -> assertTrue(result.vanillaBossBarCreated()),
                () -> assertTrue(result.fallbackUsed()));
    }

    @Test void unavailableMythicFallsBackWithoutAttemptingMythic() {
        AtomicInteger calls = new AtomicInteger();
        SpawnedBossResult result = spawn("MYTHICMOBS", true, false, true,
                ConfiguredBossSpawnerTest::vanilla,
                () -> { calls.incrementAndGet(); return mythic(); });
        assertAll(() -> assertEquals(0, calls.get()), () -> assertTrue(result.fallbackUsed()));
    }

    @Test void disabledMythicIntegrationFallsBackWithoutAttemptingMythic() {
        AtomicInteger calls = new AtomicInteger();
        SpawnedBossResult result = spawn("MYTHICMOBS", false, true, true,
                ConfiguredBossSpawnerTest::vanilla,
                () -> { calls.incrementAndGet(); return mythic(); });
        assertAll(() -> assertEquals(0, calls.get()), () -> assertTrue(result.fallbackUsed()));
    }

    @Test void failedMythicWithoutFallbackSpawnsNothing() {
        AtomicInteger vanillaCalls = new AtomicInteger();
        SpawnedBossResult result = spawn("MYTHICMOBS", true, true, false,
                () -> { vanillaCalls.incrementAndGet(); return vanilla(); }, SpawnedBossResult::failed);
        assertAll(() -> assertFalse(result.success()),
                () -> assertEquals(SpawnedBossResult.Status.FAILED, result.status()),
                () -> assertNull(result.entityUuid()),
                () -> assertFalse(result.vanillaBossBarCreated()),
                () -> assertEquals(0, vanillaCalls.get()));
    }

    @Test void noneModeIsDisabledAndSpawnsNothing() { assertDisabled("NONE"); }
    @Test void disabledAliasIsDisabledAndSpawnsNothing() { assertDisabled("DISABLED"); }

    @Test void defaultModePreservesVanillaCompatibility() {
        assertEquals(BossModeResolver.Mode.VANILLA,
                spawn(null, false, false, true, ConfiguredBossSpawnerTest::vanilla,
                        ConfiguredBossSpawnerTest::mythic).actualMode());
    }

    @Test void failedFallbackDoesNotClaimSuccessOrBossBar() {
        SpawnedBossResult result = spawn("MYTHICMOBS", true, true, true,
                SpawnedBossResult::failed, SpawnedBossResult::failed);
        assertAll(() -> assertFalse(result.success()), () -> assertTrue(result.fallbackUsed()),
                () -> assertFalse(result.vanillaBossBarCreated()));
    }

    private static void assertDisabled(String mode) {
        AtomicInteger calls = new AtomicInteger();
        Supplier<SpawnedBossResult> forbidden = () -> { calls.incrementAndGet(); return vanilla(); };
        SpawnedBossResult result = spawn(mode, true, true, true, forbidden, forbidden);
        assertAll(() -> assertEquals(SpawnedBossResult.Status.DISABLED, result.status()),
                () -> assertEquals(BossModeResolver.Mode.NONE, result.actualMode()),
                () -> assertEquals(0, calls.get()), () -> assertFalse(result.vanillaBossBarCreated()));
    }

    private static SpawnedBossResult spawn(String mode, boolean enabled, boolean available, boolean fallback,
                                           Supplier<SpawnedBossResult> vanilla,
                                           Supplier<SpawnedBossResult> mythic) {
        return ConfiguredBossSpawner.spawn(mode, enabled, available, fallback, vanilla, mythic);
    }
}
