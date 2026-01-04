package org.creepebucket.programmable_magic.gui.lib.api;

public class DataInstance {

    private final DataManager manager;
    private final String key;
    private final DataType type;
    private final SyncMode syncMode;

    DataInstance(DataManager manager, String key, DataType type, SyncMode syncMode) {
        this.manager = manager;
        this.key = key;
        this.type = type;
        this.syncMode = syncMode;
    }

    public String key() { return this.key; }
    public DataType type() { return this.type; }
    public SyncMode syncMode() { return this.syncMode; }

    public Object get() { return this.manager.get(this.key); }

    public void set(Object value) { this.manager.set(this.key, value); }

    public String getString() { return (String) get(); }
    public int getInt() { return (Integer) get(); }
    public double getDouble() { return (Double) get(); }
    public boolean getBoolean() { return (Boolean) get(); }

    public void setString(String v) { set(v); }
    public void setInt(int v) { set(v); }
    public void setDouble(double v) { set(v); }
    public void setBoolean(boolean v) { set(v); }
}
