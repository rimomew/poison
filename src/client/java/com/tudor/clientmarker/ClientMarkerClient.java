package com.tudor.clientmarker;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.LevelRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public final class ClientMarkerClient implements ClientModInitializer {
    private static final int DEFAULT_P_SCANCODE = 19; // SDL_SCANCODE_P
    private static final int OUTLINE_COLOR = 0xFF00FF00;
    private static final float OUTLINE_WIDTH = 2.0F;

    private static final KeyMapping MARK_TARGET = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.client-marker.mark_target",
                    InputConstants.Type.KEYBOARD,
                    DEFAULT_P_SCANCODE,
                    KeyMapping.Category.MISC
            )
    );

    // UUIDs are the only persistent representation of marked entities.
    private static final Set<UUID> MARKED = new HashSet<>();

    // Used only to notice client-world changes; marked entities are never stored as objects.
    private static Level trackedLevel;
    private static int cleanupCooldown;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientMarkerClient::onClientTick);
        LevelRenderEvents.COLLECT_SUBMITS.register(context -> renderMarkedEntities(
                Minecraft.getInstance(),
                context.poseStack(),
                context.submitNodeCollector()
        ));
    }

    private static void onClientTick(Minecraft client) {
        if (client.level != trackedLevel) {
            trackedLevel = client.level;
            MARKED.clear();
            cleanupCooldown = 0;
        }

        while (MARK_TARGET.consumeClick()) {
            markCrosshairTarget(client);
        }

        // Resolve by UUID only occasionally. This keeps cleanup cheap while still
        // removing unloaded/removed entities automatically.
        if (client.level != null && --cleanupCooldown <= 0) {
            cleanupCooldown = 20;
            cleanupStale(client.level);
        }
    }

    private static void markCrosshairTarget(Minecraft client) {
        if (client.player == null || client.level == null) {
            return;
        }

        HitResult hit = client.hitResult;
        if (!(hit instanceof EntityHitResult entityHit)) {
            return;
        }

        Entity target = entityHit.getEntity();
        if (!(target instanceof LivingEntity living) || living == client.player) {
            return;
        }

        UUID uuid = living.getUUID();
        if (MARKED.remove(uuid)) {
            showFeedback(client, "Unmarked: " + living.getName().getString());
        } else {
            MARKED.add(uuid);
            showFeedback(client, "Marked: " + living.getName().getString());
        }
    }

    private static void cleanupStale(Level level) {
        Iterator<UUID> iterator = MARKED.iterator();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            Entity entity = level.getEntity(uuid);
            if (!(entity instanceof LivingEntity) || entity.isRemoved()) {
                iterator.remove();
            }
        }
    }

    private static void renderMarkedEntities(
            Minecraft client,
            PoseStack poseStack,
            SubmitNodeCollector collector
    ) {
        if (MARKED.isEmpty() || client.level == null) {
            return;
        }

        CameraRenderState camera = client.gameRenderer.gameRenderState()
                .levelRenderState.cameraRenderState;
        if (!camera.initialized) {
            return;
        }

        for (UUID uuid : MARKED) {
            Entity entity = client.level.getEntity(uuid);
            if (!(entity instanceof LivingEntity living)
                    || living == client.player
                    || entity.isRemoved()) {
                continue;
            }

            AABB box = entity.getBoundingBox();
            poseStack.pushPose();
            poseStack.translate(
                    box.minX - camera.pos.x,
                    box.minY - camera.pos.y,
                    box.minZ - camera.pos.z
            );

            collector.submitShapeOutline(
                    poseStack,
                    Shapes.box(0.0, 0.0, 0.0, box.getXsize(), box.getYsize(), box.getZsize()),
                    RenderTypes.lines(),
                    OUTLINE_COLOR,
                    OUTLINE_WIDTH,
                    false
            );

            poseStack.popPose();
        }
    }

    private static void showFeedback(Minecraft client, String message) {
        client.gui.setOverlayMessage(Component.literal(message), false);
    }
}
