package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;

// import java.rmi.activation.Activator;
import java.security.SecureRandom;

public class Bot {

    private static final int maxSpeed = 15;
    private static final int visibility = 20;
    private static final int nullBlocks = 999;
    private List<Command> directionList = new ArrayList<>();

    private Car myCar;
    private Car opponent;
    private Random random;
    private GameState gameState;

    private final static Command FIX = new FixCommand();
    private final static Command NOTHING = new DoNothingCommand();
    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command DECELERATE = new DecelerateCommand();

    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    private List<Integer> speedState = new ArrayList<Integer>(Arrays.asList(0, 3, 5, 6, 8, 9, 15));

    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.myCar = gameState.player;
        this.opponent = gameState.opponent;
        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }

    public Command run() {
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
                    return ACCELERATE;
                }
            }
            return NOTHING;
        }
        else{
            // check decelerate
            if (lowerSpeed != -1 && lowerSpeed >= 5){
                int dclrtdamage = countTotalDamage(blocks, lowerSpeed + 1);
                if (curSpeed > 5 && (dclrtdamage <= 1)){
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
        return count;
    }
}

