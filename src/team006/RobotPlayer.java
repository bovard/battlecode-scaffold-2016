package team006;

import battlecode.common.*;

import java.util.Random;

public class RobotPlayer {

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) {
        // You can instantiate variables here.
        Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
                Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
        RobotType[] robotTypes = {RobotType.SCOUT, RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER,
                RobotType.GUARD, RobotType.GUARD, RobotType.VIPER, RobotType.TURRET};

        Random rand = new Random(rc.getID());

        int myAttackRange = 0;
        int searchRadius = 10;
        int taskStatus = RobotTasks.TASK_NOT_GIVEN;
        Assignment assignment = null;

        Team myTeam = rc.getTeam();
        Team enemyTeam = myTeam.opponent();

        if (rc.getType() == RobotType.ARCHON) {
            while (true) {
                // This is a loop to prevent the run() method from returning. Because of the Clock.yield()
                // at the end of it, the loop will iterate once per game round.
                try {
                    if (rc.isCoreReady()) {
                        if ( taskStatus != RobotTasks.TASK_IN_PROGRESS ) {
                            assignment = AssignmentManager.getAssignment(rc, rand);
                            taskStatus = RobotTasks.TASK_IN_PROGRESS;
                            rc.setIndicatorString(0, "Received a task");
                        } else {
                            taskStatus = RobotTasks.pursueTask(rc, assignment);
                        }
                    }
                    Clock.yield();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}
