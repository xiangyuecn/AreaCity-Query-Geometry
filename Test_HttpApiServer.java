package com.github.xiangyuecn.areacity.query;

//要是编译不过，就直接删掉这个文件就好了
//要是编译不过，就直接删掉这个文件就好了
//要是编译不过，就直接删掉这个文件就好了

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

import com.github.xiangyuecn.areacity.query.AreaCityQuery.Func;
import com.github.xiangyuecn.areacity.query.AreaCityQuery.QueryResult;
//jre rt.jar com.sun，Eclips不允许引用：Access restriction: The type 'HttpServer' is not API
//Eclips修改项目配置 Java Compiler -> Errors/Warnings -> Deprecated and restricted API，将Error的改成Warning即可
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * AreaCityQuery测试本地轻量HTTP API服务
 * 
 * GitHub: https://github.com/xiangyuecn/AreaCity-Query-Geometry （github可换成gitee）
 * 省市区县乡镇区划边界数据: https://github.com/xiangyuecn/AreaCity-JsSpider-StatsGov （github可换成gitee）
 */
public class Test_HttpApiServer {
	/** 是否允许输出大量WKT数据，默认不允许，只能输出最大20M的数据；如果要设为true，请确保没有 -Xmx300m 限制Java使用小内存 **/
	static public boolean AllowResponseBigWKT=false;
	
	static private String Desc;
	static public boolean Create(String bindIP, int bindPort) {
		Desc="========== 本地轻量HTTP API服务 ==========";
		Desc+="\n可通过 http://127.0.0.1:"+bindPort+" 访问本服务，提供的接口：";
		Desc+="\n\n  - GET /queryPoint?lng=&lat=&returnWKTKey=      查询出包含此坐标点的所有边界图形的属性数据；lng必填经度，lat必填纬度，returnWKTKey可选要额外返回边界的wkt文本数据放到此key下。";
		Desc+="\n\n  - GET /queryGeometry?wkt=&returnWKTKey=        查询出和此图形（点、线、面）有交点的所有边界图形的属性数据（包括边界相交）；wkt必填任意图形，returnWKTKey可选要额外返回边界的wkt文本数据放到此key下。";
		Desc+="\n\n  - GET /readWKT?id=&pid=&deep=&extPath=&returnWKTKey= 读取边界图形的WKT文本数据；前四个参数可以组合查询或查一个参数（边界的属性中必须要有这些字段才能查询出来），id：查询指定id|unique_id的边界；pid：查询此pid下的所有边界；deep：限制只返回特定层级的数据，取值：0省1市2区想3乡镇；extPath：查询和ext_path完全相同值的边界，首尾支持*通配符（如：*武汉*）；returnWKTKey回边界的wkt文本数据放到此key下，默认值polygon_wkt，填0不返回wkt文本数据；注意：默认只允许输出最大20M的WKT数据，请参考下面的注意事项。";
		Desc+="\n\n  - GET /debugReadGeometryGridSplitsWKT?id=&pid=&deep=&extPath=&returnWKTKey= Debug读取边界网格划分图形WKT文本数据；参数和/readWKT接口一致。";
		Desc+="\n\n  - JSON响应：{c:0, v:{...}, m:\"错误消息\"} c=0代表接口调用成功，v为内容；c=其他值调用错误，m为错误消息。";
		Desc+="\n\n  - 注意：所有输入坐标参数的坐标系必须和初始化时使用的geojson数据的坐标系一致，否则坐标可能会有比较大的偏移，导致查询结果不正确。";
		Desc+="\n\n  - 注意：如果要输出大量WKT数据，请调大Java内存，不然可能是 -Xmx300m 启动的只允许使用小内存，并且修改服务源码内的AllowResponseBigWKT=true，否则只允许输出最大20M的WKT数据。";
		
		System.out.println(Desc);
		System.out.println();
		System.out.println("绑定IP: "+bindIP+", Port: "+bindPort+", 正在启动HTTP API服务...");
		
		boolean startOK=false;
		try {
			__Start(bindIP, bindPort);
			
			startOK=true;
			System.out.println("HTTP API服务正在运行，输入 exit 退出服务...");
			while(true){
				String inStr=Test.ReadIn().trim();
				if(inStr.equals("exit")) {
					System.out.println("bye! 已退出HTTP API服务。");
					System.out.println();
					
					httpServer.stop(0);
					httpServer=null;
					break;
				}
				System.out.println("如需退出HTTP API服务请输入exit");
			}
		}catch (Exception e) {
			e.printStackTrace();
			if(!startOK) {
				System.out.println("创建HTTP服务异常："+e.getMessage());
				System.out.println();
				return false;
			}
		}
		return true;
	}
	
	
	
	static private void Req_queryPoint(HashMap<String, String> query, String[] response, String[] responseErr, int[] status, String[] contentType, HashMap<String, String> respHeader) throws Exception {
		double lng=ToNum(query.get("lng"), 999);
		double lat=ToNum(query.get("lat"), 999);
		String returnWKTKey=query.get("returnWKTKey");
		if(lng<-180 || lat<-90 || lng>180 || lat>90) {
			responseErr[0]="坐标参数值无效";
			return;
		}
		
		QueryResult res=new QueryResult();
		if(returnWKTKey!=null && returnWKTKey.length()>0) {
			res.Set_ReturnWKTKey=returnWKTKey;
		}
		AreaCityQuery.QueryPoint(lng, lat, null, res);
		
		response[0]=ResToJSON(res);
	}
	
