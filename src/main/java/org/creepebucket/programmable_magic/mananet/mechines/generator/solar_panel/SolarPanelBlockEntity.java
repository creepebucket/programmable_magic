package org.creepebucket.programmable_magic.mananet.mechines.generator.solar_panel;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.mananet.NetNodeBlockEntity;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;

import static org.creepebucket.programmable_magic.ModUtils.getTempKelvin;

public class SolarPanelBlockEntity extends NetNodeBlockEntity implements GeoBlockEntity {
    public double solarConstant = 1367, directIrradiance, diffuseIrradiance, atmosphericTransmittanceDiffuse;
    public double atmosphericTransmittanceDirect = 0.8, airMass, weatherFactDirect, weatherFactDiffuse, panelArea = 6;
    public double baseTemperature, cellTemperature, thermalFact, materialFact = 0.15, altitudeFact, power, efficiencyFact;

    public final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public SolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOLAR_PANEL_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level.isClientSide()) {
            return;
        }

        // 重新计算发电数据
        baseTemperature = getTempKelvin(getBlockPos(), (ServerLevel) level);
        altitudeFact = Math.exp((double) getBlockPos().getY() / 8400);
        //
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SolarPanelBlockEntity entity) {
        if (level.isClientSide()) return;

        // ========== 发电计算逻辑 ========== //

        //             太阳辐照度             //

        if (level.isThundering()) {
            entity.weatherFactDiffuse = 0.15;
            entity.weatherFactDirect = 0.05;
        }
        else if (level.isRaining()) {
            entity.weatherFactDiffuse = 1.2;
            entity.weatherFactDirect = 0.5;
        }
        else {
            entity.weatherFactDiffuse = 1;
            entity.weatherFactDirect = 1;
        }

        var time = level.getOverworldClockTime() % 24000;
        var zenith = Math.abs(time - 6000) * Math.PI / 12000.0F;
        var alpha = Math.PI / 2.0 - zenith;
        var alphaDeg = Math.toDegrees(alpha);

        // Kasten-Young 公式
        entity.airMass = time < 12000 ? 1.0 / (Math.sin(alpha) + 0.50572 * Math.pow(alphaDeg + 6.07995, -1.6364)) : Double.MAX_VALUE;
        var directIrradianceFact = Math.pow(entity.atmosphericTransmittanceDirect, entity.airMass * entity.altitudeFact);
        entity.directIrradiance = entity.solarConstant * directIrradianceFact * entity.weatherFactDirect;

        // Hottel 散射关系
        entity.atmosphericTransmittanceDiffuse = Math.max(0.2710 - 0.2939 * directIrradianceFact, 0) * smoothstep(0, 6, Math.toDegrees(alpha));
        entity.diffuseIrradiance = 50 * entity.atmosphericTransmittanceDiffuse * entity.weatherFactDiffuse;

        //              效率系数              //

        entity.cellTemperature = entity.baseTemperature + 30 * Math.sin((Math.PI * 2 / 24000) * time) + 15;
        entity.thermalFact = 1 - .005 * (entity.cellTemperature - 25 - 273.15);
        entity.efficiencyFact = entity.thermalFact * entity.materialFact * 100;

        // ========== 网络连接逻辑 ========== //

        if (!level.isClientSide() && level.getBlockEntity(pos.east(1)) instanceof NetNodeBlockEntity) {
            entity.connect(level, pos.east(1), Direction.WEST, Direction.EAST);
        }
        if (!level.isClientSide() && level.getBlockEntity(pos.west(1)) instanceof NetNodeBlockEntity) {
            entity.connect(level, pos.west(1), Direction.EAST, Direction.WEST);
        }
        if (!level.isClientSide() && level.getBlockEntity(pos.south(1)) instanceof NetNodeBlockEntity) {
            entity.connect(level, pos.south(1), Direction.NORTH, Direction.SOUTH);
        }
        if (!level.isClientSide() && level.getBlockEntity(pos.north(1)) instanceof NetNodeBlockEntity) {
            entity.connect(level, pos.north(1), Direction.SOUTH, Direction.NORTH);
        }

        entity.power = entity.enabled ? (entity.directIrradiance + entity.diffuseIrradiance) * entity.panelArea * entity.efficiencyFact * 0.01 : 0;
        var load = new ModUtils.Mana(-entity.power * 0.05 * 0.001, 0d, 0.0005, 0d);

        if (entity.getNetworkData().canProduce(load)) entity.getNetworkData().setLoad(load);
        entity.getNetworkData().setCache(new ModUtils.Mana(2d, 2d, 2d, 2d));
    }

    private static double smoothstep(double edge0, double edge1, double x) {
        if (x <= edge0) return 0.0d;
        if (x >= edge1) return 1.0d;
        var t = (x - edge0) / (edge1 - edge0);
        return t * t * (3.0d - 2.0d * t);
    }
}
