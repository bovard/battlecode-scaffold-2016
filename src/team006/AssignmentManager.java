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
        int targetId = 0;
        MapLocation targetLocation = null;

        if ( rc.getType() == RobotType.ARCHON ) {
            if ( Decision.doCollectParts(rc) ) { // TODO: Add a condition to trigger this archon to collect parts

                assignmentType = ARCH_COLLECT_PARTS;

                MapLocation[] partLocations = rc.sensePartLocations(RobotType.ARCHON.sensorRadiusSquared);
                if (partLocations.length < 1) {
                    MapLocation rcLoc = rc.getLocation();
                    targetLocation = new MapLocation(rcLoc.x + rand.nextInt(10) - 5, rcLoc.y + rand.nextInt(10) - 5);
                }
                int minPartDist = 9999;
                for (int i = 0; i < partLocations.length; i++) {
                    int partDist = rc.getLocation().distanceSquaredTo(partLocations[i]);
                    if (partDist < minPartDist) {
                        minPartDist = partDist;
                        targetLocation = partLocations[i];
                    }
                }
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
        return new Assignment(targetId, assignmentType, targetLocation);
    }
}