	static private void Req_queryGeometry(HashMap<String, String> query, String[] response, String[] responseErr, int[] status, String[] contentType, HashMap<String, String> respHeader) throws Exception {
		String wkt=query.get("wkt");
		String returnWKTKey=query.get("returnWKTKey");
		if(wkt==null || wkt.length()==0) {
			responseErr[0]="wkt参数无效";
			return;
		}
		Geometry geom;
		try {
			geom=new WKTReader(AreaCityQuery.Factory).read(wkt);
		}catch (Exception e) {
			responseErr[0]="wkt参数解析失败："+e.getMessage();
			return;
		}
		
		QueryResult res=new QueryResult();
		if(returnWKTKey!=null && returnWKTKey.length()>0) {
			res.Set_ReturnWKTKey=returnWKTKey;
		}
		AreaCityQuery.QueryGeometry(geom, null, res);
		
		response[0]=ResToJSON(res);
	}
	
	static private void Req_readWKT(boolean debugReadGrid, HashMap<String, String> query, String[] response, String[] responseErr, int[] status, String[] contentType, HashMap<String, String> respHeader) throws Exception {
		long id=ToLong(query.get("id"), -1);
		long pid=ToLong(query.get("pid"), -1);
		long deep=ToLong(query.get("deep"), -1);
		String extPath=query.get("extPath"); if(extPath==null) extPath="";
		String returnWKTKey=query.get("returnWKTKey");
		if(id==-1 && pid==-1 && deep==-1 && extPath.length()==0) {
			responseErr[0]="请求参数无效";
			return;
		}
		if(returnWKTKey==null || returnWKTKey.length()==0) {
			returnWKTKey="polygon_wkt";
		}
		if("0".equals(returnWKTKey)) {
			returnWKTKey=null;
		}
		
		String exp=extPath;
		if(extPath!=null && extPath.length()>0) {
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
		}
		String exp_=exp;
		String extPath_=extPath;
		
		int[] readCount=new int[] {0};
		boolean[] isWktSizeErr=new boolean[] {false};
		int[] wktSize=new int[] {0};
		Func<String, Boolean> where=new Func<String, Boolean>() {
			@Override
			public Boolean Exec(String prop) throws Exception {
				String prop2=(","+prop.substring(1, prop.length()-1)+",").replace("\"", "").replace(" ", ""); //不解析json，简单处理
				if(id!=-1) {
					if(!prop2.contains(",id:"+id+",") && !prop2.contains(",unique_id:"+id+",")) {
						return false;
					}
				}
				if(pid!=-1) {
					if(!prop2.contains(",pid:"+pid+",")) {
						return false;
					}
				}
				if(deep!=-1) {
					if(!prop2.contains(",deep:"+deep+",")) {
						return false;
					}
				}
				if(extPath_.length()>0) {
					int i0=prop.indexOf("ext_path");
					if(i0==-1)return false;
					int i1=prop.indexOf(",", i0);
					if(i1==-1)i1=prop.length();
					if(!prop.substring(i0+9, i1).contains(exp_)) {
						return false;
					}
				}
				
				readCount[0]++;
				if(isWktSizeErr[0]) {
					return false;
				}
				return true;
			}
		};
		Func<String[], Boolean> onFind=new Func<String[], Boolean>() {
			@Override
			public Boolean Exec(String[] val) throws Exception {
				wktSize[0]+=val[1].length();
				if(!AllowResponseBigWKT && wktSize[0]>20*1024*1024) {
					isWktSizeErr[0]=true;
					return false;
				}
				return true;
			}
		};
		QueryResult res;
		if(debugReadGrid) {
			res=AreaCityQuery.Debug_ReadGeometryGridSplitsWKT(returnWKTKey, null, where, onFind);
		} else {
			res=AreaCityQuery.ReadWKT_FromWkbsFile(returnWKTKey, null, where, onFind);
		}
		
		if(isWktSizeErr[0]) {
			responseErr[0]="已匹配到"+readCount[0]+"条数据，但WKT数据量超过20M限制，可修改服务源码内的AllowResponseBigWKT=true来解除限制";
			return;
		}
		response[0]=ResToJSON(res);
	}
	
	
	
	
	static private String ResToJSON(QueryResult res) {
		StringBuilder json=new StringBuilder();
		json.append("{\"list\":[");//手撸json
		for(int i=0,L=res.Result.size();i<L;i++) {
			if(i>0) json.append(","); 
			json.append(res.Result.get(i));
			res.Result.set(i, null);//已读取了结果就释放掉内存
		}
		json.append("]}");
		return json.toString();
	}
	static private String StringInnerJson(String str) {
		if (str==null || str.length()==0) {
			return "";
		}
		int len = str.length();
		StringBuilder sb = new StringBuilder(len * 2);
		char chr;
		for (int i = 0; i < len; i++) {
			chr = str.charAt(i);
			switch (chr) {
				case '"':
					sb.append('\\').append('"'); break;
				case '\\':
					sb.append('\\').append('\\'); break;
				case '\n':
					sb.append('\\').append('n'); break;
				case '\r':
					sb.append('\\').append('r'); break;
				default:
					sb.append(chr);
					break;
			}
		}
		return sb.toString();
	}
	static private long ToLong(String val, long def) {
		if(val==null || val.length()==0) {
			return def;
		}
		try {
			return Long.parseLong(val);
		}catch (Exception e) {
			return def;
		}
	}
	static private double ToNum(String val, double def) {
		if(val==null || val.length()==0) {
			return def;
		}
		try {
			return Double.parseDouble(val);
		}catch (Exception e) {
			return def;
		}
	}
	
	
	
	
	
