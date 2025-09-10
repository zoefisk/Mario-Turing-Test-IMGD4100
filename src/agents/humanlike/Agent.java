package agents.humanlike;

import engine.core.MarioAgent;
import engine.core.MarioForwardModel;
import engine.core.MarioTimer;
import engine.helper.MarioActions;

import static agents.humanlike.MarioActionHelper.*;
import static agents.humanlike.MarioDetectionHelper.*;
import static engine.core.MarioForwardModel.*;

public class Agent implements MarioAgent {

    private enum State { IDLE, PREPARING, STOMPING, BONKING, CLEARING }
    private State state = State.IDLE;

    private enum BonkPhase { PAUSING, JUMPING, LANDING }
    private BonkPhase bonkPhase = BonkPhase.PAUSING;

    private boolean[] action = new boolean[MarioActions.numberOfActions()];
    private int jumpHoldFrames = 0;
    private static final int STOMP_JUMP_HOLD = 4;   // shorter jump
    private static final int OBSTACLE_JUMP_HOLD = 12; // longer jump
    private int bonkCounter = 0;      // used only inside BONKING state machine
    private int maxJumpHold = OBSTACLE_JUMP_HOLD;
    private long walkStartTime = -1;           // when we started walking left
    private static final long WALK_DURATION_MS = 2000; // 2 seconds

    /**
     * Decode the observation from int[][] to byte[][] for easier processing.
     * @author Glenn Hartmann agent
     * @param model the Mario forward model
     * @param state the observation to decode
     * @return the decoded observation
     */
    private byte[][] decode(MarioForwardModel model, int[][] state) {
        byte[][] dstate = new byte[model.obsGridWidth][model.obsGridHeight];
        for (int i = 0; i < dstate.length; ++i)
            for (int j = 0; j < dstate[0].length; ++j)
                dstate[i][j] = 2;

        for (int x = 0; x < state.length; x++) {
            for (int y = 0; y < state[x].length; y++) {
                if (state[x][y] != 0) {
                    dstate[x][y] = 1;
                } else {
                    dstate[x][y] = 0;
                }
            }
        }
        return dstate;
    }

    @Override
    public void initialize(MarioForwardModel model, MarioTimer timer) {
        action = new boolean[MarioActions.numberOfActions()];
        walkRight(action);
    }

    @Override
    public boolean[] getActions(MarioForwardModel model, MarioTimer timer) {
        byte[][] levelSceneFromBitmap = decode(model, model.getMarioSceneObservation()); // map of the scene
        byte[][] enemiesFromBitmap = decode(model, model.getMarioEnemiesObservation()); // map of enemies

        // State machine for Mario's behavior
        switch (state) {

            // Idle state: walk right, look for enemies or obstacles
            case IDLE:

                // Check for block above to bonk
                if (underQuestionBlock(model, levelSceneFromBitmap) && model.mayMarioJump()) {
                    state = State.BONKING;
                    bonkPhase = BonkPhase.PAUSING;
                    bonkCounter = 0;
                    standStill(action);
                    System.out.println("Detected block above, preparing bonk...");
                    break;
                }

                // Check for enemies
                else if (dangerFromEnemies(enemiesFromBitmap)) {
                    state = State.PREPARING;
                    walkStartTime = System.currentTimeMillis();
                    walkLeft(action);
                    System.out.println("Enemy spotted, prepping stomp...");
                }

                // Check for gaps or obstacles
                else if (dangerFromGaps(levelSceneFromBitmap) || block(levelSceneFromBitmap)) {
                    state = State.CLEARING;
                    jumpHoldFrames = 0;
                    action[MarioActions.JUMP.getValue()] = true;
                    walkRight(action);
                    maxJumpHold = OBSTACLE_JUMP_HOLD;
                    System.out.println("Gap/obstacle detected! Clearing jump.");
                }

                // Continue walking right if nothing special
                else {
                    walkRight(action);
                }

                break;

            // Preparing to stomp: walk left for a short duration or until close to en
            // This should be changed....
            case PREPARING:
                long elapsed = System.currentTimeMillis() - walkStartTime;
                if (elapsed > WALK_DURATION_MS || enemyAheadClose(enemiesFromBitmap)) {
                    if (model.mayMarioJump() || model.isMarioOnGround()) {
                        state = State.STOMPING;
                        jumpHoldFrames = 0;
                        action[MarioActions.JUMP.getValue()] = true;
                        walkRight(action);
                        maxJumpHold = STOMP_JUMP_HOLD;
                        System.out.println("Jumping to stomp enemy!");
                    }
                } else {
                    walkLeft(action);
                }
                break;

            // Stomping on enemies state: jump and move right, wait to land
            case STOMPING:
                if (!model.isMarioOnGround()) {
                    if (jumpHoldFrames < STOMP_JUMP_HOLD) {
                        action[MarioActions.JUMP.getValue()] = true;
                        jumpHoldFrames++;
                    } else {
                        action[MarioActions.JUMP.getValue()] = false;
                    }
                    walkRight(action);
                } else {
                    state = State.IDLE;
                    System.out.println("Landed stomp, back to idle.");
                }
                break;

            // Clearing state: jump over gap or obstacle, wait to land
            case CLEARING:
                if (!model.isMarioOnGround()) {
                    if (jumpHoldFrames < OBSTACLE_JUMP_HOLD) {
                        action[MarioActions.JUMP.getValue()] = true;
                        jumpHoldFrames++;
                    } else {
                        action[MarioActions.JUMP.getValue()] = false;
                    }
                    walkRight(action);
                } else {
                    state = State.IDLE;
                    System.out.println("Landed clearing jump, back to idle.");
                }
                break;

            // Bonking to hit blocks state: pause, jump to hit block, wait to land
            case BONKING:
                switch (bonkPhase) {

                    // Pause briefly under the block
                    case PAUSING:
                        standStill(action);
                        action[MarioActions.JUMP.getValue()] = false;
                        bonkCounter++;

                        if (bonkCounter >= 15) {
                            bonkPhase = BonkPhase.JUMPING;
                            bonkCounter = 0;
                            System.out.println("Pause finished, starting jump...");
                        } else {
                            System.out.println("Standing still under block... " + bonkCounter);
                        }
                        break;

                    // Jump to hit the block
                    case JUMPING:
                        if (bonkCounter < 15) {
                            action[MarioActions.JUMP.getValue()] = true;
                            action[MarioActions.LEFT.getValue()] = false;
                            action[MarioActions.RIGHT.getValue()] = false;
                            bonkCounter++;
                            System.out.println("Bonking jump... " + bonkCounter);
                        } else {
                            action[MarioActions.JUMP.getValue()] = false;
                            bonkPhase = BonkPhase.LANDING;
                            System.out.println("Jump finished, waiting to land...");
                        }
                        break;

                    // Wait to land back on the ground
                    case LANDING:
                        if (!model.isMarioOnGround()) {
                            action[MarioActions.JUMP.getValue()] = false;
                            standStill(action);
                        } else {
                            action[MarioActions.JUMP.getValue()] = false;
                            state = State.IDLE;
                            bonkPhase = BonkPhase.PAUSING;
                            bonkCounter = 0;
                            walkRight(action);
                            System.out.println("Finished bonking, back to idle.");
                        }
                        break;
                }
                break;

        }

        return action;
    }

    @Override
    public String getAgentName() { return "HumanlikeAgent"; }
}
