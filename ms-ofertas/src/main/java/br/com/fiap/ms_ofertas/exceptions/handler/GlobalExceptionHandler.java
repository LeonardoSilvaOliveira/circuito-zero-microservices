package br.com.fiap.ms_ofertas.exceptions.handler;

import br.com.fiap.ms_ofertas.exceptions.DatabaseException;
import br.com.fiap.ms_ofertas.exceptions.RegraNegocioException;
import br.com.fiap.ms_ofertas.exceptions.ResourceNotFoundException;
import br.com.fiap.ms_ofertas.exceptions.dto.CustomErrorDTO;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import br.com.fiap.ms_ofertas.exceptions.dto.ValidationErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CustomErrorDTO> handleResourceNotFound(ResourceNotFoundException e,
                                                                 HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND; //404
        CustomErrorDTO err = new CustomErrorDTO(Instant.now(), status.value(),
                e.getMessage(), request.getRequestURI());

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomErrorDTO> methodArgumentNotValid(MethodArgumentNotValidException e,
                                                                 HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        ValidationErrorDTO err = new ValidationErrorDTO(Instant.now(), status.value(),
                "Dados inválidos", request.getRequestURI());

        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            err.addError(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.status(status).body(err);
    }

    // 400 - JSON malformado / corpo inválido (não dá para desserializar)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CustomErrorDTO> handleHttpMessageNotReadable(HttpMessageNotReadableException e,
                                                                       HttpServletRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST; //400
        CustomErrorDTO err = new CustomErrorDTO(Instant.now(), status.value(),
                "Requisição inválida (JSON malformado ou corpo não interpretável).",
                request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    // 400 - tipo inválido em PathVariable/RequestParam (ex.: /produtos/abc quando espera Long)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<CustomErrorDTO> handleTypeMismatch(MethodArgumentTypeMismatchException e,
                                                             HttpServletRequest request){
        HttpStatus status = HttpStatus.BAD_REQUEST; //400
        CustomErrorDTO err = new CustomErrorDTO(Instant.now(), status.value(),
                "Requisição inválida (parâmetro com tipo/formato incorreto).",
                request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<CustomErrorDTO> handleDatabase(DatabaseException e,
                                                         HttpServletRequest request){

        HttpStatus status = HttpStatus.CONFLICT; //409
        CustomErrorDTO err = new CustomErrorDTO(Instant.now(), status.value(),
                e.getMessage(), request.getRequestURI());

        return ResponseEntity.status(status).body(err);
    }

    // 409 - violação de regra de negócio (ex.: transição de status inválida)
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<CustomErrorDTO> handleRegraNegocio(RegraNegocioException e,
                                                             HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT; //409
        CustomErrorDTO err = new CustomErrorDTO(Instant.now(), status.value(),
                e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    // 404 - rota inexistente
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<CustomErrorDTO> handleNoResource(NoResourceFoundException e,
                                                           HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND; //404
        CustomErrorDTO err = new CustomErrorDTO(Instant.now(), status.value(),
                "Recurso não encontrado.", request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    // 405 - método HTTP não suportado na rota
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<CustomErrorDTO> handleMethodNotSupported(HttpRequestMethodNotSupportedException e,
                                                                   HttpServletRequest request) {
        HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED; //405
        CustomErrorDTO err = new CustomErrorDTO(Instant.now(), status.value(),
                e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    // 500 - fallback para qualquer erro não tratado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomErrorDTO> handleGenericException(Exception e,
                                                                 HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
        CustomErrorDTO err = new CustomErrorDTO(
                Instant.now(), status.value(),
                "Erro interno inesperado.",
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(err);
    }
}
