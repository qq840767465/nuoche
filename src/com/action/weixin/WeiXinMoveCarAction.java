package com.action.weixin;

import java.io.IOException; 
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.poi.util.PackageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import weixin.popular.api.MessageAPI;
import weixin.popular.api.OpenIdAPI;
import weixin.popular.api.TokenAPI;
import weixin.popular.api.UserAPI;
import weixin.popular.bean.AccessToken;
import weixin.popular.bean.Token;
import weixin.popular.bean.templatemessage.TemplateMessage;
import weixin.popular.bean.templatemessage.TemplateMessageItem;
import weixin.popular.util.JSSDKUtil;

import com.action.JccsAction;
import com.action.PhoneParam;
import com.action.WeixinConfig;
import com.listener.WeixinGetAccessTokenListen;
import com.pojo.Applydetail;
import com.pojo.ChongzhiHuafei;
import com.pojo.User;
import com.pojo.WeixinUser;
import com.service.weixin.WeiXinBangdingService;
import com.service.weixin.WeiXinMoveCarService;
import com.service.weixin.WeixinUserService;
import com.util.JsonFilter;
import com.util.MD5Util;
import com.util.SendVoiceUtil;
import com.util.StringUtil;
import com.util.URLManager;

@Controller
@RequestMapping("/move.do")
public class WeiXinMoveCarAction {
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private WeiXinMoveCarService service;
	@Autowired
	private WeiXinBangdingService weixinbangdingservice;
	
	
	@Autowired
	private WeixinUserService  weixinUserService;

	@RequestMapping(params = "p=moveCar")
	public String moveCar(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		String shop_url = URLManager.getServerURL(request)
				+ "/move.do?p=moveCar2";
		String shop_newUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
				+ WeixinConfig.APPID
				+ "&redirect_uri="
				+ URLEncoder.encode(shop_url, "utf-8")
				+ "&response_type=code&scope=snsapi_base&state=1#wechat_redirect";
		return "redirect:" + shop_newUrl;
	}

	/**
	 * 跳转到获取充值参数
	 */
	@RequestMapping(params = "p=getczcs")
	public String getGetCzcs(HttpServletRequest request, HttpServletResponse response) throws Exception{
		String shop_url = URLManager.getServerURL(request)
				+ "/move.do?p=czcs";
		String shop_newUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
				+ WeixinConfig.APPID
				+ "&redirect_uri="
				+ URLEncoder.encode(shop_url, "utf-8")
				+ "&response_type=code&scope=snsapi_base&state=1#wechat_redirect";
		return "redirect:" + shop_newUrl;
	}
	
	/**
	 * 获取充值参数
	 * 全恒
	 */
	@RequestMapping(params = "p=czcs")
	public String getCzcs(HttpServletRequest request, HttpServletResponse response) throws Exception{
		// ------------------获取客户的微信号 :@author lgh ------------------//
		String code = request.getParameter("code");
		String appid = WeixinConfig.APPID;
		String secret = WeixinConfig.APPSECRET;
		AccessToken accessToken = new OpenIdAPI().getOpenId(appid, secret, code);
		String weixinhao = accessToken.getOpenid();
		System.out.println(weixinhao);
		// ////System.out.println("客户微信号------------"+weixinhao);
		// --------------- end 获取客户的微信号----------------//
		request.getSession().setAttribute("openid", weixinhao);
		request.getSession().setAttribute("secret", secret);
		request.getSession().setAttribute("weixinhao", weixinhao);
		//获取充值话费的单价
		JccsAction jccsAction = new JccsAction();
		String price = jccsAction.getChongzhihuafei();
		request.getSession().setAttribute("czhfPrice", price);
		return "/weixin/chongzhihuafei.jsp";
	}
	
	/**
	 * 生成订单，跳到支付页面（充值到当前的微信用户）
	 * 全恒
	 */
	@RequestMapping(params = "p=submitnum")
	@ResponseBody
	public String goZhifu(HttpServletRequest request, HttpServletResponse response) throws Exception{
		//获取购买数量
		String account = request.getParameter("account");
		request.getSession().setAttribute("account", account);
		//获取需要支付的总金额
		String sum = request.getParameter("sum");
		request.getSession().setAttribute("sum", sum);
		System.out.println(account+"----"+sum);
		//  生成订单号
		String orderid=service.getAllDingdanhao();
		request.getSession().setAttribute("orderid", orderid);
		
		//  微信订单号
		//创建时间--生成订单时间
		
		//更新时间--用户付款时间
		//总金额
		// 返回创建订单的时间戳
		String weixinhao = (String) request.getSession().getAttribute("openid");
		System.out.println(weixinhao);
		Timestamp createtime = service.doAddOrder(account,orderid,sum,weixinhao);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String strCreatetime = sdf.format(createtime);
		request.getSession().setAttribute("strCreatetime", strCreatetime);
		return "yes";
	}
	
