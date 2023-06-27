package com.khakimov.survgame.game.component;

import com.khakimov.survgame.util.Constant;
import com.khakimov.survgame.util.GameUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Enemy {

    private int x, y;
    static BufferedImage enemyImages;

    private int countLife = 3;

    boolean dead;

    static {
        enemyImages = GameUtil.loadBufferedImage(Constant.ENEMY_IMG_PATH);
        assert enemyImages != null;
    }

    public static final int ENEMY_WIDTH = enemyImages.getWidth();
    public static final int ENEMY_HEIGHT = enemyImages.getHeight();

    boolean visible;
    private final int velocity;
    Rectangle enemyCollisionRect;
    private int height;
    private final int width;

    public Enemy() {
        this.velocity = Constant.GAME_SPEED;
        this.width = ENEMY_WIDTH;
        enemyCollisionRect = new Rectangle();
        enemyCollisionRect.width = ENEMY_WIDTH;
    }

    public void draw(Graphics g, Hero hero, List<Enemy> enemies) {
        drawNormal(g);
        if (hero.isDead()) {
            return;
        }
        movement(hero, enemies);
//      //绘制碰撞矩形
//        g.setColor(Color.white);
//        g.drawRect((int) pipeRect.getX(), (int) pipeRect.getY(), (int) pipeRect.getWidth(), (int) pipeRect.getHeight());
    }

    private void movement(Hero hero, List<Enemy> enemies) {
        int heroX = hero.getXRectangle();
        int heroY = hero.getYRectangle();

        if (x < heroX) {
            Rectangle tempRect = new Rectangle(getEnemyCollisionRect().x + velocity,
                    getEnemyCollisionRect().y,
                    getEnemyCollisionRect().width,
                    getEnemyCollisionRect().height);
            for (Enemy enemy :
                    enemies) {
                if (!tempRect.intersects(enemy.getEnemyCollisionRect())) {
                    x += velocity;
                    enemyCollisionRect.x += velocity;
                }
            }
        } else {
            Rectangle tempRect = new Rectangle(getEnemyCollisionRect().x - velocity,
                    getEnemyCollisionRect().y,
                    getEnemyCollisionRect().width,
                    getEnemyCollisionRect().height);
            for (Enemy enemy :
                    enemies) {
                if (!tempRect.intersects(enemy.getEnemyCollisionRect())) {
                    x -= velocity;
                    enemyCollisionRect.x -= velocity;
                }
            }
        }
        if (y < heroY) {
            Rectangle tempRect = new Rectangle(getEnemyCollisionRect().x,
                    getEnemyCollisionRect().y + velocity,
                    getEnemyCollisionRect().width,
                    getEnemyCollisionRect().height);
            for (Enemy enemy :
                    enemies) {
                if (!tempRect.intersects(enemy.getEnemyCollisionRect())) {
                    y += velocity;
                    enemyCollisionRect.y += velocity;
                }
            }
        } else {
            Rectangle tempRect = new Rectangle(getEnemyCollisionRect().x,
                    getEnemyCollisionRect().y - velocity,
                    getEnemyCollisionRect().width,
                    getEnemyCollisionRect().height);
            for (Enemy enemy :
                    enemies) {
                if (!tempRect.intersects(enemy.getEnemyCollisionRect())) {
                    y -= velocity;
                    enemyCollisionRect.y -= velocity;
                }
            }
        }
    }

    private void drawNormal(Graphics g) {
        g.drawImage(enemyImages, x, y, null);
    }

    public boolean isVisible() {
        return visible;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Rectangle getEnemyCollisionRect() {
        return enemyCollisionRect;
    }

    public void setAttribute(int x, int y, boolean visible) {
        this.x = x;
        this.y = y;
        this.height = enemyImages.getHeight();
        this.visible = visible;
        setRectangle(this.x, this.y, this.height);
    }

    private void setRectangle(int x, int y, int height) {
        enemyCollisionRect.x = x;
        enemyCollisionRect.y = y;
        enemyCollisionRect.height = height;
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public void die(Hero hero) {
        this.setDead(true);
        ScoreCounter.getInstance().score(hero);
    }

    public void hit(Hero hero) {
        this.countLife -= 1;
        if (countLife == 0)
            die(hero);
    }

    static class EnemyPool {
        public static final int FULL_ENEMY = 4;
        private static final List<Enemy> pool = new ArrayList<>();
        public static final int MAX_ENEMY_COUNT = 1; // 对象池中对象的最大个数

        public static void giveBack(Enemy enemy) {
            if (pool.size() < MAX_ENEMY_COUNT) {
                pool.add(enemy);
            }
        }

        public static Enemy get() {
            int size = pool.size();
            if (size > 0) {
                return pool.remove(size - 1); // 移除并返回最后一个
            } else {
                return new Enemy(); // 空对象池，返回一个新对象
            }

        }
    }
}
