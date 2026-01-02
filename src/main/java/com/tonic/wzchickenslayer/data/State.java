package com.tonic.wzchickenslayer.data;

import com.tonic.Logger;
import com.tonic.util.handler.StepHandler;
import com.tonic.wzchickenslayer.api.ChickenSlayerBuilder;
import lombok.Getter;

import java.util.function.Supplier;

/**
 * Represents the different states of the Chicken Slayer activity.
 */
public enum State {
    /**
     * Initial state where the player starts the Chicken Slayer activity.
     */
    START(ChickenSlayerBuilder::generateStart),

    /**
     * Preparation state where the player readies themselves for the next round.
     */
    PREP(ChickenSlayerBuilder::generateSetup),

    /**
     * Active gameplay state where the player engages with the Chicken Slayer skilling
     * boss.
     */
    GAME(ChickenSlayerBuilder::generateGameplay);

    @Getter
    private final StepHandler handler;

    State(Supplier<StepHandler> supplier) {
        this.handler = supplier.get();
    }

    /**
     * Transitions to the next state based on the current state.
     * 
     * @param state The current state.
     * @return The next state after transition.
     */
    public static State transition(State state) {
        switch (state) {
            case START:
                return State.PREP;
            case PREP:
                return State.GAME;
//            case GAME:
//                return State.START;
        }
        Logger.warn("Transitioned to: " + state);
        return state;
    }

    /**
     * Resets the StepHandler for the current state.
     */
    public void reset() {
        if (handler != null) {
            handler.reset();
        }
    }
}
