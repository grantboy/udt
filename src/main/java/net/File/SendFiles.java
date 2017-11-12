/**
 * 
 */
package net.File;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import judp.JudpClient;




/**
 * �����ļ�
 * @author jinyu
 *
 */
public class SendFiles {
private JudpClient client=null;
private final int bufSize=10*1024*1024;
private String remoteHost="";
private int remotePort=0;
private static  Logger log=Logger.getLogger(SendFiles.class.getName());
	public SendFiles(String host,int port)
	{
		this.remoteHost=host;
		this.remotePort=port;
	}
public void sendFile(String path)
{
	File dir=new File(path);
    if(!dir.exists())
    {
    	return;
    }
    //
    File[] f=null;
    if(dir.isDirectory())
    {
    	f=dir.listFiles();
    }
    else
    {
    	f=new File[] {dir};
    }
    readSend(f);
}
private void readSend(File[]files)
{
	if(files==null||files.length==0)
	{
		return;
	}
	//
	for(int i=0;i<files.length;i++)
	{
		File cur=files[i];
		sigleFile(cur);
		
	}
}
private void  sigleFile(File f)
{
	client=new JudpClient();
	client.connect(remoteHost, remotePort);
	DataInputStream dis =null;
	byte[] buf=new byte[bufSize];
	long fLen=f.length();
	try
	{
		String finfo="File:"+f.getName()+",Length:"+fLen;
		client.sendData(finfo.getBytes(PackagetCharSet.CharSet));
	    byte[] infobytes=new byte[14700];
	    int r= client.read(infobytes);
	   //
	   int waitTime=0;
	   while(r==0)
	   {
	    TimeUnit.MILLISECONDS.sleep(500);
	    r= client.read(infobytes);
	    waitTime++;
	    if(waitTime>1000)
	    {
	    	//10���ʱ
	    	  client.close();
	    	  log.warning("��ʱ�������ļ�ʧ�ܣ�"+f.getName());
			   buf=null;
			   System.gc();
	       return;
	    }
	    if(r==0&&client.isClose())
	    {
		   //��ʱ
		   client.close();
		   log.warning("���ն�û�л�ִ�������ļ�ʧ�ܣ�"+f.getName());
		   buf=null;
		   System.gc();
		   return;
	    }
	    else if(r>0)
	    {
	    	break;
	    }
	   }
	    String serverinfp=new String(infobytes,0,r,PackagetCharSet.CharSet);
	    String rsp="initServer:"+f.getName();
	    if(!serverinfp.equals(rsp))
	    {
	    	client.close();
	    	return;
	    }
	    log.info("��ȡ�ļ�");
	   dis = new DataInputStream(new FileInputStream(f)); 
	   int count=0;
	   long startTime=System.currentTimeMillis();
	   while((count=dis.read(buf,0, bufSize))!=-1)
	   {
		 
		   if(count==bufSize)
		   {
			   client.sendData(buf);
			   log.info("���ͣ�"+f.getName()+","+bufSize);
		   }
		   else
		   {
			   byte[] tmp=new byte[count];
			   System.arraycopy(buf, 0, tmp, 0, tmp.length);
			   client.sendData(tmp);
			   log.info("���ͣ�"+f.getName()+","+count);
		   }
		   
	   }
	   long endTime=System.currentTimeMillis();
	   client.close();
	   dis.close();
	   long speed=fLen/((endTime-startTime)/1000);
	   log.info("������ɣ�"+f.getName()+",ƽ���ٶ�(M/S)��"+speed/1024/1024);
	   
	}
	catch(Exception ex)
	{
	   
	}
	  
}
}
