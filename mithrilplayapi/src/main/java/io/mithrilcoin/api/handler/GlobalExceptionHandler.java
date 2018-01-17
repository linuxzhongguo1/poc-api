package io.mithrilcoin.api.handler;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import io.mithril.vo.exception.MithrilPlayExceptionCode;
import io.mithrilcoin.api.exception.MithrilPlayException;

/**
 * 오류처리에 대한 공통 익셉션 핸들러입니다.
 * 
 * @author kei
 *
 */
@ControllerAdvice
public class GlobalExceptionHandler {

	private static Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	/**
	 * 일반적인 오류 처리에 대한 처리 방법을 기술합니다.
	 * 
	 * @param request
	 * @param response
	 * @param e
	 * @return
	 */
	@ExceptionHandler(MithrilPlayException.class)
	public ResponseEntity<MithrilPlayException> exceptionHandler(HttpServletRequest request,
			HttpServletResponse response, MithrilPlayException e) {
		logger.error("오류메시지:{}", e.getMessage());
		return new ResponseEntity<MithrilPlayException>(e, e.getHttpStatus());
	}

	/**
	 * 심각한 오류처리 or 런타입 익셉션 등에 대한 처리 방법을 기술합니다.
	 * 
	 * @param request
	 * @param response
	 * @param t
	 * @return
	 */
	@ExceptionHandler(value = { IOException.class, SQLException.class, DataAccessException.class,
			RuntimeException.class })
	public ResponseEntity<MithrilPlayException> exceptionProcess(HttpServletRequest request,
			HttpServletResponse response, Throwable t) {
		logger.error("오류메시지:{}", "\n" + t.getMessage());
		MithrilPlayException e = new MithrilPlayException(MithrilPlayExceptionCode.FATAL_ERROR,
				HttpStatus.INTERNAL_SERVER_ERROR);
		return new ResponseEntity<MithrilPlayException>(e, e.getHttpStatus());
	}

}
