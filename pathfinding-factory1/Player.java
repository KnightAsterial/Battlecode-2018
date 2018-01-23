// import the API.
// See xxx for the javadocs.
import bc.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;

public class Player {
    // Direction is a normal java enum.
        /*
         drection[0]North
         drection[1]Northeast
         drection[2]East
         drection[3]Southeast
         drection[4]South
         drection[5]Southwest
         drection[6]West
         drection[7]Northwest
         drection[8]Center
         */
    public static Direction[] directions = Direction.values();

    /*
    in format
    y
    ^
    | [] [] [] []
    | [] [] [] []
    | [] [] [] []
    | [] [] [] []
    | ----------- > x

    can be accessed as spreadPathfindingMap[height-y][x] or using the function

    getValueInPathfindingMap(x,y)
    setValueInPathfindingMap(x,y);
     */
    public static Direction[][] spreadPathfindingMapEarth;
    public static Direction[][] spreadPathfindingMapMars;
    public static Dimension earthDimensions;
    public static Dimension marsDimensions;

    public static PlanetMap earthMap;
    public static PlanetMap marsMap;

    public static GameController gc;

    public static void main(String[] args) {
        try {
            // Connect to the manager, starting the game. Initializing methods
            gc = new GameController();
            //map stuff
            earthMap = gc.startingMap(Planet.Earth);
            marsMap = gc.startingMap(Planet.Mars);
            earthDimensions = new Dimension((int)earthMap.getWidth(), (int)earthMap.getHeight());
            marsDimensions = new Dimension((int)marsMap.getWidth(), (int)marsMap.getHeight());

            int currentTurn;

            int numFactories = 0;
            int numHealers = 0;
            int numKnights = 0;
            int numMages = 0;
            int numRangers = 0;
            int numRockets = 0;
            int numWorkers = 0;

            MapLocation target;
            boolean hasTarget = false;
            target = new MapLocation(Planet.Earth, 10, 1);
            hasTarget = true;


            //MACRO control variables
            int maxWorkerNum = 8;
            boolean karboniteCollectionStage = false;

            //initial research queue
            gc.queueResearch(UnitType.Ranger);
            gc.queueResearch(UnitType.Ranger);
            gc.queueResearch(UnitType.Ranger);


            //first turn code
            spreadPathfindingMapEarth = updatePathfindingMap(target, earthMap);
            spreadPathfindingMapMars = updatePathfindingMap(target, marsMap);
            ArrayList<MapLocation> likelyEnemyStartingLocations = new ArrayList<MapLocation>();
            VecUnit startingUnits = gc.myUnits();
            for (int i = 0; i < startingUnits.size(); i++) {
                Unit currentStartingUnit = startingUnits.get(i);
                MapLocation currentStartingUnitLocation = currentStartingUnit.location().mapLocation();
                likelyEnemyStartingLocations.add(new MapLocation(Planet.Earth, currentStartingUnitLocation.getY(), currentStartingUnitLocation.getX()));
            }


            for (int i = 0; i < directions.length; i++) {
                System.out.println("directions[" + i + "] = " + directions[i]);
            }

            for (int i = 0; i < spreadPathfindingMapEarth.length; i++) {
                for (int j = 0; j < spreadPathfindingMapEarth[0].length; j++) {
                    if (spreadPathfindingMapEarth[i][j] == null){
                        System.out.print("x");
                    }
                    else{
                        System.out.print("0");
                    }
                }
                System.out.println();
            }



            while (true) {
                currentTurn = (int)gc.round();

                System.out.println("Current round: " + currentTurn + " with ms time left: " + gc.getTimeLeftMs());

                // VecUnit is a class that you can think of as similar to ArrayList<Unit>, but immutable.
                VecUnit units = gc.myUnits();

                //resets counter
                numFactories = 0;
                numHealers = 0;
                numKnights = 0;
                numMages = 0;
                numRangers = 0;
                numRockets = 0;
                numWorkers = 0;
                //counts units
                for (int i = 0; i < units.size(); i++){
                    Unit unit = units.get(i);
                    if (unit.unitType().equals(UnitType.Factory)){
                        numFactories++;
                    }
                    else if (unit.unitType().equals(UnitType.Healer)){
                        numHealers++;
                    }
                    else if (unit.unitType().equals(UnitType.Knight)){
                        numKnights++;
                    }
                    else if (unit.unitType().equals(UnitType.Mage)){
                        numMages++;
                    }
                    else if (unit.unitType().equals(UnitType.Ranger)){
                        numRangers++;
                    }
                    else if (unit.unitType().equals(UnitType.Rocket)){
                        numRockets++;
                    }
                    else if (unit.unitType().equals(UnitType.Worker)){
                        numWorkers++;
                    }
                }

                for (int i = 0; i < units.size(); i++) {
                    Unit unit = units.get(i);
                    // Most methods on gc take unit IDs, instead of the unit objects themselves.

                    if (unit.unitType().equals(UnitType.Worker)){
                        boolean hasPerformedAction = false;
                        boolean shouldMove = true;

                        //senses adjacent friendly within 2 tiles
                        VecUnit unitsNearWorker = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(), 8, gc.team());

                        //if there is a blueprint to build, build it
                        for (int index = 0; index < unitsNearWorker.size(); index++){
                            Unit nearbyUnit = unitsNearWorker.get(index);
                            if (gc.canBuild(unit.id(), nearbyUnit.id())){
                                gc.build(unit.id(), nearbyUnit.id());
                                hasPerformedAction = true;
                                shouldMove = false;
                                break;
                            }
                        }

                        //replication loop (maxWorkerNum = 8 atm)
                        if (numWorkers <= maxWorkerNum){
                            for (int j = 0; j < 8; j++) {
                                if (gc.canReplicate(unit.id(), directions[j])){
                                    gc.replicate(unit.id(), directions[j]);
                                    break;
                                }
                            }
                        }



                    }

                    if (gc.isMoveReady(unit.id())) {
                        moveAlongBFSPath(gc, unit);
                    }
                }
                // Submit the actions we've done, and wait for our next turn.
                gc.nextTurn();
            }
        }
        catch(Exception e){
            System.out.println("Exception in main");
            e.printStackTrace();
        }
    }

    /**
     * Basic tryMove. assumes move is ready (isMoveReady() waas already checked and returns true
     * @param location
     */
    public static void tryMove(GameController gc, Unit unit, MapLocation location){
        //try block just in case of fatal exceptions!
        try{

            Direction toMove = unit.location().mapLocation().directionTo(location);

            //if can move forwards
            if (gc.canMove(unit.id(), toMove)){
                gc.moveRobot(unit.id(), toMove);
            }
            else{
                //tests moving one direction right, then one direction left
                //than a further direction right (total 2) then a further direction left
                Direction rightRotation = bc.bcDirectionRotateRight(toMove);
                if (gc.canMove(unit.id(), rightRotation)){
                    gc.moveRobot(unit.id(), rightRotation);
                }
                else {
                    Direction leftRotation = bc.bcDirectionRotateLeft(toMove);
                    if (gc.canMove(unit.id(), leftRotation)) {
                        gc.moveRobot(unit.id(), leftRotation);
                    } else {
                        rightRotation = bc.bcDirectionRotateRight(rightRotation);
                        if (gc.canMove(unit.id(), rightRotation)) {
                            gc.moveRobot(unit.id(), rightRotation);
                        } else {
                            leftRotation = bc.bcDirectionRotateLeft(leftRotation);
                            if (gc.canMove(unit.id(), leftRotation)) {
                                gc.moveRobot(unit.id(), leftRotation);
                            } else {
                            }
                        }
                    }
                }
            }

        }
        catch(Exception e){
            System.out.println("Move for unit " + unit + " failed");
            e.printStackTrace();
        }

    }

    /**
     * Basic tryMove based on direction. assumes move is ready (isMoveReady() waas already checked and returns true
     * @param toMove direction to move in
     */
    public static void tryMove(GameController gc, Unit unit, Direction toMove){
        //try block just in case of fatal exceptions!
        try{
            //if can move forwards
            if (gc.canMove(unit.id(), toMove)){
                gc.moveRobot(unit.id(), toMove);
            }
            else{
                //tests moving one direction right, then one direction left
                //than a further direction right (total 2) then a further direction left
                Direction rightRotation = bc.bcDirectionRotateRight(toMove);
                if (gc.canMove(unit.id(), rightRotation)){
                    gc.moveRobot(unit.id(), rightRotation);
                }
                else {
                    Direction leftRotation = bc.bcDirectionRotateLeft(toMove);
                    if (gc.canMove(unit.id(), leftRotation)) {
                        gc.moveRobot(unit.id(), leftRotation);
                    } else {
                        rightRotation = bc.bcDirectionRotateRight(rightRotation);
                        if (gc.canMove(unit.id(), rightRotation)) {
                            gc.moveRobot(unit.id(), rightRotation);
                        } else {
                            leftRotation = bc.bcDirectionRotateLeft(leftRotation);
                            if (gc.canMove(unit.id(), leftRotation)) {
                                gc.moveRobot(unit.id(), leftRotation);
                            } else {
                            }
                        }
                    }
                }
            }

        }
        catch(Exception e){
            System.out.println("Move for unit " + unit + " failed");
            e.printStackTrace();
        }

    }

    public static void moveAlongBFSPath(GameController gc, Unit unit){
        MapLocation unitLocation = unit.location().mapLocation();
        Direction directionToMove;
        if (gc.planet().equals(Planet.Earth)){
            directionToMove = bc.bcDirectionOpposite(getValueInPathfindingMap(unitLocation.getX(), unitLocation.getY(), spreadPathfindingMapEarth));
        }
        else{
            directionToMove = bc.bcDirectionOpposite(getValueInPathfindingMap(unitLocation.getX(), unitLocation.getY(), spreadPathfindingMapMars));
        }

        tryMove(gc, unit, directionToMove);
    }

    /**
    @param target assumes the target location is on the same planet as the planet currently being run
     @param planetMap map of planet to create pathfinding for
     */
    public static Direction[][] updatePathfindingMap(MapLocation target, PlanetMap planetMap){
        Direction[][] currentMap;
        Planet currentPlanet = planetMap.getPlanet();

        currentMap = new Direction[(int)planetMap.getHeight()][(int)planetMap.getWidth()];

        LinkedList<MapLocation> bfsQueue = new LinkedList<MapLocation>();
        MapLocation tempLocation;
        MapLocation currentBFSLocation;
        bfsQueue.add(target);

        //setValueInPathfindingMap(target.getX(), target.getY(), Direction.Center, currentMap);
        while (bfsQueue.size() > 0){
            //gets first item in bfsQueue
            currentBFSLocation = bfsQueue.poll();
            for (int i = 0; i < 8; i++) {
                tempLocation = currentBFSLocation.add(directions[i]);
                //only runs calculations if the added part is actually on the map...
                if ( (tempLocation.getY() >= 0 && tempLocation.getY() < currentMap.length)
                        && (tempLocation.getX() >= 0 && tempLocation.getX() < currentMap[0].length)) {
                    //only runs calculation if that area of the pathfindingMap hasn't been filled in yet
                    if (getValueInPathfindingMap(tempLocation.getX(), tempLocation.getY(), currentMap) == null) {
                        if (planetMap.isPassableTerrainAt(new MapLocation(currentPlanet, tempLocation.getX(), tempLocation.getY())) != 0) {
                            bfsQueue.add(tempLocation);
                            setValueInPathfindingMap(tempLocation.getX(), tempLocation.getY(), directions[i], currentMap);
                        }
                    }
                }
            }
        }

        return currentMap;
    }

    public static Direction getValueInPathfindingMap(int x, int y, Direction[][] map){
        return map[map.length-1-y][x];
    }

    public static void setValueInPathfindingMap(int x, int y, Direction myDirection, Direction[][] map){
        map[map.length-1-y][x] = myDirection;
    }

}