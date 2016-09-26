package team006;

import battlecode.common.*;
import scala.xml.PrettyPrinter;

/**
 * Created by andrewalbers on 9/14/16.
 */
public class RobotTasks {

    public static int TASK_NOT_GIVEN = -1;
    public static int TASK_IN_PROGRESS = 0;
    public static int TASK_COMPLETE = 1;
    public static int TASK_ABANDONED = 2;

    public static int pursueTask(RobotController rc, MapInfo mapInfo, Assignment assignment) {
        try {
            if ( assignment.assignmentType == AssignmentManager.ARCH_COLLECT_PARTS ) {
                return collectParts(rc, mapInfo, assignment.targetLocation, assignment.targetInt);
            } else if ( assignment.assignmentType == AssignmentManager.ARCH_BUILD_ROBOTS ){
                return buildRobot(rc, mapInfo);
            } else if ( assignment.assignmentType == AssignmentManager.BOT_MOVE_TO_LOC ) {
                return moveToLocation(rc, mapInfo, assignment.targetLocation);
            } else if ( assignment.assignmentType == AssignmentManager.BOT_ATTACK_MOVE_TO_LOC ){
                return attackMoveToLocation(rc, mapInfo, assignment.targetLocation);
            } else if ( assignment.assignmentType == AssignmentManager.BOT_TIMID_MOVE_TO_LOC ){
                return timidMoveToLocation(rc, mapInfo, assignment.targetLocation);
            } else if ( assignment.assignmentType == AssignmentManager.BOT_RETREAT_TO_NEAREST_ARCHON ){
                return retreatToLocation(rc, mapInfo, assignment.targetLocation);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return TASK_NOT_GIVEN;
    }

    // Move toward a location
    public static int moveToLocation(RobotController rc, MapInfo mapInfo, MapLocation targetLocation) {
        try {
            Direction dirToTarget = mapInfo.selfLoc.directionTo(targetLocation);

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
            } else if (rc.senseRobotAtLocation(mapInfo.selfLoc.add(dirToTarget)) == null) {
                // If cannot move forward but nothing blocking path
                rc.setIndicatorString(1, "abandoning task, nothing at target");
                return TASK_ABANDONED;
            } else if (targetLocation.equals(mapInfo.selfLoc.add(dirToTarget))) {
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
    public static int attackMoveToLocation(RobotController rc, MapInfo mapInfo, MapLocation targetLocation) {
        try {
            rc.setIndicatorString(0, "attack moving to location");
            RobotInfo[] nearbyBots = rc.senseHostileRobots(mapInfo.selfLoc, mapInfo.selfSenseRadius);
            int minDist = 99999;
            MapLocation attackLoc = null;
            // find and attack closest enemy bot
            for (RobotInfo info : nearbyBots) {
                double weaponDelay = rc.getWeaponDelay();
                if (rc.canAttackLocation(info.location)) {
                    if (weaponDelay >= 1) {
                        // if weapon is on delay, wait to recharge
                        return TASK_IN_PROGRESS;
                    }
                    // Picks closest enemy to attack
                    // TODO: attack the closest bot that this rc does most damage to
                    int thisDist = mapInfo.selfLoc.distanceSquaredTo(info.location);
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
            if (!mapInfo.selfLoc.equals(targetLocation)) {
                return moveToLocation(rc, mapInfo, targetLocation);
            }
        } catch (GameActionException gae) {
            System.out.println(gae.getMessage());
            gae.printStackTrace();
        }
        rc.setIndicatorString(1, "attack move task complete");
        return TASK_COMPLETE;
    }

    // Move toward a location but retreat and abandon if enemy sighted
    // This should typically only be used by scouts and archons
    public static int timidMoveToLocation(RobotController rc, MapInfo mapInfo, MapLocation targetLocation) {
        try {
            rc.setIndicatorString(1, "timid moving to location");
            RobotInfo[] nearbyBots = rc.senseHostileRobots(mapInfo.selfLoc, mapInfo.selfSenseRadius);
            for (RobotInfo info : nearbyBots) {
                if (mapInfo.selfType == RobotType.ARCHON){
                    // request assistance
                    SignalManager.requestHelp(rc, info.location);
                }
                return TASK_ABANDONED;
            }
            if (mapInfo.selfLoc != targetLocation) {
                return moveToLocation(rc, mapInfo, targetLocation);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return TASK_COMPLETE;
    }

    // Move toward a target location and collect parts in that area
    // Complete task when rc is on target location and cannot detect any parts
    public static int collectParts(RobotController rc, MapInfo mapInfo, MapLocation targetLocation, int radius) {
        try {
            rc.setIndicatorString(0, "collecting parts");
            if (mapInfo.selfLoc.distanceSquaredTo(targetLocation) > radius) {
                return timidMoveToLocation(rc, mapInfo, targetLocation);
            }

            Direction dirToMove = null;
            MapLocation[] partLocations = rc.sensePartLocations(mapInfo.selfSenseRadius);
            int minPartDist = 9999;

            for (int i = 0; i < partLocations.length; i++) {
                int partDist = mapInfo.selfLoc.distanceSquaredTo(partLocations[i]);
                if (partDist < minPartDist && partLocations[i].distanceSquaredTo(targetLocation) <= radius) {
                    minPartDist = partDist;
                    dirToMove = mapInfo.selfLoc.directionTo(partLocations[i]);
                }
            }
            if (dirToMove == null) {
                if (mapInfo.selfLoc.equals(targetLocation)) {
                    return TASK_COMPLETE;
                } else {
                    return timidMoveToLocation(rc, mapInfo, targetLocation);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return timidMoveToLocation(rc, mapInfo, targetLocation);
    }

    public static int buildRobot(RobotController rc, MapInfo mapInfo) {
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

    // Retreats to a given target location until no enemies in sight
    // If target location is null, moves in the opposite direction from the closest enemy
    public static int retreatToLocation(RobotController rc, MapInfo mapInfo, MapLocation targetLocation) {
        try {
            rc.setIndicatorString(0, "retreating");
            RobotInfo[] hostileBots = rc.senseHostileRobots(mapInfo.selfLoc, mapInfo.selfSenseRadius);
            if (hostileBots.length == 0) {
                return TASK_COMPLETE;
            } else if (targetLocation != null) {
                return moveToLocation(rc, mapInfo, targetLocation);
            } else {
                for (RobotInfo info : hostileBots) {
                    Direction oppositeDir = mapInfo.selfLoc.directionTo(info.location).opposite();
                    if (rc.canMove(oppositeDir)) {
                        rc.move(oppositeDir);
                        return TASK_IN_PROGRESS;
                    } else if (rc.canMove(oppositeDir.rotateRight())) {
                        rc.move(oppositeDir.rotateRight());
                        return TASK_IN_PROGRESS;
                    } else if (rc.canMove(oppositeDir.rotateLeft())) {
                        rc.move(oppositeDir.rotateLeft());
                        return TASK_IN_PROGRESS;
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
