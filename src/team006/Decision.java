package team006;

import java.util.Random;
import battlecode.common.*;

/**
 * Created by andrewalbers on 9/15/16.
 */
public class Decision {

    public static Random rand = new Random(1);

    public static boolean doCollectParts(RobotController rc) {
        if ( rand.nextInt(10) > 5 ) {
            return true;
        } else {
            return false;
        }
    }
}
