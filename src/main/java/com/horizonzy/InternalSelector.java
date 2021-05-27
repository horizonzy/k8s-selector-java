package com.horizonzy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class InternalSelector {

    List<Requirement> requirementList = new ArrayList<>();

    public InternalSelector() {
    }

    public void addRequire(Requirement requirement) {
        requirementList.add(requirement);
    }

    public void sort() {
        requirementList.sort(Comparator.comparing(Requirement::getKey));
    }

    public boolean matches(Map<String, String> labels) {
        for (Requirement requirement : requirementList) {
            if (!requirement.matches(labels)) {
                return false;
            }
        }
        return true;
    }

    public boolean empty() {
        return requirementList == null || requirementList.size() == 0;
    }

    @Override
    public String toString() {
        return "InternalSelector{" +
                "requirementList=" + requirementList +
                '}';
    }
}
