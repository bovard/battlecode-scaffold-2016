package team006;

import java.util.Random;
import battlecode.common.*;

/**
 * Created by andrewalbers on 9/15/16.
 */
public class Decision {

    public static Random rand = new Random(1);

    public static boolean doCollectParts(RobotController rc) {
        if ( rc.getTeamParts() < 100 ) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean doRunAway(RobotController rc, MapInfo mapInfo) {
        if ( rc.senseHostileRobots(mapInfo.selfLoc, mapInfo.selfSenseRadiusSq).length > 0) {
            return true;
        } else {
            return false;
        }
    }

    // return index of robot type to build from Constants.ROBOT_TYPES
    public static int botToBuild(RobotController rc, MapInfo mapInfo) {
        if (mapInfo.timeTillSpawn < 80) {
            return 2;
        } else if (rand.nextInt(16) == 15) {
            return 0;
        } else if (rand.nextInt(16) == 15) {
            return 4;
        }
        return 1;
    }
}
