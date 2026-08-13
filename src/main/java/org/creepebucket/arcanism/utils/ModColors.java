package org.creepebucket.arcanism.utils;

import org.creepebucket.arcanism.gui.lib.api.Color;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.creepebucket.arcanism.Arcanism.MODID;

public class ModColors {
    // 主题色
    public static final Color MAIN_COLOR_R = new Color(0xffe6da1f);
    public static final Color MAIN_COLOR_T = new Color(0xfff0332a);
    public static final Color MAIN_COLOR_M = new Color(0xff1fd0e0);
    public static final Color MAIN_COLOR_P = new Color(0xff2fd44a);

    // 文本颜色
    public static final Color TEXT_COLOR_R = new Color(0xffd8d45a);
    public static final Color TEXT_COLOR_T = new Color(0xffe15d50);
    public static final Color TEXT_COLOR_M = new Color(0xff4ab8c4);
    public static final Color TEXT_COLOR_P = new Color(0xff67cf67);

    // 法术 -> 颜色
    public static Map<String, Integer> SPELL_COLORS() {
        Map<String, Integer> COLOR_MAP = new LinkedHashMap<>();
        COLOR_MAP.put("spell." + MODID + ".subcategory.visual", 0xFFC832A1);
        COLOR_MAP.put("spell." + MODID + ".subcategory.entity", 0xFFC82C59);
        COLOR_MAP.put("spell." + MODID + ".subcategory.block", 0xFFEB3838);
        COLOR_MAP.put("spell." + MODID + ".subcategory.trigger", 0xFFC8702C);
        COLOR_MAP.put("spell." + MODID + ".subcategory.structure", 0xFFC8902C);
        COLOR_MAP.put("spell." + MODID + ".subcategory.flow_control", 0xFFC8B32C);
        COLOR_MAP.put("spell." + MODID + ".subcategory.dynamic_constant.number", 0xFF9FE333);
        COLOR_MAP.put("spell." + MODID + ".subcategory.constants.number", 0xFF5DEE22);
        COLOR_MAP.put("spell." + MODID + ".subcategory.operations.number", 0xFF31FF7E);
        COLOR_MAP.put("spell." + MODID + ".subcategory.dynamic_constant.vector", 0xFF3AFFED);
        COLOR_MAP.put("spell." + MODID + ".subcategory.constants.vector", 0xFF2DCDFF);
        COLOR_MAP.put("spell." + MODID + ".subcategory.operations.vector", 0xFF3498FF);
        COLOR_MAP.put("spell." + MODID + ".subcategory.operations.boolean", 0xFF424EF9);
        COLOR_MAP.put("spell." + MODID + ".subcategory.constants.boolean", 0xFF7747F0);
        COLOR_MAP.put("spell." + MODID + ".subcategory.dynamic_constant.entity", 0xFF8F21FF);
        COLOR_MAP.put("spell." + MODID + ".subcategory.operations.block", 0xFFB53EDF);

        COLOR_MAP.put("spell." + MODID + ".subcategory.custom", 0xFF000000);
        return COLOR_MAP;
    }
}
