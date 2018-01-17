package io.mithrilcoin.api.exception;

import org.springframework.http.HttpStatus;

import io.mithril.vo.exception.MithrilPlayExceptionCode;

/**
 * 미스릴 플레이 전용 예외 
 * @author Kei
 *
 */
public class MithrilPlayException extends Exception {

	private static final long serialVersionUID = -3603917361300948349L;
	private MithrilPlayExceptionCode errorCode;
	private HttpStatus httpStatus;

	/**
	 * 추가 생성자 에러코드와 HttpStatus 코드값을 설정합니다.
	 * 
	 * @param errorCode
	 * @param httpStatus
	 */
	public MithrilPlayException(MithrilPlayExceptionCode errorCode, HttpStatus httpStatus) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
		this.httpStatus = httpStatus;
	}

	/**
	 * 추가 생성자 HttpStatus를 생략할 경우 HttpStatus.BAD_REQUEST값이 기본 설정.
	 * 
	 * @param errorCode
	 */
	public MithrilPlayException(MithrilPlayExceptionCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
		this.httpStatus = HttpStatus.BAD_REQUEST;
	}

	public MithrilPlayExceptionCode getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(MithrilPlayExceptionCode errorCode) {
		this.errorCode = errorCode;
	}

	public String getCode() {
		return this.errorCode.getCode();
	}

	public String getMessage() {
		return this.errorCode.getMessage();
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString()).append(" errorCode=").append(errorCode);
		return builder.toString();
	}
}
