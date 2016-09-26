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
        try {
            if ( assignment.assignmentType == AssignmentManager.ARCH_COLLECT_PARTS ) {
                return collectParts(rc, assignment.targetLocation, assignment.targetInt);
            } else if ( assignment.assignmentType == AssignmentManager.ARCH_BUILD_ROBOTS ){
                return buildRobot(rc);
            } else if ( assignment.assignmentType == AssignmentManager.BOT_MOVE_TO_LOC ) {
                return moveToLocation(rc, assignment.targetLocation);
            } else if ( assignment.assignmentType == AssignmentManager.BOT_ATTACK_MOVE_TO_LOC ){
                return attackMoveToLocation(rc, assignment.targetLocation);
            } else if ( assignment.assignmentType == AssignmentManager.BOT_TIMID_MOVE_TO_LOC ){
                return timidMoveToLocation(rc, assignment.targetLocation);
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
            MapLocation selfLoc = rc.getLocation();
            Direction dirToTarget = rc.getLocation().directionTo(targetLocation);

            if (rc.getLocation().equals(targetLocation)) {
                // If goal reached
                rc.setIndicatorString(1, "task complete");
                return TASK_COMPLETE;
            } else if (rc.canMove(dirToTarget)) {
                rc.setIndicatorString(1, "moving to location");
                rc.move(dirToTarget);
            } else if (rc.senseRubble(rc.getLocation().add(dirToTarget)) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
                // If there's rubble in this direction, clear it
                rc.setIndicatorString(1, "clearing rubble");
                rc.clearRubble(dirToTarget);
            } else if (rc.senseRobotAtLocation(selfLoc.add(dirToTarget)) == null) {
                // If cannot move forward but nothing blocking path
                rc.setIndicatorString(1, "abandoning task, nothing at target");
                return TASK_ABANDONED;
            } else if (targetLocation.equals(selfLoc.add(dirToTarget))) {
                // if another robot sitting on top of target Location, task is complete
                rc.setIndicatorString(1, "moved close enough, objective complete");
                return TASK_COMPLETE;
            } else {
                if (rc.canMove(dirToTarget.rotateRight())) {
                    rc.setIndicatorString(1, "moving 45 right");
                    rc.move(dirToTarget.rotateRight());
                } else if (rc.canMove(dirToTarget.rotateRight().rotateRight())) {
                    rc.setIndicatorString(1, "moving 90 right");
                    rc.move(dirToTarget.rotateRight().rotateRight());
                } else if (rc.canMove(dirToTarget.rotateLeft())) {
                    rc.setIndicatorString(1, "moving 45 right");
                    rc.move(dirToTarget.rotateLeft());
                } else if (rc.canMove(dirToTarget.rotateLeft().rotateLeft())) {
                    rc.setIndicatorString(1, "moving 90 left");
                    rc.move(dirToTarget.rotateLeft().rotateLeft());
                } else {
                    rc.setIndicatorString(1, "abandoning task, cannot move");
                    return TASK_ABANDONED;
                }
            }
        } catch (GameActionException gae) {
            System.out.println(gae.getMessage());
            gae.printStackTrace();
        }
        return TASK_IN_PROGRESS;
    }

    // Move toward a location attacking any enemies along the way
    public static int attackMoveToLocation(RobotController rc, MapLocation targetLocation) {
        try {
            rc.setIndicatorString(0, "attack moving to location");
            RobotType selfType = rc.getType();
            MapLocation selfLoc = rc.getLocation();
            Team selfTeam = rc.getTeam();
            int senseRadius = (int)Math.ceil(Math.sqrt(selfType.attackRadiusSquared));
            RobotInfo[] nearbyBots = rc.senseNearbyRobots(senseRadius);
            int minDist = 99999;
            MapLocation attackLoc = null;
            // find and attack closest enemy bot TODO: attack closest bot that this rc does most damage to
            for (RobotInfo info : nearbyBots) {
                double weaponDelay = rc.getWeaponDelay();
                // if enemy within range but weapon is on delay, state task in progress and wait to recharge
                if (info.team != selfTeam && info.team != Team.NEUTRAL && rc.canAttackLocation(info.location)) {
                    if (weaponDelay >= 1) {
                        return TASK_IN_PROGRESS;
                    }
                    int thisDist = selfLoc.distanceSquaredTo(info.location);
                    if (thisDist < minDist) {
                        minDist = thisDist;
                        attackLoc = info.location;
                    }
                }
            }
            if (attackLoc != null) {
                rc.setIndicatorString(1, "attacking location");
                rc.attackLocation(attackLoc);
                return TASK_IN_PROGRESS;
            }
            if (!rc.getLocation().equals(targetLocation)) {
                return moveToLocation(rc, targetLocation);
            }
        } catch (GameActionException gae) {
            System.out.println(gae.getMessage());
            gae.printStackTrace();
        }
        rc.setIndicatorString(1, "attack move task complete");
        return TASK_COMPLETE;
    }

    // Move toward a location but retreat and abandon if enemy sighted
    public static int timidMoveToLocation(RobotController rc, MapLocation targetLocation) {
        try {
            rc.setIndicatorString(1, "timid moving to location");
            RobotType selfType = rc.getType();
            Team selfTeam = rc.getTeam();
            RobotInfo[] nearbyBots = rc.senseNearbyRobots((int)Math.ceil((Math.sqrt(selfType.sensorRadiusSquared))));
            for (RobotInfo info : nearbyBots) {
                if (info.team != selfTeam && info.team != Team.NEUTRAL) {
                    return TASK_ABANDONED;
                }
            }
            if (rc.getLocation() != targetLocation) {
                return moveToLocation(rc, targetLocation);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return TASK_COMPLETE;
    }

    // Move toward a target location and collect parts in that area
    // Complete task when rc is on target location and cannot detect any parts
    public static int collectParts(RobotController rc, MapLocation targetLocation, int radius) {
        try {
            rc.setIndicatorString(0, "collecting parts");
            MapLocation rcLocation = rc.getLocation();
            if (rcLocation.distanceSquaredTo(targetLocation) > radius) {
                return timidMoveToLocation(rc, targetLocation);
            }

            Direction dirToMove = null;
            MapLocation[] partLocations = rc.sensePartLocations((int)(Math.sqrt(RobotType.ARCHON.sensorRadiusSquared)));
            int minPartDist = 9999;

            for (int i = 0; i < partLocations.length; i++) {
                int partDist = rc.getLocation().distanceSquaredTo(partLocations[i]);
                if (partDist < minPartDist && partLocations[i].distanceSquaredTo(targetLocation) <= radius) {
                    minPartDist = partDist;
                    dirToMove = rcLocation.directionTo(partLocations[i]);
                }
            }
            if (dirToMove == null) {
                if (rcLocation.equals(targetLocation)) {
                    return TASK_COMPLETE;
                } else {
                    return timidMoveToLocation(rc, targetLocation);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return timidMoveToLocation(rc, targetLocation);
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
        } catch (GameActionException gae) {
            System.out.println(gae.getMessage());
            gae.printStackTrace();
        }
        return TASK_IN_PROGRESS;
    }
}
