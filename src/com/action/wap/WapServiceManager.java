package com.action.wap;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pojo.Business;
import com.pojo.Proxy;
import com.pojo.Service;
import com.service.wap.WapServiceManagerService;
import com.util.JsonFilter;
import com.util.Upload;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * 预约商城，服务管理
 * @author 全恒
 *
 */

@Controller
@RequestMapping("/servicemanager.do")
public class WapServiceManager {

	@Autowired
	private HttpServletRequest request;
	@Autowired
	private WapServiceManagerService wapService;
	
	/**
	 * 查询服务
	 * 返回：json
	 */
	@RequestMapping(params="servicelist")
	@ResponseBody
	public String serviceList(HttpServletRequest request, HttpServletResponse response) throws Exception{
		//得到当前商家
		Business wapbusiness = (Business)request.getSession().getAttribute("wapbusiness");
		if(wapbusiness == null){
			return "";
		}
		//根据商家查询服务
		String json = wapService.getServiceList(wapbusiness.getId());
		//返回json
		return json;
	}
	
	
	/**
	 * 进入新增服务页面
	 * 返回：新增页面
	 */
	@RequestMapping(params="goaddservice")
	public String gotoAddService(HttpServletResponse response) throws Exception{
		Business wapbusiness = (Business)request.getSession().getAttribute("wapbusiness");
		if(wapbusiness == null){
			return "";
		}
		//business不为空，则跳到新增服务页面
		return "";
	}
	
	
	/**
	 * 新增服务
	 * 刷新服务列表
	 */
	@RequestMapping(params="addservice")
	public String addService(HttpServletRequest request, HttpServletResponse response) throws Exception{
		Business wapbusiness = (Business)request.getSession().getAttribute("wapbusiness");
		if (wapbusiness == null) {
			return "";
		}
		String path = request.getSession().getServletContext()
				.getRealPath("/daili/images/lunboimg");
		Map<String, String> map = Upload.upload(request, 5 * 1024 * 1024, path);
		int bid = wapbusiness.getId();//商家id
		int stid = Integer.parseInt(map.get("stid"));//服务类型id
		String stitle = map.get("stitle");//服务标题
		String cimgdescribe = map.get("cimgdescribe");//详细描述
		double scprice = Double.parseDouble(map.get("scprice"));//服务价格
		double yyprice = Double.parseDouble(map.get("yyprice"));//预约价格
		int time = Integer.parseInt(map.get("time"));//新增时间
		String cimages = map.get("cimages");//服务图片
		wapService.addservice(bid, stid, stitle, cimgdescribe, scprice, yyprice, time, cimages);
		
		//返回服务列表
		return "";
	}
	
	/**
	 * 进入编辑服务页面
	 * 1.查询所要编辑服务的详细信息
	 * 2.进入编辑页面
	 */
	@RequestMapping(params="goeditorservice")
	public String gotoEditorService(HttpServletResponse response) throws Exception{
		Business wapbusiness = (Business)request.getSession().getAttribute("wapbusiness");
		if(wapbusiness == null){
			return "";
		}
		//business不为空，则跳到编辑服务页面
		return "";
	}
	
	/**
	 * 编辑服务
	 * 修改数据库
	 * 返回服务列表
	 */
	@RequestMapping(params="editorservice")
	public String editorService(HttpServletResponse response) throws Exception{
		Business wapbusiness = (Business)request.getSession().getAttribute("wapbusiness");
		if (wapbusiness == null) {
			return "";
		}
		int id = Integer.parseInt(request.getParameter("id"));
		String path = request.getSession().getServletContext()
				.getRealPath("/daili/images/lunboimg");
		Map<String, String> map = Upload.upload(request, 5 * 1024 * 1024, path);
		int bid = wapbusiness.getId();
		int stid = Integer.parseInt(map.get("stid"));
		String stitle = map.get("stitle");
		String cdescribe = map.get("cdescribe");
		double scprice = Double.parseDouble(map.get("scprice"));
		double yyprice = Double.parseDouble(map.get("yyprice"));
		int time = Integer.parseInt(map.get("time"));
		String cimages = map.get("cimages");
		String delLunboimages = map.get("delLunboimages");
		if (cimages == null) {
			cimages = map.get("oldLunboimages");
		} else {
			String imgPath = request.getSession().getServletContext()
					.getRealPath("/daili/images/lunboimg");
			File folder = new File(imgPath);
			File[] files = folder.listFiles();
			for (File f : files) {
				if (f.getName().equals(delLunboimages)) {
					f.delete();
				}
			}
		}
		
		wapService.editservice(id, bid, stid, stitle, cdescribe,
				scprice, yyprice, time, cimages);
		
		return "";
	}
	
	
	/**
	 * 上下架
	 */
	@RequestMapping(params="updatestatus")
	public String updateStatus(HttpServletResponse response) throws Exception{
		Business wapbusiness = (Business)request.getSession().getAttribute("wapbusiness");
		
		int id = Integer.parseInt(request.getParameter("id"));
		Service Service = wapService.updatestatus(id);
		response.getWriter().print(Service.getStatus());
		return null;
		
	}
	
	
}
