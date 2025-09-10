package agents.humanlike;

import engine.core.MarioForwardModel;

import static engine.core.MarioForwardModel.OBS_QUESTION_BLOCK;
import static engine.core.MarioForwardModel.OBS_USED_BLOCK;

public class MarioDetectionHelper {

    /**
     * Check if Mario is under a question block
     * @param model the Mario forward model
     * @param scene the scene observation
     * @return true if Mario is under a question block, false otherwise
     */
    public static boolean underQuestionBlock(MarioForwardModel model, byte[][] scene) {

        // Get Mario's position in tile coordinates
        int[] marioTilePos = model.getMarioScreenTilePos();
        int marioX = marioTilePos[0];
        int marioY = marioTilePos[1];

        // Check the tiles directly above Mario (including diagonals)
        for (int dx = 0; dx <= 2; dx++) {

            // Check up to 15 tiles above Mario
            for (int dy = 1; dy <= 8; dy++) {
                int checkX = marioX + dx;       // Check left, center, right
                int checkY = marioY - dy;       // Check above Mario

                // Ensure we don't go out of bounds
                if (checkX < 0 || checkX >= model.obsGridWidth || checkY < 0) continue;

                int[][] levelScene = model.getMarioSceneObservation(0);  // Get the level scene observation
                int Object = levelScene[checkX][checkY];                // Get the object at the checked position

                // If we hit a used block, stop checking upwards in this column
                if (Object == OBS_USED_BLOCK) {
//                    System.out.println("Used block above");
                    return false;
                }

                // If we hit a solid block, stop checking upwards in this column
                if (Object == OBS_QUESTION_BLOCK) {
//                    System.out.println("Under question block");
                    return true;
                }
            }
        }

        return false;   // No question block found above Mario
    }

    /**
     * Check if there is danger from enemies in front of Mario
     * @author This function is taken from the GlennHartmann agent
     * @param enemiesFromBitmap the enemies observation
     * @return true if there is danger from enemies, false otherwise
     */
    public static boolean dangerFromEnemies(byte[][] enemiesFromBitmap) {

        // Check vertically around Mario's foot level
        for (int y = 7; y <= 9; y++) {

            // Check horizontally in front of Mario
            for (int x = 4; x <= 15; x++) {

                // Return true if an enemy is detected in the danger zone
                if (!(x == 4 && y == 8) && enemiesFromBitmap[x][y] == 1) {
                    System.out.println("Danger from enemies");
                    return true;
                }
            }
        }

        return false; // No enemies detected in the danger zone
    }

    /**
     * Check if there is danger from gaps in front of Mario
     * @author This function is taken from the GlennHartmann agent
     * @param levelSceneFromBitmap the level scene observation
     * @return true if there is danger from gaps, false otherwise
     */
    public static boolean dangerFromGaps(byte[][] levelSceneFromBitmap) {
        for (int y = 9; y <= 10; y++) {
            for (int x = 9; x <= 12; x++) {
                if (levelSceneFromBitmap[x][y] == 0) {
                    System.out.println("Danger from gaps");
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean obstaclesIncoming(byte[][] levelSceneFromBitmap) {
    for (int y = 8; y <= 8; y++) {
        for (int x = 9; x <= 12; x++) {
            if (levelSceneFromBitmap[x][y] == 1) {
                return true;
            }
        }
    }

    return false;
}


    /**
     * Check if it is safe to jump
     * @author This function is taken from the GlennHartmann agent
     * @param levelSceneFromBitmap the level scene observation
     * @param enemiesFromBitmap the enemies observation
     * @return true if it is safe to jump, false otherwise
     */
    public static boolean safeToJump(byte[][] levelSceneFromBitmap, byte[][] enemiesFromBitmap) {

        if (safeToJumpFromGaps(levelSceneFromBitmap) && safeToJumpFromEnemies(enemiesFromBitmap)) {
//            System.out.println("Safe to jump");
            return true;
        }
        return false;
    }

    /**
     * Check if it is safe to jump from enemies
     * @author This function is taken from the GlennHartmann agent
     * @param enemiesFromBitmap the enemies observation
     * @return true if it is safe to jump from enemies, false otherwise
     */
    public static boolean safeToJumpFromEnemies(byte[][] enemiesFromBitmap) {
        for (int y = 5; y <= 9; y++) {
            for (int x = 11; x <= 14; x++) {
                if (!(x == 8 && y == 8) && enemiesFromBitmap[x][y] == 1) {
//                    System.out.println("Not safe to jump from enemies");
                    return false;
                }
            }
        }

//        System.out.println("Safe to jump from enemies");
        return true;
    }

    /**
     * Check if it is safe to jump from gaps
     * @author This function is taken from the GlennHartmann agent
     * @param levelSceneFromBitmap the level scene observation
     * @return true if it is safe to jump from gaps, false otherwise
     */
    public static boolean safeToJumpFromGaps(byte[][] levelSceneFromBitmap) {
        for (int y = 9; y <= 9; y++) {
            boolean b = false;
            for (int x = 11; x <= 14; x++) {
                if (levelSceneFromBitmap[x][y] == 1) {
                    b = true;
                    break;
                }
            }
            if (!b) {
                System.out.println("Not safe to jump from gaps");
                return false;
            }
        }

        System.out.println("Safe to jump from gaps");
        return true;
    }

    /**
     * Check if there is a block in front of Mario
     * @author This function is taken from the GlennHartmann agent
     * @param levelSceneFromBitmap the level scene observation
     * @return true if there is a block in front of Mario, false otherwise
     */
    public static boolean block(byte[][] levelSceneFromBitmap) {
        for (int y = 8; y <= 8; y++) {
            for (int x = 9; x <= 12; x++) {
                if (levelSceneFromBitmap[x][y] == 1) {
//                    System.out.println("Block in front");
                    return true;
                }
            }
        }
        return false;
    }

}
