package com.github.xiangyuecn.areacity.query;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Random;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import com.github.xiangyuecn.areacity.query.AreaCityQuery.Func;
import com.github.xiangyuecn.areacity.query.AreaCityQuery.QueryInitInfo;
import com.github.xiangyuecn.areacity.query.AreaCityQuery.QueryResult;

/**
 * AreaCityQuery测试控制台主程序
 * 
 * GitHub: https://github.com/xiangyuecn/AreaCity-Query-Geometry （github可换成gitee）
 * 省市区县乡镇区划边界数据: https://github.com/xiangyuecn/AreaCity-JsSpider-StatsGov （github可换成gitee）
 */
public class Test {
	static public void main(String[] args) throws Exception {
		//【请在这里编写你自己的测试代码】
		
		Start(args);
		
	}
	
	
	
	/** 初始化时，只读取区县级数据，否则省市区都读 **/
	static boolean Deep2Only=false;
	/** http api 服务端口号**/
	static int HttpApiServerPort=9527;
	
	static boolean HasDeep0/*省*/,HasDeep1/*市*/,HasDeep2/*区县*/,HasDeep3/*乡镇街道*/;
	static long BootMaxMemory;
	static void Init(boolean storeInWkbsFile) throws Exception {
		if(BootMaxMemory==0) BootMaxMemory=Runtime.getRuntime().maxMemory();
		if(BootMaxMemory > 300*1000*1000) {
			System.out.println("可以在启动参数中配置 -Xmx300m 调小内存来测试。");
		}
		System.out.println("========== "+(storeInWkbsFile?"Init_StoreInWkbsFile":"Init_StoreInMemory")+" ==========");
		
		File file=new File("./");
		String[] files=file.list();
		ArrayList<String> jsonFiles=new ArrayList<>();
		ArrayList<String> wkbsFiles=new ArrayList<>();
		for(String name : files) {
			String str=name.toLowerCase();
			if(str.endsWith("json") || str.endsWith("geojson")) {
				jsonFiles.add(name);
			}
			if(str.endsWith("wkbs")) {
				wkbsFiles.add(name);
			}
		}
		String jsonFile="";
		String wkbsFile="";
		
		
		System.out.println("Init_StoreInWkbsFile：用加载数据到结构化数据文件的模式进行初始化，推荐使用本方法初始化，边界图形数据存入结构化数据文件中，内存占用很低，查询时会反复读取文件对应内容，查询性能消耗主要在IO上，IO性能极高问题不大。");
		System.out.println("Init_StoreInMemory：用加载数据到内存的模式进行初始化，边界图形数据存入内存中，内存占用和json数据文件大小差不多大，查询性能极高；另外可通过设置 AreaCityQuery.SetInitStoreInMemoryUseObject=true 来进一步提升性能，但内存占用会增大一倍多。");
		System.out.println(HR);
		System.out.println("首次初始化会从.json或.geojson文件中读取边界图形数据，速度比较慢，会自动生成.wkbs结尾的结构化文件，下次初始化就很快了。");
		System.out.println("  - 如果.wkbs文件已存在并且有效，将优先从wkbs文件中读取数据，速度很快。");
		System.out.println("  - 你可以在当前目录内放入 .json|.geojson|.wkbs 文件(utf-8)，可通过菜单进行选择初始化，否则需要输入文件路径。");
		System.out.println("  - 当前目录："+new File("").getAbsolutePath());
		System.out.println("  - json文件内必须一条数据占一行，如果不是将不支持解析，请按下面的方法生成一个json文件测试。");
		System.out.println(HR);
		System.out.println("如何获取省市区县乡镇边界数据json文件：");
		System.out.println("  1. 请到开源库下载省市区边界数据ok_geo.csv文件: https://github.com/xiangyuecn/AreaCity-JsSpider-StatsGov （github可换成gitee）；");
		System.out.println("  2. 下载开源库里面的“AreaCity-Geo格式转换工具软件”；");
		System.out.println("  3. 打开转换工具软件，选择ok_geo.csv，然后导出成geojson文件即可（默认会导出全国的省级数据，通过填写不同城市名前缀可以导出不同城市）；");
		System.out.println("  4. 将导出的json文件复制到本java程序目录内，然后重新运行初始化即可解析使用此文件。");
		System.out.println(HR);
		boolean useInputFile=false;
		boolean useChoice=false;
		if(jsonFiles.size()>0 || wkbsFiles.size()>0) {
			useChoice=true;
		} else {
			System.out.println("在当前目录内未发现任何一个 .json|.geojson|.wkbs 文件，需手动填写文件路径。");
			System.out.println();
			useInputFile=true;
		}
		if(useChoice) {
			System.out.println("在当前目录内发现数据文件，请选择要从哪个文件初始化，请输入文件序号：");
			int idx=0;
			System.out.println(idx+". 手动输入文件路径");
			for(String name : jsonFiles) {
				idx++;
				System.out.println(idx+". [json]"+name);
			}
			for(String name : wkbsFiles) {
				idx++;
				System.out.println(idx+". [wkbs]"+name);
			}
			while(true) {
				System.out.print("> ");
				String txt=ReadIn();
				if(txt.length()==0){
					break;
				} else if(txt.equals("0")) {
					useInputFile=true;
					break;
				} else if(txt.length()>0) {
					idx=-1;
					try { idx=Integer.parseInt(txt); } catch (Exception e){ }
					if(idx>0 && idx<=jsonFiles.size()) {
						jsonFile=jsonFiles.get(idx-1);
					} else if(idx>0 && idx-jsonFiles.size()<=wkbsFiles.size()) {
						wkbsFile=wkbsFiles.get(idx-jsonFiles.size()-1);
					} else {
						System.out.println("输入的序号无效，请重新输入！");
						continue;
					}
					break;
				}
			}
		}
		if(useInputFile) {
			System.out.println("请输入初始化要读取的一个 .json|.geojson|.wkbs 文件完整路径：");
			while(true) {
				System.out.print("> ");
				String txt=ReadIn();
				String str=txt.toLowerCase();
				if(txt.length()==0) {
					break;
				} else if(str.endsWith("json") || str.endsWith("geojson")) {
					jsonFile=txt;
				} else if (str.endsWith("wkbs")) {
					wkbsFile=txt;
				} else {
					System.out.println("输入的文件类型无效，请重新输入！");
					continue;
				}
				File f=new File(txt);
				if(!f.exists()) {
					jsonFile="";
					wkbsFile="";
					System.out.println("文件不存在，请重新输入！(未找到文件："+f.getAbsolutePath()+")");
					continue;
				}
				break;
			}
		}
		
		String initDataFile,initSaveWkbsFile;
		if(jsonFile.length()>0) {
			initDataFile=new File(jsonFile).getAbsolutePath();
			initSaveWkbsFile=initDataFile+".wkbs";
		} else if(wkbsFile.length()>0) {
			initDataFile=new File(wkbsFile).getAbsolutePath();
			initSaveWkbsFile="";
		} else {
			System.out.println("未选择文件，不初始化，已退出！");
			return;
		}
		
		HasDeep0=HasDeep1=HasDeep2=HasDeep3=false;

		//初始化回调
		AreaCityQuery.OnInitProgress=new Func<QueryInitInfo, Boolean>() {
			long logTime=0;int maxNo=0,lastNo=0;
			@Override
			public Boolean Exec(QueryInitInfo info) throws Exception {
				if(logTime==0) {
					if(info.DataFromWkbsFile) {
						System.out.println("正在从wkbs结构化数据文件中快速读取数据...");
					} else {
						System.out.println("首次运行，正在生成wkbs结构化数据文件，速度可能会比较慢...");
					}
				}
				if(info.CurrentLine_No!=0) {
					maxNo=info.CurrentLine_No;
				}
				if(info.CurrentLine_No==0 && lastNo!=maxNo || System.currentTimeMillis()-logTime>1000) {
					logTime=System.currentTimeMillis();
					lastNo=maxNo;
					System.out.println("读取第"+lastNo+"行...");
				}
				if(info.CurrentLine_No!=0) {
					String prop=info.CurrentLine_Prop.replace(" ", "").replace("\"", ""); //不解析json，简单处理
					if(!Deep2Only && !HasDeep0)HasDeep0=prop.contains("deep:0");
					if(!Deep2Only && !HasDeep1)HasDeep1=prop.contains("deep:1");
					if(!HasDeep2)HasDeep2=prop.contains("deep:2");
					if(!Deep2Only && !HasDeep3)HasDeep3=prop.contains("unique_id:");
					
					if(Deep2Only) {
						return prop.contains("deep:2"); //只提取区级，其他一律返回false跳过解析
					}
				}
				return true;
			}
		};
		//初始化，如果未生成结构化数据文件（wkbs）这里会从json数据文件自动生成，如果生成了就只会读取wkbs文件
		if(storeInWkbsFile) {
			AreaCityQuery.Init_StoreInWkbsFile(initDataFile, initSaveWkbsFile, true);
		} else {
			AreaCityQuery.Init_StoreInMemory(initDataFile, initSaveWkbsFile, true);
		}

		System.out.println("========== "+(storeInWkbsFile?"Init_StoreInWkbsFile":"Init_StoreInMemory")+" ==========");
		System.out.println(AreaCityQuery.GetInitInfo().toString());
		System.out.println();
		System.out.println("已加载数据级别："+(HasDeep0?"√":"×")+"省，"+(HasDeep1?"√":"×")+"市，"+(HasDeep2?"√":"×")+"区县，"+(HasDeep3?"√":"×")+"乡镇 （×为未加载，可能是数据文件中并不含此级数据）");
		System.out.println();
	}
	
