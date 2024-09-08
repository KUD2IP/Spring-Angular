package com.example.book_back.handler;

import com.example.book_back.exception.OperationNotPermittedException;
import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.security.DigestException;
import java.util.HashSet;
import java.util.Set;

import static com.example.book_back.handler.BusinessErrorCodes.*;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обработчик исключения LockedException.
     * Возвращает ResponseEntity с кодом статуса UNAUTHORIZED и телом,
     * содержащим информацию об исключении.
     *
     * @param exp Исключение LockedException
     * @return ResponseEntity с кодом статуса UNAUTHORIZED и телом,
     * содержащим информацию об исключении
     */
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ExceptionResponse> handleException(LockedException exp) {
        // Создаем новый объект ExceptionResponse
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                // Устанавливаем код бизнес-ошибки
                .businessErrorCode(ACCOUNT_LOCKED.getCode())
                // Устанавливаем описание бизнес-ошибки
                .businessExceptionDescription(ACCOUNT_LOCKED.getDescription())
                // Устанавливаем сообщение об ошибке
                .error(exp.getMessage())
                // Создаем и возвращаем объект ExceptionResponse
                .build();

        // Возвращаем ResponseEntity с кодом статуса UNAUTHORIZED и телом,
        // содержащим объект ExceptionResponse
        return ResponseEntity.status(UNAUTHORIZED).body(exceptionResponse);
    }

    /**
     * Обработчик исключения DisabledException.
     *
     * @param exp Исключение DisabledException
     * @return ResponseEntity с кодом статуса UNAUTHORIZED и телом,
     * содержащим объект ExceptionResponse
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ExceptionResponse> handleDisabledException(DisabledException exp) {
        // Создаем новый объект ExceptionResponse
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                // Устанавливаем код бизнес-ошибки
                .businessErrorCode(ACCOUNT_DISABLED.getCode())
                // Устанавливаем описание бизнес-ошибки
                .businessExceptionDescription(ACCOUNT_DISABLED.getDescription())
                // Устанавливаем сообщение об ошибке
                .error(exp.getMessage())
                // Создаем и возвращаем объект ExceptionResponse
                .build();

        // Возвращаем ResponseEntity с кодом статуса UNAUTHORIZED и телом,
        // содержащим объект ExceptionResponse
        return ResponseEntity.status(UNAUTHORIZED).body(exceptionResponse);
    }

    /**
     * Обработчик исключения BadCredentialsException.
     * Возвращает ResponseEntity с кодом статуса UNAUTHORIZED и телом,
     * содержащим объект ExceptionResponse с кодом бизнес-ошибки, описанием и сообщением об ошибке.
     *
     * @param exp Исключение BadCredentialsException
     * @return ResponseEntity с кодом статуса UNAUTHORIZED и телом,
     * содержащим объект ExceptionResponse
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleException(BadCredentialsException exp) {
        // Создаем новый объект ExceptionResponse
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                // Устанавливаем код бизнес-ошибки
                .businessErrorCode(BusinessErrorCodes.BAD_CREDENTIALS.getCode())
                // Устанавливаем описание бизнес-ошибки
                .businessExceptionDescription(BusinessErrorCodes.BAD_CREDENTIALS.getDescription())
                // Устанавливаем сообщение об ошибке
                .error(BAD_CREDENTIALS.getDescription())
                // Создаем и возвращаем объект ExceptionResponse
                .build();

        // Возвращаем ResponseEntity с кодом статуса UNAUTHORIZED и телом,
        // содержащим объект ExceptionResponse
        return ResponseEntity.status(UNAUTHORIZED).body(exceptionResponse);
    }

    /**
     * Обработчик исключения MessagingException.
     *
     * @param exp Исключение MessagingException
     * @return ResponseEntity с кодом статуса INTERNAL_SERVER_ERROR и телом,
     * содержащим объект ExceptionResponse с сообщением об ошибке.
     */
    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ExceptionResponse> handleException(MessagingException exp){
        // Создаем новый объект ExceptionResponse
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                // Устанавливаем сообщение об ошибке
                .error(exp.getMessage())
                // Создаем и возвращаем объект ExceptionResponse
                .build();

        // Возвращаем ResponseEntity с кодом статуса INTERNAL_SERVER_ERROR и телом,
        // содержащим объект ExceptionResponse
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(exceptionResponse);
    }

    /**
     * Обработчик исключения MethodArgumentNotValidException.
     * Этот обработчик обрабатывает исключение, которое возникает, когда входные данные не проходят валидацию.
     * Он собирает все сообщения об ошибках валидации и возвращает их в виде ответа с кодом статуса BAD_REQUEST.
     *
     * @param exp Исключение MethodArgumentNotValidException
     * @return ResponseEntity с кодом статуса BAD_REQUEST и телом, содержащим объект ExceptionResponse с сообщениями об ошибках валидации
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleException(MethodArgumentNotValidException exp) {
        // Создаем новый набор для хранения сообщений об ошибках
        Set<String> errors = new HashSet<>();

        // Получаем все ошибки валидации и добавляем их в набор
        exp.getBindingResult().getAllErrors()
                .forEach(error -> {
                    var errorMessage = error.getDefaultMessage();
                    errors.add(errorMessage);
                });

        // Создаем новый объект ExceptionResponse с набором сообщений об ошибках
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .validationErrors(errors)
                .build();

        // Возвращаем ResponseEntity с кодом статуса BAD_REQUEST и телом, содержащим объект ExceptionResponse
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(exceptionResponse);
    }

    /**
     * Обработчик исключения Exception.
     * Возвращает ResponseEntity с кодом статуса INTERNAL_SERVER_ERROR и телом,
     * содержащим объект ExceptionResponse с описанием внутренней ошибки и сообщением об ошибке.
     *
     * @param exp Исключение Exception
     * @return ResponseEntity с кодом статуса INTERNAL_SERVER_ERROR и телом,
     * содержащим объект ExceptionResponse
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception exp) {

        exp.printStackTrace();

        // Создаем новый объект ExceptionResponse с описанием внутренней ошибки и сообщением об ошибке
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .businessExceptionDescription("Internal Server Error")
                .error(exp.getMessage())
                .build();

        // Возвращаем ResponseEntity с кодом статуса INTERNAL_SERVER_ERROR и телом,
        // содержащим объект ExceptionResponse
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(exceptionResponse);
    }

    @ExceptionHandler(OperationNotPermittedException.class)
    public ResponseEntity<ExceptionResponse> handleException(OperationNotPermittedException exp){
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(
                        ExceptionResponse.builder()
                                .error(exp.getMessage())
                                .build()
                );
    }
}
