package team006;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

/**
 * Created by andrewalbers on 9/14/16.
 */
public class SignalManager {
    public static int SIG_ASSIST = 1;

    public static void requestHelp(RobotController rc, MapLocation location) {
        try {
            MapLocation rcLoc = rc.getLocation();
            rc.broadcastMessageSignal(SIG_ASSIST, encodeLocation(rcLoc, location), 100);
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
