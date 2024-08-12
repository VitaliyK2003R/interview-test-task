package com.vk.dwzkf.test.impl;

import com.vk.dwzkf.test.Accumulator;
import com.vk.dwzkf.test.State;
import com.vk.dwzkf.test.StateObject;
import com.vk.dwzkf.test.StateObjectComparator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Roman Shageev
 * @since 12.08.2024
 */
public class AccumulatorImpl implements Accumulator {
    private final Map<Long, List<StateObject>> processStateStorage = new HashMap<>();
    private static final StateObjectComparator stateComparator = new StateObjectComparator();

    @Override
    public void accept(StateObject stateObject) {
        putStateObjectWithProcess(stateObject.getProcessId(), stateObject);
    }

    @Override
    public void acceptAll(List<StateObject> stateObjects) {
        clearProcesses();
        stateObjects.forEach(this::accept);
    }

    @Override
    public List<StateObject> drain(Long processId) {
        return releaseProcessStages(processId);
    }

    private void putStateObjectWithProcess(Long processId, StateObject stateObject) {
        if (!isProcessStateStorageInitialized(processId)) {
            initializeProcessStateStorage(processId);
        }
        if (isStateValidToInsert(processId, stateObject)) {
            getProcessStorage(processId).add(stateObject);
        }
    }

    private boolean isStateValidToInsert(Long processId, StateObject stateObject) {
        return !existNotice(processId, stateObject.getSeqNo()) && isValidStageState(processId, stateObject);
    }

    private boolean isValidStageState(Long processId, StateObject stateObject) {
        switch (stateObject.getState()) {
            case START2: return !existsPriorityState(processId, State.START1);
            case MID2 : return existsPriorityState(processId, State.MID1);
            case FINAL2: return !existsPriorityState(processId, State.FINAL1);
            default: return true;
        }
    }

    private boolean existsPriorityState(Long processId, State desiredState) {
        checkDesiredStateNotNull(desiredState);
        for (StateObject stateObject: getProcessStorage(processId)) {
            if (desiredState == stateObject.getState()) {
                return true;
            }
        }
        return false;
    }

    private void checkDesiredStateNotNull(State state) {
        if (state == null) throw new IllegalArgumentException();
    }

    private boolean isProcessStateStorageInitialized(Long processId) {
        return getProcessStorage(processId) != null;
    }

    private void initializeProcessStateStorage(Long processId) {
        processStateStorage.put(processId, new ArrayList<>());
    }

    private boolean existNotice(Long processId, Integer sequenceNumber) {
        return getProcessStorage(processId).size() == sequenceNumber;
    }

    private List<StateObject> getProcessStorage(Long processId) {
        return processStateStorage.get(processId);
    }

    private void clearProcesses() {
        for (Map.Entry<Long, List<StateObject>> processStates: processStateStorage.entrySet()) {
            processStates.getValue().clear();
        }
    }

    private List<StateObject> releaseProcessStages(Long processId) {
        checkProcessHasPriorityStates(processId);
        checkProcessHasNoStartState(processId);
        checkProcessHasNoFinalState(processId);
        List<StateObject> processStates = getProcessStorage(processId);
        processStates.sort(stateComparator);
        return processStates;
    }

    private void checkProcessHasPriorityStates(Long processId) {
        checkProcessHasPriorityStartState(processId);
        checkProcessHasPriorityFinalState(processId);
    }

    private void checkProcessHasPriorityStartState(Long processId) {
        if (existsPriorityState(processId, State.START1)) {
            excludeProcessState(processId, State.START2);
        }
    }

    private void checkProcessHasPriorityFinalState(Long processId) {
        if (existsPriorityState(processId, State.FINAL1)) {
            excludeProcessState(processId, State.FINAL2);
        }
    }

    private void checkProcessHasNoStartState(Long processId) {
        if (processNotHaveStartState(processId)) {
            excludeAllProcessStates(processId);
        }
    }

    private void checkProcessHasNoFinalState(Long processId) {
        if (processNotHaveFinalState(processId)) {
            excludeAllProcessStates(processId);
        }
    }

    private void excludeAllProcessStates(Long processId) {
        getProcessStorage(processId).clear();
    }

    private boolean processNotHaveStartState(Long processId) {
        return !existsPriorityState(processId, State.START1) && !existsPriorityState(processId, State.START2);
    }

    private boolean processNotHaveFinalState(Long processId) {
        return !existsPriorityState(processId, State.FINAL1) && !existsPriorityState(processId, State.FINAL2);
    }

    private void excludeProcessState(Long processId, State excludableState) {
        if (excludableState == null) {
            throw new IllegalArgumentException();
        }
        List<StateObject> processStates = getProcessStorage(processId);
        processStates.removeIf(stateObject -> excludableState == stateObject.getState());
    }
}
