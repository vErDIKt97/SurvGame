package com.khakimov.survgame.game.component;

import com.khakimov.survgame.game.Survival;
import com.khakimov.survgame.util.Constant;
import com.khakimov.survgame.util.GameUtil;

import java.awt.*;
import java.awt.image.BufferedImage;

import static com.khakimov.survgame.util.Constant.GAME_SPEED;
import static com.khakimov.survgame.util.Constant.WINDOW_BAR_HEIGHT;

public class Hero {

    boolean dead;
    private int x, y;
    private int heroState;
    public static final int HERO_READY = 0;
    public static final int HERO_DEAD = 1;
    private final Rectangle heroCollisionRect;
    public static final int RECT_DESCALE = 2; // Параметры компенсации ширины и высоты прямоугольника столкновения
    private final ScoreCounter scoreCounter;
    static BufferedImage heroImages;
    public static final int HERO_WIDTH;
    public static final int HERO_HEIGHT;

    static {
        heroImages = GameUtil.loadBufferedImage(Constant.HERO_IMG_PATH);
        assert heroImages != null;
        HERO_WIDTH = heroImages.getWidth();
        HERO_HEIGHT = heroImages.getHeight();
    }

    private final int radAttack = 20;

    private final Rectangle heroAttackRectangle;

    public Hero() {
        scoreCounter = ScoreCounter.getInstance();
        x = Constant.FRAME_WIDTH / 2;
        y = Constant.FRAME_HEIGHT / 2;

        int rectX = x - (HERO_WIDTH >> 1);
        int rectY = y - (HERO_HEIGHT >> 1);
        heroCollisionRect = new Rectangle(rectX, rectY, HERO_WIDTH,
                HERO_HEIGHT); // 碰撞矩形的坐标与小鸟相同
        heroAttackRectangle = new Rectangle(rectX - radAttack, rectY - radAttack, HERO_WIDTH + radAttack * 2, HERO_HEIGHT + radAttack * 2);
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
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

    public void drawHeroImg(Graphics g) {
        movement();
        g.drawImage(heroImages, x - (HERO_WIDTH >> 1), y - (HERO_HEIGHT >> 1), Color.white, null);
    }

    private void movement() {
        if ((heroCollisionRect.y < GameElementLayer.MAX_HEIGHT - HERO_HEIGHT * 2 && heroCollisionRect.y > GameElementLayer.MIN_HEIGHT + WINDOW_BAR_HEIGHT) &&
                (heroCollisionRect.x < GameElementLayer.MAX_HEIGHT - HERO_HEIGHT * 2 && heroCollisionRect.x > GameElementLayer.MIN_HEIGHT)) {

        } else
            die();

    }

    public Rectangle getHeroCollisionRect() {
        return heroCollisionRect;
    }

    public void die() {
        Survival.setCurrentReward(-1f);
        Survival.setCurrentTerminal(true);
        Survival.setGameState(Survival.GAME_OVER);
        heroState = HERO_DEAD;
    }

    public void goLeft() {
        this.x -= GAME_SPEED;
        this.heroCollisionRect.x -= GAME_SPEED;
        this.heroAttackRectangle.x -= GAME_SPEED;
    }

    public void goUp() {
        this.y += GAME_SPEED;
        this.heroCollisionRect.y += GAME_SPEED;
        this.heroAttackRectangle.y += GAME_SPEED;
    }

    public void goRight() {
        this.x += GAME_SPEED;
        this.heroCollisionRect.x += GAME_SPEED;
        this.heroAttackRectangle.x += GAME_SPEED;
    }

    public void goDown() {
        this.y -= GAME_SPEED;
        this.heroCollisionRect.y -= GAME_SPEED;
        this.heroAttackRectangle.y -= GAME_SPEED;
    }

    public void draw(Graphics g) {
        drawHeroImg(g);
        g.setColor(Color.white);
        g.drawRect((int) heroCollisionRect.getX(), (int) heroCollisionRect.getY(), (int) heroCollisionRect.getWidth(), (int) heroCollisionRect.getHeight());
        g.drawRect((int) heroAttackRectangle.getX(), (int) heroAttackRectangle.getY(), (int) heroAttackRectangle.getWidth(), (int) heroAttackRectangle.getHeight());
        //g.drawLine(1,WINDOW_BAR_HEIGHT,GameElementLayer.MAX_HEIGHT- HERO_HEIGHT * 2,GameElementLayer.MAX_HEIGHT);
    }

    public long getCurrentScore() {
        return scoreCounter.getCurrentScore();
    }

    public void reset() {
        heroState = HERO_READY;
        x = Constant.FRAME_WIDTH / 2;
        y = Constant.FRAME_HEIGHT / 2;
        heroCollisionRect.y = y - (HERO_WIDTH >> 1);
        heroCollisionRect.x = x - (HERO_WIDTH >> 1);
        heroAttackRectangle.x = x - (HERO_WIDTH >> 1) - radAttack;
        heroAttackRectangle.y = y - (HERO_WIDTH >> 1) - radAttack;
        scoreCounter.reset();
    }

    public int getXRectangle() {
        return heroCollisionRect.x;
    }

    public int getYRectangle() {
        return heroCollisionRect.y;
    }

    public Rectangle getHeroAttackRect() {
        return heroAttackRectangle;
    }
}
