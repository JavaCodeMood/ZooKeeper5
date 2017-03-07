package com.zookeeper;

import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;

import com.alibaba.fastjson.JSON; 

//���������
public class ManagerServer {
	private String serversPath;  
    private String commandPath;  
    private String configPath;  
    private ZkClient zkClient;  
    private ServerConfig config;
    //���ڼ���zookeeper��servers�ڵ���ӽڵ��б�仯
    private IZkChildListener childListener;
    //���ڼ���zookeeper��command�ڵ�����ݱ仯
    private IZkDataListener dataListener;
    //�������������б�
    private List<String> workServerList;
    
    /**
     * 
     * @param serversPath
     * @param commandPath  zookeeper�д������Ľڵ�·��
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
			//���ڼ���zookeeper��servers�ڵ���ӽڵ��б�仯
			public void handleChildChange(String parentPath,List<String> currentChilds) throws Exception{
				//���·������б�
				workServerList = currentChilds;
				System.out.println("�����������б����ı䣬�µ��б��ǣ�");
				execList();
			}
		};
		//���ڼ���zookeeper��command�ڵ�����ݱ仯
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
	
	//����������
	public void start() {  
        initRunning();  
    }  
	
	//ֹͣ������
	public void stop(){
		//ȡ������command�ڵ����ݱ仯��servers�ڵ���б�仯
		zkClient.unsubscribeChildChanges(serversPath, childListener);
		zkClient.unsubscribeDataChanges(commandPath, dataListener);
	}
	
	//��ʼ��
	private void initRunning(){
		//ִ�ж���command�ڵ����ݱ仯��servers�ڵ���б�仯
		zkClient.subscribeDataChanges(commandPath, dataListener);
		zkClient.subscribeChildChanges(serversPath, childListener);
	}
	
	/*ִ�п��������
	 * 1��list,2:create,3:modify
	 */
	private void exeCmd(String cmdType){
		if("list".equals(cmdType)){
			execList();
		}else if("create".equals(cmdType)){
			execCreate();
		}else if("modify".equals(cmdType)){
			execModify();
		}else{
			System.out.println("��������"+cmdType);
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
				//�ڵ��Ѿ������쳣��ֱ��д������
				zkClient.writeData(configPath,JSON.toJSONString(config).getBytes());
			} catch(ZkNoNodeException e){
				//��ʾ���е�һ���ڵ�ĸ��ڵ㻹û�б�����
				String parentDir = configPath.substring(0, configPath.lastIndexOf('/'));
				zkClient.createPersistent(parentDir,true);
				execCreate();
			}
		}
	}
	
	private void execModify(){
		config.setDbUser(config.getDbUser()+"_modify");
		try {
			//��д��zookeeper��
			zkClient.writeData(configPath, JSON.toJSONString(config).getBytes());
		} catch (ZkNoNodeException e) {
			execCreate();
		}
	}
    

}
