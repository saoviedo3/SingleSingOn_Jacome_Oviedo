package com.espe.micro_usuarios.controller;

import com.espe.micro_usuarios.models.entities.Usuario;
import com.espe.micro_usuarios.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.google.auth.oauth2.TokenVerifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;


@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    @Autowired
    private UsuarioService service;

    @GetMapping
    public ResponseEntity<?> findAll() {
        return ResponseEntity.ok().body(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return ResponseEntity.of(service.findById(id));
    }
    
    
    @GetMapping("/perfil")
    public String getPerfil(@RequestHeader("Authorization") String token) {
        try {
            String CLIENT_ID = "429908816051-80j1hf60q4t94tu5ikq0rbgtpleeanc4.apps.googleusercontent.com";

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), 
                    new GsonFactory()
            )
            .setAudience(Collections.singletonList(CLIENT_ID))
            .build();

            GoogleIdToken idToken = verifier.verify(token.replace("Bearer ", ""));
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                return "Usuario autenticado: " + payload.getEmail();
            } else {
                return "Token inválido";
            }

        } catch (GeneralSecurityException | IOException e) {
            return "Error verificando el token";
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Usuario usuario, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }
        Usuario usuarioCreado = service.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioCreado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody Usuario usuario, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        Optional<Usuario> usuarioOptional = service.findById(id);
        if (usuarioOptional.isPresent()) {
            Usuario usuarioDb = usuarioOptional.get();
            usuarioDb.setNombre(usuario.getNombre());
            usuarioDb.setApellido(usuario.getApellido());
            usuarioDb.setEmail(usuario.getEmail());
            usuarioDb.setTelefono(usuario.getTelefono());
            usuarioDb.setFechaNacimiento(usuario.getFechaNacimiento());
            return ResponseEntity.status(HttpStatus.CREATED).body(service.save(usuarioDb));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("message", "Usuario no encontrado"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Optional<Usuario> usuarioOptional = service.findById(id);
        if (usuarioOptional.isPresent()) {
            service.deleteById(id);
            return ResponseEntity.ok(Collections.singletonMap("message", "Usuario eliminado correctamente"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("message", "Usuario no encontrado"));
    }

}

