package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;
import org.javatuples.Pair;
import static java.lang.Math.max;

public class Bot {

    private static final int visibility = 20;
    private static final int nullBlocks = 999;
    private static final int maxBoostSpeed = 15;
    private List<Command> directionList = new ArrayList<>();

    private final static Command OIL = new OilCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command NOTHING = new DoNothingCommand();
    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command DECELERATE = new DecelerateCommand();

    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    private List<Integer> speedState = new ArrayList<Integer>(Arrays.asList(0, 3, 5, 6, 8, 9, 15));

    public Bot() {
        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }

    public Command run(GameState gameState) {
        Car myCar = gameState.player;

        // initialize car & game state
        int damage = myCar.damage;
        int curLane = myCar.position.lane;
        int curBlock = myCar.position.block;

        // initialize current, lower, and higher speed (edge case result: -1)
        int curSpeed = myCar.speed;
        int lowerSpeed = getNearbySpeed(curSpeed, -1);
        int higherSpeed = getNearbySpeed(curSpeed, 1);

        // initialize possible lanes (current, left, right)
        List<Object> blocks = getBlocksInFront(curLane, curBlock, 1, gameState);
        List<Object> leftblocks = getBlocksInFront(curLane, curBlock, 2, gameState);
        List<Object> rightblocks = getBlocksInFront(curLane, curBlock, 0, gameState);

        // offensive command
        Command offCommand = offensiveSearch(gameState);

        // fix car (maintain max speed above 6)
        if ((curBlock + higherSpeed >= 1499)) {
            return ACCELERATE;
        }

        if (damage >= 2) {
            return FIX;
        }

        if (curSpeed == 0) {
            return ACCELERATE;
        }

        // check total power ups of each route
        int frontPowerUps, leftPowerUps, rightPowerUps;
        int visfrontPowerUps, visleftPowerUps, visrightPowerUps;
        frontPowerUps = countPowerUps(blocks, curSpeed + 1);
        visfrontPowerUps = countPowerUps(blocks, visibility);
        if (!leftblocks.isEmpty()) {
            leftPowerUps = countPowerUps(leftblocks, curSpeed);
            visleftPowerUps = countPowerUps(leftblocks, visibility);
        } else {
            leftPowerUps = -1;
            visleftPowerUps = -1;
        }
        if (!rightblocks.isEmpty()) {
            rightPowerUps = countPowerUps(rightblocks, curSpeed);
            visrightPowerUps = countPowerUps(rightblocks, visibility);
        } else {
            rightPowerUps = -1;
            visrightPowerUps = -1;
        }

        // check total damage of each route
        int curdamage, leftdamage, rightdamage;
        int visfrontdamage, visleftdamage, visrightdamage;
        curdamage = countTotalDamage(blocks, curSpeed + 1);
        visfrontdamage = countTotalDamage(blocks, visibility);
        if (leftblocks.isEmpty()) {
            leftdamage = nullBlocks;
            visleftdamage = nullBlocks;
        } else {
            leftdamage = countTotalDamage(leftblocks, curSpeed);
            visleftdamage = countTotalDamage(leftblocks, visibility);
        }
        if (rightblocks.isEmpty()) {
            rightdamage = nullBlocks;
            visrightdamage = nullBlocks;
        } else {
            rightdamage = countTotalDamage(rightblocks, curSpeed);
            visrightdamage = countTotalDamage(rightblocks, visibility);
        }

        double frontWeight, leftWeight, rightWeight;
        double visfrontWeight, visleftWeight, visrightWeight;
        if (curdamage == 0) {
            frontWeight = frontPowerUps / 0.1;
            visfrontWeight = visfrontPowerUps / 0.1;
        } else {
            frontWeight = frontPowerUps / curdamage;
            visfrontWeight = visfrontPowerUps / visfrontdamage;
        }
        if (leftdamage == 0 || leftdamage == nullBlocks) {
            leftWeight = leftPowerUps / 0.1;
            visleftWeight = visleftPowerUps / 0.1;
        } else {
            leftWeight = leftPowerUps / leftdamage;
            visleftWeight = visleftPowerUps / visleftdamage;
        }
        if (rightdamage == 0 || rightdamage == nullBlocks) {
            rightWeight = rightPowerUps / 0.1;
            visrightWeight = visrightPowerUps / 0.1;
        } else {
            rightWeight = rightPowerUps / rightdamage;
            visrightWeight = visrightPowerUps / visrightdamage;
        }

        // compare damage of each route
        int[] check = { curdamage, leftdamage, rightdamage };
        Arrays.sort(check);
        int lessdamage = check[0];

        // compare damage of vis route
        // int vischeck = {visfrontDa}

        // compare weight of each route
        double[] compare = { frontWeight, leftWeight, rightWeight };
        Arrays.sort(compare);
        double bestRoute = compare[2];

        double[] viscompare = { visfrontWeight, visleftWeight, visrightWeight };
        Arrays.sort(viscompare);
        double visBestRoute = viscompare[2];

        // check boost case

        int boostdamage = countTotalDamage(blocks, 16);
        if (hasPowerUp(PowerUps.BOOST, myCar.powerups) && boostdamage <= 2 && curSpeed != maxBoostSpeed) {
            return BOOST;
        }

        if (curdamage == 0 && curSpeed > 3) {
            if (offCommand != NOTHING) {
                return offCommand;
            }
        }

        if ((curSpeed == maxBoostSpeed && curdamage != 0) ||
                (curSpeed >= 8 && curdamage != 0 && leftdamage != 0 && rightdamage != 0)) {
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                return LIZARD;
            }
        }

