package Test;

import java.util.concurrent.TimeUnit;

import judp.JudpClient;

public class TestClient {

	public static void main(String[] args) {
		long num=0;
		JudpClient client=new JudpClient("192.168.1.4", 1422);
		client.connect("192.168.1.4", 1421);
		while(true) {
			byte[]data=("Hello word "+num).getBytes();
			System.out.println("Sent data size:"+client.sendData(data)+".");
			//client.close();
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			num++;
		}

	}

}
