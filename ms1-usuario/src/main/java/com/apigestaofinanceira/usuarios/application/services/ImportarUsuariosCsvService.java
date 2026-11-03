package com.apigestaofinanceira.usuarios.application.services;

import com.apigestaofinanceira.usuarios.application.usecases.CreateUsuarioUseCase;
import com.apigestaofinanceira.usuarios.domain.model.Usuario;
import com.apigestaofinanceira.usuarios.infrastructure.adapters.input.web.dto.ImportacaoUsuarioResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImportarUsuariosCsvService {

    private final CreateUsuarioUseCase createUsuarioUseCase;

    public ImportarUsuariosCsvService(CreateUsuarioUseCase createUsuarioUseCase) {
        this.createUsuarioUseCase = createUsuarioUseCase;
    }

    public ImportacaoUsuarioResponseDTO importar(MultipartFile arquivo) {

        int totalSucesso = 0;
        int totalFalhas = 0;

        List<String> erros = new ArrayList<>();

        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("O arquivo CSV não pode estar vazio.");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8))) {

            String cabecalho = reader.readLine();

            if (cabecalho == null) {
                throw new IllegalArgumentException("O arquivo CSV não possui conteúdo.");
            }

            // Remove o BOM, caso exista, e valida o cabeçalho.
            cabecalho = cabecalho.replace("\uFEFF", "");

            List<String> colunas = separarColunas(cabecalho);

            if (colunas.size() != 3 || !colunas.get(0).trim().equalsIgnoreCase("name") || !colunas.get(1).trim().equalsIgnoreCase("email") || !colunas.get(2).trim().equalsIgnoreCase("password")) {

                throw new IllegalArgumentException("Cabeçalho inválido. Utilize: name,email,password");
            }

            String linha;
            int numeroLinha = 1;

            while ((linha = reader.readLine()) != null) {

                numeroLinha++;

                if (linha.trim().isEmpty()) {
                    continue;
                }

                try {

                    List<String> dados = separarColunas(linha);

                    if (dados.size() != 3) {
                        throw new IllegalArgumentException("A linha deve possuir nome, email e senha.");
                    }

                    String name = dados.get(0).trim();
                    String email = dados.get(1).trim();
                    String password = dados.get(2).trim();

                    validarUsuario(name, email, password);

                    Usuario usuario = Usuario.create(name, email, password);

                    createUsuarioUseCase.executar(usuario);

                    totalSucesso++;

                } catch (RuntimeException e) {

                    totalFalhas++;

                    String motivo = e.getMessage() != null ? e.getMessage() : "Erro ao cadastrar usuário.";

                    erros.add("Linha " + numeroLinha + ": " + motivo);
                }
            }

        } catch (IOException e) {

            throw new IllegalArgumentException("Não foi possível ler o arquivo CSV.");
        }

        return new ImportacaoUsuarioResponseDTO(totalSucesso, totalFalhas, erros);
    }

    private void validarUsuario(String name, String email, String password) {

        if (name.isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório.");
        }

        if (name.length() > 150) {
            throw new IllegalArgumentException("O nome deve ter no máximo 150 caracteres.");
        }

        if (email.isBlank()) {
            throw new IllegalArgumentException("O email é obrigatório.");
        }

        if (email.length() > 150 || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

            throw new IllegalArgumentException("Informe um email válido.");
        }

        if (password.isBlank()) {
            throw new IllegalArgumentException("A senha é obrigatória.");
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException("A senha deve ter no mínimo 8 caracteres.");
        }
    }

    private List<String> separarColunas(String linha) {

        List<String> colunas = new ArrayList<>();
        StringBuilder valor = new StringBuilder();

        boolean dentroDeAspas = false;

        for (int i = 0; i < linha.length(); i++) {

            char caractere = linha.charAt(i);

            if (caractere == '"') {

                if (dentroDeAspas && i + 1 < linha.length() && linha.charAt(i + 1) == '"') {

                    valor.append('"');
                    i++;

                } else if (dentroDeAspas) {

                    dentroDeAspas = false;

                } else if (valor.length() == 0) {

                    dentroDeAspas = true;

                } else {

                    throw new IllegalArgumentException("Formato CSV inválido.");
                }

            } else if (caractere == ',' && !dentroDeAspas) {

                colunas.add(valor.toString());
                valor.setLength(0);

            } else {

                valor.append(caractere);
            }
        }

        if (dentroDeAspas) {
            throw new IllegalArgumentException("Existem aspas não fechadas no CSV.");
        }

        colunas.add(valor.toString());

        return colunas;
    }
}