        // lane picking
        if ((bestRoute == frontWeight) && (bestRoute == leftWeight) && (bestRoute == rightWeight)) {
            if (visBestRoute == visfrontWeight) {
                bestRoute = frontWeight;
            } else if (visBestRoute == visleftWeight) {
                bestRoute = leftWeight;
            } else if (visBestRoute == visrightWeight) {
                bestRoute = rightWeight;
            }
        } else if ((bestRoute == frontWeight) && (bestRoute == leftWeight)) {
            if (visBestRoute == visfrontWeight) {
                bestRoute = frontWeight;
            } else if (visBestRoute == visleftWeight) {
                bestRoute = leftWeight;
            }
        } else if ((bestRoute == leftWeight) && (bestRoute == rightWeight)) {
            if (visBestRoute == visleftWeight) {
                bestRoute = leftWeight;
            } else if (visBestRoute == visrightWeight) {
                bestRoute = rightWeight;
            }
        } else if ((bestRoute == frontWeight) && (bestRoute == rightWeight)) {
            if (visBestRoute == visfrontWeight) {
                bestRoute = frontWeight;
            } else if (visBestRoute == visrightWeight) {
                bestRoute = rightWeight;
            }
        }

        if ((bestRoute == frontWeight && curdamage <= 3) || lessdamage == curdamage) {
            // check accelerate
            if (higherSpeed != -1) {
                int acclrtdamage = countTotalDamage(blocks, higherSpeed + 1);
                if (acclrtdamage <= 3 && curSpeed < 9) {
                    return ACCELERATE;
                }
            }
            return offCommand;
        } else {
            // check decelerate
            int checkdamage;
            if (bestRoute == leftWeight) {
                checkdamage = leftdamage;
            } else {
                checkdamage = rightdamage;
            }
            if (lowerSpeed != -1 && lowerSpeed > 5 && (checkdamage >= 2 || lessdamage >= 2)) {
                int dclrtdamage = countTotalDamage(blocks, lowerSpeed + 1);
                if ((dclrtdamage == 0)) {
                    return DECELERATE;
                }
            }
            if (!(leftblocks.isEmpty()) && ((bestRoute == leftWeight && leftdamage <= 3) || lessdamage == leftdamage)) {
                return TURN_LEFT;
            } else if (!(rightblocks.isEmpty())
                    && ((bestRoute == rightWeight && rightdamage <= 3) || lessdamage == rightdamage)) {
                return TURN_RIGHT;
            } else {
                return offCommand;
            }
        }
    }

    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp : available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    private int countPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        int count = 0;

        for (PowerUps powerUp : available) {
            if (powerUp.equals(powerUpToCheck)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns map of blocks and the objects in the for the current lanes, returns
     * the amount of blocks that can be traversed at max speed.
     **/
    private List<Object> getBlocksInFront(int lane, int block, int dir, GameState gameState) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        if ((dir == 2 && lane != 1) || dir == 1 || (dir == 0 && lane != 4)) {
            int startBlock = map.get(0)[0].position.block;
            Lane[] laneList = map.get(lane - dir);
            for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.visibility; i++) {
                if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                    break;
                }
                if (laneList[i].cybertruck) {
                    blocks.add("CYBERTRUCK");
                } else {
                    blocks.add(laneList[i].terrain);
                }
            }
        }
        return blocks;
    }

    // get previous/next speed state
    public int getNearbySpeed(int curSpeed, int dir) {
        if ((curSpeed == 0 && dir == -1) || (curSpeed == 15 && dir == 1)) {
            return -1;
        } else {
            int curIdx = speedState.indexOf(curSpeed);
            return speedState.get(curIdx + dir);
        }
    }

    // count damage of each lanes
    private int countTotalDamage(List<Object> blocks, int steps) {
        int count = 0;
        if (blocks.size() < steps) {
            steps = blocks.size();
        }
        for (int i = 0; i < steps; i++) {
            if (blocks.get(i) == Terrain.MUD || blocks.get(i) == Terrain.OIL_SPILL) {
                count += 1;
            } else if (blocks.get(i) == Terrain.WALL || blocks.get(i) == "CYBERTRUCK") {
                count += 2;
            }
        }
        return count;
    }

    // count boost and lizard
    private int countPowerUps(List<Object> blocks, int speed) {
        int count = 0;
        if (blocks.size() < speed) {
            speed = blocks.size();
        }
        for (int i = 0; i < speed; i++) {
            if (blocks.get(i) == Terrain.BOOST || blocks.get(i) == Terrain.LIZARD || blocks.get(i) == Terrain.EMP
                    || blocks.get(i) == Terrain.TWEET || blocks.get(i) == Terrain.OIL_POWER) {
                count += 1;
            }
        }
        return count + 1;
    }

    private Command offensiveSearch(GameState gameState) {
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;
        // create array with tuples as pair
        // tuples contain weight of the command and the command itself
        // used for prioritizing some move
        ArrayList<Pair<Integer, Command>> actions = new ArrayList<Pair<Integer, Command>>();

        // comparator for weight
        Comparator<Pair<Integer, Command>> comparePair = (Pair<Integer, Command> p1, Pair<Integer, Command> p2) -> p1
                .getValue0().compareTo(p2.getValue0());

        // oil logic
        if (hasPowerUp(PowerUps.OIL, myCar.powerups)) {
            // too many oil, just drop
            if (countPowerUp(PowerUps.OIL, myCar.powerups) > 3) {
                actions.add(Pair.with(10, OIL));
            }
            // drop oil to opponent behind
            if (opponent.position.lane == myCar.position.lane) {
                if (opponent.position.block == myCar.position.block - 1) {
                    actions.add(Pair.with(1, OIL));
                } else if (1 <= (myCar.position.block - opponent.position.block)
                        && (myCar.position.block - opponent.position.block) <= 15) {
                    actions.add(Pair.with(3, OIL));
                }
            }
        }

        // Cybertruck logic
        if (hasPowerUp(PowerUps.TWEET, myCar.powerups)) {
            // can predict opponent movement, if we are ahead
            if (myCar.position.block > opponent.position.block) {
                // initialize for checking every lane damage
                int curLane = opponent.position.lane;
                int curBlock = opponent.position.block;
                int curSpeed = opponent.speed;

                List<Object> blocks = getBlocksInFront(curLane, curBlock, 1, gameState);
                List<Object> leftBlocks = getBlocksInFront(curLane, curBlock, 2, gameState);
                List<Object> rightBlocks = getBlocksInFront(curLane, curBlock, 0, gameState);

                // calculating every damage in every lane
                int curDamage, leftDamage, rightDamage;
                curDamage = countTotalDamage(blocks, curSpeed + 1);
                if (leftBlocks.isEmpty()) {
                    leftDamage = nullBlocks;
                } else {
                    leftDamage = countTotalDamage(leftBlocks, curSpeed);
                    ;
                }
                if (rightBlocks.isEmpty()) {
                    rightDamage = nullBlocks;
                } else {
                    rightDamage = countTotalDamage(rightBlocks, curSpeed);
                }

                // finding the best route for opponent, so we can attack with cybertruck
                int[] check = { curDamage, leftDamage, rightDamage };
                Arrays.sort(check);
                int bestRoute = check[0];

                // place cybertruck in opponent's best lane
                if (bestRoute == curDamage) {
                    // opponent might accelerate/boost so add with the next speed state
                    int oppNextSpeed = getNearbySpeed(opponent.speed, 1);
                    if (oppNextSpeed != -1) {
                        if (opponent.boostCounter >= 1) {
                            // opponent most likely use boost here
                            actions.add(Pair.with(4,
                                    new TweetCommand(opponent.position.lane,
                                            opponent.position.block + maxBoostSpeed + 1)));
                        } else {
                            if (oppNextSpeed == 9){ 
                                actions.add(Pair.with(4,
                                new TweetCommand(opponent.position.lane,
                                        opponent.position.block + opponent.speed)));
                            } else {
                                actions.add(Pair.with(4,
                                        new TweetCommand(opponent.position.lane,
                                                opponent.position.block + oppNextSpeed + 1)));
                            }
                        }
                    } else {
                        actions.add(Pair.with(4,
                                new TweetCommand(opponent.position.lane,
                                        opponent.position.block + opponent.speed)));
                    }
                } else if (bestRoute == leftDamage) {
                    actions.add(Pair.with(4,
                            new TweetCommand(opponent.position.lane - 1, opponent.position.block + opponent.speed)));
                } else {
                    actions.add(Pair.with(4,
                            new TweetCommand(opponent.position.lane + 1, opponent.position.block + opponent.speed)));
                }
            }
            // TEMPORARILY TURNED OFF, because its too risky to use cybertruck when we are
            // behind
            // else {
            // // just place cybertruck infront of the opponent's face, if we are behind
            // // #GREEDY
            // actions.add(Pair.with(4,
            // new TweetCommand(opponent.position.lane, opponent.position.block +
            // opponent.speed + 1)));
            // }
        }

        // EMP logic
        // check if opponent is ahead then we can use EMP
        if (hasPowerUp((PowerUps.EMP), myCar.powerups) && myCar.position.block < opponent.position.block) {
            if (Math.abs(myCar.position.lane - opponent.position.lane) <= 1) {
                // TEMPORARILY TURNED OFF
                // if we are behind of the opponent and not in the same lane as them
                // so that it wont affect us negatively
                // if (myCar.position.lane != opponent.position.lane) {
                // actions.add(Pair.with(0, EMP));
                // }
                // CURENTLY, just check if we are behind and the opponent is in the scope of EMP
                // range
                // #GREEDY
                actions.add(Pair.with(0, EMP));
            }
        }

        // sort depends on the weight of command
        Collections.sort(actions, comparePair);
        if (actions.size() > 0) {
            // take the priority(min) command
            return actions.get(0).getValue1();
        } else {
            return NOTHING;
        }
    }
}