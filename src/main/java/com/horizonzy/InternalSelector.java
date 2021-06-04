package com.horizonzy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InternalSelector {

    private List<Requirement> requirementList;

    public InternalSelector() {
        requirementList = new ArrayList<>();
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

    public Tuple<String, Boolean> requiresExactMatch(String label) {
        for (Requirement requirement : requirementList) {
            if (requirement.getKey().equals(label)) {
                if (Operator.Equals.equals(requirement.getOperator()) || Operator.DoubleEquals
                        .equals(requirement.getOperator()) || Operator.In
                        .equals(requirement.getOperator())) {
                    if (requirement.getStrValues().size() == 1) {
                        return new Tuple<>(requirement.getStrValues().get(0), true);
                    }
                }
                return new Tuple<>("", false);
            }
        }
        return new Tuple<>("", false);
    }

    public List<Requirement> getRequirementList() {
        return requirementList;
    }

    @Override
    public String toString() {
        return requirementList.stream().map(Requirement::toString).collect(Collectors.joining(","));
    }
}
