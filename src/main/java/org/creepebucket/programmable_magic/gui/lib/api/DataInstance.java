package org.creepebucket.programmable_magic.gui.lib.api;

/**
 * 一个数据键的实例句柄：通过 {@link DataManager} 访问具体值，并携带类型与同步模式元信息。
 */
public class DataInstance {

    private final DataManager manager;
    private final String key;
    private final DataType type;
    private final SyncMode syncMode;

    /**
     * 仅允许由 {@link DataManager} 创建。
     */
    DataInstance(DataManager manager, String key, DataType type, SyncMode syncMode) {
        this.manager = manager;
        this.key = key;
        this.type = type;
        this.syncMode = syncMode;
    }

    /**
     * 返回该实例对应的数据键。
     */
    public String key() { return this.key; }

    /**
     * 返回该键声明的类型。
     */
    public DataType type() { return this.type; }

    /**
     * 返回该键的同步模式。
     */
    public SyncMode syncMode() { return this.syncMode; }

    /**
     * 读取当前值。
     */
    public Object get() { return this.manager.get(this.key); }

    /**
     * 写入当前值（并按同步模式发送到对端）。
     */
    public void set(Object value) { this.manager.set(this.key, value); }

    /**
     * 读取为字符串。
     */
    public String getString() { return (String) get(); }

    /**
     * 读取为整数。
     */
    public int getInt() { return (Integer) get(); }

    /**
     * 读取为双精度浮点数。
     */
    public double getDouble() { return (Double) get(); }

    /**
     * 读取为布尔值。
     */
    public boolean getBoolean() { return (Boolean) get(); }

    /**
     * 写入字符串。
     */
    public void setString(String v) { set(v); }

    /**
     * 写入整数。
     */
    public void setInt(int v) { set(v); }

    /**
     * 写入双精度浮点数。
     */
    public void setDouble(double v) { set(v); }

    /**
     * 写入布尔值。
     */
    public void setBoolean(boolean v) { set(v); }
}
