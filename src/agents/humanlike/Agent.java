package agents.humanlike;

import engine.core.MarioAgent;
import engine.core.MarioForwardModel;
import engine.core.MarioTimer;
import engine.helper.MarioActions;

import static agents.humanlike.MarioActionHelper.*;
import static agents.humanlike.MarioDetectionHelper.*;

public class Agent implements MarioAgent {

    private enum State { IDLE, PREPARING, STOMPING, BONKING, CLEARING, CHASING }
    private State state = State.IDLE;

    private enum BonkPhase { PAUSING, JUMPING, LANDING }
    private BonkPhase bonkPhase = BonkPhase.PAUSING;

    private boolean[] action = new boolean[MarioActions.numberOfActions()];
    private int jumpHoldFrames = 0;
    private static final int STOMP_JUMP_HOLD = 4;   // shorter jump
    private static final int OBSTACLE_JUMP_HOLD = 12; // longer jump
    private int bonkCounter = 0;      // used only inside BONKING state machine
    private int jumpCounter = 0;
    private int maxJumpHold = OBSTACLE_JUMP_HOLD;
    private long walkStartTime = -1;           // when we started walking left
    private static final long WALK_DURATION_MS = 2000; // 2 seconds



    private boolean prioritizeObstacles = true;

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
        Powerup powerup = powerupAbove(model, levelSceneFromBitmap);

        // State machine for Mario's behavior
        switch (state) {

            // Idle state: walk right, look for enemies or obstacles
            case IDLE:

                prioritizeObstacles = true;
                action[MarioActions.SPEED.getValue()] = false;

                // Check for block above to bonk
                if (underQuestionBlock(model, levelSceneFromBitmap) && model.mayMarioJump()) {
                    state = State.BONKING;
                    bonkPhase = BonkPhase.PAUSING;
                    bonkCounter = 0;
                    standStill(action);
                    break;
                }

               if ((obstaclesIncoming(levelSceneFromBitmap)) || block(levelSceneFromBitmap)){
                    state = State.CLEARING;
                    break;
                }

                // Check for enemies
                if (dangerFromEnemies(enemiesFromBitmap)) {
                    state = State.PREPARING;
                    walkStartTime = System.currentTimeMillis();
                    //walkLeft(action);
                    prioritizeObstacles = false;
                    break;
                }

                // Check for gaps
                if (dangerFromGaps(levelSceneFromBitmap)) {
                    System.out.println("Danger from gaps");
                    standStill(action);
                    state = State.CLEARING;
                    break;
                }

                // Check for power-ups
//                if (powerup == Powerup.MUSHROOM) {
//                    System.out.println("Mushroom detected above");
//                    standStill(action);
//                    state = State.CHASING;
//                    bonkCounter = 0;
//                    break;
//                } else if (powerup == Powerup.FIRE_FLOWER) {
//                    System.out.println("Fire flower detected above");
//                    standStill(action);
//                    state = State.CHASING; // This needs to be changed, because the fire flower doesn't move.
//                    bonkCounter = 0;
//                    break;
//                }

                // Continue walking right if nothing special
                walkRight(action);

                break;


            // Preparing to stomp: walk left for a short duration or until close to en
            case PREPARING:

//                long elapsed = System.currentTimeMillis() - walkStartTime;

                    if (model.mayMarioJump() || model.isMarioOnGround()) {
                        state = State.STOMPING;
                        jumpHoldFrames = 0;
                        action[MarioActions.JUMP.getValue()] = true;
                        walkRight(action);
                        maxJumpHold = STOMP_JUMP_HOLD;
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
                }
                break;

            // Clearing state: jump over gap or obstacle, wait to land
            case CLEARING:

                action[MarioActions.JUMP.getValue()] = true;
                runRight(action);

                if (jumpCounter < 15) {
                    System.out.println("Jumping to clear obstacle or gap!");
                    action[MarioActions.JUMP.getValue()] = true;
                    jumpCounter++;
                } else {
                    System.out.println("Max jump hold reached");
                    action[MarioActions.JUMP.getValue()] = false;
                    action[MarioActions.SPEED.getValue()] = false;
                    state = State.IDLE;
                    jumpCounter = 0;
                }

                walkRight(action);
                break;

            // Bonking to hit blocks state: pause, jump to hit block, wait to land
            case BONKING:
                switch (bonkPhase) {

                    // Pause briefly under the block
                    case PAUSING:
                        standStill(action);
                        action[MarioActions.JUMP.getValue()] = false;
                        bonkCounter++;

                        if (bonkCounter >= 5) {
                            bonkPhase = BonkPhase.JUMPING;
                            bonkCounter = 0;
                        }
                        break;

                    // Jump to hit the block
                    case JUMPING:
                        if (bonkCounter < 15) {
                            action[MarioActions.JUMP.getValue()] = true;
                            action[MarioActions.LEFT.getValue()] = false;
                            action[MarioActions.RIGHT.getValue()] = false;
                            bonkCounter++;
                        } else {
                            action[MarioActions.JUMP.getValue()] = false;
                            bonkPhase = BonkPhase.LANDING;
//                            bonkCounter = 0;
                        }
                        break;

                    // Wait to land back on the ground
                    case LANDING:
                        if (!model.isMarioOnGround()) {
                            action[MarioActions.JUMP.getValue()] = false;
                            standStill(action);
                        } else {
                            action[MarioActions.JUMP.getValue()] = false;

                            //                The following was us starting to figure out how to work on items, but it wasn't finalized.
//                            if (bonkCounter <= 20) {
//                                System.out.println("First checkpoint");
//                                walkRight(action);
//                                bonkPhase = BonkPhase.LANDING;
//                                bonkCounter++;
//                                break;
//                            } else if (bonkCounter <= 30) {
//                                System.out.println("Second checkpoint");
//                                standStill(action);
//                                bonkPhase = BonkPhase.LANDING;
//                                bonkCounter++;
//                                break;
//                            } else if (bonkCounter <= 52) {
//                                System.out.println("Third checkpoint");
//                                walkLeft(action);
//                                bonkPhase = BonkPhase.LANDING;
//                                bonkCounter++;
//                                break;
//                            } else if (bonkCounter <= 80) {
//                                System.out.println("Fourth checkpoint");
//                                standStill(action);
//                                bonkPhase = BonkPhase.LANDING;
//                                bonkCounter++;
//                                break;
//                            }

                            state = State.IDLE;
                            bonkPhase = BonkPhase.PAUSING;
                            bonkCounter = 0;
                            walkRight(action);
                        }
                        break;
                }
                break;


//                The following was us starting to figure out how to work on items, but it wasn't finalized.

//            case CHASING:
//                if (model.getMarioMode() == 0) {
//                    standStill(action);
//                } else if (model.getMarioMode() == 1) {
//                    state = State.IDLE;
//                }
//
//                break;

            // Items state machine
        }

        return action;
    }

    @Override
    public String getAgentName() { return "HumanlikeAgent"; }
}
