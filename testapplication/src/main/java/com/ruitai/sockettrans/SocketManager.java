package com.ruitai.sockettrans;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class SocketManager {
	private static final String TAG = "SocketManager";
	private ServerSocket server;
	private Handler handler = null;
	public SocketManager(Handler handler){
		this.handler = handler;
		Log.e(TAG,"SocketManager ");
		int port = 9999;
		while(port > 9000){
			try {
				server = new ServerSocket(port);
				break;
			} catch (Exception e) {
				port--;
			}
		}
		SendMessage(1, port);
		Thread receiveFileThread = new Thread(new Runnable(){
			@Override
			public void run() {
				while(true){
					ReceiveFile();
				}
			}
		});
		receiveFileThread.start();
	}
	void SendMessage(int what, Object obj){
		if (handler != null){
			Message.obtain(handler, what, obj).sendToTarget();
		}
	}

	void ReceiveFile(){
		try{
			Log.e(TAG,"ReceiveFile ");
			Socket name = server.accept();
			Log.e(TAG,"name = "+name);
			InputStream nameStream = name.getInputStream();
			InputStreamReader streamReader = new InputStreamReader(nameStream);
			BufferedReader br = new BufferedReader(streamReader);
			String fileName = br.readLine();
			Log.e(TAG,"fileName = "+fileName);
			br.close();
			streamReader.close();
			nameStream.close();
			name.close();
			SendMessage(0, "正在接收:" + fileName);

			Socket data = server.accept();
			InputStream dataStream = data.getInputStream();
			String saveDir = Environment.getExternalStorageDirectory().getPath() + "/Socket";
			File fileDir = new File(saveDir);
			if (!fileDir.exists()){
				final boolean mkdirs = fileDir.mkdirs();
				Log.e(TAG,"mkdirs = "+mkdirs);
			}
			String savePath = fileDir.getPath() + "/" + fileName;
			Log.e(TAG,"savePath = "+savePath);
			FileOutputStream file = new FileOutputStream(savePath, false);
			byte[] buffer = new byte[1024];
			int size = -1;
			while ((size = dataStream.read(buffer)) != -1){
				file.write(buffer, 0 ,size);
			}
			file.close();
			dataStream.close();
			data.close();
			SendMessage(1001, savePath);
			SendMessage(0, fileName + "接收完成");

		}catch(Exception e){
			Log.e(TAG,"ReceiveFile Error -->" +e);
			SendMessage(0, "接收错误:\n" + e.getMessage());
		}
	}
	public void SendFile(ArrayList<String> fileName, ArrayList<String> path, String ipAddress, int port){
		try {
			for (int i = 0; i < fileName.size(); i++){
				Socket name = new Socket(ipAddress, port);
				OutputStream outputName = name.getOutputStream();
				OutputStreamWriter outputWriter = new OutputStreamWriter(outputName);
				BufferedWriter bwName = new BufferedWriter(outputWriter);
				bwName.write(fileName.get(i));
				bwName.close();
				outputWriter.close();
				outputName.close();
				name.close();
				SendMessage(0, "正在发送" + fileName.get(i));
				
				Socket data = new Socket(ipAddress, port);
				OutputStream outputData = data.getOutputStream();
				FileInputStream fileInput = new FileInputStream(path.get(i));
				int size = -1;
				byte[] buffer = new byte[1024];
				while((size = fileInput.read(buffer, 0, 1024)) != -1){
					outputData.write(buffer, 0, size);
				}
				outputData.close();
				fileInput.close();
				data.close();
				SendMessage(0, fileName.get(i) + "  发送完成");
			}
			SendMessage(0, "所有文件发送完成");
		} catch (Exception e) {
			SendMessage(0, "发送错误:\n" + e.getMessage());
		} 
	}
}
