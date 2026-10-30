package com.api_gestao_financeira.controller;

import com.api_gestao_financeira.dto.UsuarioRequestDTO;
import com.api_gestao_financeira.dto.UsuarioResponseDTO;
import com.api_gestao_financeira.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UsuarioController {
    private final UsuarioService service;

    public UsuarioController(UsuarioService service){
        this.service = service;

    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponseDTO create(@RequestBody @Valid UsuarioRequestDTO dto){
        return service.create(dto);

    }

    @GetMapping
    public List<UsuarioResponseDTO> findAll(){
        return service.findAll();
    }

    @GetMapping("/{id}")
    public UsuarioResponseDTO findById(@PathVariable Long id){
        return service.findById(id);
    }
    @PutMapping("/{id}")
    public UsuarioResponseDTO update(@PathVariable Long id, @RequestBody @Valid UsuarioRequestDTO dto){
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
        service.delete(id);
    }


}
