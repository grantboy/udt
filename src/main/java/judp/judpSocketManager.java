/**
 * 
 */
package judp;

import java.lang.ref.ReferenceQueue;
import java.util.HashMap;
import java.util.WeakHashMap;

import java.util.logging.Logger;

import udt.UDPEndPoint;
import udt.UDTSession;

/**
 * @author jinyu
 * ������ܶ˵�judpSocket
 */
public class judpSocketManager {
	private static final Logger logger=Logger.getLogger(SocketManager.class.getName());
	@SuppressWarnings("rawtypes")
	private final ReferenceQueue q = new ReferenceQueue();
	private volatile  long num=0;
	/**
	 * ����ʹ��
	 */
	private final WeakHashMap<judpSocket,Long> hashMap=new WeakHashMap<judpSocket,Long> ();
	private final HashMap<SocketReference<judpSocket>,Long> map=new HashMap<SocketReference<judpSocket>,Long> ();
	private  UDPEndPoint endPoint=null;
	private static judpSocketManager instance=null;
	  private judpSocketManager (UDPEndPoint point){
		 
		  startGC();
		  this.endPoint=point;
		 
	  }
	  
	  /**
	   * ����û��ʹ�õ�judpSocket
	   */
	   private void startGC() {
		   Thread clearSession=new Thread(new Runnable() {

				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					while(true)
					{
						
						SocketReference<judpSocket> k;
					        try {
								while((k = (SocketReference<judpSocket>) q.remove()) != null) {
									try
									{
										map.remove(k);
								   long id=k.getid();
								   UDTSession serversession=endPoint.removeSession(id);
								if(serversession!=null)
								{
									serversession.getSocket().close();
									serversession.getSocket().getReceiver().stop();
									serversession.getSocket().getSender().stop();
									logger.info("���socket��"+id);
								}
									}
									catch(Exception ex)
									{
										logger.warning("���session��"+ex.getMessage());
									}
								}
							} catch (InterruptedException e) {
								
								e.printStackTrace();
							}
					}
					
				}
				
			});
			clearSession.setDaemon(true);
			clearSession.setName("clearSession");
			clearSession.start();
		
	}
	   
	   /**
	    * ����
	    * @param point
	    * @return
	    */
	public static synchronized judpSocketManager getInstance(UDPEndPoint point) {  
		   
	  if (instance == null) {  
		
	     instance = new judpSocketManager(point);    
	     
 }  
	  return instance;  
 }
	
	/**
	 * ���judpSocket
	 * @param socket
	 */
	public void addSocket(judpSocket socket)
	{
		SocketReference<judpSocket> tmp=new SocketReference<judpSocket>(socket,socket.getSocketID(),q);
		hashMap.put(socket, socket.getSocketID());
		map.put(tmp, socket.getSocketID());
		if(num%10==0)
		{
			System.gc();
		}
		num++;
	}
}
