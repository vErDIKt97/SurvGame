package com.khakimov.survgame.game;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDArrays;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import com.khakimov.survgame.game.component.Enemy;
import com.khakimov.survgame.game.component.GameElementLayer;
import com.khakimov.survgame.game.component.Hero;
import com.khakimov.survgame.rl.ActionSpace;
import com.khakimov.survgame.rl.LruReplayBuffer;
import com.khakimov.survgame.rl.ReplayBuffer;
import com.khakimov.survgame.rl.agent.RlAgent;
import com.khakimov.survgame.rl.env.RlEnv;
import com.khakimov.survgame.util.Constant;
import com.khakimov.survgame.util.GameUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

import static com.khakimov.survgame.ai.TrainHero.OBSERVE;
import static com.khakimov.survgame.util.Constant.*;


public class Survival extends Frame implements RlEnv {
    private static final Logger logger = LoggerFactory.getLogger(Survival.class);
    public static int trainStep = 0;
    public static int gameStep = 0;
    private static int gameState;
    public static final int GAME_START = 1;
    public static final int GAME_OVER = 2;
    private static float currentReward = 0.2f;
    private static boolean currentTerminal = false;
    private boolean withGraphics;
    private Enemy enemy;
    private Hero hero;
    private GameElementLayer gameElement;
    private final NDManager manager;
    private final ReplayBuffer replayBuffer;
    private BufferedImage currentImg;
    private NDList currentObservation;
    private ActionSpace actionSpace;
    private final Queue<NDArray> imgQueue = new ArrayDeque<>(4);
    private String trainState = "observe";

