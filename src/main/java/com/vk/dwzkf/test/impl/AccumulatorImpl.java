package com.vk.dwzkf.test.impl;

import com.vk.dwzkf.test.Accumulator;
import com.vk.dwzkf.test.StateObject;

import java.util.List;

/**
 * @author Roman Shageev
 * @since 12.08.2024
 */
public class AccumulatorImpl implements Accumulator {
    @Override
    public void accept(StateObject stateObject) {
        //your code here
    }

    @Override
    public List<StateObject> drain(Long contextId) {
        //your code here
        return null;
    }
}