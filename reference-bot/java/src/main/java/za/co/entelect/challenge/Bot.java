package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import org.javatuples.Pair;

import static java.lang.Math.max;

import java.rmi.activation.Activator;
import java.security.SecureRandom;

public class Bot {

    private static final int maxSpeed = 9;
    private List<Command> directionList = new ArrayList<>();

    private final Random random;

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();
    private final static Command NOTHING = new DoNothingCommand();

    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    public Bot() {
        this.random = new SecureRandom();
        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }

    public Command run(GameState gameState) {
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;

        //Basic fix logic
        List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block, gameState);
        List<Object> nextBlocks = blocks.subList(0,1);

        //Basic avoidance logic
        if (blocks.contains(Terrain.MUD) || nextBlocks.contains(Terrain.WALL)) {
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                return LIZARD;
            }
            if (nextBlocks.contains(Terrain.MUD) || nextBlocks.contains(Terrain.WALL)) {
                int i = random.nextInt(directionList.size());
                return directionList.get(i);
            }
        }

        //Basic fix logic
        if(myCar.damage >= 3) {
            return FIX;
        }

        //Accelerate first if going to slow
        if(myCar.speed <= 3) {
            return ACCELERATE;
        }
        
        //Basic improvement logic
        if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
            return BOOST;
        }

        //Basic aggression logic
        if (hasPowerUp(PowerUps.OIL, myCar.powerups) || hasPowerUp(PowerUps.EMP, myCar.powerups)){
            return offensiveSearch(gameState);
        }

        return NOTHING;
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
    private List<Object> getBlocksInFront(int lane, int block, GameState gameState) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.maxSpeed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        return blocks;
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

