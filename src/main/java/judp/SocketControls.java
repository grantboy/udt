/**
 * 
 */
package judp;


import udt.UDTSocket;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author jinyu
 * ���ն��ж�
 */
public class SocketControls {
 private static SocketControls instance;
private ConcurrentHashMap<Long,judpGroupSocket> hash=new  ConcurrentHashMap<Long,judpGroupSocket>();
//private static final Logger logger=Logger.getLogger(SocketManager.class.getName());
 private ArrayBlockingQueue<UDTSocket> hasSocket=new  ArrayBlockingQueue<UDTSocket>(1000);
 private SocketControls (){
	  startThread();
	   
 }
 
 /**
  * �����̼߳������Դ
  */
  private void startThread() {
	Thread  processSocket=new Thread(new Runnable() {

		@Override
		public void run() {
			ArrayList<Long> list=new ArrayList<Long>();
			while(true)
			{
			   for(Entry<Long, judpGroupSocket> entry:hash.entrySet())
			  {
				   UDTSocket socket= entry.getValue().getSocket();
				   if(socket!=null)
				   {
					   try {
						hasSocket.put(socket);
						list.add(entry.getKey());
						
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					}
				   }
			  }
			
			   //
			   if(!list.isEmpty())
			   {
				   //�Ƴ��Ѿ����ɹ���socket
				   for(int i=0;i<list.size();i++)
				   {
					   judpGroupSocket group= hash.remove(list.get(i));
					   if(group!=null)
					   {
						   group.clear();
					   }
				   }
				   list.clear();
			   }
			   else
			   {
				   //ÿ���һ��ȫ��������ͣ
				   try {
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (InterruptedException e) {
				
					e.printStackTrace();
				}
			   }
			}
		}
		
	});
	processSocket.setDaemon(true);
	processSocket.setName("processSocket");
	processSocket.start();
	
}

  /**
   * ��ȡ����
   * @return
   */
public static synchronized SocketControls getInstance() {  
	   
 if (instance == null) {  
	
    instance = new SocketControls();  
 
  
}  
 return instance;  
}

/**
 * ����UDTSocket
 * @param socket
 */
public void addSocket(UDTSocket socket)
{
	long id=socket.getSession().getDestination().getSocketID();//ͬһ��Ŀ��
	judpGroupSocket group=hash.get(id);
	if(group==null)
	{
		group=new judpGroupSocket();
		hash.put(id, group);
	}
	group.addSocket(socket);
}

/**
 * ���������ݵ�socket
 * @return
 */
public UDTSocket getSocket()
{
	try {
		return hasSocket.take();
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
	return null;
}
}
