package org.spectralmemories.bloodmoon.boss;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BossPolicyTest {
    @Test void vanillaBossWithKillerCanReward() {
        assertTrue(BossRewardPolicy.shouldReward(true, true, true, true, true));
    }

    @Test void vanillaBossWithoutRequiredKillerCannotReward() {
        assertFalse(BossRewardPolicy.shouldReward(true, true, false, true, true));
    }

    @Test void duplicateBossRewardIsRejected() {
        assertFalse(BossRewardPolicy.shouldReward(true, false, false, true, false));
    }

    @Test void absentMythicMobsFallsBackToVanilla() {
        assertEquals(BossModeResolver.Mode.VANILLA, BossModeResolver.resolve("MYTHICMOBS", true, false, true));
    }

    @Test void absentMythicMobsCanDisableBossWithoutCrashing() {
        assertEquals(BossModeResolver.Mode.NONE, BossModeResolver.resolve("MYTHICMOBS", true, false, false));
    }

    @Test void defaultModeRemainsVanilla() {
        assertEquals(BossModeResolver.Mode.VANILLA, BossModeResolver.resolve(null, false, false, true));
    }
}
