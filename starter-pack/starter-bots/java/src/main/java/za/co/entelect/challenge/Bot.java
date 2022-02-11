package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;

public class Bot {

    private static final int visibility = 20;
    private List<Command> directionList = new ArrayList<>();

    private Car myCar;
    private Car opponent;
    private Random random;
    private GameState gameState;
    private final static Command FIX = new FixCommand();
    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command DECELERATE = new DecelerateCommand();
    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);
    private final static Command NOTHING = new DoNothingCommand();
    

    private List<Integer> speedState = new ArrayList<Integer>(Arrays.asList(0, 3, 5, 6, 8, 9, 15));
    
    public int getNearbySpeed(int curSpeed, int dir){
        if (curSpeed == 0 || curSpeed == 15){
            return -1;
        }
        else{
            int curIdx = speedState.indexOf(curSpeed);
            return speedState.get(curIdx + dir);
        }
    }

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
        List<Object> blocks = getBlocksInFront(curLane, curBlock, 1);
        List<Object> leftblocks = getBlocksInFront(curLane, curBlock, 2);
        List<Object> rightblocks = getBlocksInFront(curLane, curBlock, 0);

        // fix car (maintain max speed above 6)
        if (damage >= 3){
            return FIX;
        }

        // check total damage of each route
        int curdamage, leftdamage, rightdamage;

        curdamage = countTotalDamage(blocks, curSpeed + 1);
        if (!leftblocks.isEmpty()){
            leftdamage = countTotalDamage(leftblocks, curSpeed);
        }
        else{
            leftdamage = 9999;
        }
        if (!rightblocks.isEmpty()){
            rightdamage = countTotalDamage(rightblocks, curSpeed);
        }
        else{
            rightdamage = 9999;
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

        /* line 94-103 lebih sering menang, tapi line 123-132 kalo dibandingin sama yg line 94-103 scorenya relatif lebih positif (lebih terjamin dibanding yg 94-103 :V cuma kalah mulu aja :V)
        jadi jatohnya line 123-132 lebih bisa ngemastiin botnya jarang nabrak kalo dibanding code yg di atas 
        next: cari middle point yg paling enak (score aman, tp ga kalah mulu jg :v) */
        // if (bestRoute == curdamage){
        //     /* CHECK ACCELERATE */
        //     if (higherSpeed != -1){
        //         int acclrtdamage = countTotalDamage(blocks, higherSpeed + 1);
        //         if ((acclrtdamage <= 3 || acclrtdamage <= curdamage + 1) && (damage <= 1)){
        //             return ACCELERATE;
        //         }
        //     }
        //     return NOTHING;
        // }
    }
    
    /**
     * Returns map of blocks and the objects in the current lanes, returns the amount of blocks that can be
     * traversed at max speed.
     **/
    private List<Object> getBlocksInFront(int lane, int block, int dir) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        if ((dir == 2 && lane != 1) || dir == 1 || (dir == 0 && lane != 4)){
            int startBlock = map.get(0)[0].position.block;
            Lane[] laneList = map.get(lane - dir);
            for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.visibility; i++) {
                if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                    break;
                }
                blocks.add(laneList[i].terrain);
            }
        }
        return blocks;
    }
    private int countTotalDamage(List<Object> blocks, int steps){
        int count = 0;
        if (blocks.size() < steps){
            steps = blocks.size();
        }
        for (int i = 0; i < steps; i++){
            if (blocks.get(i) == Terrain.MUD){
                count += 1;
            }
            else if (blocks.get(i) == Terrain.OIL_SPILL){
                count += 1;
            }
            else if (blocks.get(i) == Terrain.WALL){
                count += 2;
            }
        }
        return count;
    }
}
