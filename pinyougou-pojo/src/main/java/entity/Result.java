package entity;

import java.io.Serializable;

/**
 * 执行结果封装类
 * @author lenovo
 *
 */
public class Result implements Serializable{
	
	private boolean success; //成功标志
	private String message;
	
	public Result(boolean success, String message) {
		super();
		this.success = success;
		this.message = message;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	
	
}
