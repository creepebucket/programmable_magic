package org.creepebucket.arcanism.client;

import com.geckolib.animatable.GeoAnimatable;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import org.creepebucket.arcanism.gui.command.NetworkInfoScreen;
import org.creepebucket.arcanism.gui.machines.buffer.ManaBufferScreen;
import org.creepebucket.arcanism.gui.machines.consumer.liquid_heater.LiquidHeaterScreen;
import org.creepebucket.arcanism.gui.machines.consumer.water_pump.WaterPumpScreen;
import org.creepebucket.arcanism.gui.machines.generator.heat_exchanger.HeatExchangerScreen;
import org.creepebucket.arcanism.gui.machines.generator.pressure_relief_valve.PressureReliefValveScreen;
import org.creepebucket.arcanism.gui.machines.generator.solar_panel.SolarPanelScreen;
import org.creepebucket.arcanism.gui.machines.generator.steam_turbine.SteamTurbineScreen;
import org.creepebucket.arcanism.gui.machines.generator.wind_turbine.WindTurbineScreen;
import org.creepebucket.arcanism.gui.machines.io_dummy.IoDummyScreen;
import org.creepebucket.arcanism.gui.wand.WandScreen;
import org.creepebucket.arcanism.mananet.connectors.NetNodeBlockEntityBER;
import org.creepebucket.arcanism.mananet.machines.MachineBlockEntityBER;
import org.creepebucket.arcanism.mananet.machines.MachineGeoModel;
import org.creepebucket.arcanism.mananet.machines.generator.solar_panel.SolarPanelBlockEntityBER;
import org.creepebucket.arcanism.particles.client.FastDustParticle;
import org.creepebucket.arcanism.registries.ModBlockEntities;
import org.creepebucket.arcanism.registries.ModEntityTypes;
import org.creepebucket.arcanism.registries.ModFluids;
import org.creepebucket.arcanism.registries.ModMenuTypes;
import org.creepebucket.arcanism.registries.ModParticleTypes;
import org.creepebucket.arcanism.renderer.SpellEntityRenderer;
import org.creepebucket.arcanism.renderer.api.RenderHelper;
import org.creepebucket.arcanism.spells.PackedSpellSpecialRenderer;

import static org.creepebucket.arcanism.Arcanism.MODID;

