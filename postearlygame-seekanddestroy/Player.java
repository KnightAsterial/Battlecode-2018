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
    public static Direction[][] spreadPathfindingMapMars;


    public static Dimension earthDimensions;
    public static Dimension marsDimensions;

    public static PlanetMap earthMap;
    public static PlanetMap marsMap;

    public static Team myTeam;
    public static Team enemyTeam;

    public static GameController gc;

    public static Random rand = new Random();

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





            //MACRO control variables
            //workers starts with 4, increases to 8
            int maxWorkerNum = 4;
            int maxFactories = 5;
            boolean karboniteCollectionStage = false;
            boolean enoughFactories = false;
            UnitType unitTypeToProduce = UnitType.Ranger;

            HashMap<MapLocation, Direction[][]> workerKarboniteQueueEarth = new HashMap<MapLocation, Direction[][]>();

            MapLocation target;
            MapLocation initialWorkerLocation;

            AsteroidPattern asteroidPattern;

            boolean escapeToMarsMode = false;
            HashMap<MapLocation, Direction[][]> rocketsOnEarthLocations = new HashMap<MapLocation, Direction[][]>();
            ArrayList<MapLocation> rocketsWaitingForUnits = new ArrayList<MapLocation>();
            boolean pruneRocketLocationsOnEarth = false;

            LinkedList<MapLocation> passableLocationsOnMars = new LinkedList<MapLocation>();

            boolean workerSentToMars = false;

            HashMap<MapLocation, Direction[][]> factoriesToBeBuild = new HashMap<MapLocation, Direction[][]>();

            HashMap<MapLocation, Direction[][]> enemyTargetsEarth = new HashMap<MapLocation, Direction[][]>();
            HashMap<MapLocation, Direction[][]> enemyTargetsMars = new HashMap<MapLocation, Direction[][]>();
            ArrayList<MapLocation> initialEnemyLocations = new ArrayList<MapLocation>();


            //initial research queue
            //FORTESTINGPURPOSES
            //gc.queueResearch(UnitType.Rocket);

            gc.queueResearch(UnitType.Worker);
            gc.queueResearch(UnitType.Ranger);
            gc.queueResearch(UnitType.Healer);
            gc.queueResearch(UnitType.Healer);
            gc.queueResearch(UnitType.Healer);
            gc.queueResearch(UnitType.Ranger);
            gc.queueResearch(UnitType.Worker);
            gc.queueResearch(UnitType.Rocket);





            if(gc.planet() == Planet.Earth) {
                //sets target to where first enemies are
                VecUnit startingUnits = gc.myUnits();
                MapLocation firstStartingUnitLocation = startingUnits.get(0).location().mapLocation();
                initialWorkerLocation = firstStartingUnitLocation;
                workerKarboniteQueueEarth.put(initialWorkerLocation, updatePathfindingMap(initialWorkerLocation, earthMap));

                VecUnit allStartingUnitsEarth = earthMap.getInitial_units();
                for (int i = 0; i < allStartingUnitsEarth.size(); i++) {
                    Unit currentUnit = allStartingUnitsEarth.get(i);
                    if(!currentUnit.team().equals(myTeam)){
                        initialEnemyLocations.add(currentUnit.location().mapLocation());
                    }
                }

                Direction[][] mapToEnemyLocation;
                for (int i = 0; i < allStartingUnitsEarth.size(); i++) {
                    Unit currentUnit = allStartingUnitsEarth.get(i);
                    boolean canReachAny = true;
                    for(Direction[][] generatedMap : enemyTargetsEarth.values()){
                        if (getValueInPathfindingMap(currentUnit.location().mapLocation().getX(), currentUnit.location().mapLocation().getY(), generatedMap) == null){
                            canReachAny = false;
                            break;
                        }
                    }
                    if(enemyTargetsEarth.keySet().size() == 0 || !canReachAny){
                        for (int j = 0; j < initialEnemyLocations.size(); j++) {
                            mapToEnemyLocation = updatePathfindingMap(initialEnemyLocations.get(j), earthMap);
                            if(getValueInPathfindingMap(currentUnit.location().mapLocation().getX(), currentUnit.location().mapLocation().getY(), mapToEnemyLocation) != null){
                                enemyTargetsEarth.put(initialEnemyLocations.get(j), mapToEnemyLocation);
                                break;
                            }
                        }
                    }
                }
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

            MapLocation locationForTestingPassableTerrain = new MapLocation(Planet.Mars, 1,1);
            for (int i = 0; i < marsDimensions.width; i++) {
                for (int j = 0; j < marsDimensions.height; j++) {
                    locationForTestingPassableTerrain.setX(i);
                    locationForTestingPassableTerrain.setY(j);
                    if(marsMap.isPassableTerrainAt(locationForTestingPassableTerrain) != 0){
                        passableLocationsOnMars.add(locationForTestingPassableTerrain.clone());
                    }
                }
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
                    if (numFactories > maxFactories || ((numFactories)*2) > (numRangers)){
                        enoughFactories = true;
                        karboniteCollectionStage = true;
                        maxWorkerNum = 8;
                    }
                    else{
                        enoughFactories = false;
                    }
                    if (gc.round() > 600){
                        escapeToMarsMode = true;
                    }

                    //updates queue for karbonite collection
                    if(gc.round() % 100 == 60 || gc.round() == 2){
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

                    //updates factoriesToBuild moveQ
                    if(factoriesToBeBuild.size() > 0){
                        Iterator it = factoriesToBeBuild.entrySet().iterator();
                        MapLocation location;
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry) it.next();
                            location = (MapLocation) pair.getKey();
                            if (gc.canSenseLocation(location) && gc.hasUnitAtLocation(location) && gc.senseUnitAtLocation(location).unitType().equals(UnitType.Factory)) {
                                if(gc.senseUnitAtLocation(location).structureIsBuilt() != 0) {
                                    it.remove(); // avoids a ConcurrentModificationException
                                }
                            }
                            else{
                                it.remove();
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

                        //code for all units to run------------------

                        //delete targets if close enough and no enemies are within radius of 70 to that target
                        if(gc.planet().equals(Planet.Earth)) {
                            if(enemyTargetsEarth.size() > 0){
                                Iterator it = enemyTargetsEarth.entrySet().iterator();
                                MapLocation location;
                                while (it.hasNext()) {
                                    Map.Entry pair = (Map.Entry) it.next();
                                    location = (MapLocation) pair.getKey();
                                    if(unitLocation.distanceSquaredTo(location) <= 2){
                                        it.remove();
                                    }
                                }
                            }
                        }
                        else{
                            if(enemyTargetsMars.size() > 0){
                                Iterator it = enemyTargetsMars.entrySet().iterator();
                                MapLocation location;
                                while (it.hasNext()) {
                                    Map.Entry pair = (Map.Entry) it.next();
                                    location = (MapLocation) pair.getKey();
                                    if(unitLocation.distanceSquaredTo(location) <= 2){
                                        if(gc.senseNearbyUnitsByTeam(location, 70, enemyTeam).size() <= 0){
                                            it.remove();
                                        }
                                    }
                                }
                            }
                        }

                        if (unit.unitType().equals(UnitType.Worker)) {
                            boolean shouldMove = true;

                            //senses adjacent friendly within 2 tiles
                            VecUnit unitsNearWorker = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(), 8, gc.team());
                            ArrayList<Unit> unitsAdjacentToWorker = new ArrayList<Unit>();
                            for (int j = 0; j < unitsNearWorker.size(); j++) {
                                if(unitLocation.distanceSquaredTo(unitsNearWorker.get(j).location().mapLocation()) <= 2){
                                    unitsAdjacentToWorker.add(unitsNearWorker.get(j));
                                }
                            }

                            //if there is a blueprint to build, build it
                            for (int index = 0; index < unitsNearWorker.size(); index++) {
                                Unit nearbyUnit = unitsNearWorker.get(index);
                                if (gc.canBuild(unit.id(), nearbyUnit.id())) {
                                    gc.build(unit.id(), nearbyUnit.id());
                                    shouldMove = false;
                                    break;
                                }
                            }

                            //blueprinting loop
                            if(gc.planet().equals(Planet.Earth)) {
                                if (escapeToMarsMode) {
                                    if (gc.karbonite() > bc.bcUnitTypeBlueprintCost(UnitType.Rocket)) {
                                        boolean hasBlueprinted = false;
                                        for (int j = 0; j < 8; j++) {
                                            if (gc.canBlueprint(unit.id(), UnitType.Rocket, directions[j])) {
                                                gc.blueprint(unit.id(), UnitType.Rocket, directions[j]);
                                                rocketsOnEarthLocations.put(unitLocation.add(directions[j]), updatePathfindingMap(unitLocation.add(directions[j]), earthMap));
                                                hasBlueprinted = true;
                                                shouldMove = false;
                                                break;
                                            }
                                        }
                                        if (!hasBlueprinted) {
                                            MapLocation tempLocationForBlueprinting;
                                            Unit tempUnitForTesting;
                                            for (int j = 0; j < 8; j++) {
                                                tempLocationForBlueprinting = unitLocation.add(directions[j]);
                                                if (gc.hasUnitAtLocation(tempLocationForBlueprinting)) {
                                                    tempUnitForTesting = gc.senseUnitAtLocation(tempLocationForBlueprinting);
                                                    if (tempUnitForTesting.team().equals(myTeam) && !tempUnitForTesting.unitType().equals(UnitType.Worker) && !tempUnitForTesting.unitType().equals(UnitType.Factory)
                                                            && !tempUnitForTesting.unitType().equals(UnitType.Rocket)) {
                                                        gc.disintegrateUnit(tempUnitForTesting.id());
                                                        if (gc.canBlueprint(unit.id(), UnitType.Rocket, directions[j])) {
                                                            gc.blueprint(unit.id(), UnitType.Rocket, directions[j]);
                                                            shouldMove = false;
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }


                                //blueprints a factory if not adjacent to any other factory
                                else if ((unit.workerHasActed() == 0) && !enoughFactories) {
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
                                                    factoriesToBeBuild.put(unitLocation.add(directions[directionIndex]), updatePathfindingMap(unitLocation.add(directions[directionIndex]), earthMap));
                                                    shouldMove = false;
                                                }
                                            }

                                        }
                                    }
                                }
                            }

                            //replication loop (maxWorkerNum = 8 atm)
                            if ((unit.workerHasActed() == 0) && numWorkers <= maxWorkerNum) {
                                if(escapeToMarsMode){
                                    if(gc.karbonite() > (bc.bcUnitTypeBlueprintCost(UnitType.Rocket)+40)){
                                        for (int j = 0; j < 8; j++) {
                                            if (gc.canReplicate(unit.id(), directions[j])) {
                                                gc.replicate(unit.id(), directions[j]);
                                                break;
                                            }
                                        }
                                    }
                                }
                                else {
                                    if(!enoughFactories || workerKarboniteQueueEarth.size() > 0) {
                                        for (int j = 0; j < 8; j++) {
                                            if (gc.canReplicate(unit.id(), directions[j])) {
                                                gc.replicate(unit.id(), directions[j]);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            if(gc.round() > 750 && gc.planet().equals(Planet.Mars) && (unit.workerHasActed() == 0)){
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

                            if (shouldMove && gc.isMoveReady(unit.id())) {
                                if(escapeToMarsMode && gc.planet().equals(Planet.Earth)){
                                    moveToClosestInTable(gc, unit, rocketsOnEarthLocations);
                                }
                                else if (factoriesToBeBuild.size() > 0){
                                    moveToClosestInTable(gc, unit, factoriesToBeBuild);
                                }
                                /*
                                else if(!enoughFactories){
                                    randomMove(gc, unit);
                                }
                                */
                                else{
                                    if (gc.isMoveReady(unit.id())) {
                                        if(Planet.Earth.equals(gc.planet())){
                                            moveToClosestInTable(gc, unit, workerKarboniteQueueEarth);
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
                            //heal sequence: if heal is possible, heal the lowest health target
                            if(gc.isHealReady(unit.id())) {
                                VecUnit friendliesInFiringRange = gc.senseNearbyUnitsByTeam(unitLocation, 30, myTeam);

                                if (friendliesInFiringRange.size() > 0) {
                                    //healing priority: heal the one with the lowest health
                                    Unit toHealAt = friendliesInFiringRange.get(0);
                                    double minPercentHealth = (double)toHealAt.health() / (double)toHealAt.maxHealth();
                                    //finds the lowest health friendly to heal
                                    for (int j = 0; j < friendliesInFiringRange.size(); j++) {
                                        if( ((double)friendliesInFiringRange.get(j).health()/(double)friendliesInFiringRange.get(j).maxHealth()) < minPercentHealth){
                                            toHealAt = friendliesInFiringRange.get(j);
                                            minPercentHealth = (double)toHealAt.health() / (double)toHealAt.maxHealth();
                                        }
                                    }

                                    //heal (already checked for isHealReady){
                                    if (gc.canHeal(unit.id(), toHealAt.id())) {
                                        gc.heal(unit.id(), toHealAt.id());
                                    }
                                }
                            }

                            //kite sequence: run away from the closest enemy and among those, the one with the highest health (greater radius than rangers)
                            if(gc.isMoveReady(unit.id())) {
                                VecUnit enemiesInFiringRange = gc.senseNearbyUnitsByTeam(unitLocation, 82, enemyTeam);

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

                                    Unit toRunFrom = closestUnits.get(0);
                                    long maxHealth = closestUnits.get(0).health();
                                    for (Unit unitOfNearest : closestUnits) {
                                        if (unitOfNearest.health() > maxHealth){
                                            maxHealth = unitOfNearest.health();
                                            toRunFrom = unitOfNearest;
                                        }
                                    }

                                    //kite away from close enemies sequence
                                    if(gc.isMoveReady(unit.id())){
                                        tryMove(gc, unit, toRunFrom.location().mapLocation().directionTo(unitLocation));
                                    }
                                }
                            }

                            if (gc.isMoveReady(unit.id())) {
                                if(Planet.Earth.equals(gc.planet())){
                                    if(escapeToMarsMode){
                                        boolean adjacentToRocket = false;
                                        VecUnit adjacentUnits = gc.senseNearbyUnitsByTeam(unitLocation, 2, myTeam);
                                        for (int j = 0; j < adjacentUnits.size(); j++) {
                                            if(adjacentUnits.get(j).unitType().equals(UnitType.Rocket)){
                                                adjacentToRocket = true;
                                                break;
                                            }
                                        }
                                        if(!adjacentToRocket) {
                                            moveToClosestInTable(gc, unit, rocketsOnEarthLocations);
                                        }
                                    }
                                    else {
                                        combatMoveTowardsEnemy(gc, unit, enemyTargetsEarth, earthMap);
                                    }
                                }
                                else{
                                    combatMoveTowardsEnemy(gc, unit, enemyTargetsMars, marsMap);
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

                            if (gc.isMoveReady(unit.id())) {
                                if(Planet.Earth.equals(gc.planet())){
                                    if(escapeToMarsMode){
                                        boolean adjacentToRocket = false;
                                        VecUnit adjacentUnits = gc.senseNearbyUnitsByTeam(unitLocation, 2, myTeam);
                                        for (int j = 0; j < adjacentUnits.size(); j++) {
                                            if(adjacentUnits.get(j).unitType().equals(UnitType.Rocket)){
                                                adjacentToRocket = true;
                                                break;
                                            }
                                        }
                                        if(!adjacentToRocket) {
                                            moveToClosestInTable(gc, unit, rocketsOnEarthLocations);
                                        }
                                    }
                                    else {
                                        combatMoveTowardsEnemy(gc, unit, enemyTargetsEarth, earthMap);
                                    }
                                }
                                else{
                                    combatMoveTowardsEnemy(gc, unit, enemyTargetsMars, marsMap);
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
                            if(numWorkers < 1 || escapeToMarsMode){
                                unitTypeToProduce = UnitType.Worker;
                            }
                            else if (numHealers*3 < numRangers){
                                unitTypeToProduce = UnitType.Healer;
                            }
                            else{
                                unitTypeToProduce = UnitType.Ranger;
                            }

                            if(escapeToMarsMode){
                                if(gc.karbonite() > (bc.bcUnitTypeBlueprintCost(UnitType.Rocket)+100)){
                                    if (gc.canProduceRobot(unit.id(), unitTypeToProduce)) {
                                        gc.produceRobot(unit.id(), unitTypeToProduce);
                                    }
                                }
                            }
                            else if ( enoughFactories || (gc.karbonite() > (bc.bcUnitTypeBlueprintCost(UnitType.Factory)+41) ) ) {
                                if (gc.canProduceRobot(unit.id(), unitTypeToProduce)) {
                                    gc.produceRobot(unit.id(), unitTypeToProduce);
                                }
                            }
                        }

                        //rocket loop
                        else if (unit.unitType().equals(UnitType.Rocket)){
                            rocketsWaitingForUnits.add(unitLocation);
                            VecUnitID unitsInRocket = unit.structureGarrison();
                            if(gc.planet().equals(Planet.Earth)){
                                //launch conditions: rocket full, over turn 740, or if rocket is under fire and has taken more than half its health in damage
                                if(unitsInRocket.size() == unit.structureMaxCapacity()
                                        || gc.round() > 740
                                        || (unit.structureIsBuilt()!=0 && ((double)unit.health() / (double)unit.maxHealth() < 0.5)) ){
                                    MapLocation launchTarget = passableLocationsOnMars.get(rand.nextInt(passableLocationsOnMars.size()));
                                    if(gc.canLaunchRocket(unit.id(), launchTarget)){
                                        gc.launchRocket(unit.id(), launchTarget);
                                        pruneRocketLocationsOnEarth = true;
                                    }
                                }
                                else{
                                    Unit unitToLoad;
                                    for (int j = 0; j < 8; j++) {
                                        if(unitsInRocket.size() < unit.structureMaxCapacity()){
                                            if(gc.hasUnitAtLocation(unitLocation.add(directions[j]))){
                                                unitToLoad = gc.senseUnitAtLocation(unitLocation.add(directions[j]));
                                                if(unitToLoad.team().equals(myTeam) && gc.canLoad(unit.id(), unitToLoad.id())){

                                                    if(!unitToLoad.unitType().equals(UnitType.Worker)) {
                                                        gc.load(unit.id(), unitToLoad.id());
                                                    }
                                                    //only sends one worker to mars
                                                    else{
                                                        if(!workerSentToMars){
                                                            gc.load(unit.id(), unitToLoad.id());
                                                            workerSentToMars = true;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        else{
                                            break;
                                        }
                                    }
                                }
                            }
                            //on mars
                            else{
                                if(unitsInRocket.size() > 0){
                                    for (int j = 0; j < 8; j++) {
                                        if(gc.canUnload(unit.id(), directions[j])){
                                            gc.unload(unit.id(), directions[j]);
                                            if(unitsInRocket.size() > 0){
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }


                    }
                    //post unit loop

                    //updates pathfinding to rockets on earth
                    if(pruneRocketLocationsOnEarth) {
                        if (rocketsOnEarthLocations.size() > 0) {
                            Iterator it = rocketsOnEarthLocations.entrySet().iterator();
                            MapLocation location;
                            while (it.hasNext()) {
                                Map.Entry pair = (Map.Entry) it.next();
                                location = (MapLocation) pair.getKey();
                                if (!rocketsWaitingForUnits.contains(location)) {
                                    it.remove(); // avoids a ConcurrentModificationException
                                }
                            }
                        }
                        pruneRocketLocationsOnEarth = false;
                    }
                    rocketsWaitingForUnits.clear();

                    /*
                    if(escapeToMarsMode) {
                        System.out.println(rocketsOnEarthLocations.keySet());
                        System.out.println(rocketsWaitingForUnits);
                        rocketsWaitingForUnits.clear();
                    }
                    */


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

    public static void moveToClosestInTable(GameController gc, Unit unit, HashMap<MapLocation, Direction[][]> table){
        MapLocation unitLocation = unit.location().mapLocation();
        if(table.size() > 0){
            ArrayList<MapLocation> tableLocations = new ArrayList<MapLocation>();
            tableLocations.addAll(table.keySet());

            boolean atleastOneNotNull = false;
            //temp values
            MapLocation toMoveTo = null;
            long closestDistance = 1000000;
            for(MapLocation location : tableLocations){
                if(getValueInPathfindingMap(unitLocation.getX(), unitLocation.getY(), table.get(location)) != null){
                    atleastOneNotNull = true;
                    toMoveTo = location;
                    closestDistance = unitLocation.distanceSquaredTo(toMoveTo);
                    break;
                }
            }
            if(atleastOneNotNull){
                for (MapLocation location : tableLocations) {
                    if(getValueInPathfindingMap(unitLocation.getX(), unitLocation.getY(), table.get(location)) != null && unitLocation.distanceSquaredTo(location) < closestDistance){
                        toMoveTo = location;
                        closestDistance = unitLocation.distanceSquaredTo(location);
                    }
                }
                moveAlongBFSPath(gc, unit, table.get(toMoveTo));
            }
            else{
                randomMove(gc, unit);
            }


        }
        else{
            randomMove(gc, unit);
        }
    }

    public static void combatMoveTowardsEnemy(GameController gc, Unit unit, HashMap<MapLocation, Direction[][]> table, PlanetMap planetMap){
        MapLocation unitLocation = unit.location().mapLocation();
        if(table.size() > 0){
            ArrayList<MapLocation> enemyLocations = new ArrayList<MapLocation>();
            enemyLocations.addAll(table.keySet());

            boolean atleastOneNotNull = false;
            //temp values
            MapLocation toMoveTo = null;
            long closestDistance = 1000000;
            for(MapLocation location : enemyLocations){
                if(getValueInPathfindingMap(unitLocation.getX(), unitLocation.getY(), table.get(location)) != null){
                    atleastOneNotNull = true;
                    toMoveTo = location;
                    closestDistance = unitLocation.distanceSquaredTo(toMoveTo);
                    break;
                }
            }
            if(atleastOneNotNull){
                for (MapLocation location : enemyLocations) {
                    if(getValueInPathfindingMap(unitLocation.getX(), unitLocation.getY(), table.get(location)) != null && unitLocation.distanceSquaredTo(location) < closestDistance){
                        toMoveTo = location;
                        closestDistance = unitLocation.distanceSquaredTo(location);
                    }
                }
                moveAlongBFSPath(gc, unit, table.get(toMoveTo));
            }
            else{
                if(gc.getTimeLeftMs() > 1000 || (gc.getTimeLeftMs() < 1000 && gc.round()%3==0)) {
                    VecUnit nearbyEnemies = gc.senseNearbyUnitsByTeam(unitLocation, 70, enemyTeam);
                    if (nearbyEnemies.size() <= 0) {
                        randomMove(gc, unit);
                    } else {
                        //run through list of enemies I can see. if i can move towards at least one of them, add it to the table and go to it, otherwise random move
                        Direction[][] generatedMap;
                        boolean foundNewTarget = false;
                        for (int i = 0; i < nearbyEnemies.size(); i++) {
                            generatedMap = updatePathfindingMap(nearbyEnemies.get(i).location().mapLocation(), planetMap);
                            if (getValueInPathfindingMap(unitLocation.getX(), unitLocation.getY(), generatedMap) != null) {
                                foundNewTarget = true;
                                table.put(nearbyEnemies.get(i).location().mapLocation(), generatedMap);
                                moveAlongBFSPath(gc, unit, generatedMap);
                                break;
                            }
                        }
                        if (!foundNewTarget) {
                            randomMove(gc, unit);
                        }
                    }
                }
                else{
                    randomMove(gc, unit);
                }
            }


        }
        else{
            if (gc.getTimeLeftMs() > 1000 || (gc.getTimeLeftMs() < 1000 && gc.round()%3==0) ) {
                VecUnit nearbyEnemies = gc.senseNearbyUnitsByTeam(unitLocation, 70, enemyTeam);
                if (nearbyEnemies.size() <= 0) {
                    randomMove(gc, unit);
                } else {
                    //run through list of enemies I can see. if i can move towards at least one of them, add it to the table and go to it, otherwise random move
                    Direction[][] generatedMap;
                    boolean foundNewTarget = false;
                    for (int i = 0; i < nearbyEnemies.size(); i++) {
                        generatedMap = updatePathfindingMap(nearbyEnemies.get(i).location().mapLocation(), planetMap);
                        if (getValueInPathfindingMap(unitLocation.getX(), unitLocation.getY(), generatedMap) != null) {
                            foundNewTarget = true;
                            table.put(nearbyEnemies.get(i).location().mapLocation(), generatedMap);
                            moveAlongBFSPath(gc, unit, generatedMap);
                            break;
                        }
                    }
                    if (!foundNewTarget) {
                        randomMove(gc, unit);
                    }
                }
            }
            else{
                randomMove(gc, unit);
            }
        }
    }


}