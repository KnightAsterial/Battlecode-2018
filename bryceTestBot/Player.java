// import the API.
// See xxx for the javadocs.
import java.util.Random;

import bc.*;

public class Player {

    public static void main(String[] args) {

        // MapLocation is a data structure you'll use a lot.
        MapLocation loc = new MapLocation(Planet.Earth, 10, 20);
        System.out.println("loc: "+loc+", one step to the Northwest: "+loc.add(Direction.Northwest));
        System.out.println("loc x: "+loc.getX());

        // One slightly weird thing: some methods are currently static methods on a static class called bc.
        // This will eventually be fixed :/
        System.out.println("Opposite of " + Direction.North + ": " + bc.bcDirectionOpposite(Direction.North));

        // Connect to the manager, starting the game
        GameController gc = new GameController();

        // Direction is a normal java enum.
        Direction[] directions = Direction.values();
        
        Random generator = new Random();
        int n = generator.nextInt(8);
        Direction dirs[] = new Direction[8];
        dirs[0] = Direction.East;
        dirs[1] = Direction.North;
        dirs[2] = Direction.Northeast;
        dirs[3] = Direction.Northwest;
        dirs[4] = Direction.South;
        dirs[5] = Direction.Southeast;
        dirs[6] = Direction.Southwest;
        dirs[7] = Direction.West;
        
        while (true) {
            System.out.println("Current round: "+gc.round());
            // VecUnit is a class that you can think of as similar to ArrayList<Unit>, but immutable.
            VecUnit units = gc.myUnits();
            for (int i = 0; i < units.size(); i++) {
                Unit unit = units.get(i);
                
                while (gc.isMoveReady(unit.id()))
                {
	                // Most methods on gc take unit IDs, instead of the unit objects themselves.
	                if (gc.canMove(unit.id(), dirs[n])) {
	                    gc.moveRobot(unit.id(), dirs[n]);
	                }
	                n = generator.nextInt(8);
                }
            }
            // Submit the actions we've done, and wait for our next turn.
            gc.nextTurn();
        }
    }
}