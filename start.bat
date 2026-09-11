@echo off
chcp 65001 >nul
REM 校园活动管理系统 V1.0 本地启动脚本
REM 需要提前安装 JDK 17+ 和 Maven，并配置好环境变量

cd /d "%~dp0"

echo ===========================================
echo  校园活动管理系统 V1.0 本地启动脚本
echo  访问地址：http://localhost:8080
echo ===========================================
echo.

REM 检查 Java
java -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 java 命令，请先安装 JDK 17+ 并配置环境变量。
    pause
    exit /b 1
)

REM 检查 Maven
mvn -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 mvn 命令，请先安装 Maven 并配置环境变量。
    echo 或者使用 IntelliJ IDEA 直接运行 CampusActivityApplication。
    pause
    exit /b 1
)

echo [1/2] 正在编译并启动项目，首次运行会下载依赖，请耐心等待...
echo.

mvn spring-boot:run

if errorlevel 1 (
    echo.
    echo [错误] 项目启动失败，请查看上方错误信息。
    pause
    exit /b 1
)

pause
