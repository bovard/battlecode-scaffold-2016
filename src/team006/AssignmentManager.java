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
    public static int BOT_ATTACK_MOVE_TO_LOC = 5;
    public static int BOT_TIMID_MOVE_TO_LOC = 6;
    public static int BOT_RETREAT_TO_NEAREST_ARCHON = 7;
    public static int BOT_PATROL = 8;
    public static int BOT_TURRET_DEFEND = 9;
    public static int BOT_SCOUT = 10;

    public static Assignment getAssignment(RobotController rc, Random rand, MapInfo mapInfo) {

        int assignmentType = 0;
        int targetInt = 0;
        MapLocation targetLocation = null;

        if ( rc.getType() == RobotType.ARCHON ) {
            if ( Decision.doRunAway(rc, mapInfo)) {
                assignmentType = BOT_RETREAT_TO_NEAREST_ARCHON;
                targetLocation = mapInfo.getNearestFriendlyArchonLoc(rc);
            } else if ( Decision.doCollectParts(rc) ) { // TODO: Add a condition to trigger this archon to collect parts
                assignmentType = ARCH_COLLECT_PARTS;
                // this will eventually be smarter. Basically, we tell the archon to collect parts
                // within some distance of a target location
                MapLocation rcLoc = rc.getLocation();
                targetLocation = new MapLocation(rcLoc.x + rand.nextInt(11) - 5, rcLoc.y + rand.nextInt(11) - 5);
                targetInt = 7;
            } else {
                assignmentType = ARCH_BUILD_ROBOTS;
                targetInt = Decision.botToBuild(rc, mapInfo);
            }
        } else if ( rc.getType() == RobotType.SOLDIER ){

            assignmentType = BOT_PATROL;
            MapLocation rcLoc = rc.getLocation();
            targetLocation = new MapLocation(rcLoc.x + rand.nextInt(21) - 10, rcLoc.y + rand.nextInt(21) - 10);

        } else if ( rc.getType() == RobotType.GUARD ){

            assignmentType = BOT_PATROL;
            MapLocation rcLoc = rc.getLocation();
            targetLocation = new MapLocation(rcLoc.x + rand.nextInt(21) - 10, rcLoc.y + rand.nextInt(21) - 10);

        } else if ( rc.getType() == RobotType.SCOUT ){

            assignmentType = BOT_SCOUT;
            MapLocation rcLoc = rc.getLocation();
            targetLocation = new MapLocation(rcLoc.x + rand.nextInt(1001) - 500, rcLoc.y + rand.nextInt(1001) - 500);

        } else if ( rc.getType() == RobotType.VIPER ){

            assignmentType = BOT_PATROL;
            MapLocation rcLoc = rc.getLocation();
            targetLocation = new MapLocation(rcLoc.x + rand.nextInt(21) - 10, rcLoc.y + rand.nextInt(21) - 10);

        } else if ( rc.getType() == RobotType.TURRET ){

            assignmentType = BOT_TURRET_DEFEND;

        } else if ( rc.getType() == RobotType.TTM ) {

        }
        return new Assignment(targetInt, assignmentType, targetLocation);
    }

    public static Assignment getSignalAssignment(RobotController rc, MapInfo mapInfo, Signal signal, Assignment assignment) {
        int[] message = signal.getMessage();

        int assignmentType = 0;
        int targetInt = 0;
        MapLocation targetLocation;

        if (message[0] == SignalManager.SIG_ASSIST) {
            if (mapInfo.selfType == RobotType.SOLDIER || mapInfo.selfType == RobotType.GUARD) {
                assignmentType = BOT_ATTACK_MOVE_TO_LOC;
                targetLocation = SignalManager.decodeLocation(signal.getLocation(),message[1]);
                return new Assignment(targetInt, assignmentType, targetLocation);
            } else {
                return assignment;
            }
        } else {
            return assignment;
        }
    }
}