	static boolean ResultHas(QueryResult res, String str) {
		if(res.Result!=null) {
			for(int i=0,L=res.Result.size();i<L;i++) {
				if(res.Result.get(i).contains(str)) {
					return true;
				}
			}
		}
		return false;
	} 
	static void BaseTest() throws Exception {
		int loop=100;
		System.out.println("========== QueryPoint ==========");
		{
			QueryResult res=new QueryResult();
			res.Set_EnvelopeHitResult=new ArrayList<>();
			//res.Set_ReturnWKTKey="polygon_wkt";
			for(int i=0;i<loop;i++) {
				res.Result.clear();//清除一下上次的结果，只保留统计
				res.Set_EnvelopeHitResult.clear();
				res=AreaCityQuery.QueryPoint(114.044346, 22.691963, null, res);
			}
			System.out.println(res.toString());
			if(HasDeep2) {
				System.out.println(ResultHas(res, "龙华区\"")?"OK":"查询失败！");
			}
		}

		System.out.println();
		System.out.println("========== QueryGeometry ==========");
		if(!HasDeep0){
			System.out.println("无省级边界，其他级别返回结果会过多，不测试。");
		}else{
			double x0=113.305514,y0=30.564249;//河南、安徽、湖北，三省交界的一个超大矩形范围
			double x1=117.326510,y1=32.881526;
			Geometry geom=AreaCityQuery.Factory.createPolygon(new Coordinate[] {
					new Coordinate(x0, y0)
					,new Coordinate(x1, y0)
					,new Coordinate(x1, y1)
					,new Coordinate(x0, y1)
					,new Coordinate(x0, y0)
			});
			
			QueryResult res=new QueryResult();
			for(int i=0;i<loop;i++) {
				res.Result.clear();//清除一下上次的结果，只保留统计
				res=AreaCityQuery.QueryGeometry(geom, new Func<String, Boolean>() {
					@Override
					public Boolean Exec(String prop) throws Exception {
						int i0=prop.indexOf("deep\"");//高性能手撸json字符串
						if(i0==-1)return false;
						int i1=prop.indexOf(",", i0);
						if(i1==-1)i1=prop.length();
						return prop.substring(i0+6,i1).contains("0");//where条件过滤，只查找省级数据（deep==0，json内就是{deep:0}）
					}
				}, res);
			}
			System.out.println(res.toString());
			System.out.println(ResultHas(res, "湖北省\"")
					&& ResultHas(res, "河南省\"")
					&& ResultHas(res, "安徽省\"") ?"OK":"查询失败！");
		}

		System.out.println();
		System.out.println("========== ReadWKT_FromWkbsFile ==========");
		{
			QueryResult res=new QueryResult();
			for(int i=0;i<loop;i++) {
				res.Result.clear();//清除一下上次的结果，只保留统计
				res=AreaCityQuery.ReadWKT_FromWkbsFile("plygon_wkt", res, new Func<String, Boolean>() {
					@Override
					public Boolean Exec(String prop) throws Exception {
						return prop.contains("北京市 朝阳区\"")
							|| prop.contains("武汉市 洪山区\"")
							|| prop.contains("台北市 中山区\"");
					}
				}, null);
			}
			System.out.println(res.toString());
			if(HasDeep2) {
				System.out.println(ResultHas(res, "北京市 朝阳区\"")
						&& ResultHas(res, "武汉市 洪山区\"")
						&& ResultHas(res, "台北市 中山区\"") ?"OK":"查询失败！");
			}
		}
		System.out.println();
	}
	
