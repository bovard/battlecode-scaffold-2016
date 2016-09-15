package team006;

import battlecode.common.*;

/**
 * Created by andrewalbers on 9/14/16.
 */
public class Assignment {
    public int targetId;
    public int assignmentType;
    public MapLocation targetLocation;

    public Assignment(int tarInt, int assType, MapLocation tarLoc) {
        targetId = tarInt;
        assignmentType = assType;
        targetLocation = tarLoc;
    }
}
