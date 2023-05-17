#!/usr/bin/env bash
#在Linux、macOS系统终端中运行这个文件，自动完成java文件编译和运行

clear

#修改这里指定需要使用的JDK（/结尾bin目录完整路径），否则将使用已安装的默认JDK
jdkBinDir=""
#jdkBinDir="/home/download/jdk-19.0.1/bin/"

if [ "$jdkBinDir" == "" ]; then
	echo "正在读取JDK版本（如需指定JDK为特定版本或目录，请修改本sh文件内jdkBinDir为JDK bin目录）："
else
	echo "正在读取JDK（${jdkBinDir}）版本："
fi
function err(){ echo -e "\e[31m$1\e[0m"; }

${jdkBinDir}javac -version
[ ! $? -eq 0 ] && { err "需要安装JDK才能编译运行java文件"; exit; }

${jdkBinDir}javac -encoding utf-8 -cp "./*" *.java
[ ! $? -eq 0 ] && { err "java文件编译失败"; exit; }

dir="com/github/xiangyuecn/areacity/query"
if [ ! -e $dir ]; then
	mkdir -p $dir
else
	rm ${dir}/*.class > /dev/null 2>&1
fi
mv *.class ${dir}

echo "java -Xmx300m Test -cmd 已限制java最大允许使用300M内存"
${jdkBinDir}java -cp "./:./*" -Xmx300m com.github.xiangyuecn.areacity.query.Test -cmd



