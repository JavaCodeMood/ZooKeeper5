package com.zookeeper;

//服务器信息数据
/**
 * 用于记录WorkServer（工作服务器）的基本信息
 * @author dell
 *
 */
public class ServerData {
	 private String address;  
	 private Integer id;  
	 private String name;
	 
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "ServerData [address=" + address + ", id=" + id + ", name=" + name + "]";
	}  

}
