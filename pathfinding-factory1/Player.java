// import the API.
// See xxx for the javadocs.
import bc.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

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

    public static Team myTeam;
    public static Team enemyTeam;

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

            //team information
            myTeam = gc.team();
            if(Team.Blue.equals(myTeam)){
                enemyTeam = Team.Red;
            }
            else{
                enemyTeam = Team.Blue;
            }

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
            int maxFactories = 5;
            boolean karboniteCollectionStage = false;
            boolean enoughFactories = false;
            UnitType unitTypeToProduce = UnitType.Ranger;

            //initial research queue
            gc.queueResearch(UnitType.Ranger);
            gc.queueResearch(UnitType.Ranger);
            gc.queueResearch(UnitType.Ranger);


            //first turn code
            target = gc.myUnits().get(0).location().mapLocation();

            spreadPathfindingMapEarth = updatePathfindingMap(target, earthMap);
            spreadPathfindingMapMars = updatePathfindingMap(target, marsMap);
            ArrayList<MapLocation> likelyEnemyStartingLocations = new ArrayList<MapLocation>();
            VecUnit startingUnits = gc.myUnits();
            for (int i = 0; i < startingUnits.size(); i++) {
                Unit currentStartingUnit = startingUnits.get(i);
                MapLocation currentStartingUnitLocation = currentStartingUnit.location().mapLocation();
                if (earthMap.isPassableTerrainAt(currentStartingUnitLocation) != 0) {
                    likelyEnemyStartingLocations.add(new MapLocation(Planet.Earth, currentStartingUnitLocation.getY(), currentStartingUnitLocation.getX()));
                }
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
                try {
                    currentTurn = (int) gc.round();

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
                    for (int i = 0; i < units.size(); i++) {
                        Unit unit = units.get(i);
                        if (unit.unitType().equals(UnitType.Factory)) {
                            numFactories++;
                        } else if (unit.unitType().equals(UnitType.Healer)) {
                            numHealers++;
                        } else if (unit.unitType().equals(UnitType.Knight)) {
                            numKnights++;
                        } else if (unit.unitType().equals(UnitType.Mage)) {
                            numMages++;
                        } else if (unit.unitType().equals(UnitType.Ranger)) {
                            numRangers++;
                        } else if (unit.unitType().equals(UnitType.Rocket)) {
                            numRockets++;
                        } else if (unit.unitType().equals(UnitType.Worker)) {
                            numWorkers++;
                        }
                    }

                    //space for global / macro calculations
                    if (numFactories < maxFactories){
                        enoughFactories = false;
                    }
                    else{
                        enoughFactories = true;
                        karboniteCollectionStage = true;
                    }

                    //unit loop
                    for (int i = 0; i < units.size(); i++) {
                        Unit unit = units.get(i);
                        if (unit.location().isInGarrison() || unit.location().isInSpace()){
                            continue;
                        }

                        MapLocation unitLocation = unit.location().mapLocation();
                        // Most methods on gc take unit IDs, instead of the unit objects themselves.

                        if (unit.unitType().equals(UnitType.Worker)) {
                            boolean shouldMove = true;

                            //senses adjacent friendly within 2 tiles
                            VecUnit unitsNearWorker = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(), 8, gc.team());

                            //if there is a blueprint to build, build it
                            for (int index = 0; index < unitsNearWorker.size(); index++) {
                                Unit nearbyUnit = unitsNearWorker.get(index);
                                if (gc.canBuild(unit.id(), nearbyUnit.id())) {
                                    gc.build(unit.id(), nearbyUnit.id());
                                    shouldMove = false;
                                    break;
                                }
                            }

                            //replication loop (maxWorkerNum = 8 atm)
                            if (numWorkers <= maxWorkerNum) {
                                for (int j = 0; j < 8; j++) {
                                    if (gc.canReplicate(unit.id(), directions[j])) {
                                        gc.replicate(unit.id(), directions[j]);
                                        break;
                                    }
                                }
                            }

                            //blueprints a factory if not adjacent to any other factory
                            if ((unit.workerHasActed() == 0) && !enoughFactories) {
                                //only runs this if there is enough karbonite to build factory...
                                if (gc.karbonite() > bc.bcUnitTypeBlueprintCost(UnitType.Factory)) {
                                    //scan adjacent tiles
                                    MapLocation testLocation = unit.location().mapLocation();
                                    //tests tiles in all directions of worker
                                    for (int directionIndex = 0; directionIndex < 8 && (unit.workerHasActed() == 0); directionIndex++) {

                                        boolean shouldBuildHere = true;
                                        testLocation = unitLocation.add(directions[directionIndex]);
                                        for (int x = 0; x < unitsNearWorker.size(); x++) {
                                            if (unitsNearWorker.get(x).unitType().equals(UnitType.Factory)
                                                    && unitsNearWorker.get(x).location().mapLocation().isAdjacentTo(testLocation)) {
                                                //there is a factory adjacent, thus don't build
                                                shouldBuildHere = false;
                                                break;
                                            }
                                        }
                                        if (shouldBuildHere) {
                                            if (gc.canBlueprint(unit.id(), UnitType.Factory, directions[directionIndex])) {
                                                gc.blueprint(unit.id(), UnitType.Factory, directions[directionIndex]);
                                                shouldMove = false;
                                            }
                                        }

                                    }
                                }
                            }

                            //harvests karbonite from surroundings
                            if ((unit.workerHasActed() == 0)){
                                for (int j = 0; j < 9; j++) {
                                    if (gc.canHarvest(unit.id(), directions[j])){
                                        gc.harvest(unit.id(), directions[j]);
                                        shouldMove = false;
                                        break;
                                    }
                                }
                            }

                            if (shouldMove) {
                                if(karboniteCollectionStage){
                                    if(gc.isMoveReady(unit.id())) {
                                        randomMove(gc, unit);
                                    }
                                }
                                else{
                                    if (gc.isMoveReady(unit.id())) {
                                        moveAlongBFSPath(gc, unit);
                                    }
                                }

                            }
                        }


                        //knight loop
                        else if (unit.unitType().equals(UnitType.Knight)) {

                        }


                        //mage loop
                        else if (unit.unitType().equals(UnitType.Mage)) {

                        }


                        //ranger loop
                        else if (unit.unitType().equals(UnitType.Ranger)) {
                            VecUnit enemiesInFiringRange = gc.senseNearbyUnitsByTeam(unitLocation, 50, enemyTeam);
                            boolean shouldMove = true;

                            if (enemiesInFiringRange.size() > 0) {

                                ArrayList<Unit> closestUnits = new ArrayList<Unit>();


                                //attack sequence
                                long minDistanceTo = unitLocation.distanceSquaredTo(enemiesInFiringRange.get(0).location().mapLocation());
                                long tempDistanceSquaredTo_Holder;
                                for (int j = 0; j < enemiesInFiringRange.size(); j++) {
                                    tempDistanceSquaredTo_Holder = unitLocation.distanceSquaredTo(enemiesInFiringRange.get(j).location().mapLocation());
                                    if (tempDistanceSquaredTo_Holder < minDistanceTo) {
                                        minDistanceTo = tempDistanceSquaredTo_Holder;
                                    }
                                }
                                for (int j = 0; j < enemiesInFiringRange.size(); j++) {
                                    if (enemiesInFiringRange.get(j).location().mapLocation().distanceSquaredTo(unitLocation) == minDistanceTo) {
                                        closestUnits.add(enemiesInFiringRange.get(j));
                                    }
                                }

                                //firing priority: closest target. Among equally close targets, fire at one with lowest health
                                Unit toFireAt = closestUnits.get(0);
                                //kite priority, run from closest target. among equally close targets, run from one with highest health
                                Unit toRunFrom = closestUnits.get(0);

                                long minHealth = closestUnits.get(0).health();
                                long maxHealth = closestUnits.get(0).health();
                                for (Unit unitOfNearest : closestUnits) {
                                    if (unitOfNearest.health() < minHealth) {
                                        minHealth = unitOfNearest.health();
                                        toFireAt = unitOfNearest;
                                    }
                                    if (unitOfNearest.health() > maxHealth){
                                        maxHealth = unitOfNearest.health();
                                        toRunFrom = unitOfNearest;
                                    }
                                }

                                //attack
                                if (gc.isAttackReady(unit.id())) {
                                    if (gc.canAttack(unit.id(), toFireAt.id())) {
                                        gc.attack(unit.id(), toFireAt.id());
                                    }
                                }

                                //kite away from close enemies sequence
                                if(gc.isMoveReady(unit.id())){
                                    tryMove(gc, unit, toRunFrom.location().mapLocation().directionTo(unitLocation));
                                }
                            }

                            if (shouldMove && gc.isMoveReady(unit.id())) {
                                if(gc.round() < 100) {
                                    moveAlongBFSPath(gc, unit);
                                }
                                else{
                                    randomMove(gc, unit);
                                }
                            }
                        }

                        //factory loop
                        //(last to make sure anything that wants to move out of a garrison already did (otherwise always unloaded)
                        //      spams units
                        else if (unit.unitType().equals(UnitType.Factory)) {
                            for (int j = 0; j < 8; j++) {
                                if (gc.canUnload(unit.id(), directions[j])) {
                                    gc.unload(unit.id(), directions[j]);
                                    break;
                                }
                            }
                            if (gc.canProduceRobot(unit.id(), unitTypeToProduce)) {
                                gc.produceRobot(unit.id(), unitTypeToProduce);
                            }
                        }


                    }
                }
                catch(Exception e){
                    System.out.println("Exception in main while loop");
                    e.printStackTrace();
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

    public static void randomMove(GameController gc, Unit unit){
        Random rand = new Random();
        tryMove(gc, unit, directions[rand.nextInt(8)]);
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