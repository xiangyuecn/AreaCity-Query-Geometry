@echo off
::在Windows系统中双击运行这个文件，自动完成java文件编译和运行

:Run
cls

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
	echo 需要安装JDK才能编译运行java文件
	goto Pause
)

%jdkBinDir%javac -encoding utf-8 -cp "./*" *.java
if errorlevel 1 (
	echo java文件编译失败
	goto Pause
)

set dir=com\github\xiangyuecn\areacity\query
if not exist %dir% (
	md %dir%
) else (
	del %dir%\*.class > nul
)
move *.class %dir% > nul

echo java -Xmx300m Test -cmd 已限制java最大允许使用300M内存
%jdkBinDir%java -cp "./;./*" -Xmx300m com.github.xiangyuecn.areacity.query.Test -cmd

:Pause
pause
:End