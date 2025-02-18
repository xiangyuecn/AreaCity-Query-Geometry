package com.github.xiangyuecn.areacity.query;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

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
		
		
/***【运行时直接启动http api服务】	这行开头加个/即可解开这个注释，代码即生效，下面填写实际的json文件路径
		// 比如在服务器里面用，使用这段代码不会显示命令行菜单，会直接启动http服务；linux服务器中可使用 `nohup bash xxx.sh` 命令执行后台运行
		Init_FromFile(0, true, "D:/xxx/ok_geo.json"); //支持使用.json、.geojson、.wkbs文件进行初始化
		//Init_FromFile(1, true, "D:/xxx/xx1.json"); //允许使用不同的数据文件初始化多个静态实例，api接口可以调用不同实例进行查询
		//Init_FromFile(2, true, "D:/xxx/xx2.json"); 
		StartHttpApiServer(HttpApiServerPort); //直接启动http api服务，可指定端口
		if(true)return;
// ***/
		
		
		
/***【基础调用示例代码】	这行开头加个/即可解开这个注释，代码即生效
		String jsonFile="仅供测试-全国省级GeoJSON数据-大幅简化粗略版.json";
		//先获取到查询实例，默认提供的0-9的10个静态实例，每个实例可以分别使用一个数据文件进行初始化和查询，当然自己调用new AreaCityQuery()创建一个新实例使用也是一样的
		AreaCityQuery Instance=AreaCityQuery.Instances[0];
		//static public final AreaCityQuery Instance=new AreaCityQuery(); //也可以自己创建静态实例

		//查询前先初始化，每个实例全局只会初始化一次，每次查询前都调用即可（查询会在初始化完成后进行），两种初始化方式根据自己业务情况二选一
		//首次初始化会从.json或.geojson文件中读取边界图形数据，速度比较慢，会自动生成.wkbs结尾的结构化文件，下次初始化就很快了
		//首次初始化生成了.wkbs文件后，后续初始化可以只使用此wkbs文件，允许不用再提供geojson文件（数据更新时需删除wkbs文件再重新用geojson文件进行初始化），具体请阅读对应初始化方法的注释文档
		Instance.Init_StoreInWkbsFile(jsonFile, jsonFile+".wkbs", true);
		//Instance.Init_StoreInMemory("geojson文件路径", "geojson文件路径.wkbs", true);

		//Instance.OnInitProgress=(initInfo)->{ ... } //初始化过程中的回调，可以绑定一个函数，接收初始化进度信息（编写时需在Init之前进行绑定）
		System.out.println(Instance.GetInitInfo().toString()); //打印初始化详细信息，包括性能信息

		//注意：以下查询中所有坐标参数的坐标系必须和初始化时使用的geojson数据的坐标系一致，否则坐标可能会有比较大的偏移，导致查询结果不正确
		//查询包含一个坐标点的所有边界图形的属性数据，可通过res参数让查询额外返回wkt格式边界数据
		//查询结果的判定：请不要假定查询结果的数量（坐标刚好在边界上可能会查询出多个省市区），也不要假定查询结果顺序（结果中省市区顺序是乱序的），请检查判定res1.Result中的结果是否符合查询的城市级别，比如查询省市区三级：结果中必须且仅有3条数据，并且省市区都有（判断deep=0省|1市|2区 来区分数据的级别），其他一律判定为查询无效
		QueryResult res1=Instance.QueryPoint(114.044346, 22.691963, null, null);
		//当坐标位于界线外侧（如海岸线、境界线）时QueryPoint方法将不会有边界图形能够匹配包含此坐标（就算距离只相差1cm），下面这个方法将能够匹配到附近不远的边界图形数据；2500相当于一个以此坐标为中心点、半径为2.5km的圆形范围，会查询出在这个范围内和此坐标点距离最近的边界
		QueryResult res1_2=Instance.QueryPointWithTolerance(121.993491, 29.524288, null, null, 2500);

		//查询和一个图形（点、线、面）有交点的所有边界图形的属性数据，可通过res参数让查询额外返回wkt格式边界数据
		Geometry geom=new WKTReader(AreaCityQuery.Factory).read("LINESTRING(114.30115 30.57962, 117.254285 31.824198, 118.785633 32.064869)");
		//Geometry geom=AreaCityQuery.CreateSimpleCircle(114.044346, 22.691963, 10000, 24); //以指定坐标为中心点，创建一个半径为10km的圆面，圆面由24个坐标点粗略构成
		QueryResult res2=Instance.QueryGeometry(geom, null, null);

		//读取省市区的边界数据wkt格式，这个例子会筛选出武汉市所有区县
		QueryResult res3=Instance.ReadWKT_FromWkbsFile("wkt_polygon", null, (prop)->{return prop.contains("武汉市 ");}, null);
		//此方法会遍历所有边界图形的属性列表，因此可以用来遍历所有数据，提取感兴趣的属性内容，比如查询一个区划编号id对应的城市信息（城市名称、中心点）
		QueryResult res4=Instance.ReadWKT_FromWkbsFile(null, null, (prop)->{
		    prop=(","+prop.substring(1, prop.length()-1)+",").replace("\"", "").replace(" ", ""); //不解析json，简单处理
		    return prop.contains(",id:42,"); //只查询出id=42（湖北省）的属性数据（注意初始化的geojson中必须要有对应的属性名，这里是id）
		}, null);


		System.out.println(res1+"\n"+res1_2+"\n"+res2+"\n"+res3+"\n"+res4);
		if(true)return;
// ***/
		
		
		Start(args);
	}
	
	
	static int CurrentIdx=0;
	static AreaCityQuery Current=AreaCityQuery.Instances[CurrentIdx];
	/** 初始化时，只读取区县级数据，否则省市区都读 **/
	static boolean Deep2Only=false;
	/** http api 服务端口号**/
	static int HttpApiServerPort=9527;
	
	/**使用数据文件直接对实例进行初始化*/
	static void Init_FromFile(int instance, boolean storeInWkbsFile, String dataFile) throws Exception {
		CurrentIdx=instance;
		Current=AreaCityQuery.Instances[instance];
		Init__(storeInWkbsFile, dataFile);
		Current.CheckInitIsOK();
	}

	static long BootMaxMemory;
	/**命令行菜单调用初始化*/
	static void Init_FromMenu(boolean storeInWkbsFile) throws Exception {
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
		String dataFile="";
		
		
		System.out.println("Init_StoreInWkbsFile：用加载数据到结构化数据文件的模式进行初始化，推荐使用本方法初始化，边界图形数据存入结构化数据文件中，内存占用很低，查询时会反复读取文件对应内容，查询性能消耗主要在IO上，IO性能极高问题不大。");
		System.out.println("Init_StoreInMemory：用加载数据到内存的模式进行初始化，边界图形数据存入内存中，内存占用和json数据文件大小差不多大，查询性能极高；另外可通过设置 Instances[0-9].SetInitStoreInMemoryUseObject=true 来进一步提升性能，但内存占用会增大一倍多。");
		System.out.println(HR);
		System.out.println("首次初始化会从.json或.geojson文件中读取边界图形数据，并生成.wkbs结尾的结构化文件，速度比较慢（文件读写）；下次会直接从.wkbs文件进行初始化（文件只读不写），就很快了，可copy此.wkbs文件到别的地方使用（比如服务器、只读环境中）。");
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
						dataFile=jsonFiles.get(idx-1);
					} else if(idx>0 && idx-jsonFiles.size()<=wkbsFiles.size()) {
						dataFile=wkbsFiles.get(idx-jsonFiles.size()-1);
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
					dataFile=txt;
				} else if (str.endsWith("wkbs")) {
					dataFile=txt;
				} else {
					System.out.println("输入的文件类型无效，请重新输入！");
					continue;
				}
				File f=new File(txt);
				if(!f.exists()) {
					dataFile="";
					System.out.println("文件不存在，请重新输入！(未找到文件："+f.getAbsolutePath()+")");
					continue;
				}
				break;
			}
		}
		
		Init__(storeInWkbsFile, dataFile);
	}
	
	/**省**/static boolean[] HasDeep0=new boolean[AreaCityQuery.Instances.length];
	/**市**/static boolean[] HasDeep1=new boolean[AreaCityQuery.Instances.length];
	/**区县**/static boolean[] HasDeep2=new boolean[AreaCityQuery.Instances.length];
	/**乡镇街道**/static boolean[] HasDeep3=new boolean[AreaCityQuery.Instances.length];
	static void Init__(boolean storeInWkbsFile, String dataFile) throws Exception {
		String initDataFile,initSaveWkbsFile;
		String str=dataFile.toLowerCase();
		if(str.endsWith("json") || str.endsWith("geojson")) {
			initDataFile=new File(dataFile).getAbsolutePath();
			initSaveWkbsFile=initDataFile+".wkbs";
			if(!storeInWkbsFile) {
				System.out.println("用json文件进行Init_StoreInMemory初始化时，可选提供一个.wkbs后缀的文件路径，初始化时会自动生成此文件，如果不提供将不能查询WKT数据；直接回车提供，输入n不提供：");
				System.out.print("> ");
				String txt=ReadIn();
				if(txt.toLowerCase().equals("n")) {
					initSaveWkbsFile="";
				}
			}
		} else if(str.endsWith("wkbs")) {
			initDataFile=new File(dataFile).getAbsolutePath();
			initSaveWkbsFile="";
		} else {
			System.out.println("未选择文件，不初始化，已退出！");
			return;
		}
		
		HasDeep0[CurrentIdx]=HasDeep1[CurrentIdx]=HasDeep2[CurrentIdx]=HasDeep3[CurrentIdx]=false;

		//初始化回调
		Current.OnInitProgress=new Func<QueryInitInfo, Boolean>() {
			long logTime=0;int maxNo=0,lastNo=0;
			@Override
			public Boolean Exec(QueryInitInfo info) throws Exception {
				if(logTime==0) {
					if(info.DataFromWkbsFile) {
						System.out.println("正在从wkbs结构化数据文件中快速读取数据...");
					} else if(info.HasWkbsFile){
						System.out.println("首次运行，正在生成wkbs结构化数据文件，速度可能会比较慢...");
					} else {
						System.out.println("正在从json文件中读取数据，未提供wkbs文件，速度可能会比较慢...");
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
					if(!Deep2Only && !HasDeep0[CurrentIdx])HasDeep0[CurrentIdx]=prop.contains("deep:0");
					if(!Deep2Only && !HasDeep1[CurrentIdx])HasDeep1[CurrentIdx]=prop.contains("deep:1");
					if(!HasDeep2[CurrentIdx])HasDeep2[CurrentIdx]=prop.contains("deep:2");
					if(!Deep2Only && !HasDeep3[CurrentIdx])HasDeep3[CurrentIdx]=prop.contains("unique_id:");
					
					if(Deep2Only) {
						return prop.contains("deep:2"); //只提取区级，其他一律返回false跳过解析
					}
				}
				return true;
			}
		};
		//初始化，如果未生成结构化数据文件（wkbs）这里会从json数据文件自动生成，如果生成了就只会读取wkbs文件
		if(storeInWkbsFile) {
			Current.Init_StoreInWkbsFile(initDataFile, initSaveWkbsFile, true);
		} else {
			Current.Init_StoreInMemory(initDataFile, initSaveWkbsFile, true);
		}

		System.out.println("========== "+(storeInWkbsFile?"Init_StoreInWkbsFile":"Init_StoreInMemory")+" ==========");
		System.out.println(Current.GetInitInfo().toString());
		System.out.println();
		System.out.println("已加载数据级别："+(HasDeep0[CurrentIdx]?"√":"×")+"省，"+(HasDeep1[CurrentIdx]?"√":"×")+"市，"+(HasDeep2[CurrentIdx]?"√":"×")+"区县，"+(HasDeep3[CurrentIdx]?"√":"×")+"乡镇 （×为未加载，可能是数据文件中并不含此级数据）");
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
	static void BaseTest(int instanceIdx) throws Exception {
		AreaCityQuery instance=AreaCityQuery.Instances[instanceIdx];
		int loop=100;
		System.out.println("========== QueryPoint ==========");
		{
			QueryResult res=new QueryResult();
			res.Set_EnvelopeHitResult=new ArrayList<>();
			//res.Set_ReturnWKTKey="polygon_wkt";
			for(int i=0;i<loop;i++) {
				res.Result.clear();//清除一下上次的结果，只保留统计
				res.Set_EnvelopeHitResult.clear();
				res=instance.QueryPoint(114.044346, 22.691963, null, res);
			}
			System.out.println(res.toString());
			if(HasDeep2[instanceIdx]) {
				System.out.println(ResultHas(res, "龙华区\"")?"OK":"查询失败！");
			}
		}
		
		System.out.println();
		System.out.println("========== QueryPointWithTolerance ==========");
		{
			QueryResult res=new QueryResult();
			res.Set_EnvelopeHitResult=new ArrayList<>();
			double lng=121.993491,lat=29.524288;
			QueryResult res2=instance.QueryPoint(lng, lat, null, null);
			for(int i=0;i<loop;i++) {
				res.Result.clear();//清除一下上次的结果，只保留统计
				res.Set_EnvelopeHitResult.clear();
				res=instance.QueryPointWithTolerance(lng, lat, null, res, 2500);
			}
			System.out.println(res.toString());
			if(HasDeep2[instanceIdx]) {
				System.out.println(ResultHas(res, "象山县\"") && res2.Result.size()==0?"OK":"查询失败！");
			}
		}

		System.out.println();
		System.out.println("========== QueryGeometry ==========");
		if(!HasDeep0[instanceIdx]){
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
				res=instance.QueryGeometry(geom, new Func<String, Boolean>() {
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
				String wktKey="plygon_wkt";
				if(!instance.GetInitInfo().HasWkbsFile) {
					if(i==0)System.out.println("【注意】初始化时如果没有提供wkbs文件，不能查询wkt数据");
					wktKey="";
				}
				res=instance.ReadWKT_FromWkbsFile(wktKey, res, new Func<String, Boolean>() {
					@Override
					public Boolean Exec(String prop) throws Exception {
						return prop.contains("北京市 朝阳区\"")
							|| prop.contains("武汉市 洪山区\"")
							|| prop.contains("台北市 中山区\"");
					}
				}, null);
			}
			System.out.println(res.toString());
			if(HasDeep2[instanceIdx]) {
				System.out.println(ResultHas(res, "北京市 朝阳区\"")
						&& ResultHas(res, "武汉市 洪山区\"")
						&& ResultHas(res, "台北市 中山区\"") ?"OK":"查询失败！");
			}
		}
		System.out.println();
	}
	
	static void LargeRndPointTest(int instanceIdx) throws Exception {
		AreaCityQuery instance=AreaCityQuery.Instances[instanceIdx];
		System.out.println("========== QueryPoint：1万个伪随机点测试 ==========");
		System.out.println("伪随机：虽然是随机生成的点，但每次运行生成坐标列表都是相同的。");
		for(int loop=0;loop<2;loop++) {
			System.out.println((loop==0?"QueryPoint":"QueryPointWithTolerance")+"测试中，请耐心等待...");
			
			QueryResult res=new QueryResult();
			double x_0=98.0,y_00=18.0;//矩形范围囊括大半个中国版图
			double x_1=135.0,y_1=42.0;
			int size=100;//1万点
			double xStep=(x_1-x_0)/size;
			double yStep=(y_1-y_00)/size;
			while(x_0-x_1<-xStep/2) {//注意浮点数±0.000000001的差异
				double x0=x_0, x1=x_0+xStep; x_0=x1;
				double y_0=y_00;
				while(y_0-y_1<-yStep/2) {
					double y0=y_0, y1=y_0+yStep; y_0=y1;
					
					if(loop==0) {
						res=instance.QueryPoint(x0, y0, null, res);
					}else {
						res=instance.QueryPointWithTolerance(x0, y0, null, res, 2500);
					}
					res.Result.clear();//只保留统计
				}
			}
			res.Result=null;
			System.out.println(res.toString());
			System.out.println();
		}
	}
	static void ThreadRun(ArrayList<Integer> idxs) throws Exception {
		System.out.println("========== 多线程性能测试 ==========");
		boolean[] stop=new boolean[] {false};
		int ThreadCount=Math.max(1, Runtime.getRuntime().availableProcessors()-1);
		QueryResult[] SecondCompletes=new QueryResult[ThreadCount];

		System.out.println("通过开启 CPU核心数-1 个线程，每个线程内随机查询不同的坐标点，来达到性能测试的目的。");
		System.out.println("正在测试中，线程数："+ThreadCount+"，按回车键结束测试...");
		
		//测试函数
		Func<Object[], Object> run=new Func<Object[], Object>() {	
			@Override
			public Object Exec(Object[] args) throws Exception {
				QueryResult res=(QueryResult)args[0];
				int instanceIdx=(int)args[1];
				AreaCityQuery instance=AreaCityQuery.Instances[instanceIdx];
				
				//固定坐标点测试
				boolean allOk=true;
				instance.QueryPoint(114.044346, 22.691963, null, res); //广东省 深圳市 龙华区
				allOk&=ResultHas(res, "深圳市 龙华区\"");res.Result.clear();
				
				instance.QueryPoint(117.286491, 30.450399, null, res); //安徽省 铜陵市 郊区 飞地
				allOk&=ResultHas(res, "铜陵市 郊区\"");res.Result.clear();
				
				instance.QueryPoint(116.055588, 39.709385, null, res); //北京市 房山区 星城街道 飞地
				allOk&=ResultHas(res, "北京市 房山区\"");res.Result.clear();
				
				instance.QueryPoint(130.283168, 47.281807, null, res); // 黑龙江省 鹤岗市 南山区
				allOk&=ResultHas(res, "鹤岗市 南山区\"");res.Result.clear();
				
				instance.QueryPoint(118.161624, 39.656532, null, res); // 河北省  唐山市 路北区
				allOk&=ResultHas(res, "唐山市 路北区\"");res.Result.clear();
				
				instance.QueryPoint(81.869760, 41.812321, null, res); // 新疆 阿克苏地区 拜城县
				allOk&=ResultHas(res, "阿克苏地区 拜城县\"");res.Result.clear();
				
				if(HasDeep2[instanceIdx] && !allOk) {
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
						
						instance.QueryPoint(x0, y0, null, res);
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
								+(idxs.size()>1?"多实例":Current.IsStoreInMemory()?"InMemory":"InWkbsFile")
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
						Object[] args=new Object[] { res, CurrentIdx };
						Random rnd=new Random();
						long t1=System.nanoTime();
						while(!stop[0]) {
							try {
								run.Exec(args);
								
								long t0=System.nanoTime();
								if(t0-t1>=1000*1000000) {//1秒钟，给赋值一次统计数据
									SecondCompletes[threadId]=res;
									showProgress.Exec(null);
									args[0]=res=new QueryResult();
									args[1]=idxs.get(rnd.nextInt(idxs.size()));
									t1=System.nanoTime();
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
		String idxs=GetInitInstanceIdxs();
		System.out.println("========== 查询一个坐标点对应的省市区乡镇数据 ==========");
		System.out.println("注意：输入坐标参数的坐标系必须和初始化时使用的geojson数据的坐标系一致，否则坐标可能会有比较大的偏移，导致查询结果不正确。");
		System.out.println("请输入一个坐标点，格式：\"lng lat\"（允许有逗号）：");
		System.out.println("  - 比如：114.044346 22.691963，为广东省 深圳市 龙华区");
		System.out.println("  - 比如：117.286491 30.450399，为安徽省 铜陵市 郊区，在池州市 贵池区的飞地");
		System.out.println("  - 比如：121.993491 29.524288，为浙江省 宁波市 象山县，但坐标点位于海岸线外侧，不在任何边界内，需设置tolerance才能查出");
		System.out.println("  - 输入 tolerance=2500 设置距离范围容差值，单位米，比如2500相当于一个以此坐标为中心点、半径为2.5km的圆形范围；默认0不设置，-1不限制距离；当坐标位于界线外侧（如海岸线、境界线）时QueryPoint方法将不会有边界图形能够匹配包含此坐标（就算距离只相差1cm），设置tolerance后，会查询出在这个范围内和此坐标点距离最近的边界数据");
		System.out.println("  - 输入 exit 退出查询，输入 use "+idxs+" 切换查询实例");
		int tolerance=0;
		while(true){
			System.out.print((idxs.length()<2?"":"[实例"+CurrentIdx+"] ")+"> ");
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
			if(inStr.startsWith("use ")) {
				if(InTxt_SetCurrent(inStr, true)) {
					System.out.println("已设置当前实例为"+CurrentIdx);
				}
				continue;
			}
			if(inStr.startsWith("tolerance")) {
				Matcher m=Pattern.compile("^tolerance[=\\s]+([+-]*\\d+)$").matcher(inStr);
				if(!m.find()) {
					System.out.println("tolerance设置格式错误，请重新输入");
				}else {
					tolerance=Integer.parseInt(m.group(1));
					System.out.println("已设置tolerance="+tolerance);
				}
				continue;
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
			QueryResult res;
			if(tolerance==0) {
				res=Current.QueryPoint(lng, lat, null, null);
			}else {
				System.out.println("QueryPointWithTolerance tolerance="+tolerance);
				res=Current.QueryPointWithTolerance(lng, lat, null, null, tolerance);
			}
			System.out.println(res.toString());
		}
	}
	
	static void Query_Geometry() throws Exception {
		String idxs=GetInitInstanceIdxs();
		System.out.println("========== 查询和任意一个几何图形相交的省市区乡镇数据 ==========");
		System.out.println("注意：输入WKT的坐标系必须和初始化时使用的geojson数据的坐标系一致，否则坐标可能会有比较大的偏移，导致查询结果不正确。");
		System.out.println("请输入一个WKT文本（Well Known Text）：");
		System.out.println("  - 比如：POINT(114.044346 22.691963)，坐标点，为广东省 深圳市 龙华区");
		System.out.println("  - 比如：LINESTRING(114.30115 30.57962, 117.254285 31.824198, 118.785633 32.064869)，路径线段，武汉-合肥-南京 三个点连成的线段");
		System.out.println("  - 比如：POLYGON((113.305514 30.564249, 113.305514 32.881526, 117.326510 32.881526, 117.326510 30.564249, 113.305514 30.564249))，范围，湖北-河南-安徽 三省交界的一个超大矩形范围");
		System.out.println("  - 输入 exit 退出查询，输入 use "+idxs+" 切换查询实例");
		while(true){
			System.out.print((idxs.length()<2?"":"[实例"+CurrentIdx+"] ")+"> ");
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
			if(inStr.startsWith("use ")) {
				if(InTxt_SetCurrent(inStr, true)) {
					System.out.println("已设置当前实例为"+CurrentIdx);
				}
				continue;
			}
			Geometry geom;
			try {
				geom=new WKTReader(AreaCityQuery.Factory).read(inStr);
			}catch(Exception e) {
				System.out.println("输入的WKT解析失败："+e.getMessage());
				continue;
			}
			QueryResult res=Current.QueryGeometry(geom, null, null);
			System.out.println(res.toString());
		}
	}
	
	static void Read_WKT() throws Exception {
		System.out.println("========== 读取省市区乡镇边界的WKT文本数据 ==========");
		System.out.println("遍历所有边界图形的属性列表查询出符合条件的属性，然后返回图形的属性+边界图形WKT文本。 ");
		System.out.println("读取到的wkt文本，可以直接粘贴到页面内渲染显示：https://xiangyuecn.github.io/AreaCity-JsSpider-StatsGov/assets/geo-echarts.html");
		System.out.println();
		
		ExtPathExpIn("ReadWKT", new Func<Test.ExtPathExpInArgs, Object>() {
			@Override
			public Object Exec(ExtPathExpInArgs args) throws Exception {
				int[] count=new int[] { 0 };
				Current.ReadWKT_FromWkbsFile("", null, new Func<String, Boolean>() {
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
		System.out.println("读取到的wkt文本，可以直接粘贴到页面内渲染显示：https://xiangyuecn.github.io/AreaCity-JsSpider-StatsGov/assets/geo-echarts.html");
		System.out.println();
		
		ExtPathExpIn("GirdWKT", new Func<Test.ExtPathExpInArgs, Object>() {
			@Override
			public Object Exec(ExtPathExpInArgs args) throws Exception {
				int[] count=new int[] { 0 };
				Current.Debug_ReadGeometryGridSplitsWKT("", null, new Func<String, Boolean>() {
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
		String idxs=GetInitInstanceIdxs();
		System.out.println("请输入"+tag+"要查询的城市完整名称，为ext_path值：");
		System.out.println("  - 如：“湖北省 武汉市 洪山区”，精确查找");
		System.out.println("  - 如：“*武汉市*”，*通配符模糊查找");
		System.out.println("  - 如：“*”，查找全部");
		System.out.println("  - 结尾输入“ > 文件名”可保存到文件");
		System.out.println("  - 输入 exit 退出，输入 use "+idxs+" 切换查询实例");
		while(true){
			System.out.print((idxs.length()<2?"":"[实例"+CurrentIdx+"] ")+"> ");
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
			if(inStr.startsWith("use ")) {
				if(InTxt_SetCurrent(inStr, true)) {
					System.out.println("已设置当前实例为"+CurrentIdx);
				}
				continue;
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
	
	
	
	
	
	
	
	static public boolean StartHttpApiServer(int port) throws Exception {
		String clazzName=Test.class.getPackage().getName()+".Test_HttpApiServer";
		Method[] fns;
		try {
			fns=Class.forName(clazzName).getMethods();
		}catch (Exception e) {
			System.out.println("Test_HttpApiServer.java加载失败，不支持启动本地轻量HTTP API服务。");
			return false;
		}
		Method fn=null; for(Method x : fns) if(x.getName().equals("Create")) fn=x;
		return (boolean)fn.invoke(null, "0.0.0.0", port);
	}
	
	
	
	
	
	
	
	static public String ReadIn() throws Exception {
		ByteArrayOutputStream in=new ByteArrayOutputStream();
		while(true) {
			int byt=System.in.read();
			if(byt=='\r') continue;
			if(byt=='\n') {
				break;
			}
			if(in.size()>=2048) {//防止内存溢出，某些环境下可能会有无限的输入
				byte[] bytes=in.toByteArray();
				in=new ByteArrayOutputStream();
				in.write(bytes, bytes.length-1024, 1024);
			}
			in.write(byt);
		}
		return in.toString();
	}
	static public boolean InTxt_SetCurrent(String inTxt, boolean checkInit) {
		int idx=-1; try{ idx=Integer.parseInt(inTxt.split("\\s+")[1]); }catch(Exception e) {}
		if(idx<0 || idx>=AreaCityQuery.Instances.length) {
			System.out.println("use实例序号无效，请重新输入！");
			return false;
		}
		AreaCityQuery val=AreaCityQuery.Instances[idx];
		if(checkInit && val.GetInitStatus()!=2) {
			System.out.println("实例"+idx+"未初始化，可用实例["+GetInitInstanceIdxs()+"]，请重新输入！");
			return false;
		}
		CurrentIdx=idx;
		Current=val;
		return true;
	}
	static public String GetInitInstanceIdxs() {
		String initIdxs="";
		for(int i=0;i<AreaCityQuery.Instances.length;i++) {
			AreaCityQuery item=AreaCityQuery.Instances[i];
			if(item.GetInitStatus()==2) {
				initIdxs+=(initIdxs.length()>0?",":"")+i;
			}
		}
		return initIdxs;
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
			boolean isInit=Current.GetInitStatus()==2;
			ArrayList<Integer> idxs=new ArrayList<>();
			int curIdx=0; String initIdxs="";
			for(int i=0;i<AreaCityQuery.Instances.length;i++) {
				AreaCityQuery item=AreaCityQuery.Instances[i];
				if(item.GetInitStatus()==2) {
					idxs.add(i);
					initIdxs+=(initIdxs.length()>0?",":"")+i;
				}
				if(item==Current)curIdx=i;
			}
			
			
			System.out.println("【功能菜单】 当前静态实例Instances["+curIdx+"]");
			System.out.println("1. "+(isInit?"重新":"")+"初始化：调用 Init_StoreInWkbsFile   -内存占用很低（性能受IO限制）"+(Current.IsStoreInWkbsFile()?"               [已初始化]":""));
			System.out.println("2. "+(isInit?"重新":"")+"初始化：调用 Init_StoreInMemory     -内存占用和json文件差不多大（性能豪放）"+(Current.IsStoreInMemory()?"     [已初始化]":""));
			if(isInit) {
				System.out.println(HR);
				System.out.println("3. 测试：基础功能测试");
				System.out.println("4. 测试：1万个伪随机点测试");
				System.out.println("5. 测试：多线程性能测试");
				System.out.println(HR);
				System.out.println("6. 查询: QueryPoint 查找坐标点所在省市区乡镇");
				System.out.println("A. 查询: QueryGeometry 查找和图形相交的省市区乡镇");
				System.out.println("7. 查询: ReadWKT 读取省市区乡镇边界的WKT文本数据");
				System.out.println("8. 查询: Debug 读取边界网格划分图形WKT文本数据");
				System.out.println(HR);
				System.out.println("9. HTTP: 启动本地轻量HTTP API服务");
			}
			System.out.println(HR);
			System.out.println("*. 输入 use 0-"+(AreaCityQuery.Instances.length-1)+" 切换静态实例，list 列出实例信息，当前"+(initIdxs.length()==0?"无已初始化实例":"["+initIdxs+"]已初始化")+"");
			System.out.println("*. 输入 exit 退出");
			System.out.println();
			System.out.println("请输入菜单序号：");
			System.out.print("> ");
			
			boolean waitAnyKey=true;
			String inTxt="";
			while(true) {
				int byt=System.in.read();
				inTxt+=(char)byt;
				
				if(byt!='\n') {
					continue;
				}
				inTxt=inTxt.trim().toUpperCase();
				try {
					if(inTxt.equals("1") || inTxt.equals("2")) {
						Current.ResetInitStatus();
						System.gc();
						
						Init_FromMenu(inTxt.equals("1"));
						if(Current.GetInitStatus()==2) {
							waitAnyKey=false;
						}
					} else if(isInit && inTxt.equals("3")) {
						for(int i : idxs) {
							if(idxs.size()>1) System.out.println("【测试实例"+i+"】 Instances["+i+"]");
							BaseTest(i);
						}
					} else if(isInit && inTxt.equals("4")) {
						for(int i : idxs) {
							if(idxs.size()>1) System.out.println("【测试实例"+i+"】 Instances["+i+"]");
							LargeRndPointTest(i);
						}
					} else if(isInit && inTxt.equals("5")) {
						ThreadRun(idxs);
						waitAnyKey=false;
					} else if(isInit && inTxt.equals("6")) {
						Query_Point();
						waitAnyKey=false;
					} else if(isInit && inTxt.equals("A")) {
						Query_Geometry();
						waitAnyKey=false;
					} else if(isInit && inTxt.equals("7")) {
						Read_WKT();
						waitAnyKey=false;
					} else if(isInit && inTxt.equals("8")) {
						Query_DebugReadWKT();
						waitAnyKey=false;
					} else if(isInit && inTxt.equals("9")) {
						if(StartHttpApiServer(HttpApiServerPort)) {
							waitAnyKey=false;
						}
					} else if(inTxt.startsWith("USE ")) {
						if(InTxt_SetCurrent(inTxt, false)) {
							waitAnyKey=false;
						}else {
							inTxt="";
							System.out.print("> ");
							continue;
						}
					} else if(inTxt.equals("LIST")) {
						for(int i : idxs) {
							AreaCityQuery item=AreaCityQuery.Instances[i];
							QueryInitInfo info=item.GetInitInfo();
							System.out.println((item==Current?"[当前]":"")
									+"实例"+i+": Instances["+i+"] "+(item.IsStoreInMemory()?"Init_StoreInMemory":"Init_StoreInWkbsFile"));
							System.out.println("    Geometry "+info.GeometryCount+" 个（Grid切分Polygon "+info.PolygonCount+" 个）");
							System.out.println("    Data文件: "+info.FilePath_Data);
							System.out.println("    Wkbs文件: "+info.FilePath_SaveWkbs);
						}
						if(idxs.size()==0) {
							System.out.println("没有已初始化的实例信息！");
						}
					} else if(inTxt.equals("EXIT")) {
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
