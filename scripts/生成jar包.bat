@echo off
::在Windows系统中双击运行这个文件，自动完成java文件编译和打包成jar

for %%i in (%cd%) do set dir=%%~ni
if not "%dir%"=="scripts" (
	echo 请到scripts目录中运行本脚本。
	goto Pause
)

:Run
cls
cd ../
setlocal enabledelayedexpansion

::修改这里指定需要使用的JDK（\结尾bin目录完整路径），否则将使用已安装的默认JDK
set jdkBinDir=
::set jdkBinDir=D:\xxxx\jdk-18_windows-x64_bin\jdk-18.0.2.1\bin\

if "%jdkBinDir%"=="" (
	echo 正在读取JDK版本（如需指定JDK为特定版本或目录，请修改本bat文件内jdkBinDir为JDK bin目录）：
) else (
	echo 正在读取JDK（%jdkBinDir%）版本：
)


%jdkBinDir%javac -version
if errorlevel 1 (
	echo 需要安装JDK才能编译java文件
	goto Pause
)

:JarN
	echo.
	echo 请选择需要的生成操作：
	echo   1. 仅生成依赖jar文件（放到其他项目中Java代码调用，不含Test.java）
	echo   2. 生成可运行jar文件（包含Test.java控制台程序）
	echo   3. 退出
	set step=
	set /p step=请输入序号:
	echo.
	if "%step%"=="1" goto Jar1
	if "%step%"=="2" goto Jar2
	if "%step%"=="3" goto Pause
	echo 序号无效！请重新输入
	goto JarN

:Clazz
	echo 编译中...
	%jdkBinDir%javac -encoding utf-8 -cp "./*" %Clazz_Files%
	if errorlevel 1 (
		echo java文件编译失败
		goto JarN
	)

	set dir=target\classes\com\github\xiangyuecn\areacity\query
	if exist target\classes rd /S /Q target\classes > nul
	md %dir%
	move *.class %dir% > nul

	echo 编译完成，正在生成jar...
	goto %Clazz_End%

:Jar1
	set Clazz_Files=AreaCityQuery.java
	set Clazz_End=Jar1_1
	goto Clazz
	:Jar1_1
	
	set dir=target\jarLib\
	if not exist %dir% md %dir%
	set jarPath=%dir%areacity-query-geometry.lib.jar
	
	%jdkBinDir%jar cf %jarPath% -C target/classes/ com
	if errorlevel 1 (
		echo 生成jar失败
	) else (
		copy jts-core-*.jar %dir% > nul
		echo 已生成jar，文件在源码根目录：%jarPath%，请copy这个jar + jts-core-xxx.jar 到你的项目中使用。
	)
	echo.
	pause
	goto JarN

:Jar2
	set Clazz_Files=*.java
	set Clazz_End=Jar2_1
	goto Clazz
	:Jar2_1
	
	set dir=target\jarConsole\
	set dir_libs=%dir%libs\
	if not exist %dir% md %dir%
	if not exist %dir_libs% md %dir_libs%
	set jarPath=%dir%areacity-query-geometry.console.jar
	
	copy *.jar %dir_libs% > nul
	set jarArr=
	for /f %%a in ('dir /b "%dir_libs%"') do (set jarArr=!jarArr! libs/%%a)
	echo Class-Path:%jarArr%
	
	set MANIFEST=target\classes\MANIFEST.MF
	echo Manifest-Version: 1.0>%MANIFEST%
	echo Class-Path:%jarArr%>>%MANIFEST%
	echo Main-Class: com.github.xiangyuecn.areacity.query.Test>>%MANIFEST%
	
	%jdkBinDir%jar cfm %jarPath% target/classes/MANIFEST.MF -C target/classes/ com
	if errorlevel 1 (
		echo 已生成jar失败
	) else (
		echo 已生成jar，文件在源码根目录：%jarPath%，libs内已包含依赖的其他jar文件，使用时请全部复制。
		echo 请到这个文件夹里面后，执行命令运行这个jar：
		echo       java -jar areacity-query-geometry.console.jar
	)
	echo.
	pause
	goto JarN

:Pause
pause
:End