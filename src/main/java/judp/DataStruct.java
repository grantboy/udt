/**
 * 
 */
package judp;

import java.nio.ByteBuffer;

/**
 * @author jinyu
 *
 */
public class DataStruct {
public int dataLen=0;
public byte[][] buffer=null;
public long id;
private volatile int sumNum=0;
private volatile int sumLen=0;
private byte[] result=null;
public DataStruct(int num)
{
	buffer=new byte[num][];
}

/**
 * ��������
 * @return
 */
private boolean check()
{
	if(sumNum>=buffer.length)
	{
		//���ɹ�
		if(sumLen==dataLen)
		{
			//��ʼ�������
			result=new byte[dataLen];
			ByteBuffer cur=ByteBuffer.wrap(result);
			for(int i=0;i<buffer.length;i++)
			{
				if(buffer[i]==null)
				{
					return false;
				}
				else
				{
					cur.put(buffer[i]);
				}
			}
			return true;
		}
	}
	return false;
}
public void clear()
{
	buffer=null;
	result=null;
}
/**
 * ��������
 * @param data
 * @return
 */
public boolean addData(byte[]data)
{
	ByteBuffer buf=ByteBuffer.wrap(data);
	buf.getLong();
	buf.getInt();
	int index=buf.getInt();
    dataLen=buf.getInt();
    byte[] tmp=new byte[buf.limit()-buf.position()];
    buf.get(tmp);
    buffer[index]=tmp;
    sumNum++;
    sumLen+=tmp.length;
    return  check();
}

/**
 * ��ȡ����
 * @return
 */
public byte[] getData()
{
	return result;
}

}
