package org.creepebucket.programmable_magic.utils;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class BezierUtils {
    /**
     * 计算三阶贝塞尔曲线上的单个点 (2个控制点)
     * 公式: B(t) = (1-t)^3*P0 + 3*(1-t)^2*t*P1 + 3*(1-t)*t^2*P2 + t^3*P3
     *
     * @param t  进度 (0.0 到 1.0)
     * @param p0 起始点
     * @param p1 控制点1 (影响起始段的走向)
     * @param p2 控制点2 (影响结束段的走向)
     * @param p3 终点
     */
    public static Vec3 getCubicBezierPoint(float t, Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3) {
        var u = 1 - t;

        // 预计算次幂，减少计算量
        var uu = u * u;
        var uuu = uu * u;
        var tt = t * t;
        var ttt = tt * t;
        // 计算各个分量 (1, 3, 3, 1 是杨辉三角的系数)
        var x = uuu * p0.x + 3 * uu * t * p1.x + 3 * u * tt * p2.x + ttt * p3.x;
        var y = uuu * p0.y + 3 * uu * t * p1.y + 3 * u * tt * p2.y + ttt * p3.y;
        var z = uuu * p0.z + 3 * uu * t * p1.z + 3 * u * tt * p2.z + ttt * p3.z;
        return new Vec3(x, y, z);
    }

    /**
     * 生成三阶贝塞尔曲线的点集
     *
     * @param segments 线段数量（生成的点数为 segments + 1）
     */
    public static List<Vec3> generateCubicCurve(Vec3 start, Vec3 cp0, Vec3 cp1, Vec3 end, int segments) {
        List<Vec3> points = new ArrayList<>();
        for (int i = 0; i <= segments; i++) {
            var t = (float) i / segments;
            points.add(getCubicBezierPoint(t, start, cp0, cp1, end));
        }
        return points;
    }
}
