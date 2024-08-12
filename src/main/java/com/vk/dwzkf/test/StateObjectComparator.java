package com.vk.dwzkf.test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StateObjectComparator implements Comparator<StateObject> {
    private static final List<State> priorityStateList;

    static {
        priorityStateList = new ArrayList<>();
        priorityStateList.add(State.FINAL2);
        priorityStateList.add(State.FINAL1);
        priorityStateList.add(State.MID2);
        priorityStateList.add(State.MID1);
        priorityStateList.add(State.START2);
        priorityStateList.add(State.START1);
    }

    @Override
    public int compare(StateObject comparableState, StateObject comparisonState) {
        if (isNotPriorityStates(comparableState.getState(), comparisonState.getState())) {
            return compareNotPriorityStates();
        }
        return comparePriorityStates(comparableState, comparisonState);
    }

    private int comparePriorityStates(StateObject comparableState, StateObject comparisonState) {
        int firstStatePriority = getPriority(comparableState.getState());
        int secondStatePriority = getPriority(comparisonState.getState());
        if (firstStatePriority == secondStatePriority) {
            return compareWithSequenceNumber(comparableState.getSeqNo(), comparisonState.getSeqNo());
        }
        return Integer.compare(secondStatePriority, firstStatePriority);
    }

    private int compareNotPriorityStates() {
        return 0;
    }

    private boolean isNotPriorityStates(State comparableState, State comparisonState) {
        return (State.MID1 == comparableState && State.MID1 == comparisonState)
                || (State.MID2 == comparableState && State.MID1 == comparisonState)
                || (State.MID1 == comparableState && State.MID2 == comparisonState);
    }

    private int compareWithSequenceNumber(int comparableSequence, int comparisonSequence) {
        return Integer.compare(comparableSequence, comparisonSequence);
    }

    private int getPriority(State state) {
        for (int i = 0; i < priorityStateList.size(); i++) {
            if (priorityStateList.get(i).compareTo(state) == 0) {
                return i;
            }
        }
        throw new IllegalArgumentException();
    }
}