	static private HttpServer httpServer;
	static private void __Start(String bindIP, int bindPort) throws Exception {
		if(httpServer!=null) {
			try {
				httpServer.stop(0);
			}catch(Exception e) {}
		}
		Func<HttpExchange, Object> fn=new Func<HttpExchange, Object>() {
			@Override
			public Object Exec(HttpExchange context) throws Exception {
				URI url=context.getRequestURI();
				String path=url.getPath(); if(path==null||path.length()==0)path="/";
				String queryStr=url.getQuery(); if(queryStr==null) queryStr="";
				
				HashMap<String, String> query=new HashMap<>();
				String[] queryArr=queryStr.split("&");
				for(String s : queryArr) {
					if(s.length()>0) {
						String[] kv=s.split("=");
						if(kv.length==2) {
							query.put(kv[0], URLDecoder.decode(kv[1], "utf-8"));
						}
					}
				}
				
				int[] status=new int[] { 200 };
				String[] contentType=new String[] {"text/json; charset=utf-8"};
				HashMap<String, String> respHeader=new HashMap<>();
				respHeader.put("Access-Control-Allow-Origin", "*");
				
				
				boolean isApi=true;
				String[] response=new String[] { "" };
				String[] responseErr=new String[] { "" };
				try {
					if(path.equals("/queryPoint")){
						Req_queryPoint(query, response, responseErr, status, contentType, respHeader);
					} else if (path.equals("/queryGeometry")) {
						Req_queryGeometry(query, response, responseErr, status, contentType, respHeader);
					} else if (path.equals("/readWKT")) {
						Req_readWKT(false, query, response, responseErr, status, contentType, respHeader);
					} else if (path.equals("/debugReadGeometryGridSplitsWKT")) {
						Req_readWKT(true, query, response, responseErr, status, contentType, respHeader);
					} else if (path.equals("/")) {
						isApi=false;
						contentType[0]="text/html; charset=utf-8";
						response[0]="<h1>AreaCityQuery HttpApiServer Running!</h1>\n<pre style='word-break:break-all;white-space:pre-wrap'>"+Desc+"</pre>";
					} else {
						isApi=false;
						status[0]=404;
						contentType[0]="text/html; charset=utf-8";
						response[0]="<h1>请求路径 "+path+" 不存在！</h1>";
					}
				} catch (Throwable e) {
					e.printStackTrace();
					if(e instanceof OutOfMemoryError) {
						System.gc();
					}
					responseErr[0]="接口调用异常："+e.getMessage();
				}
				
				
				if(isApi) {
					if(responseErr[0].length()>0) {//手撸json
						response[0]="{\"c\":1,\"v\":null,\"m\":\""+StringInnerJson(responseErr[0])+"\"}";
					} else {
						response[0]="{\"c\":0,\"v\":"+response[0]+",\"m\":\"\"}";
					}
				}
				
				respHeader.put("Content-Type", contentType[0]);
				Headers header=context.getResponseHeaders();
				for(Entry<String, String> kv : respHeader.entrySet()) {
					header.set(kv.getKey(), kv.getValue());
				}
				
				byte[] sendData=response[0].getBytes("utf-8");
				context.sendResponseHeaders(status[0], sendData.length);
				context.getResponseBody().write(sendData);
				
				
				StringBuilder log=new StringBuilder();
				log.append("["+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"]");
				log.append(status[0]);
				log.append(" "+context.getRequestMethod());
				log.append(" "+path);
				if(queryStr.length()>0) {
					log.append("?"+queryStr);
				}
				log.append(" "+sendData.length);
				System.out.println(log);
				
				return null;
			}
		};
		
		// https://www.apiref.com/java11-zh/jdk.httpserver/com/sun/net/httpserver/HttpServer.html
		httpServer = HttpServer.create(new InetSocketAddress(bindIP, bindPort), 0);
		httpServer.createContext("/", new HttpHandler() {
			@Override
			public void handle(HttpExchange context) throws IOException {
				try {
					fn.Exec(context);
				} catch (Throwable e) {
					e.printStackTrace();
					if(e instanceof OutOfMemoryError) {
						System.gc();
					}
				}
				context.close();
			}
		});
		httpServer.start();
	}
}
