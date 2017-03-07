package com.zookeeper;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;

import com.alibaba.fastjson.JSON;

//����������
/*
 * workServer����������Ϣ
 */
public class WorkServer {
	private String serversPath;  
    private String configPath;  
    private ZkClient zkClient;  
    private ServerConfig config;  
    private ServerData serverData;  
      
    private IZkDataListener dataListener;//���ݼ�����  
    
    public WorkServer(String configPath,String serversPath,ServerData serverData,ZkClient zkClient,ServerConfig initconfig){
    	this.serversPath = serversPath;
		this.configPath = configPath;
		this.zkClient = zkClient;
		this.serverData = serverData;
		this.config = initconfig;
		
		//dataListener���ڼ���config�ڵ�����ݸı�
		this.dataListener = new IZkDataListener(){

			/*
			 * �����ݵ�ֵ�ı�ʱ�����
			 * Object data,���data�ǽ�ServerConfig����ת��json�ַ�������
			 * ����ͨ�������е�Object data �õ���ǰ���ݽڵ����µ�������Ϣ 
             * �õ����data��Ϣ���������л���ServerConfig����Ȼ����µ��Լ���serverconfig������ 
			 * @see org.I0Itec.zkclient.IZkDataListener#handleDataChange(java.lang.String, java.lang.Object)
			 */
			public void handleDataChange(String dataPath, Object data) throws Exception {
				String retJson = new String((byte[]) data);
				ServerConfig serverConfigLocal = (ServerConfig)JSON.parseObject(retJson, ServerConfig.class);
				//��������
				updateConfig(serverConfigLocal);  
				System.out.println("�µĹ��������������ǣ�"+serverConfigLocal.toString());
				
			}

			public void handleDataDeleted(String dataPath) throws Exception {
				
				
			}
			
		};
    }
    
    /** 
     * ��������� 
     */  
    public void start(){  
        System.out.println("work server start...");  
        initRunning();  
    }  
      
    /** 
     * �����ֹͣ 
     */  
    public void stop(){  
        System.out.println("work server stop...");  
        //ȡ������  
        zkClient.unsubscribeDataChanges(configPath, dataListener);  
      
    }  
    
    //�������ĳ�ʼ��
    private void initRunning(){
    	registMeToZookeeper();
    	//����config�ڵ�ĸı�
    	zkClient.subscribeDataChanges(configPath, dataListener);
    }
    
    /** 
     * ����ʱ��zookeeperע���Լ� 
     */  
    private void registMeToZookeeper(){  
        //��zookeeper��ע���Լ��Ĺ�����ʵ������servers�ڵ���ע��һ����ʱ�ڵ�  
        //������ʱ�ڵ�  
        String mePath = serversPath.concat("/").concat(serverData.getAddress());  
        try{  
            //�����ǽ�json���л�  
            zkClient.createEphemeral(mePath, JSON.toJSONString(serverData).getBytes());   
        } catch (ZkNoNodeException e) {  
            //���ڵ㲻����  
            zkClient.createPersistent(serversPath, true);  
            registMeToZookeeper();  
        }  
          
    }  
    
    /** 
     * ��������zookeeper��config�ڵ��������Ϣ�ı�ʱ��Ҫ��ȡ������Ϣ�������Լ���������Ϣ 
     */  
    private void updateConfig(ServerConfig serverConfig){  
        this.config = serverConfig;  
    }  

	

}
