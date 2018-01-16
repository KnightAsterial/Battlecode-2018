// import the API.
// See xxx for the javadocs.
import bc.*;

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

    | [] [] [] []
    | [] [] [] []
    | [] [] [] []
    | [] [] [] []
    | ----------- > x

    can be accessed as spreadPathfindingMap[height-y][x] or using the function

    getValueInPathfindingMap(x,y)
    setValueInPathfindingMap(x,y);
     */
    public static Direction[][] spreadPathfindingMap;

    public static void main(String[] args) {

        // Connect to the manager, starting the game. Initializing methods
        GameController gc = new GameController();
        PlanetMap earthMap = gc.startingMap(Planet.Earth);
        spreadPathfindingMap = new Direction[(int)earthMap.getHeight()][(int)earthMap.getWidth()];
        MapLocation target;
        boolean hasTarget = false;

        target = new MapLocation(Planet.Earth, 3,3,);
        hasTarget = true;


        for (int i = 0; i < directions.length; i++) {
            System.out.println("directions[" + i + "] = " + directions[i]);
        }


        while (true) {
            System.out.println("Current round: "+gc.round() + "with ms time left: " + gc.getTimeLeftMs());

            if (hasTarget == true){
                updatePathfindingMap();
            }

            // VecUnit is a class that you can think of as similar to ArrayList<Unit>, but immutable.
            VecUnit units = gc.myUnits();
            for (int i = 0; i < units.size(); i++) {
                Unit unit = units.get(i);

                // Most methods on gc take unit IDs, instead of the unit objects themselves.
                if (gc.isMoveReady(unit.id())) {
                    tryMove(gc, unit, testLocation);
                }
            }
            // Submit the actions we've done, and wait for our next turn.
            gc.nextTurn();
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

    public static void updatePathfindingMap(){
        //ASUDHIAS UD aSKJDH ASLKJDH ASKLJH sakJDH KASJ h
    }

    public static Direction getValueInPathfindingMap(int x, int y){
        return spreadPathfindingMap[spreadPathfindingMap.length-y][x];
    }

    public static void setValueInPathfindingMap(int x, int y, Direction myDirection){
        spreadPathfindingMap[spreadPathfindingMap.length-y][x] = myDirection;
    }

}