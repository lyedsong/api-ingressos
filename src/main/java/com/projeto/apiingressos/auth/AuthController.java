
package com.projeto.apiingressos.auth;

import com.projeto.apiingressos.model.Usuario;
import com.projeto.apiingressos.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(@RequestBody AuthRequest requisicao) {

        if (requisicao.username() == null || requisicao.username().isBlank()
                || requisicao.password() == null || requisicao.password().isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of("error", "DADOS_INVALIDOS"));
        }

        if (usuarioRepository.existsByUsername(requisicao.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "USUARIO_JA_EXISTE"));
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(requisicao.username());
        usuario.setPassword(
                passwordEncoder.encode(requisicao.password())
        );

        usuarioRepository.save(usuario);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest requisicao) {

        Usuario usuario = usuarioRepository
                .findByUsername(requisicao.username())
                .orElse(null);

        if (usuario == null || !passwordEncoder.matches(
                requisicao.password(),
                usuario.getPassword())) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "CREDENCIAIS_INVALIDAS"));
        }

        String token = jwtService.gerarToken(usuario.getUsername());

        return ResponseEntity.ok(
                new TokenResponse(token)
        );
    }

    public record AuthRequest(String username, String password) {
    }

    public record TokenResponse(String token) {
    }
}
