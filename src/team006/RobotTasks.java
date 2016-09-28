package team006;

import battlecode.common.*;
import scala.reflect.internal.*;
import scala.reflect.internal.Constants;
import scala.xml.PrettyPrinter;

import java.awt.*;
import java.util.ArrayList;

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
                return buildRobot(rc, mapInfo, assignment.targetInt);
            } else if ( assignment.assignmentType == AssignmentManager.BOT_MOVE_TO_LOC ) {
                return moveToLocation(rc, mapInfo, assignment.targetLocation);
            } else if ( assignment.assignmentType == AssignmentManager.BOT_ATTACK_MOVE_TO_LOC ){
                return attackMoveToLocation(rc, mapInfo, assignment.targetLocation);
            } else if ( assignment.assignmentType == AssignmentManager.BOT_TIMID_MOVE_TO_LOC ){
                return timidMoveToLocation(rc, mapInfo, assignment.targetLocation);
            } else if ( assignment.assignmentType == AssignmentManager.BOT_RETREAT_TO_NEAREST_ARCHON ){
                return retreatToLocation(rc, mapInfo, assignment.targetLocation);
            } else if ( assignment.assignmentType == AssignmentManager.BOT_PATROL ){
                return attackMoveToLocation(rc, mapInfo, assignment.targetLocation);
            } else if (assignment.assignmentType == AssignmentManager.BOT_TURRET_DEFEND) {
                return turretDefend(rc, mapInfo);
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
            } else if (rc.senseRubble(mapInfo.selfLoc.add(dirToTarget)) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH
                    && rc.senseRubble(mapInfo.selfLoc.add(dirToTarget)) <= GameConstants.RUBBLE_OBSTRUCTION_THRESH * 5) {
                // If there's a reasonable amount of rubble in this direction, clear it
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

//    public static int patrol(RobotController rc, MapInfo mapInfo, MapLocation targetLocation) {
//        try {
//            rc.setIndicatorString(0, "patrolling");
//
//            // move until near designated target area
//            if (mapInfo.selfLoc.equals(targetLocation) == false) {
//                return attackMoveToLocation(rc, mapInfo, targetLocation);
//            }
//
//            MapLocation targetEnemyLocation = null;
//            ArrayList<MapLocation> enemyLocations = mapInfo.enemyLocations;
//            int minEnemyDist = 9999999;
//
//            for (MapLocation enemyLocation : enemyLocations ) {
//                int enemyDist = mapInfo.selfLoc.distanceSquaredTo(enemyLocation);
//                if (enemyDist < minEnemyDist) {
//                    minEnemyDist = enemyDist;
//                    targetEnemyLocation = enemyLocation;
//                }
//            }
//            if (targetEnemyLocation == null) {
//                // if no enemy locations found, task complete
//                return TASK_COMPLETE;
//            } else {
//                return attackMoveToLocation(rc, mapInfo, targetEnemyLocation);
//            }
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//            e.printStackTrace();
//        }
//        return attackMoveToLocation(rc, mapInfo, targetLocation);
//    }

    // just use attackMoveToLocation, since
    public static int turretDefend(RobotController rc, MapInfo mapInfo){
        try {
            return attackMoveToLocation(rc, mapInfo, mapInfo.selfLoc);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return TASK_ABANDONED;
    }

    // Move toward a location attacking any enemies along the way
    public static int attackMoveToLocation(RobotController rc, MapInfo mapInfo, MapLocation targetLocation) {
        try {
            rc.setIndicatorString(0, "attack moving to location");
            RobotInfo[] hostileBots = rc.senseHostileRobots(mapInfo.selfLoc, mapInfo.selfSenseRadiusSq);
            int minZombieDist = 99999;
            int minOpponentDist = 99999;
            int minRange = mapInfo.selfType == RobotType.TURRET ? 5 : 0;
            MapLocation attackLoc = null;
            MapLocation zombieLoc = null;
            MapLocation opponentLoc = null;

            // find and attack closest enemy bot
            for (RobotInfo info : hostileBots) {
                // Find closest enemy location TODO: find optimal enemy location
                int thisDist = mapInfo.selfLoc.distanceSquaredTo(info.location);
                if (info.team.equals(Team.ZOMBIE)) {
                    if (thisDist < minZombieDist && thisDist > minRange) {
                        minZombieDist = thisDist;
                        zombieLoc = info.location;
                    }
                } else if (thisDist < minOpponentDist && thisDist > minRange) {
                    minOpponentDist = thisDist;
                    opponentLoc = info.location;
                }
            }

            if (zombieLoc != null) {
                attackLoc = zombieLoc;
            }
            if (opponentLoc != null) {
                if (attackLoc == null || mapInfo.selfType != RobotType.GUARD) {
                    attackLoc = opponentLoc;
                }
            }

            if (attackLoc != null) {
                if (mapInfo.selfWeaponDelay >= 1) {
                    rc.setIndicatorString(1, "recharging");
                    if (mapInfo.selfType == RobotType.TURRET) {
                        return TASK_IN_PROGRESS;
                    } else {
                        // if mobile, pursue target while recharging
                        return moveToLocation(rc, mapInfo, attackLoc);
                    }
                } else if (rc.canAttackLocation(attackLoc)) {
                    rc.setIndicatorString(1, "attacking location");
                    rc.attackLocation(attackLoc);
                    return TASK_IN_PROGRESS;
                } else {
                    if (mapInfo.selfType == RobotType.TURRET) {
                        rc.setIndicatorString(1, "no enemies in range");
                        return TASK_IN_PROGRESS;
                    } else {
                        // if mobile, pursue target
                        return moveToLocation(rc, mapInfo, attackLoc);
                    }
                }
            } else if (mapInfo.selfType == RobotType.TURRET) {
                // no enemies found, keep on sitting there
                rc.setIndicatorString(1, "watching for enemies");
                return TASK_IN_PROGRESS;
            } else if (!mapInfo.selfLoc.equals(targetLocation)) {
                rc.setIndicatorString(1, "moving toward target location");
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
            RobotInfo[] hostileRobots = rc.senseHostileRobots(mapInfo.selfLoc, mapInfo.selfSenseRadiusSq);
            for (RobotInfo info : hostileRobots) {
                if (mapInfo.selfType == RobotType.ARCHON){
                    // request assistance
                    SignalManager.requestHelp(rc, info.location);
                }
                return TASK_ABANDONED;
            }
            if (mapInfo.selfType == RobotType.ARCHON) {
                RobotInfo[] neutralRobots = rc.senseNearbyRobots(mapInfo.selfLoc, mapInfo.selfSenseRadiusSq, Team.NEUTRAL);
                for (RobotInfo neutralInfo : neutralRobots) {
                    if (mapInfo.selfLoc.distanceSquaredTo(neutralInfo.location) == 1) {
                        rc.activate(neutralInfo.location);
                        rc.setIndicatorString(1, "activating a neutral");
                        return TASK_IN_PROGRESS;
                    }
                }
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
            if (mapInfo.selfLoc.distanceSquaredTo(targetLocation) > radius * radius) {
                return timidMoveToLocation(rc, mapInfo, targetLocation);
            }


            MapLocation targetPartLocation = null;
            MapLocation[] partLocations = rc.sensePartLocations(mapInfo.selfSenseRadiusSq);
            int minPartDist = 999999;

            for (MapLocation partLocation : partLocations ) {
                int partDist = mapInfo.selfLoc.distanceSquaredTo(partLocation);
                if (partDist < minPartDist && partLocation.distanceSquaredTo(targetLocation) <= radius * radius) {
                    minPartDist = partDist;
                    targetPartLocation = partLocation;
                }
            }
            if (targetPartLocation == null) {
                if (mapInfo.selfLoc.equals(targetLocation)) {
                    // if no parts found and archon in center of collecting region, task complete
                    return TASK_COMPLETE;
                } else {
                    // if no parts seen, move toward center of target region
                    return timidMoveToLocation(rc, mapInfo, targetLocation);
                }
            } else {
                return timidMoveToLocation(rc, mapInfo, targetPartLocation);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return timidMoveToLocation(rc, mapInfo, targetLocation);
    }

    public static int buildRobot(RobotController rc, MapInfo mapInfo, int typeIndex) {
        try {
            rc.setIndicatorString(0, "building Robots");
            // Choose a random unit to build
            RobotType typeToBuild = team006.Constants.ROBOT_TYPES[typeIndex];
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
            RobotInfo[] hostileBots = rc.senseHostileRobots(mapInfo.selfLoc, mapInfo.selfSenseRadiusSq);
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
