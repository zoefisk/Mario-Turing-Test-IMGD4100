//package agents.humanlike;
//
//import engine.core.MarioAgent;
//import engine.core.MarioForwardModel;
//import engine.core.MarioTimer;
//import engine.helper.MarioActions;
//
//import static engine.core.MarioForwardModel.*;
//
//public class Agent implements MarioAgent {
//
//    private boolean[] action = new boolean[MarioActions.numberOfActions()];
//    private int jumpCount = 0; // counter to determine if you've done a 'full' jump yet
//
//    private void runAway() {
//        action[MarioActions.RIGHT.getValue()] = false;
//        action[MarioActions.LEFT.getValue()] = true;
//    }
//
//    private boolean dangerFromGaps(byte[][] levelSceneFromBitmap) {
//        for (int y = 9; y <= 10; y++) {
//            for (int x = 9; x <= 12; x++) {
//                if (levelSceneFromBitmap[x][y] == 0) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//
//    private boolean obstaclesIncoming(byte[][] levelSceneFromBitmap) {
//        for (int y = 8; y <= 8; y++) {
//            for (int x = 9; x <= 12; x++) {
//                if (levelSceneFromBitmap[x][y] == 1) {
//                    System.out.println("obstacle incoming");
//                    return true;
//                }
//            }
//        }
//
//        return false;
//    }
//
//    private boolean obstacleIncoming(MarioForwardModel model, byte[][] scene){
//
//        byte[][] levelSceneFromBitmap = decode(model, model.getMarioSceneObservation());
//        int[] marioTilePos = model.getMarioScreenTilePos();
//        int marioX = marioTilePos[0];
//        int marioY = marioTilePos[1];
//
//        for (int dx = 1; dx < 3; dx++) {
//            int checkX = marioX + dx;
//
//            int[][] levelScene = model.getMarioSceneObservation();
//            int object = levelSceneFromBitmap[checkX][marioY];
//            if (object == 2){
//                System.out.println("Pipe incoming");
//                return true;
//            }
//        }
//
//        return false;
//    }
//    //check if a question block is present
//    private boolean underQuestionBlock(MarioForwardModel model, byte[][] scene) {
//
//        int[] marioTilePos = model.getMarioScreenTilePos();
//        int marioX = marioTilePos[0];
//        int marioY = marioTilePos[1];
//        int maxDetect;
//
//        if (action[MarioActions.SPEED.getValue()] == false){
//            maxDetect = 1;
//        } else {
//            maxDetect = 2;
//        }
//
//        for (int dx = -1; dx <= maxDetect; dx++) {
//            for (int dy = 1; dy <= 18; dy++) {
//                int checkX = marioX + dx;
//                int checkY = marioY - dy;
//
//                if (checkX < 0 || checkX >= model.obsGridWidth || checkY < 0) continue;
//                int[][] levelScene = model.getMarioSceneObservation();
//                int Object = levelScene[checkX][checkY];
//                if ((Object == OBS_QUESTION_BLOCK)) {
//                    //System.out.println("Under question block");
//                    return true;
//                }
//            }
//        }
//
//        return false;
//    }
//
//    // stolen from glennHartmann
//    // check if an enemy is close
//    private boolean dangerFromEnemies(byte[][] enemiesFromBitmap) {
//        for (int y = 7; y <= 9; y++) {
//            for (int x = 8; x <= 12; x++) {
//                if (!(x == 8 && y == 8) && enemiesFromBitmap[x][y] == 1) {
//                    return true;
//                }
//            }
//        }
//
//        return false;
//    }
//
//    // stolen from glennHartmann agent
//    private byte[][] decode(MarioForwardModel model, int[][] state) {
//        byte[][] dstate = new byte[model.obsGridWidth][model.obsGridHeight];
//        for (int i = 0; i < dstate.length; ++i)
//            for (int j = 0; j < dstate[0].length; ++j)
//                dstate[i][j] = 2;
//
//        for (int x = 0; x < state.length; x++) {
//            for (int y = 0; y < state[x].length; y++) {
//                if (state[x][y] != 0) {
//                    dstate[x][y] = 1;
//                } else {
//                    dstate[x][y] = 0;
//                }
//            }
//        }
//        return dstate;
//    }
//
//    //stolen from Glenn Hartmann
//    //Checks if a solid tile is in front of you
//    private boolean block(byte[][] levelSceneFromBitmap) {
//        for (int y = 8; y <= 8; y++) {
//            for (int x = 9; x <= 12; x++) {
//                if (levelSceneFromBitmap[x][y] == 1) {
//                    return true;
//                }
//            }
//        }
//
//        return false;
//    }
//
//    @Override
//    public void initialize(MarioForwardModel model, MarioTimer timer) {
//        action = new boolean[MarioActions.numberOfActions()];
//
//        // Mario always starts walking to the right as the game starts.
//        action[MarioActions.RIGHT.getValue()] = true;
//        action[MarioActions.SPEED.getValue()] = false;
//
//        long startTime = timer.getRemainingTime();
//    }
//
//    @Override
//    public boolean[] getActions(MarioForwardModel model, MarioTimer timer) {
//        byte[][] levelSceneFromBitmap = decode(model, model.getMarioSceneObservation()); // map of the scene
//        byte[][] enemiesFromBitmap = decode(model, model.getMarioEnemiesObservation()); // map of enemies
//
//        //jump to hit a question block
//        if (underQuestionBlock(model, levelSceneFromBitmap) && model.mayMarioJump()) {
//            action[MarioActions.JUMP.getValue()] = true;
//        } else {
//            action[MarioActions.JUMP.getValue()] = false;
//        }
//
//        //Jump over obstacles
//        if (obstaclesIncoming(levelSceneFromBitmap) && model.mayMarioJump()){
//            action[MarioActions.JUMP.getValue()] = true;
//            action[MarioActions.RIGHT.getValue()] = true;
//        }
//
//        //Determine whether or not to run
//        if ((!dangerFromEnemies(enemiesFromBitmap) && !obstaclesIncoming(levelSceneFromBitmap))){
//            action[MarioActions.SPEED.getValue()] = true;
//        } else {
//            action[MarioActions.SPEED.getValue()] = false;
//        }
//
//        //ensures that jump is held
//        if(!model.isMarioOnGround()){
//            action[MarioActions.JUMP.getValue()] = true;
//        }
//
//        //moves over if blocked
//        if (block(levelSceneFromBitmap)){
//            action[MarioActions.RIGHT.getValue()] = false;
//            action[MarioActions.LEFT.getValue()] = true;
//        } else {
//            action[MarioActions.RIGHT.getValue()] = true;
//            action[MarioActions.LEFT.getValue()] = false;
//        }
//
//        //jump over gaps
//        if ((dangerFromGaps(levelSceneFromBitmap)) && model.mayMarioJump()){
//            action[MarioActions.JUMP.getValue()] = true;
//        }
//
//        return action;
//    }
//
//    @Override
//    public String getAgentName() { return "HumanlikeAgent"; }
//}
