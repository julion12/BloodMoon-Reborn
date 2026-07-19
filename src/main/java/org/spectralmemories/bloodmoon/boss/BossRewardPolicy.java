package org.spectralmemories.bloodmoon.boss;

public final class BossRewardPolicy {
    private BossRewardPolicy() { }
    public static boolean shouldReward(boolean enabled, boolean requireKiller, boolean killerPresent,
                                       boolean rewardOnce, boolean firstReward) {
        return enabled && (!requireKiller || killerPresent) && (!rewardOnce || firstReward);
    }
}