	/**
	 * 显示订单
	 * 全恒
	 */
	@RequestMapping(params="p=showorder")
	public String showOrder(HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		return "weixin/showCzhfOrder.jsp";
	}
	
	/**
	 * 支付
	 */
	@RequestMapping(params="p=toPayHuafei")
	public String toPayHuafei(HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		//System.out.println("来支付了!");
		// 1.得openId
		String orderid = request.getParameter("orderid");
		String money = request.getParameter("money");
		String account = request.getParameter("account");
		String openid = (String) request.getSession().getAttribute("openid");
		System.out.println("orderid:"+orderid);
		System.out.println("money:"+money);
		System.out.println("openid:"+openid);
		System.out.println("account:"+account);
		//  查询数据库中的记录
		ChongzhiHuafei chongzhiHuafei = service.getdetailByOrderid(orderid);
		//System.out.println("数据库中的金额"+applydetail.getTotalFee());
		if(chongzhiHuafei!=null)
		{
			// 2.去支付
			//System.out.println("从中心详细；金额匹配成功!");
			request.getSession().setAttribute("openid", openid);
			request.getSession().setAttribute("account", account);
			String notifyUrl = URLManager.getServerURL(request)+ "/weixinCzhf.do";
			String json = JSSDKUtil.setPayParam(request, notifyUrl, money, orderid,
					openid);
			// 4.调用微信支付
			return "/weixin/pay/pay_example3.jsp";
		}else{
			return "";
		}
	}
	
	/**
	 * 支付完成后页面跳转
	 */
	@RequestMapping(params="p=changeminute2")
	public String changeMinute2(HttpServletRequest request, HttpServletResponse response) throws Exception{
		String url3_4 = URLManager.getServerURL(request)
				+ "/move.do?p=dhtongzhi";
		String newUrl3_4 = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
				+ WeixinConfig.APPID
				+ "&redirect_uri="
				+ URLEncoder.encode(url3_4, "utf-8")
				+ "&response_type=code&scope=snsapi_base&state=1#wechat_redirect";
		return "redirect:"+newUrl3_4;
	}
	
	
	/**
	 * 绑定车牌
	 * 
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(params = "p=bindCar")     
	public String bindCar(HttpServletResponse response) throws IOException {
		// ------------------获取客户的微信号 :@author lgh ------------------//
		String code = request.getParameter("code");
		String appid = WeixinConfig.APPID;
		String secret = WeixinConfig.APPSECRET;
		AccessToken accessToken = new OpenIdAPI()
				.getOpenId(appid, secret, code);
		String weixinhao = accessToken.getOpenid();
		User user = service.findUserByOpen(weixinhao);
		if (user != null && user.getOverdue() == 2) {
			return "/weixin/bangding_success.jsp";
		}

		return moveCar2(request, response);

	}

	@RequestMapping(params = "p=moveCar2")    
	public String moveCar2(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		// ------------------获取客户的微信号 :@author lgh ------------------//
		// String code = request.getParameter("code");
		// String appid = WeixinConfig.APPID;
		// String secret = WeixinConfig.APPSECRET;
		// AccessToken accessToken = new OpenIdAPI()
		// .getOpenId(appid, secret, code);
		// String weixinhao = accessToken.getOpenid();
		// //System.out.println("微信号" + weixinhao);
		JSSDKUtil.setJsSdkParam(request);

		return "/weixin/movecar.jsp";
	}

	/**
	 * 扫码通知
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(params = "p=saoma")
	public String saoMa(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String qrid = request.getParameter("qrid");
		String shop_url = URLManager.getServerURL(request)
				+ "/move.do?p=saoma2&qrid=" + qrid;
		String shop_newUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
				+ WeixinConfig.APPID
				+ "&redirect_uri="
				+ URLEncoder.encode(shop_url, "utf-8")
				+ "&response_type=code&scope=snsapi_base&state=1#wechat_redirect";

		return "redirect:" + shop_newUrl;
		// JSSDKUtil.setJsSdkParam(request);
		// return "/move.do?p=saoma2&qrid=" + qrid;

	}

	@RequestMapping(params = "p=saoma2")
	public String saoMa2(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		// ------------------获取客户的微信号 :@author lgh ------------------//
		String code = request.getParameter("code");
		String appid = WeixinConfig.APPID;
		String secret = WeixinConfig.APPSECRET;
		AccessToken accessToken = new OpenIdAPI()
				.getOpenId(appid, secret, code);
		String weixinhao = accessToken.getOpenid();
		//System.out.println("微信号:" + weixinhao); // 有的时候，要验证是否新用户，就要录入手机号码
		request.getSession().setAttribute("user_wx", weixinhao);
		//JSSDKUtil.setJsSdkParam(request);

		// 判断是否绑定
		/*
		 * int nn = weixinbangdingservice.panduan(weixinhao); if(nn==1) { String
		 * url3_3 = URLManager.getServerURL(request) +
		 * "/personCenter.do?method=index"; String newUrl3_3 =
		 * "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" +
		 * WeixinConfig.APPID + "&redirect_uri=" + URLEncoder.encode(url3_3,
		 * "utf-8") +
		 * "&response_type=code&scope=snsapi_base&state=1#wechat_redirect";
		 * return "redirect:"+newUrl3_3; }
		 */