	static void LargeRndPointTest() throws Exception {
		System.out.println("========== QueryPoint：1万个伪随机点测试 ==========");
		System.out.println("伪随机：虽然是随机生成的点，但每次运行生成坐标列表都是相同的。");
		System.out.println("测试中，请耐心等待...");
		{
			QueryResult res=new QueryResult();
			double x_0=98.0,y_00=21.0;//矩形范围囊括大半个中国版图
			double x_1=122.0,y_1=42.0;
			int size=100;//1万点
			double xStep=(x_1-x_0)/size;
			double yStep=(y_1-y_00)/size;
			while(x_0-x_1<-xStep/2) {//注意浮点数±0.000000001的差异
				double x0=x_0, x1=x_0+xStep; x_0=x1;
				double y_0=y_00;
				while(y_0-y_1<-yStep/2) {
					double y0=y_0, y1=y_0+yStep; y_0=y1;
					
					res=AreaCityQuery.QueryPoint(x0, y0, null, res);
					res.Result.clear();//只保留统计
				}
			}
			res.Result=null;
			System.out.println(res.toString());
			System.out.println();
		}
	}
	static void ThreadRun() throws Exception {
		System.out.println("========== 多线程性能测试 ==========");
		boolean[] stop=new boolean[] {false};
		int ThreadCount=Math.max(1, Runtime.getRuntime().availableProcessors()-1);
		QueryResult[] SecondCompletes=new QueryResult[ThreadCount];

		System.out.println("通过开启 CPU核心数-1 个线程，每个线程内随机查询不同的坐标点，来达到性能测试的目的。");
		System.out.println("正在测试中，线程数："+ThreadCount+"，按回车键结束测试...");
		
		//测试函数
		Func<QueryResult, Object> run=new Func<AreaCityQuery.QueryResult, Object>() {	
			@Override
			public Object Exec(QueryResult res) throws Exception {
				//固定坐标点测试
				boolean allOk=true;
				AreaCityQuery.QueryPoint(114.044346, 22.691963, null, res); //广东省 深圳市 龙华区
				allOk&=ResultHas(res, "深圳市 龙华区\"");res.Result.clear();
				
				AreaCityQuery.QueryPoint(117.286491, 30.450399, null, res); //安徽省 铜陵市 郊区 飞地
				allOk&=ResultHas(res, "铜陵市 郊区\"");res.Result.clear();
				
				AreaCityQuery.QueryPoint(116.055588, 39.709385, null, res); //北京市 房山区 星城街道 飞地
				allOk&=ResultHas(res, "北京市 房山区\"");res.Result.clear();
				
				AreaCityQuery.QueryPoint(130.283168, 47.281807, null, res); // 黑龙江省 鹤岗市 南山区
				allOk&=ResultHas(res, "鹤岗市 南山区\"");res.Result.clear();
				
				AreaCityQuery.QueryPoint(118.161624, 39.656532, null, res); // 河北省  唐山市 路北区
				allOk&=ResultHas(res, "唐山市 路北区\"");res.Result.clear();
				
				AreaCityQuery.QueryPoint(81.869760, 41.812321, null, res); // 新疆 阿克苏地区 拜城县
				allOk&=ResultHas(res, "阿克苏地区 拜城县\"");res.Result.clear();
				
				if(HasDeep2 && !allOk) {
					throw new Exception("查询失败！");
				}
				
				
				//随机坐标点测试
				Random rnd=new Random();
				int count=100;//只计算100次
				
				double x_0=98.0+rnd.nextDouble(),y_00=21.0+rnd.nextDouble();//矩形范围囊括大半个中国版图
				double x_1=122.0+rnd.nextDouble(),y_1=42.0+rnd.nextDouble();
				int size=100;//1万点
				double xStep=(x_1-x_0)/size;
				double yStep=(y_1-y_00)/size;
				x_0+=(rnd.nextInt(size*size-count))*xStep; //随机选择开始位置
				while(x_0-x_1<-xStep/2) {//注意浮点数±0.000000001的差异
					double x0=x_0, x1=x_0+xStep; x_0=x1;
					double y_0=y_00;
					while(y_0-y_1<-yStep/2) {
						double y0=y_0, y1=y_0+yStep; y_0=y1;
						
						AreaCityQuery.QueryPoint(x0, y0, null, res);
						res.Result.clear();//只保留统计
						
						count--;
						if(count==0) {
							return null;
						}
					}
				}
				return null;
			}
		};
		//更新统计显示
		long startTime=System.currentTimeMillis();
		long[] showProgressTime=new long[] {0};
		Func<Object,Object> showProgress=new Func<Object, Object>() {
			@Override
			public Object Exec(Object val) throws Exception {
				synchronized (showProgressTime) {
					if(System.nanoTime()-showProgressTime[0]<1000*1000000) {
						return null;
					}
					
					QueryResult res=new QueryResult();
					for(int i=0;i<SecondCompletes.length;i++) {
						if(SecondCompletes[i]==null) {
							return null;
						}
						SecondCompletes[i].Result=null;
						res.Add(SecondCompletes[i]);
					}
					showProgressTime[0]=System.nanoTime();
					
					res.QueryCount=Math.max(2, res.QueryCount);
					res.StartTimeN=0;
					res.EndTimeN=1000L*1000000*ThreadCount;
					String[] arr=res.toString().split("\n");
					long dur=System.currentTimeMillis()-startTime;
					long f=dur/60000,m=dur/1000%60;
					String s=(f<10?"0":"")+f+":"+(m<10?"0":"")+m;
					if(!stop[0]) {
						System.out.print("\r"+s
								+" QPS["+ThreadCount+"线程 "+res.QueryCount+"]"
								+"[单线程 "+res.QueryCount/ThreadCount+"]"
								+(AreaCityQuery.IsStoreInMemory()?"InMemory":"InWkbsFile")
								+" "+arr[1]+"。按回车键结束测试...");
					}
					return null;
				}
			}
		};
		//获得一个线程执行函数
		Func<Integer,Func<Object, Object>> newThreadRun=new Func<Integer, Func<Object, Object>>() {
			@Override
			public Func<Object, Object> Exec(Integer threadId) throws Exception {
				return new Func<Object, Object>() {
					@Override
					public Object Exec(Object val) throws Exception {
						QueryResult res=new QueryResult();//这个只能单线程用
						long t1=System.nanoTime();
						while(!stop[0]) {
							try {
								run.Exec(res);
								
								long t0=System.nanoTime();
								if(t0-t1>=1000*1000000) {//1秒钟，给赋值一次统计数据
									t1=t0;
									SecondCompletes[threadId]=res;
									showProgress.Exec(null);
									res=new QueryResult();
								}
							} catch(Exception e) {
								e.printStackTrace();
							}
						}
						return null;
					}
				};
			}
		};
		//开启多线程
		int[] threadCount=new int[] { ThreadCount };
		for(int i=0;i<ThreadCount;i++) {
			Func<Object, Object> threadRun=newThreadRun.Exec(i);
			new Thread(new Runnable() {
				public void run() {
					try {
						threadRun.Exec(null);
					} catch(Exception e) {
						e.printStackTrace();
					} finally {
						synchronized (threadCount) { threadCount[0]--; }
					}
				}
			}).start();
		}
		
		ReadIn();
		stop[0]=true;
		System.out.println("等待线程结束...");
		while(threadCount[0]>0) {
			try { Thread.sleep(10); }catch (Exception e) { }
		}
		System.out.println("多线程性能测试已结束。");
		System.out.println();
	}
	
	
	
