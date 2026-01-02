package com.tonic.wzchickenslayer.api;
import com.tonic.Logger;
import com.tonic.api.entities.NpcAPI;
import com.tonic.api.entities.TileItemAPI;
import com.tonic.api.game.MovementAPI;
import com.tonic.api.widgets.InventoryAPI;
import com.tonic.data.wrappers.*;
import com.tonic.util.ClickManagerUtil;
import com.tonic.util.handler.AbstractHandlerBuilder;
import com.tonic.util.handler.StepHandler;
import net.runelite.api.TileItem;
import net.runelite.api.gameval.ItemID;

import static com.tonic.api.game.MovementAPI.walkAproxWorldPoint;
import static com.tonic.api.game.MovementAPI.walkRelativeToWorldPoint;
import static com.tonic.wzchickenslayer.api.ChickenAPI.ChickenPEN;


/**
 * Chicken Slayer class.
 */
public class ChickenSlayerBuilder extends AbstractHandlerBuilder<ChickenSlayerBuilder> {
    /**
     * Generates the starting handler for Chicken Slayer.
     * 
     * @return The starting StepHandler.
     */
    public static StepHandler generateStart() {
        return new ChickenSlayerBuilder()
                .walkChickenArea()
                .build();
    }

    /**
     * Generates the setup handler for Chicken Slayer.
     * 
     * @return The setup StepHandler.
     */
    public static StepHandler generateSetup() {
        return new ChickenSlayerBuilder()
                .build();
    }

    /**
     * Generates the gameplay handler for Chicken Slayer.
     * 
     * @return The gameplay StepHandler.
     */
    public static StepHandler generateGameplay() {
        return new ChickenSlayerBuilder()
                .attackNPC()
                .build();
    }

    private ChickenSlayerBuilder walkChickenArea() {
        walkTo(ChickenPEN);

//        walkAproxWorldPoint(ChickenPEN,4);
        return this;
    }

    private boolean passesOwnershipFilter(TileItemEx item)
    {
        TileItem tileItem = item.getItem();
        return tileItem != null && tileItem.getOwnership() == TileItem.OWNERSHIP_SELF;
    }

    private ChickenSlayerBuilder attackNPC() {
        String[] lootItems = {"Feather","Bones"};

        add("start", context -> {
            if (PlayerEx.getLocal().isIdle() && !PlayerEx.getLocal().healthBarVisible() ) {
                return jump("attack", context);
            }
            if (InventoryAPI.isFull())
                return jump("bury", context);

            return jump("start", context);

        });

        add("attack", context -> {
            NpcEx npc = NpcAPI.search()
                    .withName("Chicken")
                    .canAttack()
                    .nearest();

//            chicken.getTile().setGroundObject();
            ClickManagerUtil.queueClickBox(npc);
            Logger.info("Pre attack Health: "+npc.healthBarVisible());
            NpcAPI.interact(npc, "Attack");
            Logger.info("Post attack Health: "+npc.healthBarVisible());

            // Store npc in context for other states
            context.put("targetNPC", npc);
        });
        addDelay(5);
        addDelayUntil(() -> !MovementAPI.isMoving());

        add("combat", context -> {
            NpcEx npc = context.get("targetNPC");
            if (PlayerEx.getLocal().getInCombatWith() == null && !npc.isDead()){
                return jump("start", context);
            }
            if (npc.isDead())
                return jump("DeathDelay", context);
            return jump("combat", context);
        });
        addDelay("DeathDelay",6) ;
        add("loot", (context) -> {
            Logger.info("Looting Items: "+lootItems[0]);
            TileItemEx item = TileItemAPI.search().withName(lootItems[0]).nearest();
            if(passesOwnershipFilter(item)) {
                TileItemAPI.interact(item, "Take");
            }
        });
        addDelay(4);
        add("loot2",(context) -> {
            Logger.info("Looting Items: "+lootItems[1]);
            TileItemEx item = TileItemAPI.search().withName(lootItems[1]).nearest();
            if(passesOwnershipFilter(item)) {
                TileItemAPI.interact(item, "Take");
            }
            return jump("start", context);

        });
        addDelay(4);
        add("bury", context -> {
            if (InventoryAPI.count(ItemID.BONES) < 1)
                return jump("start", context);

            Logger.info("Burying Bones");
            ItemEx bones = InventoryAPI.search()
                    .withName("Bones")
                    .first();
            bones.interact("Bury");

            return jump("bury", context); // loop until no bones left
        });

        addDelayUntil("end", () -> {
            Logger.warn("Reached the end");
            return true;
        });
        return this;
    }

}