		/*
		 * service.addWeixinUser(weixinhao); service.addUser(weixinhao);
		 */

		String qrid = request.getParameter("qrid");
		int n = weixinbangdingservice.findbyqrid(qrid);
		User u = service.findbyqrid(qrid);
		request.getSession().setAttribute("user", u);
		// String username = u.getName().substring(0, 1);
		// request.setAttribute("username", username);
		request.getSession().setAttribute("uweixinhao", weixinhao);
		if (n == 1) {
			request.getSession().setAttribute("qrid", qrid);

			UserAPI userAPI = new UserAPI();
			weixin.popular.bean.User weixinUser = userAPI.userInfo(
					WeixinGetAccessTokenListen.access_token, weixinhao);
			if (weixinUser != null && weixinUser.getSubscribe() != null
					&& weixinUser.getSubscribe() == 1) {
				 weixinUserService.addUser(weixinUser);
				return "/weixin/bangdingchepai.jsp";
			} else {
				return "/weixin/guanzhu.jsp";
			}
		}
		if (n == 0) {
			return "/weixin/bangdingshibai.jsp";
		}

		if (n == 2) {

			// 如果扫的是自己
			if (u.getWeixinUser() != null
					&& u.getWeixinUser().getOpenid().equals(weixinhao)) {

				String url3_3 = URLManager.getServerURL(request)
						+ "/personCenter.do?method=index";
				String newUrl3_3 = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
						+ WeixinConfig.APPID
						+ "&redirect_uri="
						+ URLEncoder.encode(url3_3, "utf-8")
						+ "&response_type=code&scope=snsapi_base&state=1#wechat_redirect";
				return "redirect:" + newUrl3_3;
			}

			UserAPI userAPI = new UserAPI();
			weixin.popular.bean.User weixinUser = userAPI.userInfo(
					WeixinGetAccessTokenListen.access_token, weixinhao);
			int gz = 1;
			
			if (weixinUser != null && weixinUser.getSubscribe() != null
					&& weixinUser.getSubscribe() == 1) {
				weixinUserService.addUser(weixinUser);
				gz = 0;
			}
			
			//System.out.println("--------------------------------");
			//System.out.println(weixinUser);
			//System.out.println(weixinUser.getSubscribe());
			//System.out.println(gz);
			//System.out.println("-------------------------------");
			
			
			request.getSession().setAttribute("gz", gz);
			return "/weixin/movecarview.jsp?"+Math.random();
		} else {
			return "/weixin/bangdingshibai.jsp";
		}

	}

	/**
	 * 微信模板通知
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(params = "p=dxtongzhi")
	@ResponseBody
	public String dxtongzhi(HttpServletRequest request,
			HttpServletResponse response) throws UnsupportedEncodingException {
		TemplateMessage templateMessage = new TemplateMessage();
		String wxh = request.getParameter("wxh");
		String jyz_url = URLManager.getServerURL(request)
				+ "/move.do?p=xgjl&wxh=" + wxh;
		String jyz_newUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
				+ WeixinConfig.APPID
				+ "&redirect_uri="
				+ URLEncoder.encode(jyz_url, "utf-8")
				+ "&response_type=code&scope=snsapi_base&state=1#wechat_redirect";
		// String mid = service.messageIdCX(1);
		// String id = mid;

		String czwxh = request.getParameter("wxh");
		String user_wx = request.getParameter("user_wx");
		String tel = request.getParameter("tel");
		String chepai = request.getParameter("cpqz").trim()
				+ request.getParameter("chepai").trim();
		chepai = new String(chepai.getBytes("iso8859-1"), "utf-8");
		String wz = request.getParameter("weizhin");

		templateMessage.setUrl(jyz_url);
		templateMessage.setTopcolor("#173177");
		templateMessage.setTouser(wxh); // 接收者
		templateMessage
				.setTemplate_id("f5inTP7ZvB0r1W7qynF3Li3zAWJOF3DrvG_UeCHh-h0");

		// TemplateMessageItem item = new TemplateMessageItem(value, color)

		LinkedHashMap linkedHashMap = new LinkedHashMap();
		linkedHashMap.put("first", new TemplateMessageItem("尊敬的车主您好，您有新的挪车请求。",
				"#173177"));
		linkedHashMap.put("remark", new TemplateMessageItem((StringUtil
				.isNotNull(wz) ? "位于" : "")
				+ wz + "请您及时前去处理", "#173177"));
		Date date = new Date();
		SimpleDateFormat spd = new SimpleDateFormat("yyyy年MM月dd日  HH:mm");
		String t = spd.format(date);
		linkedHashMap.put("keyword1",
				new TemplateMessageItem(chepai, "#173177"));
		linkedHashMap.put("keyword2", new TemplateMessageItem(t, "#173177"));

		//System.out.println(linkedHashMap.size());
		templateMessage.setData(linkedHashMap);
		MessageAPI messageAPI = new MessageAPI();

		String appid = WeixinConfig.APPID;
		String secret = WeixinConfig.APPSECRET;
		Token token = new TokenAPI().token(appid, secret);
		String access_token = token.getAccess_token();
		messageAPI.messageTemplateSend(access_token, templateMessage);

		String phone = tel;
		String car = chepai;
		String address = wz;
		int mcid = service.addNCJL(czwxh, user_wx, wz);
		if (mcid != -1) {
			String a = "yes";
			List list = new ArrayList();
			list.add(a);
			list.add(mcid);
			JsonConfig config = new JsonConfig();
			JsonFilter.ignoredSet(config);
			String json = JSONArray.fromObject(list, config).toString();
			return json;
		} else {
			return "";

		}
	}

	// 电话通知   跳转
	@RequestMapping(params = "p=dhtongzhi")
	public String dhtongzhi(HttpServletResponse response)
			throws UnsupportedEncodingException {
		String shop_url = URLManager.getServerURL(request)
				+ "/move.do?p=dhtongzhi2";
		String shop_newUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
				+ WeixinConfig.APPID
				+ "&redirect_uri="
				+ URLEncoder.encode(shop_url, "utf-8")
				+ "&response_type=code&scope=snsapi_base&state=1#wechat_redirect";
		return "redirect:" + shop_newUrl;
	}

	@RequestMapping(params = "p=dhtongzhi2")  //免费电话    跳转到电话填写界面
	public String dhtongzhi2() {
		String code = request.getParameter("code");
		String appid = WeixinConfig.APPID;
		String secret = WeixinConfig.APPSECRET;
		AccessToken accessToken = new OpenIdAPI()
				.getOpenId(appid, secret, code);
		String weixinhao = accessToken.getOpenid();
		WeixinUser wxuser = service.findBywxh1(weixinhao);

		request.setAttribute("wxuser", wxuser);

		JSSDKUtil.setJsSdkParam(request);
		return "/weixin/dhtianxie.jsp";
	}
	
	@RequestMapping(params = "p=dianhua")  //电话通知   跳转到微信服务器
	public String dianhua(HttpServletResponse response) throws UnsupportedEncodingException {
		String utel = request.getParameter("utel");
		String uwx = request.getParameter("uwx");
		String weizhin = request.getParameter("weizhin");
		weizhin = new String(weizhin.getBytes("iso8859-1"), "utf-8");
		//System.out.println(weizhin);
		String shop_url = URLManager.getServerURL(request)
				+ "/move.do?p=dianhua2&utel="+utel+"&uwx="+uwx+"&weizhin="+weizhin;
		String shop_newUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
				+ WeixinConfig.APPID
				+ "&redirect_uri="
				+ URLEncoder.encode(shop_url, "utf-8")
				+ "&response_type=code&scope=snsapi_base&state=1#wechat_redirect";
		return "redirect:" + shop_newUrl;
	}
	
	@RequestMapping(params = "p=dianhua2")   //电话通知    填写用户自己的电话
	public String dianhua2() throws UnsupportedEncodingException {
		String code = request.getParameter("code");
		String appid = WeixinConfig.APPID;
		String secret = WeixinConfig.APPSECRET;
		AccessToken accessToken = new OpenIdAPI()
		.getOpenId(appid, secret, code);
		String weixinhao = accessToken.getOpenid();
		String utel = request.getParameter("utel");
		String uwx = request.getParameter("uwx");
		String weizhin = request.getParameter("weizhin");
		weizhin = new String(weizhin.getBytes("iso8859-1"), "utf-8");
		//System.out.println(weizhin);
		request.setAttribute("uwx", uwx);
		request.setAttribute("weizhin", weizhin);
		request.setAttribute("utel", utel);
		WeixinUser wxuser =service.findBywxh1(weixinhao);
		if (wxuser != null) {
			request.setAttribute("wxuser", wxuser);
			//System.out.println(utel);
			return "/weixin/dhtianxie2.jsp";
			//return "/move.do?p=bohaopan&bhpwxh="+weixinhao+"&str="+utel;
		}
		JSSDKUtil.setJsSdkParam(request);
		return "/weixin/bohaosb.jsp";
	}
	
	@RequestMapping(params = "p=dhtongzhi3")//免费电话    跳转到拨号盘界面
	public String dhtongzhi3() {
		String tel = request.getParameter("tel");
		String wxh = request.getParameter("wxh");
		request.setAttribute("bhpwxh", wxh);
		service.addTEL(wxh, tel);
		return "/weixin/bohaopan.jsp";
	}

	@RequestMapping(params = "p=pttongzhi")  //平台通知
	@ResponseBody
	public String pttongzhi(HttpServletRequest request,
			HttpServletResponse response) throws UnsupportedEncodingException {
		String czwxh = request.getParameter("wxh");
		String user_wx = request.getParameter("user_wx");
		String tel = request.getParameter("tel");
		String chepai = request.getParameter("cpqz").trim()
				+ request.getParameter("chepai").trim();
		chepai = new String(chepai.getBytes("iso8859-1"), "utf-8");
		String wz = request.getParameter("weizhin");
		String phone = tel;
		String car = chepai;
		String address = wz;
		int ret = SendVoiceUtil.send(phone, address, car);
		if (ret == 2) {
			// //System.out.println("成功");
			// 车主微信号，用户微信号，位置
			int mcid = service.addNCJL(czwxh, user_wx, wz);
			String a = "yes";
			List list = new ArrayList();
			list.add(a);
			list.add(mcid);
			JsonConfig config = new JsonConfig();
			JsonFilter.ignoredSet(config);
			String json = JSONArray.fromObject(list, config).toString();
			return json;
		} else {
			// //System.out.println("失败");
			return "";
		}

	}

	@RequestMapping(params = "p=cxzt")
	@ResponseBody
	public String cxzt() {
		String mcid = request.getParameter("mcid");
		int a = service.cxzt(mcid);
		// //System.out.println("action" + mcid);
		if (a == 1) {
			return "yes";

		}
		return "";
	}

	@RequestMapping(params = "p=xgjl")
	public String XGJL() {
		String wx = request.getParameter("wxh");
		service.xiugaiStauts2(wx);
		return "/weixin/tongzhinuoche.jsp";
	}

	@RequestMapping(params = "p=bohaopan")   //免费电话    跳到网络电话服务器
	@ResponseBody
	public String bohaopanend() throws HttpException, IOException {
		String wxh = request.getParameter("bhpwxh"); // 微信号
		String str = request.getParameter("str"); // 拨打的电话
		WeixinUser wxuser = service.findBywxh1(wxh);
		////System.out.println(wxh);
		if ((wxuser!=null) && (wxuser.getTimeLeft() > 0) ) {

			String SERVER_URL = "http://api.hbyxyj.cn/apicall.do?command=call";
			String mePhone = wxuser.getWxtel();
			String hePhone = str;
			String appid = PhoneParam.APPID;
			String md5key = PhoneParam.MD5KEY;
			String url = URLManager.getServerURL(request) + "/mianfeicall.do";
			String returnUrl = URLEncoder.encode(url, "utf-8");
			int time = wxuser.getTimeLeft();

			StringBuffer sb = new StringBuffer();
			sb.append("&appid=");
			sb.append(appid);
			sb.append("&callees=");
			sb.append(hePhone);
			sb.append("&caller=");
			sb.append(mePhone);
			sb.append("&return_url=");
			// sb.append(returnUrl);
			// sb.append("&maxtime=").append(time);
			String paramString = sb.toString();

			String sign = MD5Util.getMD5(paramString + url + md5key)
					.toLowerCase();

			String callUrl = SERVER_URL + paramString + returnUrl + "&sign="
					+ sign + "&maxtime=" + time * 10 + "&params="
					+ wxuser.getOpenid();
			////System.out.println(callUrl);

			// {"msg":"call success","code":1,"data":{"orderid":"3e12d73d6a152d7431411432a5c506e9","showno":"+862825054800","ssid":"1200_726_4294967295_20180508142915@cdcallenabler12bhw.caas.sc.chinamobile.com"}}

			HttpClient client = new HttpClient();
			GetMethod getMethod = new GetMethod(callUrl);
			int ret = client.executeMethod(getMethod);
			if (ret == 200) {
				////System.out.println(getMethod.getResponseBodyAsString());
				getMethod.releaseConnection();
			}
			//System.out.println("done!");

			return "yes";
		}

		return "no";
	}
	
	@RequestMapping(params = "p=bohaopan2")  // 电话通知    跳转到网络电话
	public String bohaopanend2() throws HttpException, IOException {
		String wxh = request.getParameter("bhpwxh"); // 微信号
		String uwx = request.getParameter("uwx");
		String str = request.getParameter("str"); // 拨打的电话
		String mytel = request.getParameter("tel");
		String wz = request.getParameter("weizhin");
		if (request.getMethod().equalsIgnoreCase("GET"))
		    wz = new String(wz.getBytes("iso8859-1"), "utf-8");
		WeixinUser wxuser = service.findBywxh1(wxh);
		//System.out.println(wxh);
		if ((wxuser!=null) && (wxuser.getTimeLeft() > 0) ) {

			String SERVER_URL = "http://api.hbyxyj.cn/apicall.do?command=call";
			String mePhone = mytel;
			String hePhone = str;
			String appid = PhoneParam.APPID;
			String md5key = PhoneParam.MD5KEY;
			String url = URLManager.getServerURL(request) + "/mianfeicall.do";
			String returnUrl = URLEncoder.encode(url, "utf-8");
			int time = wxuser.getTimeLeft();
			
			
			service.addTEL(wxh, mePhone);
			StringBuffer sb = new StringBuffer();
			sb.append("&appid=");
			sb.append(appid);
			sb.append("&callees=");
			sb.append(hePhone);
			sb.append("&caller=");
			sb.append(mePhone);
			sb.append("&return_url=");
			// sb.append(returnUrl);
			// sb.append("&maxtime=").append(time);
			String paramString = sb.toString();

			String sign = MD5Util.getMD5(paramString + url + md5key)
					.toLowerCase();

			String callUrl = SERVER_URL + paramString + returnUrl + "&sign="
					+ sign + "&maxtime=" + time * 10 + "&params="
					+ wxuser.getOpenid();
			//System.out.println(callUrl);

			// {"msg":"call success","code":1,"data":{"orderid":"3e12d73d6a152d7431411432a5c506e9","showno":"+862825054800","ssid":"1200_726_4294967295_20180508142915@cdcallenabler12bhw.caas.sc.chinamobile.com"}}

			HttpClient client = new HttpClient();
			GetMethod getMethod = new GetMethod(callUrl);
			int ret = client.executeMethod(getMethod);
			if (ret == 200) {
				//System.out.println(getMethod.getResponseBodyAsString());
				getMethod.releaseConnection();
			}
			//System.out.println("done!");
			service.addNCJL(uwx, wxh, wz);
			
			return "/weixin/bohaocj.jsp";
		}

		return "/weixin/bohaosb.jsp";
	}

}
