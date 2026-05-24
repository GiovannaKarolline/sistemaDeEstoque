package br.edu.inventory.produto.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Tratamento global de exceções (Global Exception Handler).
 *
 * <p>Princípio Susan Fowler (Estabilidade): respostas de erro padronizadas
 * e informativas facilitam o diagnóstico e integração entre serviços.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProdutoNotFoundException.class)
    public ResponseEntity<ErroResponse> handleNotFound(ProdutoNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErroResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    @ExceptionHandler(SkuDuplicadoException.class)
    public ResponseEntity<ErroResponse> handleSkuDuplicado(SkuDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErroResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> handleValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = ((FieldError) error).getField();
            erros.put(campo, error.getDefaultMessage());
        });
        ErroResponse response = new ErroResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação",
                erros
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErroResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Erro interno: " + ex.getMessage()));
    }

    // DTO de resposta de erro
    @Getter
    @RequiredArgsConstructor
    public static class ErroResponse {
        private final int status;
        private final String mensagem;
        private Map<String, String> detalhes;
        private final LocalDateTime timestamp = LocalDateTime.now();

        public ErroResponse(int status, String mensagem, Map<String, String> detalhes) {
            this.status = status;
            this.mensagem = mensagem;
            this.detalhes = detalhes;
        }
    }
}
