package team006;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by andrewalbers on 9/25/16.
 */

public class MapInfo {
    public Map<Integer, MapLocation> archonLocations = new HashMap<>();
    public ArrayList<MapLocation> enemyLocations = new ArrayList<>();
    public int roundNum = 0;
    public RobotType selfType = null;
    public Team selfTeam = null;
    public int selfSenseRadiusSq = 0;
    public double selfWeaponDelay = 0;
    public MapLocation selfLoc = null;
    public Signal urgentSignal = null;
    public int[] spawnSchedule = null;
    public int timeTillSpawn = 999999;

    public MapInfo(RobotController rc) {
        spawnSchedule = rc.getZombieSpawnSchedule().getRounds();
        selfType = rc.getType();
        selfSenseRadiusSq = selfType.sensorRadiusSquared;
    }

    public void updateAll(RobotController rc) {
        // Read and update signals
        Map<Integer, MapLocation> newArchonPositions = new HashMap<Integer, MapLocation>();
        Signal[] signals = rc.emptySignalQueue();
        selfLoc = rc.getLocation();
        selfWeaponDelay = rc.getWeaponDelay();
        roundNum = rc.getRoundNum();
        urgentSignal = null;

        // Update Zombie Spawn Date
        if (spawnSchedule.length > 0) {
            if (spawnSchedule[0] < roundNum) {
                for (int i = 0; i < spawnSchedule.length; i++){
                    if (spawnSchedule[i] >= roundNum) {
                        spawnSchedule = Arrays.copyOfRange(spawnSchedule, i, spawnSchedule.length);
                        break;
                    }
                }
            }
            timeTillSpawn = spawnSchedule[0] - roundNum;
        } else {
            timeTillSpawn = 999999;
        }

        // Process Signals
        MapLocation thisLocation;
        for (Signal signal : signals){
            if (signal.getTeam() == selfTeam) {
                thisLocation = signal.getLocation();
                int[] message = signal.getMessage();
                if (message != null) {
                    if (message[0] == SignalManager.SIG_ASSIST) {
                        urgentSignal = signal;
                    } else if (message[0] == SignalManager.SIG_UPDATE_ARCHON_LOC) {
                        newArchonPositions.put(signal.getID(),signal.getLocation());
                    }
                } else {
                    if (enemyLocations.contains(thisLocation) == false) {
                        enemyLocations.add(thisLocation);
                        rc.setIndicatorString(4, "adding location " + thisLocation.x + " " + thisLocation.y);
                    }
                }
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
