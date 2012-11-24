package com.main;

import java.io.*;
import java.nio.*;

/**
 * 发送线程
 * @author Administrator
 *
 */
public class Send extends Thread{

	private DataInputStream dis ;
	private DataOutputStream dos;
	private enum TState{
		eSelect,   //选择菜单
		eBind,     //绑定
		eSubmit,   //提交
		eUnbind,   //反绑定
		eExit      //退出
	}
	private TState state = TState.eSelect;  //初始化
	private BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	
	private String ChatContent;
	
	public Send(DataInputStream dis,DataOutputStream dos) {
		this.dis = dis;
		this.dos = dos;
	}
	
	public void run(){
		while (true){
			try{
				switch (state) {
				case eSelect:
					System.out.println("请选择功能?1,2,3,4");
					ChatContent = br.readLine();
					if (ChatContent.equals("1")){
						state = TState.eBind;
					}else if (ChatContent.equals("2")){   
						state = TState.eSubmit;
					}else if (ChatContent.equals("3")){   
						state = TState.eUnbind;
					}else if (ChatContent.equals("4")){   
						state = TState.eExit;
					}
					break;
				case eBind:
					Sendbind();
					state = TState.eSelect;
					break;
				case eSubmit:
					Submit();
					state = TState.eSelect;
					break;
				case eUnbind:
					state = TState.eSelect;
					break;
				case eExit:
					break;
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	private void Sendbind() throws Exception{
		Bind bind = new Bind();
		bind.Login_Name     = "10628997";
		bind.Login_Passowrd = "10628997";
		bind.Login_Type     = 1;
		
		ByteBuffer buffer = bind.getAllBuffer();
		
		dos.write(buffer.array());
		dos.flush();		
	}
	
	private void Submit()throws Exception{
		String msg = "ok";
		Submit sub = new Submit();
		sub.head.Cmd_id  = SGIP.SGIP_SUBMIT;
		
		sub.SPNumber = "10628997";
		sub.ChargeNumber = "000000000000000000000";
		sub.UserCount = 1;
		sub.UserNumber = "8615608074554";
		sub.CorpId     = "71320";
		sub.ServiceType= "9081012601";
		sub.FeeType    = 1;
		sub.FeeValue   = "0";
		sub.GivenValue = "0";
		sub.AgentFlag  = 1;
		sub.MorelatetoMTFlag = 2;
		sub.Priority         = 0;
		sub.ReportFlag       = 2;
		sub.TP_pid           = 0 ;
	    sub.TP_udhi          = 0;
	    sub.MessageCoding    = 15;
	    sub.MessageType      = 0;
	    sub.MessageLength    = msg.length();
	    sub.MessageContent   = msg;
	    
	    ByteBuffer buffer = sub.getAllBuffer();
	    byte b[] = buffer.array();
	    for (int i=0;i<b.length;i++)
	    	System.out.println(b[i]);
	    
	    
	    dos.write(buffer.array());
	    dos.flush();
	    
	}
}
