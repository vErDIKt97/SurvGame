package com.khakimov.survgame.game.component;

import com.khakimov.survgame.game.Survival;

public class ScoreCounter {
    private static final ScoreCounter scoreCounter = new ScoreCounter();
    private long score;

    public static ScoreCounter getInstance() {
        return scoreCounter;
    }

    public void score(Hero hero) {
        if (!hero.isDead()) {
            Survival.setCurrentReward(1f);
            score += 1;
        }
    }

    public long getCurrentScore() {
        return score;
    }

    public void reset() {
        score = 0;
    }
}
