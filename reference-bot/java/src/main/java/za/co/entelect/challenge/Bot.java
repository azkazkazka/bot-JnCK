package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import org.javatuples.Pair;

import static java.lang.Math.max;

// import java.rmi.activation.Activator;
import java.security.SecureRandom;

public class Bot {

    private static final int maxSpeed = 9;
    private static final int visibility = 20;
    private static final int nullBlocks = 999;
    private static final int maxBoostSpeed = 15;
    private List<Command> directionList = new ArrayList<>();

    private final Random random;

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
        this.random = new SecureRandom();
        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }

    public Command run(GameState gameState) {
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;

        // initialize car & game state
        int damage = myCar.damage;
        int curLane = myCar.position.lane;
        int curBlock = myCar.position.block;

        // initialize current, lower, and higher speed (edge case result: -1)
        int curSpeed = myCar.speed;
        int lowerSpeed = getNearbySpeed(curSpeed, -1); 
        int higherSpeed = getNearbySpeed(curSpeed, +1);
        
        // initialize possible lanes (current, left, right)
        List<Object> blocks = getBlocksInFront(curLane, curBlock, 1, gameState);
        List<Object> leftblocks = getBlocksInFront(curLane, curBlock, 2, gameState);
        List<Object> rightblocks = getBlocksInFront(curLane, curBlock, 0, gameState);

        // fix car (maintain max speed above 6)
        if (damage >= 3){
            return FIX;
        }

        if (curSpeed == 0){
            return ACCELERATE;
        }

        if ((curSpeed == maxBoostSpeed) || ((curSpeed == maxSpeed) && (damage == 1))){
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)){
                return LIZARD;
            }
        }

        // check total damage of each route
        int curdamage, leftdamage, rightdamage;

        curdamage = countTotalDamage(blocks, curSpeed + 1);
        if (leftblocks.isEmpty()){
            leftdamage = nullBlocks;
        }
        else{
            leftdamage = countTotalDamage(leftblocks, curSpeed);;
        }
        if (rightblocks.isEmpty()){
            rightdamage = nullBlocks;
        }
        else{
            rightdamage = countTotalDamage(rightblocks, curSpeed);
        }

        // check total boost/lizard of each route
        int frontBoost, leftBoost, rightBoost;
        frontBoost = countBoostLizard(blocks, curSpeed + 1);

        if (!leftblocks.isEmpty()){
            leftBoost = countBoostLizard(leftblocks, curSpeed);
        } else{
            leftBoost = -1;
        }
        
        if (!rightblocks.isEmpty()){
            rightBoost = countBoostLizard(rightblocks, curSpeed);
        } else{
            rightBoost = -1;
        }

        if ((frontBoost <= 1) && (rightBoost <= 1) && (leftBoost <= 1)){
            // compare damage of each route
            int[] check = {curdamage, leftdamage, rightdamage};
            Arrays.sort(check);
            int bestRoute = check[0];

            // compare with accelerate & decelerate case (BAGIAN INI BLM DIPIKIRIN BET TP YAUDALAYA)
            if (bestRoute == curdamage){
                // check accelerate
                if (higherSpeed != -1){
                    int acclrtdamage = countTotalDamage(blocks, higherSpeed + 1);
                    if (acclrtdamage <= 3 || (acclrtdamage/curdamage) <= (higherSpeed/curSpeed)){
                        if (hasPowerUp(PowerUps.BOOST, myCar.powerups)){
                            return BOOST;
                        } else {
                            return ACCELERATE;
                        }
                    }
                }
                return offensiveSearch(gameState);
            }
            else{
                // check decelerate
                if (lowerSpeed != -1 && lowerSpeed >= 5){
                    int dclrtdamage = countTotalDamage(blocks, lowerSpeed + 1);
                    if ((dclrtdamage <= 2 || dclrtdamage <= bestRoute + 1)){
                        return DECELERATE;
                    }
                }
                if (bestRoute == leftdamage){
                    return TURN_LEFT;
                }
                else{
                    return TURN_RIGHT;
                }
            }            
        }

        // compare boost/damage of each route
        float frontWeight = frontBoost/curdamage;
        float leftWeight = leftBoost/leftdamage;
        float rightWeight = rightBoost/rightdamage;
        float[] compare = {frontWeight, leftWeight, rightWeight};
        Arrays.sort(compare);
        float bestRoute = compare[2];

        // compare with accelerate & decelerate case (BAGIAN INI BLM DIPIKIRIN BET TP YAUDALAYA)
        // TINJAU ULANG
        if (bestRoute == frontWeight && curdamage < 5){
            // check accelerate
            if (higherSpeed != -1){
                int acclrtdamage = countTotalDamage(blocks, higherSpeed + 1);
                if (acclrtdamage <= 3 || (acclrtdamage/curdamage) <= (higherSpeed/curSpeed)){
                    if (hasPowerUp(PowerUps.BOOST, myCar.powerups)){
                        return BOOST;
                    } else {
                        return ACCELERATE;
                    }
                }
            }
            return offensiveSearch(gameState);
        }
        else{
            // check decelerate
            if (lowerSpeed != -1 && lowerSpeed >= 5){
                int dclrtdamage = countTotalDamage(blocks, lowerSpeed + 1);
                if ((dclrtdamage <= 2 || dclrtdamage <= bestRoute + 1)){
                    return DECELERATE;
                }
            }

            // TINJAU ULANG
            if (bestRoute == leftWeight && leftdamage < 5){
                return TURN_LEFT;
            }
            else if (bestRoute == rightWeight && rightdamage < 5){
                return TURN_RIGHT;
            }
            else {
                return NOTHING;
            }
            
        }
    }

    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    private int countPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        int count = 0;

        for (PowerUps powerUp: available) {
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
        if ((dir == 2 && lane != 1) || dir == 1 || (dir == 0 && lane != 4)){
            int startBlock = map.get(0)[0].position.block;
            Lane[] laneList = map.get(lane - dir);
            for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.visibility; i++) {
                if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                    break;
                }
                if (laneList[i].cybertruck){
                    blocks.add("CYBERTRUCK");
                }
                blocks.add(laneList[i].terrain);
            }
        }
        return blocks;
    }

    // get previous/next speed state
    public int getNearbySpeed(int curSpeed, int dir){
        if (curSpeed == 0 || curSpeed == 15){
            return -1;
        }
        else{
            int curIdx = speedState.indexOf(curSpeed);
            return speedState.get(curIdx + dir);
        }
    }

    // count damage of each lanes
    private int countTotalDamage(List<Object> blocks, int steps){
        int count = 0;
        if (blocks.size() < steps){
            steps = blocks.size();
        }
        for (int i = 0; i < steps; i++){
            if (blocks.get(i) == Terrain.MUD || blocks.get(i) == Terrain.OIL_SPILL){
                count += 1;
            }
            else if (blocks.get(i) == Terrain.WALL || blocks.get(i) == "CYBERTRUCK"){
                count += 2;
            }
        }
        return count + 1;
    }

    // count boost and lizard
    private int countBoostLizard(List<Object> blocks, int speed){
        int count = 0;
        if (blocks.size() < speed){
            speed = blocks.size();
        }
        for (int i = 0; i < speed; i++){
            if (blocks.get(i) == Terrain.BOOST){
                count += 1;
            } else if (blocks.get(i) == Terrain.LIZARD){
                count += 1;
            }
        }
        return count + 1;
    }

    private Command offensiveSearch(GameState gameState){
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;
        //create array with tuples as pair
        //tuples contain weight of the command and the command itself
        //used for prioritizing some move
        ArrayList<Pair<Integer, Command>> actions = new ArrayList<Pair<Integer, Command>>();

        //comparator for weight
        Comparator<Pair<Integer, Command>> comparePair = (Pair<Integer, Command> p1, Pair<Integer, Command> p2) 
            -> p1.getValue0().compareTo(p2.getValue0());

        // oil logic
        if (hasPowerUp(PowerUps.OIL, myCar.powerups)) {
            // too many oil, just drop
            if (countPowerUp(PowerUps.OIL, myCar.powerups) > 3){
                actions.add(Pair.with(10, OIL));
            }
            // drop oil to opponent behind
            if (opponent.position.lane == myCar.position.lane) {
                if (opponent.position.block == myCar.position.block - 1){
                    actions.add(Pair.with(1, OIL));
                }
            // there should be more logic to this
            }
        }

        //Cybertruck logic
        //bit hard to implement

        // EMP logic
        if (hasPowerUp((PowerUps.EMP), myCar.powerups) && myCar.position.block > opponent.position.block){
            if (Math.abs(myCar.position.lane - opponent.position.lane) <= 1){
                // this basically, if we are ahead of the opponent and not in the same lane as them
                if (myCar.position.lane != opponent.position.lane){
                    actions.add(Pair.with(0, EMP));
                }
            }
        }

        // sort depends on the weight of command
        Collections.sort(actions, comparePair);
        if (actions.size() > 0){
            // take the priority(min) command
            return actions.get(0).getValue1();
        } else {
            return NOTHING;
        }
    }
}

