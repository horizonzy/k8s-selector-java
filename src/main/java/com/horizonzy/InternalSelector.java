package com.horizonzy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
}
