package com.main;

public class SGIP {
	  //消息ID定义
	  public static final int SGIP_BIND         =0x00000001;   //SP用该消息向SMG发出建立连接的请求
	  public static final int SGIP_BIND_RESP    =0x80000001;   //SP对Bind命令的应答
	  public static final int SGIP_UNBIND       =0x00000002;   //SP用该消息向SMG通知将要断开现有的连接
	  public static final int SGIP_UNBIND_RESP  =0x80000002;   //SP对Unbind命令的应答
	  public static final int SGIP_SUBMIT       =0x00000003;   //SP用该消息向SMG请求发送短消息
	  public static final int SGIP_SUBMIT_RESP  =0x80000003;   //SMG对Submit命令的应答
	  public static final int SGIP_DELIVER      =0x00000004;   //SMG发送一条短消息到SP
	  public static final int SGIP_DELIVER_RESP =0x80000004;   //SP用该消息对从SMG接收到的Deliver命令作出应答
	  public static final int SGIP_REPORT       =0x00000005;   //SMG用该命令通知SP一条Submit命令所发送的MT当前结果
	  public static final int SGIP_REPORT_RESP  =0x80000005;   //SP对从SMG接收到的Report命令作出应答
	  
	  public static final int MaxSubmit=100;         //一次提交的最大短消息数，用于群发
	  
	  
	  //消息头
	  public final int Message_Length=0;               //消息的总长度(字节) 4 Integer
	  public final int Command_ID=0;                   //命令ID  4   Integer
	  public final int Sequence_Number=0;              //序列号  12  Integer
	  
	  //socket state
	  public String sckState=null;
}
