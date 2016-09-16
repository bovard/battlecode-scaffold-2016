package team006;

import battlecode.common.*;

/**
 * Created by andrewalbers on 9/14/16.
 */
public class RobotTasks {

    public static int TASK_NOT_GIVEN = -1;
    public static int TASK_IN_PROGRESS = 0;
    public static int TASK_COMPLETE = 1;
    public static int TASK_ABANDONED = 2;

    public static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
            Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
    public static RobotType[] robotTypes = {RobotType.SCOUT, RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER,
            RobotType.GUARD, RobotType.GUARD, RobotType.VIPER, RobotType.TURRET};

    public static int pursueTask(RobotController rc, Assignment assignment) {
        rc.setIndicatorString(0, "pursuing my task");
        try {
            if ( assignment.assignmentType == AssignmentManager.ARCH_COLLECT_PARTS ) {
                return moveToLocation(rc, assignment.targetLocation);
            } else if ( assignment.assignmentType == AssignmentManager.ARCH_BUILD_ROBOTS ){
                return buildRobot(rc);
            } else if ( assignment.assignmentType == AssignmentManager.BOT_MOVE_TO_LOC ) {
                return moveToLocation(rc, assignment.targetLocation);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return TASK_NOT_GIVEN;
    }

    // Move toward a location
    public static int moveToLocation(RobotController rc, MapLocation targetLocation) {
        try {
            rc.setIndicatorString(0, "moving to Location");
            Direction dirToMove = rc.getLocation().directionTo(targetLocation);
            if (rc.canMove(dirToMove)) {
                rc.move(dirToMove);
            } else if (rc.senseRubble(rc.getLocation().add(dirToMove)) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
                // If there's rubble in this direction, clear it
                rc.clearRubble(dirToMove);
            } else if (rc.getLocation().equals(targetLocation)) {
                // If goal reached
                return TASK_COMPLETE;
            } else {
                // If can't move there and no rubble to clear...
                return TASK_ABANDONED;
                // TODO: don't assume this is a map border. Could be something else blocking path
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return TASK_IN_PROGRESS;
    }

    public static int buildRobot(RobotController rc) {
        try {
            rc.setIndicatorString(0, "building Robots");
            // Choose a random unit to build
            RobotType typeToBuild = RobotType.SOLDIER;
            // Check for sufficient parts
            if (rc.hasBuildRequirements(typeToBuild)) {
                // Choose a random direction to try to build in
                Direction dirToBuild = Direction.NORTH;
                for (int i = 0; i < 8; i++) {
                    // If possible, build in this direction
                    if (rc.canBuild(dirToBuild, typeToBuild)) {
                        rc.build(dirToBuild, typeToBuild);
                        return TASK_COMPLETE;
                    } else {
                        // Rotate the direction to try
                        dirToBuild = dirToBuild.rotateLeft();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return TASK_IN_PROGRESS;
    }
}
