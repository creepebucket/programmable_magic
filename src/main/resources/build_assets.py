import json
import os
from PIL import Image
import sys


def process_texture_pack(json_config_path):
    """
    处理材质包生成
    :param source_image_path: 源PNG图片路径
    :param json_config_path: JSON配置文件路径
    """
    try:
        # 加载JSON配置
        with open(json_config_path, 'r', encoding='utf-8') as f:
            config_data = json.load(f)
        print(f"成功加载JSON配置: {json_config_path}")

        # 遍历所有配置项
        for item in config_data:
            for texture_path, operations in item.items():

                source_img = Image.open(texture_path).convert("RGBA")
                print(f"成功加载源图片: {texture_path} ({source_img.width}x{source_img.height})")

                for op in operations:
                    try:
                        # 解析坐标参数
                        from_tl = op['from_top_left']
                        from_br = op['from_bottom_right']
                        target_path = op['target_img_path']

                        # 计算裁剪区域
                        crop_box = (
                            from_tl[0],
                            from_tl[1],
                            from_br[0] + 1,  # +1 因为Pillow的crop右边界是排他性的
                            from_br[1] + 1  # +1 同上
                        )

                        # 裁剪图片
                        cropped_img = source_img.crop(crop_box)

                        # 创建目标目录
                        os.makedirs(os.path.dirname(target_path), exist_ok=True)

                        # 处理目标位置参数
                        if 'to_top_left' in op and 'to_bottom_right' in op:
                            to_tl = op['to_top_left']
                            to_br = op['to_bottom_right']

                            # 检查目标图片是否存在
                            if os.path.exists(target_path):
                                target_img = Image.open(target_path).convert("RGBA")
                                print(f"更新现有图片: {target_path}")
                            else:
                                # 创建新的透明画布
                                canvas_width = to_br[0] + 1
                                canvas_height = to_br[1] + 1
                                target_img = Image.new("RGBA", (canvas_width, canvas_height), (0, 0, 0, 0))
                                print(f"创建新图片: {target_path} ({canvas_width}x{canvas_height})")

                            # 将裁剪的图片粘贴到目标位置
                            target_img.paste(cropped_img, (to_tl[0], to_tl[1]))
                            target_img.save(target_path)
                            print(
                                f"图片部分更新: {target_path} 位置 [{to_tl[0]},{to_tl[1]}] 到 [{to_br[0]},{to_br[1]}]")
                        else:
                            # 直接保存裁剪的图片
                            cropped_img.save(target_path)
                            print(f"保存新图片: {target_path} ({cropped_img.width}x{cropped_img.height})")

                    except KeyError as e:
                        print(f"配置项缺少必要字段: {e} | 操作: {op}")
                    except Exception as e:
                        print(f"处理操作失败: {e} | 操作: {op}")

                if 'source_img' in locals():
                    source_img.close()

    except FileNotFoundError as e:
        print(f"文件未找到: {e}")
    except json.JSONDecodeError:
        print("JSON解析错误，请检查配置文件格式")
    except Exception as e:
        print(f"处理过程中发生未知错误: {e}")
    finally:
        if 'source_img' in locals():
            source_img.close()


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("用法: python texture_pack_generator.py <JSON配置路径>")
        print("示例: python texture_pack_generator.py mappings.json")
        sys.exit(1)

    json_config = sys.argv[1]
    process_texture_pack(json_config)
