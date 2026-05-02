package org.creepebucket.programmable_magic.mananet.mechines.wind_turbine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.mananet.NetNodeBlockEntity;
import org.creepebucket.programmable_magic.registries.ModAttachments;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class WindTurbineBlockEntity extends NetNodeBlockEntity implements GeoBlockEntity {
    public double airDensityBase = 1.225, airDensityTempFactBase, airDensityTempFact, airDensityPressureFact, airDensityHumidFact, airDensity;
    public double windSpeedBase = 4.5, windSpeedAltitudeFact, windSpeedTimeFact, windSpeedWeatherFact, windSpeed;
    public double windShearExponent, power;
    public boolean enabled;

    public static final RawAnimation SPIN_ANIMATION = RawAnimation.begin().thenLoop("animation");
    public final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public WindTurbineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WIND_TURBINE_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        enabled = input.getBooleanOr("enabled", false);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("enabled", enabled);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("wind_turbine", test -> {
            test.setControllerSpeed((float) Math.min(getData(ModAttachments.WIND_TURBINE_POWER) / 100d, 3));
            return test.setAndContinue(SPIN_ANIMATION);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level.isClientSide()) return;

        // 重新计算发电数据

        //              空气密度             //

        // 温度乘数
        var biomeTemp = level.getBiome(getBlockPos()).value().getBaseTemperature();
        var tempKelvin = biomeTemp * 20 + 273.15;
        var tempCelsius = biomeTemp * 20;
        var tempFact = (15 + 273.15) / tempKelvin;

        // 海拔乘数
        var altitude = (getBlockPos().get(Direction.Axis.Y) - 64) * 10; // 64为海平面基准, 放大10倍
        var pressureFact = Math.exp((double) -altitude / 8400);
        var atmosphericPressure = 101325 * pressureFact;

        // 湿度乘数
        var relativeHumidity = level.getBiome(getBlockPos()).value().getModifiedClimateSettings().downfall(); // 正好是0到1
        var saturatedVaporPressure = 611.2 * Math.exp(17.67 * tempCelsius / (tempCelsius + 243.5)); // 饱和水汽压 Tetens公式
        var vaporPressure = relativeHumidity * saturatedVaporPressure; // 水汽分压
        var humidityFact = ((1 - vaporPressure / atmosphericPressure) * 0.028965 + (vaporPressure / atmosphericPressure) * 0.018015) / 0.028965;

        var finalAirDensity = airDensityBase * tempFact * pressureFact * humidityFact;

        airDensityTempFactBase = tempFact;
        airDensityTempFact = tempFact;
        airDensityPressureFact = pressureFact;
        airDensityHumidFact = humidityFact;
        airDensity = finalAirDensity;

        //               风速               //

        // 高度乘数
        var erosion = ((ServerLevel) level).getChunkSource().randomState().router().erosion()
                .compute(new DensityFunction.SinglePointContext(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ()));
        windShearExponent = ((erosion + 2.42) / 4.84) * 0.25 + 0.1; // 风切变指数
        windSpeedAltitudeFact = Math.pow(Math.max(altitude / 10, 1), windShearExponent);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WindTurbineBlockEntity entity) {
        if (level.isClientSide()) return;

        // ========== 发电计算逻辑 ========== //

        //              空气密度             //

        var time = level.getDayTime() % 24000;
        var tempKelvin = (15 + 273.15) / entity.airDensityTempFactBase + 10 * Math.sin((Math.PI * 2 / 24000) * time);
        var tempFact = (15 + 273.15) / tempKelvin;
        var airDensity = entity.airDensityBase * tempFact * entity.airDensityPressureFact * entity.airDensityHumidFact;

        entity.airDensityTempFact = tempFact;
        entity.airDensity = airDensity;

        //               风速               //

        var windSpeedBase = entity.windSpeedBase;
        var altitudeFact = entity.windSpeedAltitudeFact;

        // 时间乘数
        var timeFact = 0.174167 * Math.sin((Math.PI / 12000) * time - (Math.PI / 3)) + 0.975833; // 回归出来的, 不要纠结...

        // 天气乘数
        var weatherFact = 0d;
        if (level.isThundering()) weatherFact = 1.3;
        else if (level.isRaining()) weatherFact = 1.15;
        else weatherFact = timeFact; // 雨雪天气时不受时间影响

        var windSpeed = windSpeedBase * altitudeFact * weatherFact;

        //              实际功率             //

        var power = 0.5 * entity.airDensity * 6 * windSpeed * windSpeed * windSpeed * 0.25;

        entity.windSpeedTimeFact = timeFact;
        entity.windSpeedWeatherFact = weatherFact;
        entity.windSpeed = windSpeed;
        entity.power = entity.enabled ? power : 0;
        entity.setData(ModAttachments.WIND_TURBINE_POWER, entity.power);

        entity.getNetworkData().setLoad(new ModUtils.Mana(0d, 0d, -entity.power / 20000, 0d));
        entity.getNetworkData().setCache(new ModUtils.Mana(2d, 2d, 2d, 2d));

        // ========== 网络连接逻辑 ========== //

        if (!level.isClientSide() && level.getBlockEntity(pos.above(5)) instanceof NetNodeBlockEntity) {
            entity.connect(level, pos.above(5), Direction.DOWN, Direction.UP);
        }
    }
}
