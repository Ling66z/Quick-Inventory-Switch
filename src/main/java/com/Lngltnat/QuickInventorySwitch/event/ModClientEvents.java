package com.Lngltnat.QuickInventorySwitch.event;

import com.Lngltnat.QuickInventorySwitch.QuickInventorySwitch;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = QuickInventorySwitch.MODID)
public class ModClientEvents {


    public static final KeyMapping INVENTORYSWAP = new KeyMapping("key.categories.quickinventoryswitch.examplecategory",
            KeyConflictContext.IN_GAME, //不在菜单里用
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_TAB,
            "key.categories.misc"
    );

    public static final Lazy<KeyMapping> OILSALT_MAPPING = Lazy.of(() -> INVENTORYSWAP
    );

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(OILSALT_MAPPING.get());
    }

    private static int currentRow = 0;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        MultiPlayerGameMode gameMode = mc.gameMode;
        while (OILSALT_MAPPING.get().consumeClick()) {
            if(player == null) return;
            if(gameMode == null) return;
            swapHotbarWithRow(mc.player, currentRow);
            currentRow = (currentRow + 1) % 3;
        }
    }

    private static void swapHotbarWithRow(Player player, int row) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null) return;

        // 主背包起始槽位 ID：9 + row * 9
        int inventorySlotStart = 9 + row * 9;

        for (int hotbarIndex = 0; hotbarIndex < 9; hotbarIndex++) {
            int inventorySlotId = inventorySlotStart + hotbarIndex;

            // 模拟：鼠标悬停在背包槽位 inventorySlotId 上，按下数字键 hotbarIndex
            mc.gameMode.handleInventoryMouseClick(
                    player.inventoryMenu.containerId, // 当前菜单的 containerId
                    inventorySlotId,                  // 被交换的背包槽位 ID
                    hotbarIndex,                      // 目标快捷栏索引 0~8
                    ClickType.SWAP,                   // 交换操作
                    player
            );

            ItemStack inSlot = player.getInventory().getItem(hotbarIndex);
            if (!inSlot.isEmpty()) {
                if (player.level().isClientSide) {
                    inSlot.setPopTime(5);
//                } else if (player instanceof ServerPlayer) {
//                    player.containerMenu.broadcastChanges(); //deepseek说是冗余的
                }
            }
        }
    }

}