	static void Query_Point() throws Exception {
		System.out.println("========== 查询一个坐标点对应的省市区乡镇数据 ==========");
		System.out.println("请输入一个坐标点，格式：\"lng lat\"（允许有逗号）：");
		System.out.println("  - 比如：114.044346 22.691963，为广东省 深圳市 龙华区");
		System.out.println("  - 比如：117.286491 30.450399，为安徽省 铜陵市 郊区，在池州市 贵池区的飞地");
		System.out.println("  - 输入 exit 退出查询");
		while(true){
			System.out.print("> ");
			String inStr=ReadIn().trim();
			if(inStr.length()==0) {
				System.out.println("输入为空，请重新输入！如需退出请输入exit");
				continue;
			}
			if(inStr.equals("exit")) {
				System.out.println("bye! 已退出查询。");
				System.out.println();
				return;
			}
			String[] arr=inStr.split("[,\\s]+");
			double lng=-999,lat=-999;
			if(arr.length==2) {
				try {
					lng=Double.parseDouble(arr[0]);
					lat=Double.parseDouble(arr[1]);
				}catch(Exception e) {
					lng=lat=-999;
				}
			}
			if(lng<-180 || lat<-90 || lng>180 || lat>90) {
				System.out.println("输入的坐标格式不正确");
				continue;
			}
			QueryResult res=AreaCityQuery.QueryPoint(lng, lat, null, null);
			System.out.println(res.toString());
		}
	}
	
