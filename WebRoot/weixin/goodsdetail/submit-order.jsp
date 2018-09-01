<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html>
<html lang="zh-CN">

	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>支付订单</title>
		<meta name="viewport" content="initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no, width=device-width">
		<meta name="apple-mobile-web-app-capable" content="yes">
		<meta name="apple-touch-fullscreen" content="yes">
		<meta name="apple-mobile-web-app-status-bar-style" content="black">
		<meta name="format-detection" content="telephone=no">
		<meta name="format-detection" content="address=no">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/weixin/css/common.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/weixin/css/index.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/weixin/css/mui.min.css" />
		<link rel="stylesheet" href="${pageContext.request.contextPath}/weixin/css/reset.css">
		<script type="text/javascript" src="${pageContext.request.contextPath}/weixin/js/jquery-1.7.1.min.js"></script>
		<script src="${pageContext.request.contextPath}/weixin/js/mui.min.js"></script>
		<script src="${pageContext.request.contextPath}/weixin/js/swiper.jquery.min.js" type="text/javascript"></script>
	</head>

	<body>
		<header class="mui-bar mui-bar-nav" style="background: #6cccca;background-color:#FF7900;">
			<span class="mui-icon mui-icon-arrowleft mui-action-back" style="color: white;"></span>
			<span class="mui-title" style="color: white;">支付订单</span>
		</header>
		<div class="mui-scroll-wrapper mui-content">
			<div class="mui-scroll">
				<div id="container">
					<div id="main">
						<div class="in-order-summary">
							<div class="in-avatar" style="margin-left: 70px;">
								<img src="${pageContext.request.contextPath}/weixin/images/c.png" alt="商家头像">
							</div>
							<div class="in-order-desc">
								<div class="cell-ellipsis">
									<span class="in-order-fee in-bottom-line">¥<span id="submit_zongjia">0.00</span></span>
								</div>
								<h4 class="cell-ellipsis">
									<span class="in-order-title in-bottom-line">卡卡商城</span>
								</h4>
							</div>
						</div>
						<div class="in-pay-method cell-box" id="promptBtn">
							<div class="in-pay-type cell-access">
								<div class="cell-icon">
									<img src="${pageContext.request.contextPath}/weixin/images/jiangjin.png" alt="银行卡支付">
								</div>
								<div class="cell-left">
									<div class="in-pay-title cell-ellipsis">卡卡积分支付 </div>
									<div class="in-pay-info cell-ellipsis">
										<span class="in-amount-info">可用积分 ¥<span id="confirm_yue">0</span></span>
									</div>
									<div class="in-pay-info cell-ellipsis" style="float: right;margin-top: -42px;">
										<span class="in-amount-info" id="confirm_my" style="display: none;"> 需支付¥<span id="confirm_money">0</span></span>
										<span class="in-amount-info" id="confirm_jf" style="display: none;"> 已选择¥<span id="confirm_jifen">0</span></span>
									</div>
								</div>
								<div class="checkbox" id="jifenpay" style="margin-right: 100px;">
									<input onkeyup="go(this.value)" placeholder="请输入积分" style="font-size: 12px;width: 130px;border:none;border-bottom: 1px solid #cccccc;" type="text" name="pay-method" data-index="0" id="chkJifen">
								</div>
							</div>
						</div>
						<div class="in-pay-method cell-box">
							<div class="in-pay-type cell-access" id="jixupay" pay-method="display: block;">
								<div class="cell-left">
									<div class="in-pay-title cell-ellipsis" style="text-align: center;margin-left: 50px;"><span class="in-amount-info">第三方支付  ¥<span id="jixu_money">0</span></span>
									</div>
								</div>
							</div>
							<div style="display: block;" id="weixin-div">
								<div class="in-pay-type cell-access">
									<div class="cell-icon">
										<img src="${pageContext.request.contextPath}/weixin/img/weixin2-2.png" alt="微信支付">
									</div>
									<div class="cell-left">
										<div class="in-pay-title cell-ellipsis">微信支付 </div>
										<div class="in-pay-info cell-ellipsis">
											<span class="in-amount-info">推荐安装微信6.0.2及以上版本</span>
										</div>
									</div>
									<div class="checkbox">
										<input type="radio" name="pay-method" data-index="1">
										<span class="checkbox-icon-round"></span>
									</div>
								</div>
							</div>
						</div>
						<div class="btn-line" id="confirm_pay">
							<button class="btn btn-submit">
								<span class="in-confirm">确认支付</span>
								<span class="in-need-pay">¥<span id="confirm_payPrice">0</span></span>
							</button>
						</div>
					</div>
				</div>
			</div>
		</div>
	</body>
	<script src="${pageContext.request.contextPath}/weixin/js/index.min.14hpKd.js"></script>
	<script src="${pageContext.request.contextPath}/weixin/url.js"></script>
	<script>
		mui.init();
		var userid = '${userinfo.usersId}'; //会员ID
		var self = null;
		var view = null;
		var money = null; //总金额
		var jifen = null; //会员积分
		var ddhao = null; //订单号
		var wxstatus = null; //状态 判断微信支付是否启用
		var alipaystatus = null; //状态  判断支付宝支付是否启用
		var orid = null; //订单ID
		var num = 0; //众筹商品购买数量
		var gid = null; //商品ID
		var goodid = [];
		var gnum = [];
		var viewid = null; //页面传过来的ID
		var PAYSERVER = '';
		var wxChannel = null; //微信支付
		var aliChannel = null; //支付宝支付
		var channel = null; //支付通道
		var paytype = 'alipay'; //支付方式
		var ALIPAYSERVER = null;
		var WEIXINSERVER = null;
		var inputJf = 0; //输入的积分
		var orderId = null; //订单编号
		window.onload =function (){
			num ='${param.num}';
			gid = '${param.gid}';
			goodid = '${param.goodid}';
			gnum = '${param.gnum}';
			viewid = '${param.id}';
			orderId = '${param.orderId}';
			ALIPAYSERVER = "${pageContext.request.contextPath}/" + "apppay.do?p=payByAlipay"; //支付宝支付
			WEIXINSERVER = "${pageContext.request.contextPath}/" + "apppay.do?p=payByWeixin"; //微信支付
			
			if("submit-order" == viewid) { //购物车
				mui.post("${pageContext.request.contextPath}/" + "apporder.do?p=getAllByUidMoney", {
					userid: userid
				}, function(data) {
					getAllMoney(data);
				});
			} else if("submit-order.html" == viewid) //订单页面支付
			{
				orid = '${param.orid}';
				mui.post("${pageContext.request.contextPath}/" + "apporder.do?p=getAllOrderByUidMoney", {
					userid: userid,
					orid: orid
				}, function(data) {
					getAllMoney(data);
				});
			} else if("crowdfunding-order.html" == viewid) {
				mui.post("${pageContext.request.contextPath}/" + "apporder.do?p=getAllByUidMoney", {
					userid: userid
				}, function(data) {
					getAllMoney(data);
				});
			}
		//});
		}
		//得到积分 和 总金额
		function getAllMoney(data) {
			var map = eval("(" + data + ")");
			for(var key in map) {
				if(key == "jifen") {
					jifen = map[key];
				}
				if(key == "money") {
					money = map[key];
				}
				if(key == "ddhao") {
					ddhao = map[key];
				}
				if(key == "alipayStatus") {
					alipaystatus = map[key];
					if(alipaystatus == '1') {
						$("#alipay-div").css('display', 'block');
					} else {
						$("#alipay-div").css('display', 'none');
					}
				}
				if(key == "status") {
					wxstatus = map[key];
					if(wxstatus == '1') {
						$("#weixin-div").css('display', 'block');
					} else {
						//$("#weixin-div").css('display', 'none');
					}
				}
			}
			$("#submit_zongjia").html(money.toFixed(2)); //标题总金额 
			$("#confirm_yue").html(jifen.toFixed(2)); //可用积分
			$("#confirm_payPrice").html(money.toFixed(2)); //支付按钮上面的金额 
			$("#confirm_money").html(money.toFixed(2)); //要支付的积分支付金额
			$("#jixu_money").html(money.toFixed(2));
			if(money * 1 > jifen * 1) {
				//mui.toast("余额不足"); 
				//$("#confirm_my").html("积分不足");
				//$("#chkJifen").prop("disabled",true);
			}
		}
		//积分支付
		document.getElementById("promptBtn").addEventListener('click2', function(e) {
			//e.detail.gesture.preventDefault(); //修复iOS 8.x平台存在的bug，使用mui.prompt会造成输入法闪一下又没了
			var btnArray = ['取消', '确定'];
			mui.prompt('请输入支付金额：', '', '积分支付', btnArray, function(e) {
				if(e.index == 1) {
					var payPrice = $("#confirm_payPrice").text(); //需支付总金额
					var jifenyue = $("#confirm_yue").text(); //会员积分余额
					var reg = /^[0-9]+(\.[0-9]{1,2})?$/; //正则表达式 验证
					var jifenpay = parseFloat(e.value); //输入的积分
					if(!reg.test(jifenpay)) {
						mui.alert("只能输入有1~2位小数的正实数");
						return;
					}

					if((payPrice * 1) > (jifenyue * 1) || jifenpay > (jifenyue * 1)) {
						mui.alert("积分不足");
						return;
					}

					$("#confirm_jifen").html(jifenpay);
					return false;
					//如果输入的积分==需支付的总金额 则该订单支付完毕
					var yishurujifen = $("#confirm_jifen").text(); //已支付积分
					if(jifenpay >= payPrice) {
						jifenpay = (jifenpay * 1) + (yishurujifen * 1); //总积分=已支付积分+现在支付积分
						if(jifenpay > payPrice) {
							jifenpay = payPrice;
						}
						mui.post("${pageContext.request.contextPath}/" + "apppay.do?p=payjifen", {
							userid: userid,
							jifenpay: jifenpay,
							ddhao: ddhao,
							payPrice: payPrice
						}, function(data) {
							if(data == "支付成功") {
								//商品购买成功后 修改商品购买数量
								//if("crowdfunding-order.html"==viewid)
								//{
								mui.post("${pageContext.request.contextPath}/" + "appgoods.do?p=editZcGoodsNum", {
									num: num,
									gid: gid,
									viewid: viewid,
									orid: orid,
									goodid: goodid,
									gnum: gnum
								}, function(data) {
								//	plus.webview.getWebviewById('crowdfunding/crowdfunding.html').reload();
								});
								//}
								//plus.webview.getWebviewById('crowdfunding/crowdfunding.html').reload();
								//plus.webview.getWebviewById('center.html').reload();
								if(view == "crowdfunding-order.html") {
								//	plus.webview.getWebviewById('crowdfunding-order.html').close();
								} else if("submit-order.html" == view) {
								//	plus.webview.getWebviewById('submit-order.html').close();
								} else if(view == "submit-order") {
									//plus.webview.getWebviewById('submit-order').close();
								}
								window.location = "${pageContext.request.contextPath}/weixin/goodsdetail/paySuccess.jsp";
							}
						});
					} else {
						$("#confirm_payPrice").html(((payPrice * 1) - (jifenpay * 1)).toFixed(2) * 1);
						$("#jixupay").css('display', 'block');
						$("#jixu_money").html(((payPrice * 1) - (jifenpay * 1)).toFixed(2) * 1);
					}

					$("#confirm_jifen").html(((jifenpay * 1) + (yishurujifen * 1)).toFixed(2)); //已支付积分 未全部支付
					$("#confirm_jf").css('display', 'block'); //需支付 隐藏
					$("#confirm_my").css('display', 'none'); //已支付显示
				} else {
					$("input[name=check]").attr('checked', false);
				}
			})
		}, "div");

		$(function() {
			$("#confirm_pay").click(function() {
				
				if(inputJf == money) //完全使用积分支付
				{
					var payPrice = $("#confirm_payPrice").text(); //需支付总金额
					var jifenyue = $("#confirm_yue").text(); //会员积分余额
					var reg = /^[0-9]+(\.[0-9]{1,2})?$/; //正则表达式 验证
					var jifenpay = inputJf; //输入的积分
					if(!reg.test(jifenpay)) {
						mui.alert("只能输入有1~2位小数的正实数");
						return;
					}

					if((payPrice * 1) > (jifenyue * 1) || jifenpay > (jifenyue * 1)) {
						mui.alert("积分不足");
						return;
					}
					mui.post("${pageContext.request.contextPath}/" + "apppay.do?p=payjifen", {
						userid: userid,
						jifenpay: jifenpay,
						ddhao: orderId,
						payPrice: payPrice
					}, function(data) {
						
						if(data == "请先登录") {

							mui.openWindow({
								url: '/home/login.jsp',
								show: {
									duration: 250
								}
							});
							mui.toast("请先登录");
							return;
						} else if(data == "大于") {
							mui.toast("积分不够");
							return;
						} else if(data == "订单不存在或已支付") {
							mui.toast("订单不存在或已支付");
							return;
						}
						//plus.webview.getWebviewById('center.html').reload();
						if(view == "crowdfunding-order.html") {
						//	plus.webview.getWebviewById('crowdfunding-order.html').close();
						} else if("submit-order.html" == view) {
							//plus.webview.getWebviewById('submit-order.html').close();
						} else if(view == "submit-order") {
						//	plus.webview.getWebviewById('submit-order').close();
						}
						mui.openWindow({
							url: 'paySuccess.jsp',
							id: 'paySuccess'
						});
						
					});
					return;
					
			
				}
				//调用微信支付
				if ($("input[name=pay-method]:checked").size()==0)
				{
				  mui.alert("请选择支付方式");
				  return false
				}
				window.location = "${pageContext.request.contextPath}/wexinBuyGoodsPay.do?p=toPay&orderId="+ddhao+"&money="+money;

			});
		});
		//发起支付方式
		function pay(py) {
			mui.showWaiting('检测支付环境...');
			if(py == 'alipay') {
				PAYSERVER = ALIPAYSERVER;
				channel = aliChannel;
			} else if(py == 'weixin') {
				PAYSERVER = WEIXINSERVER;
				channel = wxChannel;
			} else {
				mui.closeWaiting();
				mui.alert("不支持此通道!", null, "捐赠");
				return;
			}
			/*payReq(function(result){
				console.log("支付成功" + result);
				mui.closeWaiting();
				
				mui.openWindow({
					url:'paySuccess.html',
					id:'paySuccess'
				});
			},function(e){
				console.log("支付失败" + e.message);
				mui.closeWaiting();
			});
		}*/
			doPay();
		}
		/*payReq=function(successcb,errorcb)
		{
			doPay(successcb,successcb);
		};*/
		function doPay(successcb, errorcb) {
			var price = $("#confirm_payPrice").text(); //总金额
			var jifen = $("#confirm_jifen").text(); //已支付的多少积分
			var url = PAYSERVER + "&userid=" + userid + "&money=" + (money-inputJf).toFixed(2) + "&ddhao=" + orderId;
			mui.ajax(url, {
				type: "post",
				success: function(data) {
					var paySrc = data;
					plus.payment.request(channel, paySrc, function() {
						mui.closeWaiting();
						
						//支付成功回调函数
						//alert("支付成功");
						//支付成功 修改订单
						plus.webview.getWebviewById('center.html').reload();
						mui.openWindow({
							url: 'paySuccess.html',
							id: 'paySuccess'
						});

						mui.closeWaiting();
					}, function(e) {
						//支付失败回调函数
						alert("支付失败" + e.message);
						mui.closeWaiting();
						$(":radio").prop("checked", false);
					});
				},
				eerror: function(ldz, errorType, error) {
					mui.closeWaiting();
					errorcb(error);
				}
			});
		}

		function go(jf) //输入的积分
		{
			inputJf = 0;
			if(isNaN(jf)) {
				$("#chkJifen").val("");
				return false;
			}

			if(1 * jf <= 1 * jifen && 1 * jf <= 1 * money) {
				$("#jixu_money").html( (money - jf).toFixed(2));
				inputJf = jf;
			} else {
				$("#chkJifen").val("");
				$("#jixu_money").html(money);
				mui.toast("积分超出使用范围");
			}

		}
	</script>

</html>