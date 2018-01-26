// import the API.
// See xxx for the javadocs.
import bc.*;

import java.awt.*;
import java.util.*;

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
            int producing = 0;

            int numFactories = 0;
            int numHealers = 0;
            int numKnights = 0;
            int numMages = 0;
            int numRangers = 0;
            int numRockets = 0;
            int numWorkers = 0;





            //MACRO control variables
            int maxWorkerNum = 8;
            int maxFactories = 5;
            boolean karboniteCollectionStage = false;
            boolean enoughFactories = false;
            UnitType unitTypeToProduce = UnitType.Ranger;

            HashMap<MapLocation, Direction[][]> workerKarboniteQueueEarth = new HashMap<MapLocation, Direction[][]>();

            MapLocation target;
            MapLocation initialEnemyLocation;
            MapLocation initialWorkerLocation;
            /**
             * equals 0 if symmetrical over x axis
             * equals 1 if symmetrical over y axis
             * equal 2 if symmetrical by rotation
             */
            int earthSymmetry;
            AsteroidPattern asteroidPattern;


            //initial research queue
            gc.queueResearch(UnitType.Worker);
            gc.queueResearch(UnitType.Ranger);
            gc.queueResearch(UnitType.Healer);
            gc.queueResearch(UnitType.Healer);
            gc.queueResearch(UnitType.Healer);
            gc.queueResearch(UnitType.Ranger);
            gc.queueResearch(UnitType.Worker);
            gc.queueResearch(UnitType.Rocket);


            //first turn code
            if(isSymmetricalOverX(earthMap)){
                earthSymmetry = 0;
            }
            else if(isSymmetricalOverY(earthMap)){
                earthSymmetry = 1;
            }
            else{
                earthSymmetry = 2;
            }


            if(gc.planet() == Planet.Earth) {
                //sets target to where first enemies are
                VecUnit startingUnits = gc.myUnits();
                MapLocation firstStartingUnitLocation = startingUnits.get(0).location().mapLocation();

                //if symmetrical over x
                if (earthSymmetry == 0){
                    target = new MapLocation(Planet.Earth, 
                                                firstStartingUnitLocation.getX(), 
                                                (int)earthMap.getHeight()-1-firstStartingUnitLocation.getY());
                }
                //if symmetrical over y
                else if (earthSymmetry == 1){
                    target = new MapLocation(Planet.Earth,
                            (int)earthMap.getWidth()-1-firstStartingUnitLocation.getX(),
                            firstStartingUnitLocation.getY());
                }
                //if rotationally symmetrical
                else{
                    target = new MapLocation(Planet.Earth,
                            (int)earthMap.getWidth()-1-firstStartingUnitLocation.getX(),
                            (int)earthMap.getHeight()-1-firstStartingUnitLocation.getY());
                }
                initialEnemyLocation = target.clone();
                initialWorkerLocation = firstStartingUnitLocation;
                spreadPathfindingMapEarth = updatePathfindingMap(target, earthMap);
                workerKarboniteQueueEarth.put(initialWorkerLocation, updatePathfindingMap(initialWorkerLocation, earthMap));
            }
            //MARS FIRST TURN
            else{
                //temp thing for target
                target = new MapLocation(Planet.Mars,0,0);
                //sets target to where first asteroid hits
                asteroidPattern = gc.asteroidPattern();
                for (int i = 0; i < 1000; i++) {
                    if(asteroidPattern.hasAsteroid(i) && marsMap.isPassableTerrainAt(asteroidPattern.asteroid(i).getLocation())!= 0){
                        target = asteroidPattern.asteroid(i).getLocation();
                        break;
                    }
                }
                spreadPathfindingMapMars = updatePathfindingMap(target, marsMap);
            }



            /*
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
            */



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
                    if (numFactories > maxFactories || numFactories > (gc.round()/20 + 1)){
                        enoughFactories = true;
                        karboniteCollectionStage = true;
                    }
                    else{
                        enoughFactories = false;
                    }

                    //updates queue for karbonite collection
                    if(gc.round() % 100 == 60){
                        if(gc.planet().equals(Planet.Earth)){
                            workerKarboniteQueueEarth.clear();
                            VecMapLocation allLocations = gc.allLocationsWithin(
                                        new MapLocation(Planet.Earth, 0, 0), (long)( Math.pow(earthDimensions.width,2)+Math.pow(earthDimensions.height,2)) );
                            System.out.println("# visible locations = " + allLocations.size());
                            for (int i = 0; i < allLocations.size(); i++) {
                                if(gc.canSenseLocation(allLocations.get(i)) && gc.karboniteAt(allLocations.get(i)) > 0){
                                    workerKarboniteQueueEarth.put(allLocations.get(i), updatePathfindingMap(allLocations.get(i), earthMap));
                                }
                            }

                        }
                    }
                    

                    //updates worker pathfinding map and karbonite target
                    if(workerKarboniteQueueEarth.size() > 0) {
                        Iterator it = workerKarboniteQueueEarth.entrySet().iterator();
                        MapLocation location;
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry) it.next();
                            location = (MapLocation) pair.getKey();
                            if (gc.canSenseLocation(location) && gc.karboniteAt(location) == 0) {
                                it.remove(); // avoids a ConcurrentModificationException
                            }
                        }
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

                            //replication loop (maxWorkerNum = 8 atm)
                            if ((unit.workerHasActed() == 0) && numWorkers <= maxWorkerNum) {
                                for (int j = 0; j < 8; j++) {
                                    if (gc.canReplicate(unit.id(), directions[j])) {
                                        gc.replicate(unit.id(), directions[j]);
                                        break;
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
                                if(!enoughFactories){
                                    if(gc.isMoveReady(unit.id())) {
                                        randomMove(gc, unit);
                                    }
                                }
                                else{
                                    if (gc.isMoveReady(unit.id())) {
                                        if(Planet.Earth.equals(gc.planet())){
                                            if(workerKarboniteQueueEarth.size() > 0){
                                                ArrayList<MapLocation> karboniteLocations = new ArrayList<MapLocation>();
                                                karboniteLocations.addAll(workerKarboniteQueueEarth.keySet());
                                                MapLocation toMoveTo = karboniteLocations.get(0);
                                                long closestDistance = unitLocation.distanceSquaredTo(toMoveTo);
                                                for (MapLocation location : karboniteLocations) {
                                                    if(unitLocation.distanceSquaredTo(location) < closestDistance){
                                                        toMoveTo = location;
                                                        closestDistance = unitLocation.distanceSquaredTo(location);
                                                    }
                                                }
                                                moveAlongBFSPath(gc, unit, workerKarboniteQueueEarth.get(toMoveTo));
                                            }
                                            else{
                                                randomMove(gc, unit);
                                            }
                                        }
                                        else{
                                            moveAlongBFSPath(gc, unit, spreadPathfindingMapMars);
                                        }
                                    }
                                }

                            }
                        }
                        
                      //healer loop
                        else if (unit.unitType().equals(UnitType.Healer)) {
                        	VecUnit enemiesInFiringRange = gc.senseNearbyUnitsByTeam(unitLocation, 70, enemyTeam);
                        	VecUnit friendliesInFiringRange = gc.senseNearbyUnitsByTeam(unitLocation, 30, myTeam);
                            boolean shouldMove = true;

                            if (enemiesInFiringRange.size() > 0) {
                                ArrayList<Unit> closestUnitsF = new ArrayList<Unit>();

                                //finding friendly sequence
                                long minDistanceToF = unitLocation.distanceSquaredTo(friendliesInFiringRange.get(0).location().mapLocation());
                                long tempDistanceSquaredTo_HolderF;
                                for (int j = 0; j < friendliesInFiringRange.size(); j++) {
                                    tempDistanceSquaredTo_HolderF = unitLocation.distanceSquaredTo(friendliesInFiringRange.get(j).location().mapLocation());
                                    if (tempDistanceSquaredTo_HolderF < minDistanceToF) {
                                        minDistanceToF = tempDistanceSquaredTo_HolderF;
                                    }
                                }
                                for (int j = 0; j < friendliesInFiringRange.size(); j++) {
                                    if (friendliesInFiringRange.get(j).location().mapLocation().distanceSquaredTo(unitLocation) == minDistanceToF) {
                                        closestUnitsF.add(friendliesInFiringRange.get(j));
                                    }
                                }

                                //firing priority: closest target. Among equally close targets, fire at one with lowest health
                                Unit toHealAt = closestUnitsF.get(0);
                                //kite priority, run from closest target. among equally close targets, run from one with highest health
                                Unit toRunTo = closestUnitsF.get(0);

                                long minHealth = closestUnitsF.get(0).health();
                               
                                //finds the lowest health friendly to heal
                                for (Unit unitOfNearest : closestUnitsF) {
                                    if (unitOfNearest.health() < minHealth) {
                                        minHealth = unitOfNearest.health();
                                        toHealAt = unitOfNearest;
                                        toRunTo = unitOfNearest;
                                    }
                                }

                                //heal
                                if (gc.isHealReady(unit.id())) {
                                    if (gc.canHeal(unit.id(), toHealAt.id())) {
                                        gc.heal(unit.id(), toHealAt.id());
                                    }
                                }

                                //tries to stay with rangers/allies
                                if(gc.isMoveReady(unit.id())){
                                    tryMove(gc, unit, toRunTo.location().mapLocation().directionTo(unitLocation));
                                }
                            }

                            if (shouldMove && gc.isMoveReady(unit.id())) {
                                if(gc.round() < 1000) {
                                    if(Planet.Earth.equals(gc.planet())){
                                        moveAlongBFSPath(gc, unit, spreadPathfindingMapEarth);
                                    }
                                    else{
                                        moveAlongBFSPath(gc, unit, spreadPathfindingMapMars);
                                    }
                                }
                                else{
                                    randomMove(gc, unit);
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
                                if(gc.round() < 1000) {
                                    if(Planet.Earth.equals(gc.planet())){
                                        moveAlongBFSPath(gc, unit, spreadPathfindingMapEarth);
                                    }
                                    else{
                                        moveAlongBFSPath(gc, unit, spreadPathfindingMapMars);
                                    }
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
                            if(numWorkers < 1){
                                unitTypeToProduce = UnitType.Worker;
                            }
                            else if(numRangers < 50 && producing < 3){
                                unitTypeToProduce = UnitType.Ranger;
                                producing++;
                            }
                            else if(numRangers >= 50 || producing == 3)
                            {
                            	unitTypeToProduce = UnitType.Healer;
                            	producing = 0;
                            }
                            if( enoughFactories || (gc.karbonite() > (bc.bcUnitTypeBlueprintCost(UnitType.Factory)+200) ) ) {
                                if (gc.canProduceRobot(unit.id(), unitTypeToProduce)) {
                                    gc.produceRobot(unit.id(), unitTypeToProduce);
                                }
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

    public static void moveAlongBFSPath(GameController gc, Unit unit, Direction[][] map){
        try {
            MapLocation unitLocation = unit.location().mapLocation();
            Direction directionToMove;
            directionToMove = bc.bcDirectionOpposite(getValueInPathfindingMap(unitLocation.getX(), unitLocation.getY(), map));

            tryMove(gc, unit, directionToMove);
        }
        catch(Exception e){
            System.out.println("BFS move failed on unit: " + unit);
            randomMove(gc, unit);
        }
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

    public static boolean isSymmetricalOverY(PlanetMap map){
        MapLocation toTest = new MapLocation(map.getPlanet(), 0,0);
        MapLocation opposite = new MapLocation(map.getPlanet(), 0 ,0);
        for (int i = 0; i < map.getWidth()/2; i++) {
            toTest.setX(i);
            opposite.setX((int)(map.getWidth()-1-i));
            for (int j = 0; j < map.getHeight(); j++) {
                toTest.setY(j);
                opposite.setY(j);
                if( !(map.isPassableTerrainAt(toTest) == map.isPassableTerrainAt(opposite)) ){
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isSymmetricalOverX(PlanetMap map){
        MapLocation toTest = new MapLocation(map.getPlanet(), 0,0);
        MapLocation opposite = new MapLocation(map.getPlanet(), 0 ,0);
        for (int i = 0; i < map.getHeight()/2; i++) {
            toTest.setY(i);
            opposite.setY((int)(map.getHeight()-1-i));
            for (int j = 0; j < map.getWidth(); j++) {
                toTest.setX(j);
                opposite.setX(j);
                if( !(map.isPassableTerrainAt(toTest) == map.isPassableTerrainAt(opposite)) ){
                    return false;
                }
            }
        }
        return true;
    }


}