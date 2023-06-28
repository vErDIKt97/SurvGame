package com.khakimov.survgame.game.component;

import com.khakimov.survgame.game.component.Enemy.EnemyPool;
import com.khakimov.survgame.util.Constant;
import com.khakimov.survgame.util.GameUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GameElementLayer {
    private final List<Enemy> enemies;
    public static final int VERTICAL_INTERVAL = Constant.FRAME_HEIGHT >> 2;
    public static final int MIN_HEIGHT = 0;
    public static final int MAX_HEIGHT = Constant.FRAME_HEIGHT;

    public GameElementLayer() {
        this.enemies = new ArrayList<>();
    }

    public void draw(Graphics g, Hero hero) {
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            if (enemy.isVisible()) {
                enemy.draw(g, hero,enemies);
            } else {
                Enemy remove = enemies.remove(i);
                EnemyPool.giveBack(remove);
                i--;
            }
        }
        hero.drawHeroImg(g);
        isCollideEnemy(hero);
        isCollideHero(hero);
        generateEnemy(hero);
    }

    private void isCollideEnemy(Hero hero) {
        if (hero.isDead())
            return;
        for (Enemy enemy :
                enemies) {
            if (!enemy.isDead()){
                if (enemy.getEnemyCollisionRect().intersects(hero.getHeroAttackRect())) {
                    enemy.hit(hero);
                    if (enemy.isDead())
                        enemy.die(hero);
                    return;
                }

            }
        }
    }

    private void generateEnemy(Hero hero) {
        if (hero.isDead()) {
            return;
        }
        enemies.removeIf(Enemy::isDead);
        if (enemies.size() == 0 || enemies.size() <= EnemyPool.MAX_ENEMY_COUNT) {
            // 若容器为空，则添加一对水管
            Rectangle tempEnemyRec = GameUtil.getRandomNotCollideRectangleEnemy(MIN_HEIGHT, MAX_HEIGHT + 1, enemies, hero); // 随机生成水管高度
            Enemy enemy = EnemyPool.get();
            enemy.setAttribute(tempEnemyRec.x, tempEnemyRec.y, true);
            enemies.add(enemy);
        }
    }

    private void isCollideHero(Hero hero) {
        if (hero.isDead()) {
            return;
        }
        for (Enemy enemy : enemies) {
            if (enemy.getEnemyCollisionRect().intersects(hero.getHeroCollisionRect())) {
                hero.die();
                return;
            }
        }
    }

    public void reset() {
        for (Enemy enemy : enemies) {
            EnemyPool.giveBack(enemy);
        }
        enemies.clear();
    }

}