	static void Read_WKT() throws Exception {
		System.out.println("========== 读取省市区乡镇边界的WKT文本数据 ==========");
		System.out.println("遍历所有边界图形的属性列表查询出符合条件的属性，然后返回图形的属性+边界图形WKT文本。 ");
		System.out.println("读取到的wkt文本，可以直接粘贴到页面内渲染显示：https://xiangyuecn.gitee.io/areacity-jsspider-statsgov/assets/geo-echarts.html");
		System.out.println();
		
		ExtPathExpIn("ReadWKT", new Func<Test.ExtPathExpInArgs, Object>() {
			@Override
			public Object Exec(ExtPathExpInArgs args) throws Exception {
				int[] count=new int[] { 0 };
				AreaCityQuery.ReadWKT_FromWkbsFile("", null, new Func<String, Boolean>() {
					@Override
					public Boolean Exec(String prop) throws Exception {
						return ExtPathMatch(prop, args.extPath_exp);
					}
				}, getWktReadFn("ReadWKT", args, count));
				if(count[0] == 0) {
					System.out.println("未找到“"+args.extPath_inputTxt+"”匹配的属性！");
				} else {
					System.out.println("查找完成，共"+count[0]+"条");
				}
				return null;
			}
		});
	}

	static void Query_DebugReadWKT() throws Exception {
		System.out.println("========== Debug: 读取边界网格划分图形WKT文本数据 ==========");
		System.out.println("调试用的，读取已在wkbs结构化文件中保存的网格划分图形WKT数据，用于核对网格划分情况。");
		System.out.println("读取到的wkt文本，可以直接粘贴到页面内渲染显示：https://xiangyuecn.gitee.io/areacity-jsspider-statsgov/assets/geo-echarts.html");
		System.out.println();
		
		ExtPathExpIn("GirdWKT", new Func<Test.ExtPathExpInArgs, Object>() {
			@Override
			public Object Exec(ExtPathExpInArgs args) throws Exception {
				int[] count=new int[] { 0 };
				AreaCityQuery.Debug_ReadGeometryGridSplitsWKT("", null, new Func<String, Boolean>() {
					@Override
					public Boolean Exec(String prop) throws Exception {
						return ExtPathMatch(prop, args.extPath_exp);
					}
				}, getWktReadFn("GirdWKT", args, count));
				if(count[0] == 0) {
					System.out.println("未找到“"+args.extPath_inputTxt+"”匹配的边界！");
				} else {
					System.out.println("查找完成，共"+count[0]+"条");
				}
				return null;
			}
		});
	}
	
	
	static Func<String[], Boolean> getWktReadFn(String tag, ExtPathExpInArgs args, int[] count){
		return new Func<String[], Boolean>() {
			@Override
			public Boolean Exec(String[] val) throws Exception {
				count[0]++;
				if(args.outFile!=null) {
					String str=val[0] +"\t"+ val[1]+"\n";
					byte[] bytes=str.getBytes("utf-8");
					args.outFile.write(bytes);
					
					System.out.println(count[0]+"条"+tag+"属性："+val[0]);
					System.out.println("        "+bytes.length+"字节已保存到文件："+args.outFilePath);
				} else {
					String str=count[0]+"条"+tag+"属性："+val[0];
					if(val[1].length()>500) {
						System.out.println(str);
						str="        WKT超长未显示（"+val[1].length()+"字节），请命令后面输入\" > 文件名\"输出到文件";
					} else {
						str=str +"\t"+ val[1];
					}
					System.out.println(str);
				}
				return false;
			}
		};
	}
	static boolean ExtPathMatch(String prop, String exp) {
		int i0=prop.indexOf("ext_path");
		if(i0==-1)return false;
		int i1=prop.indexOf(",", i0);
		if(i1==-1)i1=prop.length();
		return prop.substring(i0+9, i1).contains(exp);
	}
	static class ExtPathExpInArgs{
		String extPath_exp;
		String extPath_inputTxt;
		String outFilePath;
		FileOutputStream outFile;
	}
	static void ExtPathExpIn(String tag, Func<ExtPathExpInArgs, Object> fn) throws Exception {
		System.out.println("请输入"+tag+"要查询的城市完整名称，为ext_path值：");
		System.out.println("  - 如：“湖北省 武汉市 洪山区”，精确查找");
		System.out.println("  - 如：“*武汉市*”，*通配符模糊查找");
		System.out.println("  - 如：“*”，查找全部");
		System.out.println("  - 结尾输入“ > 文件名”可保存到文件");
		System.out.println("  - 输入 exit 退出");
		while(true){
			System.out.print("> ");
			String inStr=ReadIn().trim();
			if(inStr.length()==0) {
				System.out.println("输入为空，请重新输入！如需退出请输入exit");
				continue;
			}
			if(inStr.equals("exit")) {
				System.out.println("bye! 已退出读取。");
				System.out.println();
				return;
			}
			ExtPathExpInArgs args=new ExtPathExpInArgs();
			String[] ins=inStr.split(" > ");
			args.extPath_inputTxt=ins[0];
			
			String outFilePath= ins.length>1?ins[1]:"";
			FileOutputStream outFile=null;
			if(outFilePath.length()>0) {
				outFilePath=new File(outFilePath).getAbsolutePath();
				outFile=new FileOutputStream(outFilePath);
			}
			args.outFilePath=outFilePath;
			args.outFile=outFile;
			
			
			String exp=ins[0];
			if(exp.equals("*")) {
				exp="";
			} else {
				if(exp.startsWith("*")) {
					exp=exp.substring(1);
				}else {
					exp="\""+exp;
				}
				if(exp.endsWith("*")) {
					exp=exp.substring(0, exp.length()-1);
				}else {
					exp=exp+"\"";
				}
			}
			args.extPath_exp=exp;
			
			fn.Exec(args);
			
			if(outFile!=null)outFile.close();
		}
	}
	
	
	
	
	
	
	
