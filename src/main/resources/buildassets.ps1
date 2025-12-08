# 跨平台资源切片（Windows PowerShell 版）
# 假设已安装 Python3 与 pip。为简洁起见，直接用 venv 的 python 执行。

$ErrorActionPreference = 'Stop'

# 进入脚本目录，确保相对路径正确
$script_dir = Split-Path -LiteralPath $MyInvocation.MyCommand.Path -Parent
Set-Location $script_dir

$python_bin = 'py'
$venv_dir = Join-Path $script_dir '.venv-assets'
$venv_python = Join-Path $venv_dir 'Scripts/python.exe'
$use_venv = $true

# 检查 venv 能力
try {
    & $python_bin -c "import venv" | Out-Null
} catch {
    $use_venv = $false
}

if ($use_venv) {
    if (-not (Test-Path $venv_python)) {
        & $python_bin -m venv $venv_dir
    }
}

if ($use_venv -and (Test-Path $venv_python)) {
    $pycmd = $venv_python
} else {
    $pycmd = $python_bin
}

# 安装 Pillow（若缺失）
$probe = & $pycmd -c "import importlib.util as u; print('OK' if u.find_spec('PIL') else 'MISS')"
if ($probe -ne 'OK') {
    & $pycmd -m pip install --no-cache-dir Pillow>=10.0.0
}

# 执行资源生成脚本
& $pycmd build_assets.py buildconfig.json
