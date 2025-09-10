package agents.humanlike;

import engine.helper.MarioActions;

public class MarioActionHelper {

    /**
     * Make Mario walk left
     * @param action the action array to modify
     */
    public static void walkLeft(boolean[] action) {
        action[MarioActions.RIGHT.getValue()] = false;
        action[MarioActions.LEFT.getValue()] = true;
        action[MarioActions.SPEED.getValue()] = false;
    }

    /**
     * Make Mario walk right
     * @param action the action array to modify
     */
    public static void walkRight(boolean[] action) {
        action[MarioActions.RIGHT.getValue()] = true;
        action[MarioActions.LEFT.getValue()] = false;
    }

    /**
     * Make Mario run left
     * @param action the action array to modify
     */
    public static void runLeft(boolean[] action) {
        walkLeft(action);
        action[MarioActions.SPEED.getValue()] = true;
    }

    /**
     * Make Mario run right
     * @param action the action array to modify
     */
    public static void runRight(boolean[] action) {
        walkRight(action);
        action[MarioActions.SPEED.getValue()] = true;
    }

    /**
     * Make Mario stand still
     * @param action the action array to modify
     */
    public static void standStill(boolean[] action) {
        action[MarioActions.RIGHT.getValue()] = false;
        action[MarioActions.LEFT.getValue()] = false;
        action[MarioActions.SPEED.getValue()] = false;
        action[MarioActions.JUMP.getValue()] = false;
    }

    // Function for quick jump?
    // Function for longer jump?
}