public class ClientEventHandler {
    @SubscribeEvent
    public static void registerScreen(RegisterMenuScreensEvent event) {
        event.register(
                ModMenuTypes.WAND_MENU.get(),
                WandScreen::new
        );
        event.register(
                ModMenuTypes.MACHINE_MENU.get(),
                WindTurbineScreen::new
        );
		event.register(
				ModMenuTypes.SOLAR_PANEL_MENU.get(),
				SolarPanelScreen::new
		);
		event.register(
				ModMenuTypes.HEAT_EXCHANGER_MENU.get(),
				HeatExchangerScreen::new
		);
		event.register(
				ModMenuTypes.STEAM_TURBINE_MENU.get(),
				SteamTurbineScreen::new
		);
		event.register(
				ModMenuTypes.PRESSURE_RELIEF_VALVE_MENU.get(),
				PressureReliefValveScreen::new
		);
		event.register(
				ModMenuTypes.WATER_PUMP_MENU.get(),
				WaterPumpScreen::new
		);
		event.register(
				ModMenuTypes.LIQUID_HEATER_MENU.get(),
				LiquidHeaterScreen::new
		);
		event.register(
				ModMenuTypes.NETWORK_INFO.get(),
				NetworkInfoScreen::new
		);
		event.register(
				ModMenuTypes.IO_DUMMY.get(),
				IoDummyScreen::new
		);
		event.register(
				ModMenuTypes.MANA_BUFFER_MENU.get(),
				ManaBufferScreen::new
		);
	}

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.SPELL_ENTITY.get(), SpellEntityRenderer::new);

		registerMachineBER(event, ModBlockEntities.WIND_TURBINE_BLOCK_ENTITY.get(), "wind_turbine", new AABB(-2, -1, -2, 3, 7, 3));
		event.registerBlockEntityRenderer(ModBlockEntities.SOLAR_PANEL_BLOCK_ENTITY.get(), SolarPanelBlockEntityBER::new);
		registerMachineBER(event, ModBlockEntities.HEAT_EXCHANGER_BLOCK_ENTITY.get(), "heat_exchanger", new AABB(-1, 0, -1, 2, 2, 2));
		registerMachineBER(event, ModBlockEntities.STEAM_TURBINE_BLOCK_ENTITY.get(), "steam_turbine", new AABB(-1, 0, -1, 2, 5, 2));
		registerMachineBER(event, ModBlockEntities.PRESSURE_RELIEF_VALVE_BLOCK_ENTITY.get(), "pressure_relief_valve", new AABB(0, 0, 0, 1, 5, 1));
		registerMachineBER(event, ModBlockEntities.WATER_PUMP_BLOCK_ENTITY.get(), "water_pump", new AABB(-1, 0, -1, 2, 2, 2));
        registerMachineBER(event, ModBlockEntities.LIQUID_HEATER_BLOCK_ENTITY.get(), "liquid_heater", new AABB(-1, 0, -1, 2, 2, 2));
        registerMachineBER(event, ModBlockEntities.SMALL_MANA_BUFFER_BLOCK_ENTITY.get(), "small_mana_buffer", new AABB(0, 0, 0, 1, 1, 1));
        registerMachineBER(event, ModBlockEntities.MEDIUM_MANA_BUFFER_BLOCK_ENTITY.get(), "medium_mana_buffer", new AABB(0, 0, 0, 1, 3, 1));
        registerMachineBER(event, ModBlockEntities.LARGE_MANA_BUFFER_BLOCK_ENTITY.get(), "large_mana_buffer", new AABB(-1, 0, -1, 2, 4, 2));
        event.registerBlockEntityRenderer(ModBlockEntities.BASIC_MANA_CONNECTOR_BLOCK_ENTITY.get(), context -> new NetNodeBlockEntityBER());
        event.registerBlockEntityRenderer(ModBlockEntities.HORIZONTAL_MANA_CONNECTOR_BLOCK_ENTITY.get(), context -> new NetNodeBlockEntityBER());
        event.registerBlockEntityRenderer(ModBlockEntities.VERTICAL_MANA_CONNECTOR_BLOCK_ENTITY.get(), context -> new NetNodeBlockEntityBER());
    }

    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.FAST_DUST.get(), FastDustParticle.Provider::new);
    }

    public static void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(RenderHelper.SOLID_FACE_PIPELINE);
    }

    public static <T extends BlockEntity & GeoAnimatable> void registerMachineBER(
            EntityRenderersEvent.RegisterRenderers event,
            net.minecraft.world.level.block.entity.BlockEntityType<T> type, String name, AABB bbox) {
        event.registerBlockEntityRenderer(type, ctx -> new MachineBlockEntityBER<>(ctx,
                new MachineGeoModel<>(
                        Identifier.fromNamespaceAndPath(MODID, "block/machines/" + name),
                        Identifier.fromNamespaceAndPath(MODID, "block/machines/" + name),
                        Identifier.fromNamespaceAndPath(MODID, "textures/machines/" + name + ".png")),
                be -> {
                    var pos = be.getBlockPos();
                    return new AABB(pos.getX() + bbox.minX, pos.getY() + bbox.minY, pos.getZ() + bbox.minZ,
                            pos.getX() + bbox.maxX, pos.getY() + bbox.maxY, pos.getZ() + bbox.maxZ);
                }
        ));
    }

    public static void registerSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(
                Identifier.fromNamespaceAndPath(MODID, "packed_spell"),
                PackedSpellSpecialRenderer.Unbaked.MAP_CODEC
        );
    }

    @SubscribeEvent
    public static void registerFluidModels(RegisterFluidModelsEvent event) {
        event.register(new FluidModel.Unbaked(
                new Material(Identifier.fromNamespaceAndPath(MODID, "block/steam_still")),
                new Material(Identifier.fromNamespaceAndPath(MODID, "block/steam_flowing")),
                null,
                (FluidTintSource) state -> 0xFF_CCCCCC
        ), ModFluids.STEAM_SOURCE, ModFluids.STEAM_FLOWING);
    }
}
