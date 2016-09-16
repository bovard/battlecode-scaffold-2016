package team006;

import battlecode.common.*;

import java.util.Random;

/**
 * Created by andrewalbers on 9/14/16.
 */
public class AssignmentManager {
    public static int ARCH_COLLECT_PARTS = 1; // MapLocation
    public static int ARCH_BUILD_ROBOTS = 2; // --
    public static int ARCH_ACTIVATE_NEUTRALS = 3;
    public static int BOT_MOVE_TO_LOC = 4;

    public static Assignment getAssignment(RobotController rc, Random rand) {

        int assignmentType = 0;
        int targetInt = 0;
        MapLocation targetLocation = null;

        if ( rc.getType() == RobotType.ARCHON ) {
            if ( Decision.doCollectParts(rc) ) { // TODO: Add a condition to trigger this archon to collect parts
                assignmentType = ARCH_COLLECT_PARTS;
                // this will eventually be smarter. Basically, we tell the archon to collect parts
                // within some distance of a target location
                MapLocation rcLoc = rc.getLocation();
                targetLocation = new MapLocation(rcLoc.x + rand.nextInt(100) - 50, rcLoc.y + rand.nextInt(100) - 50);
                targetInt = 50;
            } else {
                assignmentType = ARCH_BUILD_ROBOTS;
            }
        } else if ( rc.getType() == RobotType.SOLDIER ){

            assignmentType = BOT_MOVE_TO_LOC;

            MapLocation rcLoc = rc.getLocation();
            targetLocation = new MapLocation(rcLoc.x + rand.nextInt(10) - 5, rcLoc.y + rand.nextInt(10) - 5);

        } else if ( rc.getType() == RobotType.GUARD ){

            assignmentType = BOT_MOVE_TO_LOC;

            MapLocation rcLoc = rc.getLocation();
            targetLocation = new MapLocation(rcLoc.x + rand.nextInt(10) - 5, rcLoc.y + rand.nextInt(10) - 5);

        } else if ( rc.getType() == RobotType.SCOUT ){

            assignmentType = BOT_MOVE_TO_LOC;

            MapLocation rcLoc = rc.getLocation();
            targetLocation = new MapLocation(rcLoc.x + rand.nextInt(10) - 5, rcLoc.y + rand.nextInt(10) - 5);

        } else if ( rc.getType() == RobotType.VIPER ){

            assignmentType = BOT_MOVE_TO_LOC;

            MapLocation rcLoc = rc.getLocation();
            targetLocation = new MapLocation(rcLoc.x + rand.nextInt(10) - 5, rcLoc.y + rand.nextInt(10) - 5);

        } else if ( rc.getType() == RobotType.TURRET ){

        } else if ( rc.getType() == RobotType.TTM ) {

        }
        return new Assignment(targetInt, assignmentType, targetLocation);
    }
}
