package org.creepebucket.programmable_magic.mananet;

import net.minecraft.world.level.Level;
import org.creepebucket.programmable_magic.ModUtils;

import java.util.Map;

public class NetworkManaData {
    public Long id;
    public Level level;
    public Map<String, ModUtils.Mana> data;

    public NetworkManaData(Long id, Level level, Map<String, ModUtils.Mana> data) {
        this.data = data;
        this.id = id;
        this.level = level;
    }

    public ModUtils.Mana getCurrent(){
        return data.get("current");
    }

    public NetworkManaData setCurrent(ModUtils.Mana current){
        data.put("current", current);
        return this;
    }

    public ModUtils.Mana getLoad(){
        return data.get("load");
    }

    public NetworkManaData setLoad(ModUtils.Mana load){
        NetworkManaManager.touch(level, id);
        data.put("load", data.get("load").add(load));
        return this;
    }

    public ModUtils.Mana getCache(){
        return data.get("cache");
    }

    public NetworkManaData setCache(ModUtils.Mana cache){
        NetworkManaManager.touch(level, id);
        data.put("cache", data.get("cache").add(cache));
        return this;
    }

    /**
     * 获取下一刻的魔力储量
     */
    public ModUtils.Mana getNext(){
        return getCurrent().subtract(getLoad());
    }

    /**
     * 获取下一刻能不能继续运行
     */
    public boolean canProduce(){
        return !new ModUtils.Mana().anyGreaterThan(getNext());
    }
}
