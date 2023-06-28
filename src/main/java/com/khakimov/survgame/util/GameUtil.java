package com.khakimov.survgame.util;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import com.khakimov.survgame.game.component.Enemy;
import com.khakimov.survgame.game.component.Hero;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import static com.khakimov.survgame.game.component.Enemy.ENEMY_HEIGHT;
import static com.khakimov.survgame.game.component.Enemy.ENEMY_WIDTH;

/**
 * 工具类，游戏中用到的工具都在此类
 *
 * @author Kingyu
 */
public class GameUtil {

    private GameUtil() {
    }

    /**
     * 装载图片
     *
     * @param imgPath 图片路径
     * @return 图片资源
     */
    public static BufferedImage loadBufferedImage(String imgPath) {
        try {
            return ImageIO.read(new FileInputStream(imgPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 返回指定区间的一个随机数
     *
     * @param min     区间最小值，包含
     * @param max     区间最大值，不包含
     * @param enemies
     * @return 该区间的随机数
     */
    public static Rectangle getRandomNotCollideRectangleEnemy(int min, int max, List<Enemy> enemies, Hero hero) {
        Rectangle temp = getRandomEnemyRectangle(min, max);
        boolean collide = true;
        while (collide) {
            temp = getRandomEnemyRectangle(min, max);
            collide = false;
            for (Enemy enemy :
                    enemies) {
                if (temp.intersects(enemy.getEnemyCollisionRect()) || temp.intersects(hero.getHeroAttackRect()))
                    collide = true;
            }
        }
        return temp;
    }

    private static Rectangle getRandomEnemyRectangle(int min, int max) {
        int X = (int) (Math.random() * (max - min) + min);
        int Y = (int) (Math.random() * (max - min) + min);
        Rectangle tempRec = new Rectangle(X, Y, ENEMY_WIDTH, ENEMY_HEIGHT);
        return tempRec;
    }

    /**
     * Image preprocess
     *
     * @param observation input BufferedImage
     * @return NDArray:Shape(80,80,1)
     */
    public static NDArray imgPreprocess(BufferedImage observation) {
        return NDImageUtils.toTensor(
                NDImageUtils.resize(
                        ImageFactory.getInstance().fromImage(observation).toNDArray(NDManager.newBaseManager(), Image.Flag.GRAYSCALE)
                        , 80, 80));
    }
}