	static public boolean StartHttpApiServer() throws Exception {
		String clazzName=Test.class.getPackage().getName()+".Test_HttpApiServer";
		Method[] fns;
		try {
			fns=Class.forName(clazzName).getMethods();
		}catch (Exception e) {
			System.out.println("Test_HttpApiServer.java加载失败，不支持启动本地轻量HTTP API服务。");
			return false;
		}
		Method fn=null; for(Method x : fns) if(x.getName().equals("Create")) fn=x;
		return (boolean)fn.invoke(null, "0.0.0.0", HttpApiServerPort);
	}
	
	
	
	
	
	
	
	static public String ReadIn() throws Exception {
		ByteArrayOutputStream in=new ByteArrayOutputStream();
		while(true) {
			int byt=System.in.read();
			if(byt=='\r') continue;
			if(byt=='\n') {
				break;
			}
			in.write(byt);
		}
		return in.toString();
	}
	static boolean IsCmd=false;
	static String HR="-----------------------------------";
	static void Start(String[] args) throws Exception {
		if(args.length>0) {
			System.out.print(args.length+"个启动参数");
			for(int i=0;i<args.length;i++) {
				if(args[i].equals("-cmd")) {
					IsCmd=true;
				}
				System.out.print("，参数"+(i+1)+"："+args[i]);
			}
			System.out.println(IsCmd?"，已进入命令行模式。":"");
			System.out.println();
		}
		
		while(true) {
			boolean isInit=AreaCityQuery.GetInitStatus()==2;
			System.out.println("【功能菜单】");
			System.out.println("1. "+(isInit?"重新":"")+"初始化：调用 Init_StoreInWkbsFile   -内存占用很低（性能受IO限制）"+(AreaCityQuery.IsStoreInWkbsFile()?"               [已初始化]":""));
			System.out.println("2. "+(isInit?"重新":"")+"初始化：调用 Init_StoreInMemory     -内存占用和json文件差不多大（性能豪放）"+(AreaCityQuery.IsStoreInMemory()?"     [已初始化]":""));
			if(isInit) {
				System.out.println(HR);
				System.out.println("3. 测试：基础功能测试");
				System.out.println("4. 测试：1万个伪随机点测试");
				System.out.println("5. 测试：多线程性能测试");
				System.out.println(HR);
				System.out.println("6. 查询: QueryPoint 查找坐标点所在省市区乡镇");
				System.out.println("7. 查询: ReadWKT 读取省市区乡镇边界的WKT文本数据");
				System.out.println("8. 查询: Debug 读取边界网格划分图形WKT文本数据");
				System.out.println(HR);
				System.out.println("9. HTTP: 启动本地轻量HTTP API服务");
			}
			System.out.println(HR);
			System.out.println("*. 输入 exit 退出");
			System.out.println();
			System.out.println("请输入菜单序号：");
			System.out.print("> ");
			
			boolean waitAnyKey=true;
			String inTxt="";
			while(true) {
				int byt=System.in.read();
				inTxt+=(char)byt;
				inTxt=inTxt.trim();
				
				if(byt!='\n') {
					continue;
				}
				try {
					if(inTxt.equals("1") || inTxt.equals("2")) {
						AreaCityQuery.ResetInitStatus();
						System.gc();
						
						Init(inTxt.equals("1"));
						if(AreaCityQuery.GetInitStatus()==2) {
							waitAnyKey=false;
						}
					} else if(isInit && inTxt.equals("3")) {
						BaseTest();
					} else if(isInit && inTxt.equals("4")) {
						LargeRndPointTest();
					} else if(isInit && inTxt.equals("5")) {
						ThreadRun();
						waitAnyKey=false;
					} else if(isInit && inTxt.equals("6")) {
						Query_Point();
						waitAnyKey=false;
					} else if(isInit && inTxt.equals("7")) {
						Read_WKT();
						waitAnyKey=false;
					} else if(isInit && inTxt.equals("8")) {
						Query_DebugReadWKT();
						waitAnyKey=false;
					} else if(isInit && inTxt.equals("9")) {
						if(StartHttpApiServer()) {
							waitAnyKey=false;
						}
					} else if(inTxt.equals("exit")) {
						System.out.println("bye!");
						return;
					} else {
						inTxt="";
						System.out.println("序号无效，请重新输入菜单序号！");
						System.out.print("> ");
						continue;
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
				break;
			}
			
			if(waitAnyKey) {
				System.out.println("按任意键继续...");
				int n=System.in.read();
				if(n=='\r') {
					System.in.read();
				}
			}
		}
	}
}
