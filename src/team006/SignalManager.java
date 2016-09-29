package team006;

import battlecode.common.*;

/**
 * Created by andrewalbers on 9/14/16.
 */
public class SignalManager {
    public static int SIG_ASSIST = 1;
    public static int SIG_UPDATE_ARCHON_LOC = 2;
    public static int SIG_SCOUT = 3;

    public static void requestHelp(RobotController rc, MapInfo mapInfo, MapLocation location) {
        try {
            if (mapInfo.selfType == RobotType.ARCHON) {
                rc.broadcastMessageSignal(SIG_ASSIST, encodeLocation(mapInfo.selfLoc, location), 1000);
            } else {
                rc.broadcastSignal(100);
            }
        } catch (GameActionException gae) {
            System.out.println(gae.getMessage());
            gae.printStackTrace();
        }
    }

    public static void signalArchonLoc(RobotController rc, MapInfo mapInfo) {
        try {
            rc.broadcastMessageSignal(SIG_UPDATE_ARCHON_LOC, encodeLocation(mapInfo.selfLoc, mapInfo.selfLoc), 1000);
        } catch (GameActionException gae) {
            System.out.println(gae.getMessage());
            gae.printStackTrace();
        }
    }

    public static void scoutEnemies(RobotController rc, MapInfo mapInfo, RobotInfo[] enemies) {
        try {
            rc.broadcastMessageSignal(SIG_SCOUT, enemies.length, 1000);
        } catch (GameActionException gae) {
            System.out.println(gae.getMessage());
            gae.printStackTrace();
        }
    }

    // encodes a map location relative to the broadcaster's location in a single int
    // 0206 : location is +2 x and +6 y from broadcaster's location
    // 1206 : location is -2 x and +6 y from broadcaster's location
    public static int encodeLocation(MapLocation rcLoc, MapLocation targetLoc) {
        int diffX = targetLoc.x - rcLoc.x;
        int diffY = targetLoc.y - rcLoc.y;
        int locSignal = Math.abs(diffX) + (100 * Math.abs(diffY));
        locSignal += diffX < 0 ? 10 : 0;
        locSignal += diffY < 0 ? 1000 : 0;
        return locSignal;
    }

    public static MapLocation decodeLocation(MapLocation rcLoc, int locSignal) {
        int signal = locSignal;
        int diffX = locSignal / 1000 == 1 ? -1 : 1;
        signal = signal % 1000;
        diffX = diffX * ( signal / 100 );
        signal = signal % 100;
        int diffY = signal / 10 == 1 ? -1 : 1;
        signal = signal % 10;
        diffY = diffY * signal;
        return new MapLocation(rcLoc.x + diffX, rcLoc.y + diffY);
    }
}
