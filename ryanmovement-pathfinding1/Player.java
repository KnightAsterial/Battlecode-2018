// import the API.
// See xxx for the javadocs.
import bc.*;

import java.awt.*;
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

    public static PlanetMap earthMap;
    public static PlanetMap marsMap;

    public static GameController gc;

    public static void main(String[] args) {
        try {
            // Connect to the manager, starting the game. Initializing methods
            gc = new GameController();
            earthMap = gc.startingMap(Planet.Earth);
            marsMap = gc.startingMap(Planet.Mars);
            spreadPathfindingMapEarth = new Direction[(int) earthMap.getHeight()][(int) earthMap.getWidth()];
            spreadPathfindingMapMars = new Direction[(int) marsMap.getHeight()][(int) marsMap.getWidth()];
            MapLocation target;
            boolean hasTarget = false;
            boolean targetChanged = true;

            target = new MapLocation(Planet.Earth, 4, 1);
            hasTarget = true;


            for (int i = 0; i < directions.length; i++) {
                System.out.println("directions[" + i + "] = " + directions[i]);
            }

            while (true) {
                System.out.println("Current round: " + gc.round() + "with ms time left: " + gc.getTimeLeftMs());

                if (targetChanged) {
                    updatePathfindingMap(target);
                    targetChanged = false;
                }

                // VecUnit is a class that you can think of as similar to ArrayList<Unit>, but immutable.
                VecUnit units = gc.myUnits();
                for (int i = 0; i < units.size(); i++) {
                    Unit unit = units.get(i);
                    // Most methods on gc take unit IDs, instead of the unit objects themselves.
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
    @param myLocation assumes the target location is on the same planet as the planet currently being run
     */
    public static void updatePathfindingMap(MapLocation myLocation){
        Direction[][] currentMap;
        boolean isEarth;
        if (gc.planet().equals(Planet.Earth)){
            isEarth = true;
        }
        else{
            isEarth = false;
        }

        if (isEarth){
            currentMap = new Direction[spreadPathfindingMapEarth.length][spreadPathfindingMapEarth[0].length];
        }
        else{
            currentMap = new Direction[spreadPathfindingMapMars.length][spreadPathfindingMapMars[0].length];
        }

        LinkedList<MapLocation> bfsQueue = new LinkedList<MapLocation>();
        MapLocation tempLocation;
        MapLocation currentBFSLocation;
        bfsQueue.add(myLocation);
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
                        if (isEarth){
                            if (earthMap.isPassableTerrainAt(new MapLocation(Planet.Earth, tempLocation.getX(), tempLocation.getY())) != 0) {
                                bfsQueue.add(tempLocation);
                                setValueInPathfindingMap(tempLocation.getX(), tempLocation.getY(), directions[i], currentMap);
                            }
                        }
                        else{
                            if (marsMap.isPassableTerrainAt(new MapLocation(Planet.Mars, tempLocation.getX(), tempLocation.getY())) != 0) {
                                bfsQueue.add(tempLocation);
                                setValueInPathfindingMap(tempLocation.getX(), tempLocation.getY(), directions[i], currentMap);
                            }
                        }
                    }
                }
            }
        }

        if (isEarth){
            spreadPathfindingMapEarth = currentMap;
        }
        else{
            spreadPathfindingMapMars = currentMap;
        }
    }

    public static Direction getValueInPathfindingMap(int x, int y, Direction[][] map){
        return map[map.length-1-y][x];
    }

    public static void setValueInPathfindingMap(int x, int y, Direction myDirection, Direction[][] map){
        map[map.length-1-y][x] = myDirection;
    }

}