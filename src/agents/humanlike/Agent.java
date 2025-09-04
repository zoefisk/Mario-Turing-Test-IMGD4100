package agents.humanlike;

import engine.core.MarioAgent;
import engine.core.MarioForwardModel;
import engine.core.MarioTimer;
import engine.helper.MarioActions;

import java.nio.charset.StandardCharsets;

import static engine.core.MarioForwardModel.*;

public class Agent implements MarioAgent {

    private boolean[] action = new boolean[MarioActions.numberOfActions()];
    private int jumpCount = 0; // counter to determine if you've done a 'full' jump yet

    private void runAway() {
        action[MarioActions.RIGHT.getValue()] = false;
        action[MarioActions.LEFT.getValue()] = true;
    }



    private boolean underQuestionBlock(MarioForwardModel model, byte[][] scene) {

        int[] marioTilePos = model.getMarioScreenTilePos();
        int marioX = marioTilePos[0];
        int marioY = marioTilePos[1];

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 1; dy <= 15; dy++) {
                int checkX = marioX + dx;
                int checkY = marioY - dy;

                if (checkX < 0 || checkX >= model.obsGridWidth || checkY < 0) continue;
                int[][] levelScene = model.getMarioSceneObservation();
                int Object = levelScene[checkX][checkY];
                if ((Object == OBS_QUESTION_BLOCK)) {
                    System.out.println("Under question block");
                    return true;
                }
            }
        }

        return false;
    }

    // stolen from glennHartmann
    private boolean dangerFromEnemies(byte[][] enemiesFromBitmap) {
        for (int y = 7; y <= 9; y++) {
            for (int x = 8; x <= 12; x++) {
                if (!(x == 8 && y == 8) && enemiesFromBitmap[x][y] == 1) {
                    return true;
                }
            }
        }

        return false;
    }

    // stolen from glennHartmann agent
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

        // Mario always starts walking to the right as the game starts.
        action[MarioActions.RIGHT.getValue()] = true;
        action[MarioActions.SPEED.getValue()] = false;
    }

    @Override
    public boolean[] getActions(MarioForwardModel model, MarioTimer timer) {
        byte[][] levelSceneFromBitmap = decode(model, model.getMarioSceneObservation()); // map of the scene
        byte[][] enemiesFromBitmap = decode(model, model.getMarioEnemiesObservation()); // map of enemies

//        if (dangerFromEnemies(enemiesFromBitmap)) {
//            runAway();
//        }

//        // if jump is active and jumpCount is too big, deactivate - jump is over and
//        // you'll need to get ready for next one
//        if (action[MarioActions.JUMP.getValue()] && jumpCount >= 8) {
//            action[MarioActions.JUMP.getValue()] = false;
//            jumpCount = 0;
//        }
//        // otherwise you're in the middle of jump, increment jumpCount
//        else if (action[MarioActions.JUMP.getValue()]) {
//            jumpCount++;
//        }

        if (underQuestionBlock(model, levelSceneFromBitmap) && model.mayMarioJump()) {
            action[MarioActions.JUMP.getValue()] = true;

        } else {
            action[MarioActions.JUMP.getValue()] = false;
        }


        if(!model.isMarioOnGround()){
            action[MarioActions.JUMP.getValue()] = true;
        }

        return action;
    }

    @Override
    public String getAgentName() { return "HumanlikeAgent"; }
}
