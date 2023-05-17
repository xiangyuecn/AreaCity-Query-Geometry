#!/usr/bin/env bash
#在Linux、macOS系统终端中运行这个文件，自动完成java文件编译和打包成jar

dir=`pwd`; dir=`basename $dir`;
if [ "$dir" != "scripts" ]; then echo "请到scripts目录中运行本脚本。"; exit; fi

clear
cd ../

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
[ ! $? -eq 0 ] && { err "需要安装JDK才能编译java文件"; exit; }


function JarN(){
	echo ""
	echo "请选择需要的生成操作："
	echo "  1. 仅生成依赖jar文件（放到其他项目中Java代码调用，不含Test.java）"
	echo "  2. 生成可运行jar文件（包含Test.java控制台程序）"
	echo "  3. 退出"
	read -p "请输入序号:" step
	echo ""
	if [ "$step" == 1 ]; then Jar1;
	elif [ "$step" == 2 ]; then Jar2;
	elif [ "$step" == 3 ]; then exit;
	else echo "序号无效！请重新输入"; fi
	
	read -s -n1 -p "按任意键继续...";
	echo ""
	JarN;
}

function Clazz(){
	echo 编译中...
	${jdkBinDir}javac -encoding utf-8 -cp "./*" $1
	[ ! $? -eq 0 ] && { err "java文件编译失败"; return 1; }

	dir="target/classes/com/github/xiangyuecn/areacity/query"
	if [ -e $dir ]; then rm -r target/classes > /dev/null 2>&1; fi
	mkdir -p $dir
	mv *.class $dir
	
	echo 编译完成，正在生成jar...
}

function Jar1(){
	Clazz AreaCityQuery.java
	[ ! $? -eq 0 ] && { return 1; }
	
	dir="target/jarLib/"
	if [ ! -e $dir ]; then mkdir -p $dir; fi
	jarPath="${dir}areacity-query-geometry.lib.jar"
	
	${jdkBinDir}jar cf $jarPath -C target/classes/ com
	[ ! $? -eq 0 ] && { err "生成jar失败"; return 1; }
	cp jts-core-*.jar $dir
	echo "已生成jar，文件在源码根目录：${jarPath}，请copy这个jar + jts-core-xxx.jar 到你的项目中使用。"
}

function Jar2(){
	Clazz "*.java"
	[ ! $? -eq 0 ] && { return 1; }
	
	dir=target/jarConsole/
	dir_libs=${dir}libs/
	[ ! -e $dir ] && { mkdir -p $dir; }
	[ ! -e $dir_libs ] && { mkdir -p $dir_libs; }
	jarPath=${dir}areacity-query-geometry.console.jar
	
	cp *.jar $dir_libs
	jarArr=""
	for a in `ls $dir_libs`; do jarArr="${jarArr} libs/${a}"; done
	echo Class-Path: $jarArr
	
	MANIFEST=target/classes/MANIFEST.MF
	echo Manifest-Version: 1.0>$MANIFEST
	echo Class-Path:${jarArr}>>$MANIFEST
	echo Main-Class: com.github.xiangyuecn.areacity.query.Test>>$MANIFEST
	
	${jdkBinDir}jar cfm $jarPath target/classes/MANIFEST.MF -C target/classes/ com
	[ ! $? -eq 0 ] && { err "生成jar失败"; return 1; }
	echo "已生成jar，文件在源码根目录：${jarPath}，libs内已包含依赖的其他jar文件，使用时请全部复制。"
	echo "请到这个文件夹里面后，执行命令运行这个jar："
	echo "      java -jar areacity-query-geometry.console.jar"
}

JarN;
