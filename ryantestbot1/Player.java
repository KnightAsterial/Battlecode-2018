// import the API.
// See xxx for the javadocs.
import bc.*;

public class Player {
    public static void main(String[] args) {
        // You can use other files in this directory, and in subdirectories.
        Extra extra = new Extra(27);
        System.out.println(extra.toString());

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
        
        //RESEARCH QUEUE
        gc.queueResearch(UnitType.Worker);
        
        int numFactories = 0;
        int numHealers = 0;
        int numKnights = 0;
        int numMages = 0;
        int numRangers = 0;
        int numRockets = 0;
        int numWorkers = 0;

        while (true) {
        	try{
	            System.out.println("Current round: "+gc.round() + " with time left: " + gc.getTimeLeftMs());
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
	                
	                boolean hasPerformedAction = false;
	                boolean shouldMove = true;
	                
	                //worker loop
	                if (unit.unitType() == UnitType.Worker){
	                	//senses adjacent friendly within 2 tiles
	                	VecUnit unitsNearWorker = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(), 8, gc.team());
	                	//if there is a blueprint to build, build it
	                	for (int index = 0; index < unitsNearWorker.size(); i++){
	                		Unit nearbyUnit = unitsNearWorker.get(index);
	                		if (gc.canBuild(unit.id(), nearbyUnit.id())){
	                			gc.build(unit.id(), nearbyUnit.id());
	                			hasPerformedAction = true;
	                			shouldMove = false;
	                			break;
	                		}
	                	}
	                	
	                	//blueprints a factory if not adjacent to any other factory
	                	if (!hasPerformedAction && numFactories <= 5){
	                		//only runs this if there is enough karbonite to build factory...
	                		if (gc.karbonite() > bc.bcUnitTypeBlueprintCost(UnitType.Factory)){
		                		//scan adjacent tiles
		                		MapLocation testLocation = unit.location().mapLocation();
		                		//tests tiles in all directions of worker
		                		for (int directionIndex = 0; directionIndex < directions.length && !hasPerformedAction; directionIndex++){
		                			
		                			boolean shouldBuildHere = true;
		                			if (!directions[directionIndex].equals(Direction.Center)){
		                				testLocation = unit.location().mapLocation();
		                				testLocation = testLocation.add(directions[directionIndex]);
		                				for (int x = 0; x < unitsNearWorker.size(); x++){
		                					if (unitsNearWorker.get(x).unitType().equals(UnitType.Factory) 
		                							&& unitsNearWorker.get(x).location().mapLocation().isAdjacentTo(testLocation)){
		                						//there is a factory adjacent, thus don't build
		                						shouldBuildHere = false;
		                						break;
		                					}
		                				}
		                				if (shouldBuildHere){
		                					if (gc.canBlueprint(unit.id(), UnitType.Factory, directions[directionIndex])){
		                						gc.blueprint(unit.id(), UnitType.Factory, directions[directionIndex]);
		                						hasPerformedAction = true;
		                						shouldMove = false;
		                					}
		                				}
		                			}
		                			
		                		}
	                		}
	                	}
	                }
	                
	                if (shouldMove){
		                // Most methods on gc take unit IDs, instead of the unit objects themselves.
		                if (gc.isMoveReady(unit.id()) && gc.canMove(unit.id(), Direction.Southeast)) {
		                    gc.moveRobot(unit.id(), Direction.Southeast);
		                }
	                }

	            }
	            // Submit the actions we've done, and wait for our next turn.
	            gc.nextTurn();
        	}
        	catch(Exception e){
        		e.printStackTrace();
        	}
        }
    }
    
}