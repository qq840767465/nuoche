package com.service.wap;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.dao.BusinessDAO;
import com.dao.HqlDAO;
import com.dao.ServiceDAO;
import com.dao.ServiceTypeDAO;
import com.pojo.Business;
import com.pojo.Service;
import com.pojo.ServiceType;
import com.util.JsonFilter;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * 
 * @author 全恒
 *
 */

@org.springframework.stereotype.Service
public class WapServiceManagerService {

	@Autowired
	private HqlDAO hqlDAO;
	@Autowired
	private BusinessDAO businessDAO;
	@Autowired
	private ServiceDAO serviceDAO;
	@Autowired
	private ServiceTypeDAO serviceTypeDAO;
	
	public String getServiceList(Integer businessid) {
		String hql = "from Service where business.id=?";
		List servicelist = hqlDAO.query(hql, businessid);
		System.out.println("servicelist:"+servicelist.size());
		//转换成json
		/**
		 * JsonConfig config=new JsonConfig();
		JsonFilter.ignoredSet(config);
		Map map =new HashMap();
		map.put("list", list);
		JSONObject obj=new JSONObject();
		obj.putAll(map, config);
		String json=obj.toString();
		System.out.println(json);
		return json;
		 */
		JsonConfig config = new JsonConfig();
		JsonFilter.ignoredSet(config);
		Map map = new HashMap();
		map.put("servicelist", servicelist);
		JSONObject jsonObject = new JSONObject();
		jsonObject.putAll(map, config);
		String json = jsonObject.toString();
		System.out.println(json);
		
		return json;
	}

	public void addservice(int bid, int stid, String stitle, String desc, double scprice, double yyprice,
			int time, String cimages) {
		Service service=new Service();
		Business business = businessDAO.findById(bid);
		service.setBusiness(business);
		ServiceType serviceType = serviceTypeDAO.findById(stid);
		service.setServiceType(serviceType);
		service.setTitle(stitle);
		service.setServiceDesc(desc);
		service.setShichangPrice(scprice);
		service.setYuyuePrice(yyprice);
		service.setTime(time);
		service.setImage("daili/images/lunboimg/"+cimages);
		service.setStatus((short)1);
		//创建时间，更新时间
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = sdf.format(date);
		Timestamp created = Timestamp.valueOf(dateString);
		service.setCreated(created);
		service.setUpdated(created);
		
		serviceDAO.save(service);
		
	}

	public void editservice(int id, int bid, int stid, String stitle, String desc, double scprice, double yyprice,
			int time, String cimages) {
		Service service=serviceDAO.findById(id);
		if(service!=null){
			Business business = businessDAO.findById(bid);
			service.setBusiness(business);
			ServiceType serviceType = serviceTypeDAO.findById(stid);
			service.setServiceType(serviceType);
			service.setTitle(stitle);
			service.setServiceDesc(desc);
			service.setShichangPrice(scprice);
			service.setYuyuePrice(yyprice);
			service.setTime(time);
			service.setImage("daili/images/lunboimg/"+cimages);
			/*Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String dateString = sdf.format(date);
			Timestamp update = Timestamp.valueOf(dateString);*/
			//修改更新时间
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String dateString = sdf.format(date);
			Timestamp updated = Timestamp.valueOf(dateString);
			service.setUpdated(updated);
		}
		serviceDAO.save(service);
		
	}

	public Service updatestatus(int id) {
		Service Service=serviceDAO.findById(id);
		int status=Service.getStatus();
		if(Service!=null)
			Service.setStatus((short) (1-status));
			serviceDAO.save(Service);
		return Service;
	}

}
