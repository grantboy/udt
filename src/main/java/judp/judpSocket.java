/**
 * 
 */
package judp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import udt.UDTSession;
import udt.UDTSocket;
import udt.packets.Destination;

/**
 * @author jinyu
 *
 *����˷��ص�����ӿڶ���
 *����socket����������ݵĶ���
 */
public class judpSocket {
private   int bufSize=65535;
private UDTSocket socket=null;
private boolean isClose=false;
private long sendLen=0;//��������
private long socketID=0;//ID
private Thread closeThread;
private final int waitClose=10*1000;
private PackagetCombin pack=new PackagetCombin();
//private int readLen=0;
public int dataLen=0;
public void setRecBufferSize(int size)
{
	bufSize=size;
}
public boolean getCloseState()
{
	//�ײ��Ѿ��ر�
	return isClose|socket.isClose();
}
public judpSocket(UDTSocket  usocket)
{
	this.socket=usocket;
	socketID=socket.getSession().getSocketID();
}

/**
 * ��ȡID
 * @return
 */
public long getSocketID()
{
	return socketID;
}

/**
 * �ر�
 * �ȴ�������ɹر�
 */
public void close()
{
	isClose=true;
	//������ʵ�ر�
	if(sendLen==0)
	{
		stop();
		 System.out.println("����ر�socket");
	}
	else
	{
		//�й����������򻺳�
		//SocketManager.getInstance().add(socket);
	
		if(closeThread==null)
		{
			closeThread=new Thread(new Runnable() {

				@Override
				public void run() {
					int num=0;
				while(true)
				{
					if(socket.getSender().isSenderEmpty())
					{
						stop();
						break;
					}
					else
					{
						try {
							TimeUnit.MILLISECONDS.sleep(100);
							num++;
							if(waitClose<=num*100)
							{
								stop();
								break;
							}
						} catch (InterruptedException e) {
							
							e.printStackTrace();
						}
					}
				}
					
				}
				
			});
			closeThread.setDaemon(true);
			closeThread.setName("closeThread");
		}
		if(closeThread.isAlive())
		{
			return;
		}
		else
		{
			closeThread.start();
		}
	}
}

/**
 * �����ر�
 */
public void stop()
{
	//û�з��������ֱ�ӹرգ�����Ҫ�ȴ����ݷ������
	 try {
		socket.close();
		UDTSession serversession=socket.getEndpoint().removeSession(socketID);
		if(serversession!=null)
		{
			serversession.getSocket().close();
		     socket.getReceiver().stop();
		     socket.getSender().stop();
			System.out.println("����ر�socket:"+serversession.getSocketID());
		}
		
		serversession=null;
	} catch (IOException e) {
		e.printStackTrace();
	}
	 System.out.println("����ر�socket");
}

/**
 * ��ȡ����
 * ���ؽ��յ��ֽڴ�С
 */
public int readData(byte[]data)
{
    if(getCloseState()) {
    	return -1;
	}
	try {
    	int r=socket.getInputStream().read(data);
	  	//readLen+=r;
	 	return r;
	} catch (IOException e) {
		e.printStackTrace();
	}

	return -1;
}

/**
 * ��ȡȫ������
 */
public byte[] readALL()
{
	 byte[] result=null;
	  if(socket!=null)
	  {
		  byte[]  readBytes=new byte[bufSize];//������
		  int r=0;
		  while(true)
		  {
			  if(getCloseState())
				{
					return null;
				}
		       r=readData(readBytes);
		      if(r==-1)
		      {
		    	  result=pack.getData();
		    	  break;
		      }
		      else
		      {
		         // readLen+=r;
		    	  if(r==0)
		    	  {
		    		  try {
						TimeUnit.MILLISECONDS.sleep(100);
						
						continue;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
		    	  }
		    	 //
		    		  byte[] buf=new byte[r];
		    		  System.arraycopy(readBytes, 0, buf, 0, r);
		    		  if(pack.addData(buf))
		    		  {
		    			  result=pack.getData();
		    			  break;
		    		  }
		    	 
		    	  
		      }
		  } 
		
	  }
	  
	  return result;
}


/*
 * ��ȡ��ʼ������
 */
public long getInitSeqNo()
{
	if(socket!=null)
	{
	   return	socket.getSession().getInitialSequenceNumber();
	}
	return 0;
}

/**
 * ���Ͱ���
 */
public int getDataStreamLen()
{
    return socket.getSession().getDatagramSize();
}

/**
 * Ŀ��socket
 * @return
 */
public Destination getDestination()
{

    if(socket!=null)
    {
       return   socket.getSession().getDestination();
    }
    Destination tmp = null;
    try {
        tmp = new Destination(InetAddress.getLocalHost(), 0);
    } catch (UnknownHostException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    return tmp;
}
/**
 * ��������
 * �����ݲ��ܷ���
 */
public boolean sendData(byte[]data) {
	if(getCloseState())
	{
		return false;
	}
	try {
		
		socket.getOutputStream().write(data);
		socket.getOutputStream().flush();
		sendLen=+1;
		return true;
	} catch (IOException e) {
		e.printStackTrace();
	}
	return false;
}

/**
 * �ְ���������
 * ���ٴηָ����ݣ�ͬʱ���ͷ
 * ��Ӧ��Ҫ��readALL
 * @param data
 * @return
 */
public boolean sendSplitData(byte[]data) {
	if(getCloseState())
	{
		return false;
	}
	 byte[][]result=null;
	 if(dataLen==0)
	 {
		 result= PackagetSub.splitData(data);
	 }
	 else
	 {
		 PackagetSub sub=new PackagetSub();
		 result=sub.split(data, dataLen);
	 }
	 for(int i=0;i<result.length;i++)
	 {
		 if(!sendData(result[i]))
		 {
			 //һ�η���ʧ���򷵻�ʧ��
			 return false;
		 }
	 }
	return true;
}
/**
 * ��ȡԶ��host
 * @return
 */
public String getRemoteHost() {
return	socket.getSession().getDestination().getAddress().getHostName();
	
}

/**
 * ��ȡԶ�˶˿�
 * @return
 */
public int getRemotePort() {
  return	socket.getSession().getDestination().getPort();
}

/**
 * socketid
 * @return
 */
public long getID() {
	
	return socketID;
}
/**
 * �����Ƕ�ȡΪ������д��Ϊ��
 * �����д��Ϊ��������ȡ�ٶ���ʱ�����ݸ��Ƕ�ʧ
 * Ĭ�϶�ȡΪ������û�ж�ȡ�������ǣ��������ݣ��ȴ��ظ�
 * ���ô����ݶ�ȡ��������
 * @param isRead
 */
public void  setBufferRW(boolean isRead)
{
	try {
		socket.getInputStream().resetBufMaster(isRead);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

/**
 * ���ô����ݶ�ȡ
 * Ĭ�� false
 * @param islarge
 */
public void setLargeRead(boolean islarge)
{
	try {
		socket.getInputStream().setLargeRead(islarge);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}


}
