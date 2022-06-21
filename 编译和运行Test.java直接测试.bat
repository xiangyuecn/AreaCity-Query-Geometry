@echo off

:Run
cls

javac -version
if errorlevel 1 (
	echo 需要安装JDK才能编译运行java文件
	goto Pause
)

javac -encoding utf-8 -Djava.ext.dirs=./ *.java
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
java -Djava.ext.dirs=./ -Xmx300m com.github.xiangyuecn.areacity.query.Test -cmd

:Pause
pause
:End