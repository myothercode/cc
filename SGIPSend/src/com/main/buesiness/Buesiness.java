package com.main.buesiness;

import java.util.List;
import java.util.ArrayList;

public class Buesiness implements IBuesiness {
	
    public Object[] get106289977(){
    	Object[] Bu=new Object[4];
    	Bu[0]="9081112400"; //业务编码
    	Bu[1]=(byte)2;      //1B 计费类型 1免费,2,按条计费,3包月计费
    	Bu[2]="100";        //计费值，单位是分
    	Bu[3]=(byte)0;        ////1B 引起MT消息的原因0-MO点播引起的第一条MT消息；1-MO点播引起的非第一条MT消息；2-非MO点播引起的MT消息；3-系统反馈引起的MT消息
    	return Bu;
    }	

	public Object[] get106289975(){
    	Object Bu[]=new Object[4];;
    	Bu[0]="9081031501"; //业务编码
    	Bu[1]=(byte)2;      //1B 计费类型 1免费,2,按条计费,3包月计费
    	Bu[2]="0";        //计费值，单位是分
    	Bu[3]=(byte)0;
    	return Bu;
	}
	
	public Object[] get106558411(){
	   	Object Bu[]=new Object[4];;
    	Bu[0]="9081226101"; //业务编码
    	Bu[1]=(byte)2;      //1B 计费类型 1免费,2,按条计费,3包月计费
    	Bu[2]="0";        //计费值，单位是分
    	Bu[3]=(byte)0;
    	return Bu;
	}
	
	public Object[] get10628997(){
	   	Object Bu[]=new Object[4];;
    	Bu[0]="9081012601"; //业务编码
    	Bu[1]=(byte)3;      //1B 计费类型 1免费,2,按条计费,3包月计费
    	Bu[2]="500";        //计费值，单位是分
    	Bu[3]=(byte)2;
    	return Bu;
	}
	
	public Object[] get106289978(){
	   	Object Bu[]=new Object[4];;
    	Bu[0]="9081234001"; //业务编码
    	Bu[1]=(byte)3;      //1B 计费类型 1免费,2,按条计费,3包月计费
    	Bu[2]="200";        //计费值，单位是分
    	Bu[3]=(byte)2;
    	return Bu;
	}
	
	public Object[] get106289976(){
	   	Object Bu[]=new Object[4];;
    	Bu[0]="9081012501"; //业务编码
    	Bu[1]=(byte)3;      //1B 计费类型 1免费,2,按条计费,3包月计费
    	Bu[2]="500";        //计费值，单位是分
    	Bu[3]=(byte)2;
    	return Bu;
	}
	
	public Object[] get106558412(){
	   	Object Bu[]=new Object[4];;
    	Bu[0]="9081226102"; //业务编码
    	Bu[1]=(byte)3;      //1B 计费类型 1免费,2,按条计费,3包月计费
    	Bu[2]="1500";        //计费值，单位是分
    	Bu[3]=(byte)2;
    	return Bu;
	}

}
