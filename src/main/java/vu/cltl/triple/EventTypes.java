package vu.cltl.triple;

import com.hp.hpl.jena.rdf.model.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 10/11/2017.
 */
public class EventTypes {

    static final String[] types = {"eso:Attacking", "eso:BeingInExistence", "eso:Damaging", "eso:Destroying", "eso:Injuring",
            "eso:Killing", "fn:Attack", "fn:Catastrophe", "fn:Cause_harm", "fn:Cause_impact", "fn:Cause_to_end",
            "fn:Death", "fn:Destroying", "fn:Existence", "fn:Experience_bodily_harm", "fn:Firing", "fn:Hit_target",
            "fn:Killing", "fn:Recovery", "fn:Resurrection", "fn:Shoot_projectiles", "fn:Use_firearm"};

    static final String[] kills = {"eso:BeingInExistence",  "eso:Destroying",
            "eso:Killing", "fn:Cause_to_end",
            "fn:Death", "fn:Destroying", "fn:Existence",
            "fn:Killing"};

    static final String[] injuries = { "eso:Damaging", "eso:Injuring",
             "fn:Cause_harm", "fn:Cause_impact",
            "fn:Experience_bodily_harm", "fn:Hit_target",
            "fn:Recovery", "fn:Resurrection"};

    public static boolean isType(String type) {
        for (int i = 0; i < types.length; i++) {
            String s = types[i];
            if (s.equals(type)) return true;
        }
        return false;
    }

    public static boolean isKill(String type) {
        for (int i = 0; i < kills.length; i++) {
            String s = kills[i];
            if (s.equals(type)) return true;
        }
        return false;
    }

    public static boolean isInjury(String type) {
        for (int i = 0; i < injuries.length; i++) {
            String s = injuries[i];
            if (s.equals(type)) return true;
        }
        return false;
    }

    public static ArrayList<String> getEventSubjectUris(HashMap<String, ArrayList<Statement>> tripleMap) {
        Set keySet = tripleMap.keySet();
        ArrayList<String> eventUris = new ArrayList<String>();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String tripleKey = keys.next();
            ArrayList<Statement> statements = tripleMap.get(tripleKey);
            for (int i = 0; i < statements.size(); i++) {
                Statement statement = statements.get(i);
                if (eventTypeMatch(statement)) {
                    eventUris.add(tripleKey);
                    break;
                }
            }
        }
        return eventUris;
    }

    public static ArrayList<String> getAllEventSubjectUris(HashMap<String, ArrayList<Statement>> tripleMap) {
        Set keySet = tripleMap.keySet();
        ArrayList<String> eventUris = new ArrayList<String>();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String tripleKey = keys.next();
            ArrayList<Statement> statements = tripleMap.get(tripleKey);
            for (int i = 0; i < statements.size(); i++) {
                Statement statement = statements.get(i);
                    eventUris.add(tripleKey);
                    break;
            }
        }
        return eventUris;
    }

    /**
     * KS util
     * @param statement
     * @return
     */
    public static boolean eventLabelMatch (Statement statement) {
        if (statement.getPredicate().getLocalName().equals("label")) {
            String objValue = TrigUtil.getPrettyNSValue(statement.getObject().toString()).toLowerCase();
            if (objValue.startsWith("injure")) {
                return true;
            } else if (objValue.startsWith("wound")) {
                return true;
            } else if (objValue.startsWith("shoot")) {
                return true;
            } else if (objValue.startsWith("fire")) {
                return true;
            } else if (objValue.startsWith("shoot")) {
                return true;
            } else if (objValue.startsWith("shot")) {
                return true;
            } else if (objValue.startsWith("kill")) {
                return true;
            } else if (objValue.startsWith("murder")) {
                return true;
            } else if (objValue.startsWith("dead")) {
                return true;
            } else if (objValue.startsWith("die")) {
                return true;
            } else if (objValue.startsWith("death")) {
                return true;
            }
        }
        return false;
    }

    /**
     * KS util
     * @param statement
     * @return
     */
    public static boolean eventTypeMatch (Statement statement) {
        if (statement.getPredicate().getLocalName().equals("type")) {
            String objValue = TrigUtil.getPrettyNSValue(statement.getObject().toString());
            if (EventTypes.isType(objValue))  {
                return true;
            }
        }
        return false;
    }


}
