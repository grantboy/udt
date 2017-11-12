/**
 * 
 */
package net.File;


import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import judp.JudpServer;
import judp.judpSocket;



/**
 * �����ļ�
 * @author jinyu
 * 
 */
public class RecviceFiles {
private JudpServer server=null;
private Thread recThread=null;
private static  Logger log=Logger.getLogger(RecviceFiles.class.getName());
private String dir="";
private ExecutorService pools = Executors.newCachedThreadPool();
public void setDir(String dir)
{
	this.dir=dir;
}
public boolean start(String host,int port)
{
	File cur=new File(dir);
	if(!cur.exists())
	{
		cur.mkdir();
	}
	server=new JudpServer(host, port);
    boolean r=	server.start();
    recThread=new Thread(new Runnable() {
    
    	private	ConcurrentHashMap<String,Long>  fileInfo=new ConcurrentHashMap<String,Long>();
    	private ConcurrentHashMap<String,Long> hashFile=new ConcurrentHashMap<String,Long>();
    	
		@Override
		public void run() {
			
	 while(true)
	    {
		
		final judpSocket ss= server.accept();
		pools.execute(new Runnable() {
			 String fileName="";
			private long sumBytes=0;
			private ConcurrentLinkedQueue<byte[]> recQueue=new ConcurrentLinkedQueue<byte[]>();
			private boolean  isStop=false;
			/**
			 * д���ļ�
			 * @param data
			 * @return
			 */
			 private boolean writeFile(byte[] data)
			 {
		    	   try  
		           {  
		    		   String filePath=dir+"/"+fileName;
		    		   FileOutputStream fs=new FileOutputStream(filePath+".tmp",true);
		               DataOutputStream out=new DataOutputStream(fs);  
		               out.write(data); 
		               out.close();  
		                File  f=new File(filePath+".tmp");
		                long flen=fileInfo.get(fileName);
		                if(flen==f.length())
		                {
		                	//���
		                	log.info(fileName+"�������");
		                	fileInfo.remove(fileName);
		                	hashFile.remove(fileName);
		                	//
		                	 f.renameTo(new   File(filePath));   //����  
		                	 return true;
		                }
		                log.info(fileName+":"+f.length());
		           } 
		    	   catch (Exception e)  
		           {  
		               e.printStackTrace();  
		           }
				return false;  
		     }
			 
			 /**
			  * ��������д���ļ�
			  */
			 private  void  recData()
			 {
				 Thread fileW=new Thread(new Runnable() {

					@Override
					public void run() {
						while(!isStop)
						{
							byte[]tmp=recQueue.poll();
							if(tmp==null)
							{
								try {
									TimeUnit.MILLISECONDS.sleep(100);
								} catch (InterruptedException e) {
									
									e.printStackTrace();
								}
								continue;
							}
							if(writeFile(tmp))
							{
								isStop=true;
							    break;
							}
						}
						//
						recQueue.clear();
						System.gc();
					}
					 
				 });
				 fileW.setDaemon(true);
				 fileW.setName(fileName+"_Thread");
				 fileW.start();
			 }
			@Override
			public void run() {
				 byte[] recData=new byte[65535];
				 long lastTime=System.currentTimeMillis();
				 long speed=0;
				while(!isStop)
				{
				int r= ss.readData(recData);
				if(r==0)
				{
					continue;
				}
				else if(r==-1)
				{
					break;
				}
				//����Ip
				String key=ss.getRemoteHost()+ss.getRemoteHost();
				if (hashFile.containsKey(key)) {
					//˵���ǽ�������
					byte[] tmp=new byte[r];
					System.arraycopy(recData, 0, tmp, 0, r);
					sumBytes+=r;
					recQueue.offer(tmp);
					//
					try
					{
						//
						long timespan=System.currentTimeMillis()-lastTime;
						if(timespan>1000)
						{
							speed=sumBytes/(timespan/1000);
							sumBytes=0;//�Ѿ�����
							lastTime=System.currentTimeMillis();
						}
						else
						{
							//����1s
						}
					   
					}
					catch(Exception ex)
					{
						
					}
					log.info("�ļ������ٶ�(M/S)��"+speed/1024/1024);
				}
				else
				{
					//��Ϣ
					String info = null;
					try {
						info = new String(recData,0,r,PackagetCharSet.CharSet);
					} catch (UnsupportedEncodingException e) {
					
						e.printStackTrace();
					}
					String[] finfo=info.split(",");
					if(finfo!=null&&finfo.length==2)
					{
						String name="";
						long len=0;
						if(finfo[0].startsWith("File:"))
						{
							name=finfo[0].substring(5);
						}
						if(finfo[1].startsWith("Length:"))
						{
							String flen=finfo[1].substring(7);
							len=Long.valueOf(flen);
						}
						hashFile.put(key, 1L);
						fileInfo.put(name, len);
						fileName=name;
						if(len==0)
						{
							//���ļ�����������
							byte[]tmp=name.getBytes();
							ByteBuffer buf=ByteBuffer.allocate(tmp.length+4);
							buf.putInt(tmp.length);
							buf.put(tmp);
							writeFile(buf.array());
							break;
						}
						else
						{
							recData();
						}
						try {
							ss.sendData(("initServer:"+name).getBytes(PackagetCharSet.CharSet));
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						ss.setLargeRead(true);
						log.info("������Ϣ");
						
					}
				   }
			   }
				//
				ss.close();
				
			}
		});
		
		}
		}
    });
    recThread.setDaemon(true);
    recThread.setName("recfiles");
    recThread.start();
return r;
}
}
