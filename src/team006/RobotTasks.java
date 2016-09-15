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

    public static int pursueTask(RobotController rc, Assignment assignment) {
        rc.setIndicatorString(0, "pursuing my task");
        try {
            if ( assignment.assignmentType == AssignmentManager.ARCH_COLLECT_PARTS ) {
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
}
