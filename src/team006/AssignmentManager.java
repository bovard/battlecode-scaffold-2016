package team006;

import battlecode.common.*;

import java.util.Random;

/**
 * Created by andrewalbers on 9/14/16.
 */
public class AssignmentManager {
    public static int ARCH_COLLECT_PARTS = 1; //
    public static int ARCH_BUILD_ROBOTS = 2;
    public static int ARCH_ACTIVATE_NEUTRALS = 3;

    public static Assignment getAssignment(RobotController rc, Random rand) {

        int assignmentType = 0;
        int targetId = 0;
        MapLocation targetLocation = null;

        if ( rc.getType() == RobotType.ARCHON ) {
            if (true) { // TODO: Add a condition to trigger this archon to collect parts

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
            }
        }

        return new Assignment(targetId, assignmentType, targetLocation);
    }
}
