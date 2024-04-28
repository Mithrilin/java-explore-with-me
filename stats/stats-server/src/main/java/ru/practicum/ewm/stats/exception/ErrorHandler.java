package ru.practicum.ewm.stats.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.dto.exception.ApiError;
import ru.practicum.ewm.dto.exception.NotValidException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler({NotValidException.class})
    public ResponseEntity<ApiError> handleValidationException(Exception ex) throws IOException {
        ApiError apiError = new ApiError(
                Collections.singletonList(error(ex)),
                ex.getLocalizedMessage(),
                "Incorrectly made request",
                HttpStatus.BAD_REQUEST,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    private String error(Exception e) throws IOException {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String error = sw.toString();
        sw.close();
        pw.close();
        return error;
    }
}
