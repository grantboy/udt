package Test;

import java.util.concurrent.TimeUnit;

import judp.JudpClient;

public class TestClient {

	public static void main(String[] args) {
		long num=0;
		while(true)
		{
			JudpClient client=new JudpClient();
			client.connect("192.168.30.128", 5555);
			byte[]data=("hello word "+num).getBytes();
			client.sendData(data);
			client.close();
			try {
				System.out.println("µÈ´ý");
				TimeUnit.SECONDS.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			num++;
		}

	}

}
