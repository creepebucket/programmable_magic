#!/usr/bin/env bash
set -euo pipefail

# 切换到脚本所在目录，确保相对路径正确
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

PYTHON_BIN="python3"
VENV_DIR="$SCRIPT_DIR/.venv-assets"
USE_VENV=true

# 检查 venv 能力
if ! "$PYTHON_BIN" -c "import venv" >/dev/null 2>&1; then
	USE_VENV=false
fi

if [ "$USE_VENV" = true ]; then
	if [ ! -d "$VENV_DIR" ]; then
		"$PYTHON_BIN" -m venv "$VENV_DIR" || USE_VENV=false
	fi
	# 若未生成激活脚本则降级
	if [ ! -f "$VENV_DIR/bin/activate" ]; then
		USE_VENV=false
	fi
fi

if [ "$USE_VENV" = true ]; then
	# shellcheck disable=SC1091
	source "$VENV_DIR/bin/activate"
	PYCMD="python"
else
	PYCMD="$PYTHON_BIN"
	# 确保 pip 可用
	if ! "$PYCMD" -m pip --version >/dev/null 2>&1; then
		if command -v curl >/dev/null 2>&1; then
			curl -sS https://bootstrap.pypa.io/get-pip.py -o get-pip.py
		elif command -v wget >/dev/null 2>&1; then
			wget -q https://bootstrap.pypa.io/get-pip.py -O get-pip.py
		else
			echo "需要 curl 或 wget 以安装 pip" >&2
			exit 1
		fi
		"$PYCMD" get-pip.py
		rm -f get-pip.py
	fi
fi

# 安装 Pillow
"$PYCMD" - <<'PY'
try:
	import PIL  # noqa
	print("Pillow 已安装，跳过安装步骤")
except Exception:
	print("安装 Pillow 依赖...")
	import sys, subprocess
	subprocess.check_call([sys.executable, "-m", "pip", "install", "--no-cache-dir", "Pillow>=10.0.0"])  # 可按需固定版本
PY

# 执行资源生成脚本
"$PYCMD" build_assets.py buildconfig.json
