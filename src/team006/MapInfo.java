package team006;

import battlecode.common.*;

import java.util.*;

/**
 * Created by andrewalbers on 9/25/16.
 */

public class MapInfo {
    public Map<Integer, MapLocation> archonLocations = new HashMap<>();
    public ArrayList<MapLocation> enemyLocations = new ArrayList<>();
    public int roundNum = 0;
    public Map<Integer, Integer> scoutSignals = new HashMap<>(); // <scoutId : roundLastSignaled>
    public int selfScoutsCreated = 0;
    public RobotType selfType = null;
    public Team selfTeam = null;
    public int selfId;
    public int selfSenseRadiusSq = 0;
    public int selfAttackRadiusSq = 0;
    public double selfWeaponDelay = 0;
    public int cyclesSinceSignaling = 0;
    public MapLocation selfLoc = null;
    public Signal urgentSignal = null;
    public int[] spawnSchedule = null;
    public int timeTillSpawn = 999999;

    public MapInfo(RobotController rc) {
        spawnSchedule = rc.getZombieSpawnSchedule().getRounds();
        selfType = rc.getType();
        selfId = rc.getID();
        selfSenseRadiusSq = selfType.sensorRadiusSquared;
        selfAttackRadiusSq = selfType.attackRadiusSquared;
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
                    } else if (message[0] == SignalManager.SIG_SCOUT) {
                        scoutSignals.put(signal.getRobotID(),roundNum);
                    }
                }
            }
        }

        if (selfType == RobotType.ARCHON) {
            // stop recording last signals from scouts that are probably dead
            for (Iterator<Map.Entry<Integer, Integer>> it = scoutSignals.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Integer, Integer> entry = it.next();
                if (roundNum - entry.getValue() > 50) {
                    it.remove();
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

    public void incrementScoutsCreated() {
        selfScoutsCreated = selfScoutsCreated +1;
    }
}
