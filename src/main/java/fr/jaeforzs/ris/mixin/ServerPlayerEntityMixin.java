package fr.jaeforzs.ris.mixin;

import fr.jaeforzs.ris.RisWorldState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Unique
    private static final double PUSH_STRENGTH = 2.0;

    @Unique
    private static final int SEARCH_RADIUS = 64;

    /**
     * Флаг: было ли уже выполнено снятие экипировки для текущей активной компликации.
     * Сбрасывается при смене компликации или остановке таймера.
     */
    @Unique
    private String ris$lastStripComplication = null;

    @Inject(method = "tick", at = @At("TAIL"))
    private void ris$onTick(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        MinecraftServer server = player.getEntityWorld().getServer();
        if (server == null) return;

        RisWorldState state = RisWorldState.getServerState(server);

        // Таймер не запущен или завершён — сбрасываем флаг и выходим
        if (!state.timerRunning || state.completed) {
            ris$lastStripComplication = null;
            return;
        }

        String comp = state.complicationId;

        // --- Одноразовое снятие экипировки при активации компликации ---
        // Выполняется только один раз: когда comp изменился (или только что стал активным).
        // Это покрывает случай захода в мир с уже надетой бронёй/offhand,
        // а также респавна (AFTER_RESPAWN тоже вызывает applyComplicationToPlayer,
        // но там нет стриппинга — добавим его здесь через тот же механизм).
        if (!comp.equals(ris$lastStripComplication)) {
            if ("ris.comp.fragile_bones".equals(comp)) {
                ris$forceUnequipArmor(player);
            } else if ("ris.comp.one_armed".equals(comp)) {
                ris$forceUnequipOffhand(player);
            }
            ris$lastStripComplication = comp;
        }

        // --- Логика, которая действительно нужна каждый тик ---

        if ("ris.comp.hydrophobe".equals(comp)) {
            if (player.isTouchingWater()) {
                ris$killByWater(player);
            }
        }

        if ("ris.comp.thalassophobe".equals(comp)) {
            if (ris$isInWaterBiome(player)) {
                ris$handleOceanPanic(player);
            }
        }
    }

    @Unique
    private void ris$forceUnequipOffhand(ServerPlayerEntity player) {
        ItemStack offhandStack = player.getOffHandStack();
        if (offhandStack.isEmpty()) return;

        player.equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY);

        if (!player.getInventory().insertStack(offhandStack)) {
            player.dropItem(offhandStack, false, true);
        }
    }

    @Unique
    private void ris$forceUnequipArmor(ServerPlayerEntity player) {
        EquipmentSlot[] armorSlots = {
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        };

        for (EquipmentSlot slot : armorSlots) {
            ItemStack stack = player.getEquippedStack(slot);
            if (stack.isEmpty()) continue;

            player.equipStack(slot, ItemStack.EMPTY);

            if (!player.getInventory().insertStack(stack)) {
                player.dropItem(stack, false, true);
            }
        }
    }

    @Unique
    private void ris$killByWater(ServerPlayerEntity player) {
        player.damage(player.getEntityWorld(), player.getDamageSources().drown(), Float.MAX_VALUE);
    }

    @Unique
    private boolean ris$isInWaterBiome(ServerPlayerEntity player) {
        ServerWorld world = player.getEntityWorld();
        BlockPos pos = player.getBlockPos();
        RegistryEntry<Biome> biome = world.getBiome(pos);

        if (biome.isIn(BiomeTags.IS_OCEAN)) return true;
        if (biome.isIn(BiomeTags.IS_DEEP_OCEAN)) return true;
        if (biome.isIn(BiomeTags.IS_RIVER)) return true;

        String biomeName = biome.getIdAsString().toLowerCase();
        return biomeName.contains("ocean") ||
                biomeName.contains("river") ||
                biomeName.contains("beach");
    }

    @Unique
    private boolean ris$isNotWaterBiome(ServerWorld world, BlockPos pos) {
        RegistryEntry<Biome> biome = world.getBiome(pos);

        if (biome.isIn(BiomeTags.IS_OCEAN)) return false;
        if (biome.isIn(BiomeTags.IS_DEEP_OCEAN)) return false;
        if (biome.isIn(BiomeTags.IS_RIVER)) return false;

        String biomeName = biome.getIdAsString().toLowerCase();
        return !biomeName.contains("ocean") &&
                !biomeName.contains("river") &&
                !biomeName.contains("beach");
    }

    @Unique
    private void ris$handleOceanPanic(ServerPlayerEntity player) {
        Entity vehicle = player.getVehicle();
        if (vehicle instanceof AbstractBoatEntity boat) {
            ris$destroyBoat(player, boat);
        }

        ris$pushAwayFromOcean(player);
    }

    @Unique
    private void ris$destroyBoat(ServerPlayerEntity player, AbstractBoatEntity boat) {
        ServerWorld world = player.getEntityWorld();
        Vec3d boatPos = boat.getEntityPos();

        player.stopRiding();
        player.setPosition(boatPos.x, boatPos.y + 0.5, boatPos.z);

        world.playSound(
                null,
                boat.getX(), boat.getY(), boat.getZ(),
                SoundEvents.ENTITY_ITEM_BREAK,
                SoundCategory.PLAYERS,
                1.0f, 1.0f
        );

        boat.discard();
    }

    @Unique
    private void ris$pushAwayFromOcean(ServerPlayerEntity player) {
        ServerWorld world = player.getEntityWorld();
        BlockPos playerPos = player.getBlockPos();

        Vec3d escapeDirection = ris$findEscapeDirection(world, playerPos);

        double vx, vz;
        if (escapeDirection != null) {
            vx = escapeDirection.x * PUSH_STRENGTH;
            vz = escapeDirection.z * PUSH_STRENGTH;
        } else {
            double angle = Math.random() * Math.PI * 2;
            vx = Math.cos(angle) * PUSH_STRENGTH;
            vz = Math.sin(angle) * PUSH_STRENGTH;
        }

        player.setVelocity(vx, 0.7, vz);
        player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
    }

    @Unique
    private Vec3d ris$findEscapeDirection(ServerWorld world, BlockPos playerPos) {
        Vec3d bestDirection = null;
        double closestDistance = Double.MAX_VALUE;

        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x += 4) {
            for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z += 4) {
                if (x == 0 && z == 0) continue;

                BlockPos checkPos = playerPos.add(x, 0, z);

                if (ris$isNotWaterBiome(world, checkPos)) {
                    double distance = playerPos.getSquaredDistance(checkPos);
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        bestDirection = new Vec3d(x, 0, z).normalize();
                    }
                }
            }
        }

        return bestDirection;
    }
}