    public Survival(NDManager manager, int batchSize, int replayBufferSize, boolean withGraphics) {
        this(manager, new LruReplayBuffer(batchSize, replayBufferSize));
        this.withGraphics = withGraphics;
        if (this.withGraphics) {
            initFrame();
            this.setVisible(true);
        }
        actionSpace = new ActionSpace();
        actionSpace.add(new NDList(manager.create(DO_NOTHING)));
        actionSpace.add(new NDList(manager.create(MOVE_LEFT)));
        actionSpace.add(new NDList(manager.create(MOVE_UP)));
        actionSpace.add(new NDList(manager.create(MOVE_RIGHT)));
        actionSpace.add(new NDList(manager.create(MOVE_DOWN)));
        currentImg = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);
        currentObservation = createObservation(currentImg);
        gameElement = new GameElementLayer();
        hero = new Hero();
        setGameState(GAME_START);
    }

    public Survival(NDManager manager, ReplayBuffer replayBuffer) {
        this.manager = manager;
        this.replayBuffer = replayBuffer;
    }

    public static void setCurrentReward(float currentReward) {
        Survival.currentReward = currentReward;
    }

    public static void setCurrentTerminal(boolean currentTerminal) {
        Survival.currentTerminal = currentTerminal;
    }

    private void initFrame() {
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setTitle(GAME_TITLE);
        setLocation(FRAME_X, FRAME_Y);
        setResizable(false);
        setVisible(true);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    public NDList createObservation(BufferedImage currentImg) {
        NDArray observation = GameUtil.imgPreprocess(currentImg);
        if (imgQueue.isEmpty()) {
            for (int i = 0; i < 4; i++) {
                imgQueue.offer(observation);
            }
            return new NDList(NDArrays.stack(new NDList(observation, observation, observation, observation), 1));
        } else {
            imgQueue.remove();
            imgQueue.offer(observation);
            NDArray[] buf = new NDArray[4];
            int i = 0;
            for (NDArray nd : imgQueue) {
                buf[i++] = nd;
            }
            return new NDList(NDArrays.stack(new NDList(buf[0], buf[1], buf[2], buf[3]), 1));
        }
    }

    public static void setGameState(int gameState) {
        Survival.gameState = gameState;
    }

    @Override
    public void reset() {
        currentReward = 0.2f;
        currentTerminal = false;
    }

    @Override
    public NDList getObservation() {
        return currentObservation;
    }

    @Override
    public ActionSpace getActionSpace() {
        return this.actionSpace;
    }

    @Override
    public void step(NDList action, boolean training) {
        if (action.singletonOrThrow().getInt(1) == 1) {
            hero.goLeft();
        }
        if (action.singletonOrThrow().getInt(2) == 1) {
            hero.goUp();
        }
        if (action.singletonOrThrow().getInt(3) == 1) {
            hero.goRight();
        }
        if (action.singletonOrThrow().getInt(4) == 1) {
            hero.goDown();
        }
        stepFrame();
        if (this.withGraphics) {
            this.repaint();
            try {
                Thread.sleep(FPS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        NDList preObservation = currentObservation;
        currentObservation = createObservation(currentImg);

        SurvivalStep step = new SurvivalStep(manager.newSubManager(),
                preObservation, currentObservation, action, currentReward, currentTerminal);
        if (training) {
            replayBuffer.addStep(step);
        }
        logger.info("GAME_STEP " + gameStep +
                " / " + "TRAIN_STEP " + trainStep +
                " / " + getTrainState() +
                " / " + "ACTION " + (Arrays.toString(action.singletonOrThrow().toArray())) +
                " / " + "REWARD " + step.getReward().getFloat() +
                " / " + "SCORE " + getScore());
        if (gameState == GAME_OVER) {
            restartGame();
        }
    }

    private void restartGame() {
        setGameState(GAME_START);
        gameElement.reset();
        hero.reset();
    }

    private long getScore() {
        return this.hero.getCurrentScore();
    }

    private String getTrainState() {
        return this.trainState;
    }

    public void stepFrame() {
        Graphics bufG = currentImg.getGraphics();
        bufG.setColor(Constant.BG_COLOR);
        bufG.fillRect(0, 0, Constant.FRAME_WIDTH, Constant.FRAME_HEIGHT);
        hero.draw(bufG);
        gameElement.draw(bufG, hero);
    }

    @Override
    public void update(Graphics g) {
        g.drawImage(currentImg, 0, 0, null);
    }

    @Override
    public Step[] runEnvironment(RlAgent agent, boolean training) {
        Step[] batchSteps = new Step[0];
        reset();

        // run the game
        NDList action = agent.chooseAction(this, training);
        step(action, training);
        if (training) {
            batchSteps = this.getBatch();
        }
        if (gameStep % 5000 == 0) {
            this.closeStep();
        }
        if (gameStep <= OBSERVE) {
            trainState = "observe";
        } else {
            trainState = "explore";
        }
        gameStep++;
        return batchSteps;

    }

    private void closeStep() {
        replayBuffer.closeStep();
    }

    @Override
    public Step[] getBatch() {
        return replayBuffer.getBatch();
    }

    @Override
    public void close() {
        manager.close();
    }

    private class SurvivalStep implements RlEnv.Step {
        private final NDManager manager;
        private final NDList preObservation;
        private final NDList postObservation;
        private final NDList action;
        private final float reward;
        private final boolean terminal;


        private SurvivalStep(NDManager manager, NDList preObservation, NDList postObservation, NDList action, float reward, boolean terminal) {
            this.manager = manager;
            this.preObservation = preObservation;
            this.postObservation = postObservation;
            this.action = action;
            this.reward = reward;
            this.terminal = terminal;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NDList getPreObservation(NDManager manager) {
            preObservation.attach(manager);
            return preObservation;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NDList getPreObservation() {
            return preObservation;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NDList getPostObservation(NDManager manager) {
            postObservation.attach(manager);
            return postObservation;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NDList getPostObservation() {
            return postObservation;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public NDManager getManager() {
            return this.manager;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NDList getAction() {
            return action;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NDArray getReward() {
            return manager.create(reward);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isTerminal() {
            return terminal;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close() {
            this.manager.close();
        }

    }

}
