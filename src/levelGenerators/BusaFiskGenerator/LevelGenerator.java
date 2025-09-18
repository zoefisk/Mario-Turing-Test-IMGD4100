package levelGenerators.BusaFiskGenerator;

import java.util.Random;

import engine.core.MarioLevelGenerator;
import engine.core.MarioLevelModel;
import engine.core.MarioTimer;

public class LevelGenerator implements MarioLevelGenerator {

    LevelAesthetic levelAesthetic;
    int difficulty;

    private final static int STRAIGHT_ODDS = 0;
    private final static int HILL_ODDS = 1;
    private final static int PIPE_ODDS = 2;
    private final static int CANNON_ODDS = 3;
    private final static int JUMP_ODDS = 4;

    private int[] odds = new int[5];
    private int totalOdds;
    private Random random;


    public int buildChunk(MarioLevelModel level, int length, int levelLength){
        int result = random.nextInt(totalOdds);
        int chunkType = 0;

        for (int i = 0; i < odds.length; i++){
            if (odds[i] <= result){
                chunkType = i;
            }
        }

        switch (chunkType) {
            case STRAIGHT_ODDS:
                return buildStraight(level, length, levelLength, false);
            case JUMP_ODDS:
                return buildJump(level, length, levelLength);
            case PIPE_ODDS:
                return buildTubes(level, length, levelLength);
        }

        return 0;
    }

    public LevelGenerator() {
//        this.levelType = new LevelType("Overground", "map address");
    }

//    public LevelGenerator(LevelType levelType, int difficulty) {
//        this.levelType = levelType;
//    }

    @Override
    public String getGeneratedLevel(MarioLevelModel model, MarioTimer timer) {
        model.clearMap();




        // Use this once we figure out level type stuff
//        String levelName = levelType.getName();
//        switch (levelName) {
//            case "Overground":
//                // Generate overground level
//                break;
//            case "Underground":
//                // Generate underground level
//                break;
//            case "Castle":
//                // Generate castle level
//                break;
//            case "Sky":
//                // Generate sky level
//                break;
//            default:
//                break;
//        }

        return model.getMap();
    }

    private int buildJump(MarioLevelModel model, int xo, int maxLength) {
        int js = random.nextInt(4) + 2;
        int jl = random.nextInt(2) + 2;
        int length = js * 2 + jl;

        boolean hasStairs = random.nextInt(3) == 0;

        int floor = model.getHeight() - 1 - random.nextInt(4);
        for (int x = xo; x < xo + length; x++) {
            if (x < xo + js || x > xo + length - js - 1) {
                for (int y = 0; y < model.getHeight(); y++) {
                    if (y >= floor) {
                        model.setBlock(x, y, MarioLevelModel.GROUND);
                    } else if (hasStairs) {
                        if (x < xo + js) {
                            if (y >= floor - (x - xo) + 1) {
                                model.setBlock(x, y, MarioLevelModel.GROUND);
                            }
                        } else {
                            if (y >= floor - ((xo + length) - x) + 2) {
                                model.setBlock(x, y, MarioLevelModel.GROUND);
                            }
                        }
                    }
                }
            }
        }

        return length;
    }

    private int buildCannons(MarioLevelModel model, int xo, int maxLength) {
        int length = random.nextInt(10) + 2;
        if (length > maxLength)
            length = maxLength;

        int floor = model.getHeight() - 1 - random.nextInt(4);
        int xCannon = xo + 1 + random.nextInt(4);
        for (int x = xo; x < xo + length; x++) {
            if (x > xCannon) {
                xCannon += 2 + random.nextInt(4);
            }
            if (xCannon == xo + length - 1)
                xCannon += 10;
            int cannonHeight = floor - random.nextInt(4) - 1;

            for (int y = 0; y < model.getHeight(); y++) {
                if (y >= floor) {
                    model.setBlock(x, y, MarioLevelModel.GROUND);
                } else {
                    if (x == xCannon && y >= cannonHeight) {
                        model.setBlock(x, y, MarioLevelModel.BULLET_BILL);
                    }
                }
            }
        }

        return length;
    }

    private int buildHillStraight(MarioLevelModel model, int xo, int maxLength) {
        int length = random.nextInt(10) + 10;
        if (length > maxLength)
            length = maxLength;

        int floor = model.getHeight() - 1 - random.nextInt(4);
        for (int x = xo; x < xo + length; x++) {
            for (int y = 0; y < model.getHeight(); y++) {
                if (y >= floor) {
                    model.setBlock(x, y, MarioLevelModel.GROUND);
                }
            }
        }

        addEnemyLine(model, xo + 1, xo + length - 1, floor - 1);

        int h = floor;

        boolean keepGoing = true;

        boolean[] occupied = new boolean[length];
        while (keepGoing) {
            h = h - 2 - random.nextInt(3);

            if (h <= 0) {
                keepGoing = false;
            } else {
                int l = random.nextInt(5) + 3;
                int xxo = random.nextInt(length - l - 2) + xo + 1;

                if (occupied[xxo - xo] || occupied[xxo - xo + l] || occupied[xxo - xo - 1]
                        || occupied[xxo - xo + l + 1]) {
                    keepGoing = false;
                } else {
                    occupied[xxo - xo] = true;
                    occupied[xxo - xo + l] = true;
                    addEnemyLine(model, xxo, xxo + l, h - 1);
                    if (random.nextInt(4) == 0) {
                        decorate(model, xxo - 1, xxo + l + 1, h);
                        keepGoing = false;
                    }
                    for (int x = xxo; x < xxo + l; x++) {
                        for (int y = h; y < floor; y++) {
                            int yy = 9;
                            if (y == h)
                                yy = 8;
                            if (model.getBlock(x, y) == MarioLevelModel.EMPTY) {
                                if (yy == 8) {
                                    model.setBlock(x, y, MarioLevelModel.PLATFORM);
                                } else {
                                    model.setBlock(x, y, MarioLevelModel.PLATFORM_BACKGROUND);
                                }
                            }
                        }
                    }
                }
            }
        }

        return length;
    }

