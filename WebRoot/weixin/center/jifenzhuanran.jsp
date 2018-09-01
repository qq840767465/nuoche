<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@page import="weixin.popular.bean.AccessToken"%>
<%@page import="weixin.popular.api.OpenIdAPI"%>
<%@page import="com.action.WeixinConfig"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html>

	<head>

		<meta charset="utf-8">
		<meta name="viewport" content="width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=1,user-scalable=no" />
		<meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
		<meta name="apple-mobile-web-app-capable" content="yes">
		<meta name="apple-mobile-web-app-status-bar-style" content="black">

		<title>积分转出</title>
		<script src="${pageContext.request.contextPath}/weixin/js/mui.min.js"></script>
		<script src="${pageContext.request.contextPath}/weixin/js/jquery-1.9.0.min.js"></script>
		<link href="${pageContext.request.contextPath}/weixin/css/mui.min.css" rel="stylesheet" />
		<link href="${pageContext.request.contextPath}/weixin/home/loginapp.css" rel="stylesheet" />
		<script src="${pageContext.request.contextPath}/weixin/url.js"></script> 

	</head>

	<body>

		<header class="mui-bar mui-bar-nav" style="background: #ff7900;">
			<a class="mui-action-back mui-icon mui-icon-left-nav mui-pull-left" style="color: white;"></a>
			<span class="mui-title" style="color: white;">积分转让</span>
		</header>

		<div class="mui-content">

			<div class="top3">
				
				<div class="mui-input-row">
					<input type="text" class="mui-input-clear ren" placeholder="手机号 " id="tel" >
				</div>
				
				<div class="mui-input-row mui-disabled">
					<input type="text" disabled="disabled" class="mui-input-clear ren" placeholder="昵称 " id="name">
				</div>
				

				<div class="mui-input-row">
					<input type="text" class="mui-input-clear ren1" placeholder="积分" id="jifen">
				</div>
				
				<div class="mui-input-row">
					<input type="password" class="mui-input-clear ren1" placeholder="支付密码" style="border-top: hidden;border-bottom: hidden;" id="password">
				</div>
				
			</div>

			<button class="top" id="btnOk">确定转出</button>
		</div>
	</body>
	<script type="text/javascript">
	    
	    mui.init();

	    //根据电话号码获取名称
		$("#tel").blur(function() {
		   
		    var tel= this;
			var reg = /^1[0-9]{10}$/;
			if (!reg.test(tel.value)) {
				mui.alert('抱歉，请输入正确的手机号!');
				return;
			}
			mui.ajax("${pageContext.request.contextPath}/users.do?p=getNameByTel",{
				type: "post",
				data:{tel:tel.value},
				timeout:30000,
				success: function(data) {
					if (data=="")
					{
					   $("#name").val("");
					   mui.alert('对方手机号不存在');
					}
					else
					{
					   $("#name").val(data);
					}
				},
				error: function() {
					mui.toast('网络异常,请稍候再试');
				}
			});
		});
		
		//提交
		$("#btnOk").click(function(){
		    var tel = $("#tel").val();
		    var reg = /^1[0-9]{10}$/;
			if (!reg.test(tel)) {
				mui.alert('请输入正确的手机号!');
				return false;
			}
		    
		    var name = $("#name").val();
		    if (name=="")
		    {
		       mui.alert("电话号码不存在");
		       return false;
		    }
		    
		    var jifen = $("#jifen").val();
		    if ( isNaN(jifen))
		    {
		        mui.alert("积分只能是数字");
		        return false;
		    }
		    
		    if (jifen<0)
		    {
		        mui.alert("积分必须大于0");
		        return false;
		    }
		    
		    
		    mui.ajax("${pageContext.request.contextPath}/users.do?p=jifenZhuanchu",{
				type: "post",
				data:{tel:tel.value,jifen:jifen},
				timeout:30000,
				success: function(data) {
				    if (data=="请登录先")
				    {
				       mui.alert("请登录先");
				       return false;
				    }
				
					if (data=="")
					{
					   $("#name").val("");
					   mui.alert('对方手机号不存在');
					}
					
					if (data=="不能自己转给自己")
					{
					   $("#name").val("");
					   mui.alert('不能自己转给自己');
					}
					
                    if (data=="积分不够")
					{
					   mui.alert("积分不够");
					   return false;
					}
					
					if (data=="支付密码错误")
					{
					   mui.alert("支付密码错误");
					   return false;
					}
					
					mui.alert("转出成功");
				},
				error: function() {
					mui.toast('网络异常,请稍候再试');
				}
			});
		});
	
		
	
	
	</script>
	
	

</html>