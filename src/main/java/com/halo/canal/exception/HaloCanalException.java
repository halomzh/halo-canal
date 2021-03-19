package com.halo.canal.exception;

import lombok.Getter;

/**
 * 自定义异常
 *
 * @author shoufeng
 */

public class HaloCanalException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	@Getter
	private String msg;
	@Getter
	private int code = 500;

	public HaloCanalException(String msg) {
		super(msg);
		this.msg = msg;
	}

	public HaloCanalException(String msg, Throwable e) {
		super(msg, e);
		this.msg = msg;
	}

	public HaloCanalException(String msg, int code) {
		super(msg);
		this.msg = msg;
		this.code = code;
	}

	public HaloCanalException(String msg, int code, Throwable e) {
		super(msg, e);
		this.msg = msg;
		this.code = code;
	}

}
