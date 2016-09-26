package team006;

import battlecode.common.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andrewalbers on 9/25/16.
 */
public class MapInfo {
    public Map<Integer, MapLocation> archonLocations = new HashMap<Integer, MapLocation>();
    public RobotType selfType = null;
    public Team selfTeam = null;
    public int selfSenseRadius = 0;
    public MapLocation selfLoc = null;
    public Signal urgentSignal = null;

    public MapInfo(RobotController rc) {
        selfType = rc.getType();
        selfTeam = rc.getTeam();
        selfLoc = rc.getLocation();
        selfSenseRadius = (int)Math.ceil((Math.sqrt(rc.getType().sensorRadiusSquared)));
    }

    public void updateSelf(RobotController rc) {
        selfLoc = rc.getLocation();

        // Read and update signals
        Map<Integer, MapLocation> newArchonPositions = new HashMap<Integer, MapLocation>();
        Signal[] signals = rc.emptySignalQueue();
        urgentSignal = null;
        for (Signal signal : signals){
            if (signal.getMessage()[0] == SignalManager.SIG_ASSIST) {
                urgentSignal = signal;
            } else if (signal.getTeam() == selfTeam) {
                newArchonPositions.put(signal.getID(),signal.getLocation());
            }
        }
        if (newArchonPositions.size() > 0) {
            archonLocations = newArchonPositions;
        }
    }

    public MapLocation getNearestFriendlyArchonLoc(RobotController rc) {
        int nearestDist = 999999;
        MapLocation nearestLocation = null;
        int rcId = rc.getID();
        MapLocation rcLoc = rc.getLocation();
        for (Map.Entry<Integer, MapLocation> archonLocation : archonLocations.entrySet()){
            if (archonLocation.getKey() != rcId) {
                if (rcLoc.distanceSquaredTo(archonLocation.getValue()) < nearestDist) {
                    nearestLocation = archonLocation.getValue();
                }
            }
        }
        return nearestLocation;
    }
}
