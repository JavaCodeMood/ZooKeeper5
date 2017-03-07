package com.zookeeper;

import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;

import com.alibaba.fastjson.JSON; 

//管理服务器
public class ManagerServer {
	private String serversPath;  
    private String commandPath;  
    private String configPath;  
    private ZkClient zkClient;  
    private ServerConfig config;
    //用于监听zookeeper中servers节点的子节点列表变化
    private IZkChildListener childListener;
    //用于监听zookeeper中command节点的数据变化
    private IZkDataListener dataListener;
    //工作服务器的列表
    private List<String> workServerList;
    
    /**
     * 
     * @param serversPath
     * @param commandPath  zookeeper中存放命令的节点路径
     * @param configPath
     * @param zkClient
     * @param config
     */
	public ManagerServer(String serversPath, String commandPath, String configPath, ZkClient zkClient,
			ServerConfig config) {
		super();
		this.serversPath = serversPath;
		this.commandPath = commandPath;
		this.configPath = configPath;
		this.zkClient = zkClient;
		this.config = config;
		this.childListener = new IZkChildListener(){
			//用于监听zookeeper中servers节点的子节点列表变化
			public void handleChildChange(String parentPath,List<String> currentChilds) throws Exception{
				//更新服务器列表
				workServerList = currentChilds;
				System.out.println("工作服务器列表发生改变，新的列表是：");
				execList();
			}
		};
		//用于监听zookeeper中command节点的数据变化
		this.dataListener = new IZkDataListener(){
			public void handleDataDeleted(String dataPath) throws Exception{
				
			}

			public void handleDataChange(String dataPath, Object data) throws Exception {
				String cmd = new String((byte[]) data);
				System.out.println("cmd:"+cmd);
				exeCmd(cmd);
				
			}
		};
	}
	
	//启动服务器
	public void start() {  
        initRunning();  
    }  
	
	//停止服务器
	public void stop(){
		//取消订阅command节点数据变化和servers节点的列表变化
		zkClient.unsubscribeChildChanges(serversPath, childListener);
		zkClient.unsubscribeDataChanges(commandPath, dataListener);
	}
	
	//初始化
	private void initRunning(){
		//执行订阅command节点数据变化和servers节点的列表变化
		zkClient.subscribeDataChanges(commandPath, dataListener);
		zkClient.subscribeChildChanges(serversPath, childListener);
	}
	
	/*执行控制命令函数
	 * 1：list,2:create,3:modify
	 */
	private void exeCmd(String cmdType){
		if("list".equals(cmdType)){
			execList();
		}else if("create".equals(cmdType)){
			execCreate();
		}else if("modify".equals(cmdType)){
			execModify();
		}else{
			System.out.println("错误的命令！"+cmdType);
		}
	}
    
	private void execList(){
		System.out.println(workServerList.toString());
	}
	
	private void execCreate(){
		if(!zkClient.exists(configPath)){
			try {
				zkClient.createPersistent(configPath, JSON.toJSONString(config).getBytes());  
			} catch (ZkNodeExistsException e) {
				//节点已经存在异常，直接写入数据
				zkClient.writeData(configPath,JSON.toJSONString(config).getBytes());
			} catch(ZkNoNodeException e){
				//表示其中的一个节点的父节点还没有被创建
				String parentDir = configPath.substring(0, configPath.lastIndexOf('/'));
				zkClient.createPersistent(parentDir,true);
				execCreate();
			}
		}
	}
	
	private void execModify(){
		config.setDbUser(config.getDbUser()+"_modify");
		try {
			//回写到zookeeper中
			zkClient.writeData(configPath, JSON.toJSONString(config).getBytes());
		} catch (ZkNoNodeException e) {
			execCreate();
		}
	}
    

}
