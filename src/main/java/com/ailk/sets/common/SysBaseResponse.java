package com.ailk.sets.common;

import java.io.Serializable;

public class SysBaseResponse implements Serializable{

	private static final long serialVersionUID = -7121528919560763168L;

	public static final String SUCCESS = "0"; // 成功
	public static final String EABORT = "1"; // 异常退出
	public static final String EBADDESC = "2"; // 描述错误
	public static final String EBLOCK = "3"; // 阻塞
	public static final String EINVAL = "4"; // 参数无效
	public static final String ELIMIT = "5"; // 超过限制
	public static final String ENOENT = "6"; // 没有入口
	public static final String EOS = "7"; // 操作系统异常
	public static final String EPERM = "8"; // 权限错误
	public static final String EPROTO = "9"; // 协议错误
	public static final String ESVCERR = "10"; // 服务错误
	public static final String ESVCFAIL = "11"; // 服务失败
	public static final String ESYSTEM = "12"; // 系统错误
	public static final String ETIME = "13"; // 超时
	public static final String ERELEASE = "14"; // 版本错误
	public static final String EMATCH = "15"; // 匹配错误
	public static final String EDIAGNOSTIC = "16"; // 诊断错误
	public static final String EDUPENT = "17"; // 重复入口
	public static final String EEXPIRE = "18"; // 超时无效
	public static final String EDEVID = "19"; // 设备错误
	public static final String EVERIFY = "20"; // 获取验证码失败
	public static final String ILLEGAL = "21";  //无效，非法
}
