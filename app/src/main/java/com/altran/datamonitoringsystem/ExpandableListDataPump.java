package com.altran.datamonitoringsystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpandableListDataPump {

    public static HashMap<String, List<String>> getData() {
        HashMap<String, List<String>> expandableListDetail = new HashMap<>();
        List<String> level = new ArrayList<>();
        level.add("level 1");
        expandableListDetail.put("normal",level);
        expandableListDetail.put("sleepy", level);
        expandableListDetail.put("late", level);
        expandableListDetail.put("angry", level);
        expandableListDetail.put("stressed", level);
        expandableListDetail.put("Under Medication", level);

        return expandableListDetail;
    }
}
