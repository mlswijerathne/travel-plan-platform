package com.travelplan.guide.dto;

import java.util.List;
import java.util.Map;

public class WeeklySchedule {
    private Map<String, List<TimeRange>> schedule;

    public WeeklySchedule() {}

    public Map<String, List<TimeRange>> getSchedule() { return schedule; }
    public void setSchedule(Map<String, List<TimeRange>> schedule) { this.schedule = schedule; }
}
