package com.tonic.wzchickenslayer;

import com.tonic.Logger;
import com.tonic.services.breakhandler.BreakHandler;
import com.tonic.wzchickenslayer.api.ChickenAPI;

import com.tonic.wzchickenslayer.data.State;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import javax.inject.Inject;

@PluginDescriptor(name = "WZ Chicken Slayer", description = "Kills chickens for you", tags = { "wassuppzzz",
        "chicken", "combat", "auto" })
public class ChickenSlayerPlugin extends Plugin {
    @Inject
    private ClientToolbar clientToolbar;
    @Inject
    public OverlayManager overlayManager;
    @Inject
    private BreakHandler breakHandler;
    @Inject
    private ChickenOverlay overlay;
    private SidePanel panel;
    private NavigationButton navButton;
    private State state;

    @Override
    protected void startUp() {


        panel = injector.getInstance(SidePanel.class);
        navButton = NavigationButton.builder()
                .tooltip("WZ Chicken Slayer")
                .icon(ImageUtil.loadImageResource(getClass(), "icon.png"))
                .panel(panel)
                .build();

        clientToolbar.addNavigation(navButton);
        breakHandler.register(this);
        overlayManager.add(overlay);

        reset();
    }

    @Override
    protected void shutDown() {
        clientToolbar.removeNavigation(navButton);
        overlayManager.remove(overlay);
        breakHandler.unregister(this);
        reset();
    }

    /**
     * Resets the plugin state.
     */
    private void reset() {
        State.PREP.reset();
        State.GAME.reset();
        State.START.reset();
        state = null;
    }

    /**
     * Starts a new round with the given state.
     * 
     * @param newState The state to start the new round with.
     */
    private void newRound(State newState) {
        newState.reset();
        state = newState;
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        // Shutdown if stopped and still running
        if (!panel.isRunning() && state != null) {
            reset();
            return;
        }

        try {
            if (breakHandler != null && breakHandler.isBreaking(this)) {
                Logger.info("WZ ChickenSlayer: On break, returning");
                return;
            }
        } catch (Exception ex) {

        }

        // Start new round if started but not yet running
        if (panel.isRunning() && state == null) {
            newRound(State.START);
        }


        // Execute current state and transition if needed
        if (!state.getHandler().step()) {
            Logger.warn("About to transition state!" + state);

            state.getHandler().reset();
            state = State.transition(state);
        }
    }

    @Subscribe
    public void onClientTick(ClientTick event) {
        overlay.update(state);
    }

    public void startBreaks()
    {
        breakHandler.start(this);
    }

    public void stopBreaks()
    {
        breakHandler.stop(this);
    }
}
