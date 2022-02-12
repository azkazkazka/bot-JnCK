package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;

public class Bot {

    private static final int maxSpeed = 9;
    private static final int maxBoostSpeed = 15;
    private List<Integer> directionList = new ArrayList<>();

    private Random random;
    private GameState gameState;
    private Car opponent;
    private Car myCar;
    private final static Command FIX = new FixCommand();
    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command DECELERATE = new DecelerateCommand();
    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);
    private final static Command NOTHING = new DoNothingCommand();
    private final static Command USE_BOOST = new BoostCommand();
    private final static Command USE_LIZARD = new LizardCommand();

    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.myCar = gameState.player;
        this.opponent = gameState.opponent;

        directionList.add(-1);
        directionList.add(1);
    }

    public Command run() {
        // init stats awal player 
        int curDamage = myCar.damage;
        int curSpeed = myCar.speed;
        int curLane = myCar.position.lane;
        int curBlock = myCar.position.block;

        // init 3 lane
        List<Object> frontBlocks = getBlocksInFront(curLane, curBlock, 1);
        List<Object> leftBlocks = getBlocksInFront(curLane, curBlock, 2);
        List<Object> rightBlocks = getBlocksInFront(curLane, curBlock, 0);

        // init ntar utk check damage tiap lane
        int frontDamage, leftDamage, rightDamage;

        // init ntar utk check boost/lizard tiap lane
        int frontBoost, leftBoost, rightBoost;

        // check
        if (curDamage >= 2) {
            return FIX;
        }

        // check lg boost_speed ga
        if ((curSpeed == maxBoostSpeed) || ((curSpeed == maxSpeed) && (curDamage == 1))){
            //check total damage
            frontDamage = countTotalDamage(frontBlocks, curSpeed + 1);

            if (!leftBlocks.isEmpty()){
                leftDamage = countTotalDamage(leftBlocks, curSpeed);
            } else{
                leftDamage = 1000;
            }

            if (!rightBlocks.isEmpty()){
                rightDamage = countTotalDamage(rightBlocks, curSpeed);
            } else{
                rightDamage = 1000;
            }
            //kl ad yg nol, pilih
            if(frontDamage == 0) {
                return NOTHING;
            } else if (leftDamage == 0){
                return TURN_LEFT;
            } else if (rightDamage == 0){
                return TURN_RIGHT;
            }
            //kl ad lizard, pake
            if (checkPowerUp(PowerUps.LIZARD, myCar.powerups)){
                return USE_LIZARD;
            }
            //check total boost & lizard
            frontBoost = countBoostLizard(frontBlocks, curSpeed + 1);

            if (!leftBlocks.isEmpty()){
                leftBoost = countBoostLizard(leftBlocks, curSpeed);
            } else{
                leftBoost = -1;
            }
            
            if (!rightBlocks.isEmpty()){
                rightBoost = countBoostLizard(rightBlocks, curSpeed);
            } else{
                rightBoost = -1;
            }
            // bandingin perbandingan
            float frontWeight = frontBoost/frontDamage;
            float leftWeight = leftBoost/leftDamage;
            float rightWeight = rightBoost/rightDamage;
            float[] compare = {frontWeight, leftWeight, rightWeight};
            Arrays.sort(compare);
            float bestRoute = compare[2];

            // pilih jalan
            if (bestRoute == frontWeight){
                return NOTHING;
            } else if (bestRoute == leftWeight){
                return TURN_LEFT;
            } else {
                return TURN_RIGHT;
            }
        }

        // check ad speed_boost ga
        if (checkPowerUp(PowerUps.BOOST, myCar.powerups)){
            return USE_BOOST;
        }

        //check total damage
        frontDamage = countTotalDamage(frontBlocks, curSpeed + 1);

        if (!leftBlocks.isEmpty()){
            leftDamage = countTotalDamage(leftBlocks, curSpeed);
        } else{
            leftDamage = 1000;
        }

        if (!rightBlocks.isEmpty()){
            rightDamage = countTotalDamage(rightBlocks, curSpeed);
        } else{
            rightDamage = 1000;
        }
        //check total boost & lizard
        frontBoost = countBoostLizard(frontBlocks, curSpeed + 1);

        if (!leftBlocks.isEmpty()){
            leftBoost = countBoostLizard(leftBlocks, curSpeed);
        } else{
            leftBoost = -1;
        }
        
        if (!rightBlocks.isEmpty()){
            rightBoost = countBoostLizard(rightBlocks, curSpeed);
        } else{
            rightBoost = -1;
        }
        // bandingin perbandingan
        float frontWeight = frontBoost/frontDamage;
        float leftWeight = leftBoost/leftDamage;
        float rightWeight = rightBoost/rightDamage;
        float[] compare = {frontWeight, leftWeight, rightWeight};
        Arrays.sort(compare);
        float bestRoute = compare[2];

        // pilih jalan
        if (bestRoute == frontWeight){
            return NOTHING;
        } else if (bestRoute == leftWeight){
            return TURN_LEFT;
        } else {
            return TURN_RIGHT;
        }

    }

    /**
     * Returns map of blocks and the objects in the for the current lanes, returns the amount of blocks that can be
     * traversed at max speed.
     **/
    private List<Object> getBlocksInFront(int lane, int block, int dir) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();

        if ((dir == 2 && lane != 1) || dir == 1 || (dir == 0 && lane != 4)){
            int startBlock = map.get(0)[0].position.block;
            Lane[] laneList = map.get(lane - dir);

            for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.maxBoostSpeed; i++) {
                if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                    break;
                }
                blocks.add(laneList[i].terrain);
            }
        }
        return blocks;
    }

    private int countTotalDamage(List<Object> blocks, int speed){
        int count = 0;
        if (blocks.size() < speed){
            steps = blocks.size();
        }
        for (int i = 0; i < speed; i++){
            if (blocks.get(i) == Terrain.MUD){
                count += 1;
            } else if (blocks.get(i) == Terrain.OIL_SPILL){
                count += 1;
            } else if (blocks.get(i) == Terrain.WALL){
                count += 2;
            }
        }
        return count;
    }

    private int countBoostLizard(List<Object> blocks, int speed){
        int count = 0;
        if (blocks.size() < speed){
            steps = blocks.size();
        }
        for (int i = 0; i < speed; i++){
            if (blocks.get(i) == Terrain.BOOST){
                count += 1;
            } else if (blocks.get(i) == Terrain.LIZARD){
                count += 1;
            }
        }
        return count;
    }

    private Boolean checkPowerUp(PowerUps powerUpIsIn, PowerUps[] isIn){
        for(PowerUps powerUp: isIn){
            if(powerUp.equals(powerUpIsIn)){
                return true;
            }
        }
        return false;
    }

}
