package com.tonic.wzchickenslayer;

import com.tonic.ui.VitaOverlay;
import com.tonic.wzchickenslayer.data.State;
import net.runelite.client.ui.overlay.OverlayPosition;

import java.awt.*;

/**
 * WinterOverlay class.
 */
public class ChickenOverlay extends VitaOverlay {
    public ChickenOverlay() {
        super();
        setPosition(OverlayPosition.TOP_CENTER);
        setHeight(28);
        setWidth(150);
    }

    /**
     * Update the overlay based on the current state.
     *
     * @param state The current state of the SLaying activity.
     */
    public void update(State state)
    {
        clear();
        if(state == null)
        {
            newLine("Inactive", 14, Color.RED);
            return;
        }
        switch (state)
        {
            case START:
                newLine("Starting...", 14, Color.YELLOW);
                break;
            case PREP:
                newLine("Preparation Phase", 14, Color.CYAN);
                break;
            case GAME:
                newLine("Slaying", 14, Color.GREEN);
                break;
        }
    }
}
