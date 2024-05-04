package ru.practicum.ewm.exception;

import lombok.NonNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.practicum.ewm.dto.exception.ApiError;
import ru.practicum.ewm.dto.exception.BadRequestException;
import ru.practicum.ewm.dto.exception.ConflictException;
import ru.practicum.ewm.dto.exception.NotFoundException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({BadRequestException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiError> handleMissingRequestParameter(Exception ex) throws IOException {
        ApiError apiError = new ApiError(
                Collections.singletonList(error(ex)),
                ex.getLocalizedMessage(),
                "Incorrectly made request",
                HttpStatus.BAD_REQUEST,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<ApiError> handleNotFound(Exception ex) throws IOException {
        ApiError apiError = new ApiError(
                Collections.singletonList(error(ex)),
                ex.getLocalizedMessage(),
                "The required object was not found.",
                HttpStatus.NOT_FOUND,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ApiError> handleConflict(Exception ex) throws IOException {
        ApiError apiError = new ApiError(
                Collections.singletonList(error(ex)),
                ex.getLocalizedMessage(),
                "For the requested operation the conditions are not met",
                HttpStatus.CONFLICT,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected @NonNull ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                                   @NonNull HttpHeaders headers,
                                                                                   @NonNull HttpStatus status,
                                                                                   @NonNull WebRequest request) {
        ApiError apiError = new ApiError(
                Arrays.stream(ex.getStackTrace()).map(String::valueOf).collect(Collectors.toList()),
                ex.getLocalizedMessage(),
                "Incorrectly made request",
                HttpStatus.BAD_REQUEST,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    public @NonNull ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                        @NonNull HttpHeaders headers,
                                                                        @NonNull HttpStatus status,
                                                                        @NonNull WebRequest request) {
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add("Field: " + error.getField() + ". Error: " + error.getDefaultMessage() + " Value: " +
                    error.getRejectedValue());
        }
        ApiError apiError = new ApiError(
                errors,
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
