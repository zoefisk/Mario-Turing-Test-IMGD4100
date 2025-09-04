package agents.humanlike;

import engine.core.MarioAgent;
import engine.core.MarioForwardModel;
import engine.core.MarioTimer;
import engine.helper.MarioActions;

public class Agent implements MarioAgent {

    private boolean[] action = new boolean[MarioActions.numberOfActions()];

    private void runAway() {
        action[MarioActions.RIGHT.getValue()] = false;
        action[MarioActions.LEFT.getValue()] = true;
    }

    private boolean underQuestionBlock(MarioForwardModel model, byte[][] scene) {
        int marioX = model.obsGridWidth / 2;
        int marioY = model.obsGridHeight / 2;

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = 1; dy <= 3; dy++) {
                int checkX = marioX + dx;
                int checkY = marioY - dy;

                if (checkX < 0 || checkX >= model.obsGridWidth || checkY < 0) continue;

                if (scene[checkX][checkY] == MarioForwardModel.OBS_QUESTION_BLOCK) {
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
        action[MarioActions.RIGHT.getValue()] = true;
    }

    @Override
    public boolean[] getActions(MarioForwardModel model, MarioTimer timer) {
        byte[][] levelSceneFromBitmap = decode(model, model.getMarioSceneObservation()); // map of the scene
        byte[][] enemiesFromBitmap = decode(model, model.getMarioEnemiesObservation()); // map of enemies

        if (dangerFromEnemies(enemiesFromBitmap)) {
            runAway();
        }

//        if (underQuestionBlock(model, levelSceneFromBitmap) && model.mayMarioJump()) {
//            action[MarioActions.JUMP.getValue()] = true;
//        } else {
//            action[MarioActions.JUMP.getValue()] = false;
//        }

        return action;
    }

    @Override
    public String getAgentName() { return "HumanlikeAgent"; }
}


// if Mario detects an enemy, stop for 0.5-1 seconds to decide what to do.
    // jump on top of enemy and kill it
