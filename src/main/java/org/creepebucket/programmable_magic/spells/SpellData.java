package org.creepebucket.programmable_magic.spells;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class SpellData {
    // 法术基本信息
    private Player caster;
    private Vec3 position;
    private Vec3 direction;
    private Entity target;
    
    // 法术属性（可被法术修改）
    private double power = 1.0;
    private double range = 10.0;
    private int delay = 0; // 延时（tick）
    private boolean isActive = true;
    
    // 魔力消耗（四种类型）
    private Map<String, Double> manaCosts;
    
    // 自定义数据存储
    private Map<String, Object> customData;
    
    public SpellData(Player caster, Vec3 position, Vec3 direction) {
        this.caster = caster;
        this.position = position;
        this.direction = direction;
        this.manaCosts = new HashMap<>();
        this.customData = new HashMap<>();
        
        // 初始化四种魔力类型的消耗为0
        this.manaCosts.put("radiation", 0.0);
        this.manaCosts.put("temperature", 0.0);
        this.manaCosts.put("momentum", 0.0);
        this.manaCosts.put("pressure", 0.0);
    }
    
    // Getters and Setters
    public Player getCaster() { return caster; }
    public void setCaster(Player caster) { this.caster = caster; }
    
    public Vec3 getPosition() { return position; }
    public void setPosition(Vec3 position) { this.position = position; }
    
    public Vec3 getDirection() { return direction; }
    public void setDirection(Vec3 direction) { this.direction = direction; }
    
    public Entity getTarget() { return target; }
    public void setTarget(Entity target) { this.target = target; }
    
    public double getPower() { return power; }
    public void setPower(double power) { this.power = power; }
    
    public double getRange() { return range; }
    public void setRange(double range) { this.range = range; }
    
    public int getDelay() { return delay; }
    public void setDelay(int delay) { this.delay = delay; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    // 魔力消耗相关方法
    public double getManaCost(String type) {
        return manaCosts.getOrDefault(type, 0.0);
    }
    
    public void setManaCost(String type, double cost) {
        manaCosts.put(type, cost);
    }
    
    public void addManaCost(String type, double additionalCost) {
        double current = getManaCost(type);
        setManaCost(type, current + additionalCost);
    }
    
    public void multiplyManaCost(String type, double multiplier) {
        double current = getManaCost(type);
        setManaCost(type, current * multiplier);
    }
    
    public void powerManaCost(String type, double exponent) {
        double current = getManaCost(type);
        setManaCost(type, Math.pow(current, exponent));
    }
    
    public Map<String, Double> getAllManaCosts() {
        return new HashMap<>(manaCosts);
    }
    
    // 自定义数据存储
    public void setCustomData(String key, Object value) {
        customData.put(key, value);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getCustomData(String key, Class<T> type) {
        Object value = customData.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    public boolean hasCustomData(String key) {
        return customData.containsKey(key);
    }

    // 只读地获取全部自定义数据的副本
    public Map<String, Object> getAllCustomData() {
        return new HashMap<>(customData);
    }
}