    private void addEnemyLine(MarioLevelModel model, int x0, int x1, int y) {
        char[] enemies = new char[]{MarioLevelModel.GOOMBA,
                MarioLevelModel.GREEN_KOOPA,
                MarioLevelModel.RED_KOOPA,
                MarioLevelModel.SPIKY};
        for (int x = x0; x < x1; x++) {
            if (random.nextInt(35) < difficulty + 1) {
                int type = random.nextInt(4);
                if (difficulty < 1) {
                    type = 0;
                } else if (difficulty < 3) {
                    type = 1 + random.nextInt(3);
                }
                model.setBlock(x, y, MarioLevelModel.getWingedEnemyVersion(enemies[type], random.nextInt(35) < difficulty));
            }
        }
    }

    private int buildTubes(MarioLevelModel model, int xo, int maxLength) {
        int length = random.nextInt(10) + 5;
        if (length > maxLength)
            length = maxLength;

        int floor = model.getHeight() - 1 - random.nextInt(4);
        int xTube = xo + 1 + random.nextInt(4);
        int tubeHeight = floor - random.nextInt(2) - 2;
        for (int x = xo; x < xo + length; x++) {
            if (x > xTube + 1) {
                xTube += 3 + random.nextInt(4);
                tubeHeight = floor - random.nextInt(2) - 2;
            }
            if (xTube >= xo + length - 2)
                xTube += 10;

            char tubeType = MarioLevelModel.PIPE;
            if (x == xTube && random.nextInt(11) < difficulty + 1) {
                tubeType = MarioLevelModel.PIPE_FLOWER;
            }

            for (int y = 0; y < model.getHeight(); y++) {
                if (y >= floor) {
                    model.setBlock(x, y, MarioLevelModel.GROUND);
                } else {
                    if ((x == xTube || x == xTube + 1) && y >= tubeHeight) {
                        model.setBlock(x, y, tubeType);
                    }
                }
            }
        }

        return length;
    }

    private int buildStraight(MarioLevelModel model, int xo, int maxLength, boolean safe) {
        int length = random.nextInt(10) + 2;
        if (safe)
            length = 10 + random.nextInt(5);
        if (length > maxLength)
            length = maxLength;

        int floor = model.getHeight() - 1 - random.nextInt(4);
        for (int x = xo; x < xo + length; x++) {
            for (int y = 0; y < model.getHeight(); y++) {
                if (y >= floor) {
                    model.setBlock(x, y, MarioLevelModel.GROUND);
                }
            }
        }

        if (!safe) {
            if (length > 5) {
                decorate(model, xo, xo + length, floor);
            }
        }

        return length;
    }

    private void decorate(MarioLevelModel model, int x0, int x1, int floor) {
        if (floor < 1)
            return;

        boolean rocks = true;
        addEnemyLine(model, x0 + 1, x1 - 1, floor - 1);

        int s = random.nextInt(4);
        int e = random.nextInt(4);

        if (floor - 2 > 0) {
            if ((x1 - 1 - e) - (x0 + 1 + s) > 1) {
                for (int x = x0 + 1 + s; x < x1 - 1 - e; x++) {
                    model.setBlock(x, floor - 2, MarioLevelModel.COIN);
                }
            }
        }

        s = random.nextInt(4);
        e = random.nextInt(4);

        if (floor - 4 > 0) {
            if ((x1 - 1 - e) - (x0 + 1 + s) > 2) {
                for (int x = x0 + 1 + s; x < x1 - 1 - e; x++) {
                    if (rocks) {
                        if (x != x0 + 1 && x != x1 - 2 && random.nextInt(3) == 0) {
                            if (random.nextInt(4) == 0) {
                                model.setBlock(x, floor - 4, MarioLevelModel.NORMAL_BRICK);
                            } else {
                                model.setBlock(x, floor - 4, MarioLevelModel.NORMAL_BRICK);
                            }
                        } else if (random.nextInt(4) == 0) {
                            if (random.nextInt(4) == 0) {
                                model.setBlock(x, floor - 4, MarioLevelModel.COIN);
                            } else {
                                model.setBlock(x, floor - 4, MarioLevelModel.COIN);
                            }
                        } else {
                            model.setBlock(x, floor - 4, MarioLevelModel.COIN);
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getGeneratorName() {
        return "BusaFiskGenerator";
    }

}
