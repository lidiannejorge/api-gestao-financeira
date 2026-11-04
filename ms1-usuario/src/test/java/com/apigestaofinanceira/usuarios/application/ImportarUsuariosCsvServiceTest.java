package com.apigestaofinanceira.usuarios.application;
import com.apigestaofinanceira.usuarios.application.services.ImportarUsuariosCsvService;
import com.apigestaofinanceira.usuarios.application.usecases.CreateUsuarioUseCase;
import com.apigestaofinanceira.usuarios.infrastructure.adapters.input.web.dto.ImportacaoUsuarioResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportarUsuariosCsvServiceTest {

    @Mock
    private CreateUsuarioUseCase createUsuarioUseCase;

    private ImportarUsuariosCsvService importarUsuariosCsvService;

    @BeforeEach
    void setUp() {
        importarUsuariosCsvService =
                new ImportarUsuariosCsvService(createUsuarioUseCase);
    }

    @Test
    void deveImportarUsuariosComSucesso() {
        String csv = """
                name,email,password
                Ana,ana@gmail.com,senha1234
                João,joao@gmail.com,senha5678
                """;

        MockMultipartFile arquivo = criarArquivo(csv);

        ImportacaoUsuarioResponseDTO resultado =
                importarUsuariosCsvService.importar(arquivo);

        assertEquals(2, resultado.getTotalSucesso());
        assertEquals(0, resultado.getTotalFalhas());
        assertTrue(resultado.getErros().isEmpty());

        verify(createUsuarioUseCase, times(2))
                .executar(any());
    }

    @Test
    void deveContinuarImportacaoQuandoUmaLinhaForInvalida() {
        String csv = """
                name,email,password
                Ana,ana@gmail.com,123
                João,joao@gmail.com,senha5678
                """;

        MockMultipartFile arquivo = criarArquivo(csv);

        ImportacaoUsuarioResponseDTO resultado =
                importarUsuariosCsvService.importar(arquivo);

        assertEquals(1, resultado.getTotalSucesso());
        assertEquals(1, resultado.getTotalFalhas());
        assertEquals(1, resultado.getErros().size());

        verify(createUsuarioUseCase, times(1))
                .executar(any());
    }

    @Test
    void deveRejeitarArquivoVazio() {
        MockMultipartFile arquivo = criarArquivo("");

        assertThrows(IllegalArgumentException.class,
                () -> importarUsuariosCsvService.importar(arquivo));

        verifyNoInteractions(createUsuarioUseCase);
    }

    @Test
    void deveRejeitarCabecalhoInvalido() {
        String csv = """
                nome,email,senha
                Ana,ana@gmail.com,senha1234
                """;

        MockMultipartFile arquivo = criarArquivo(csv);

        assertThrows(IllegalArgumentException.class,
                () -> importarUsuariosCsvService.importar(arquivo));

        verifyNoInteractions(createUsuarioUseCase);
    }

    @Test
    void deveRejeitarLinhaComQuantidadeIncorretaDeColunas() {
        String csv = """
                name,email,password
                Ana,ana@gmail.com
                """;

        MockMultipartFile arquivo = criarArquivo(csv);

        ImportacaoUsuarioResponseDTO resultado =
                importarUsuariosCsvService.importar(arquivo);

        assertEquals(0, resultado.getTotalSucesso());
        assertEquals(1, resultado.getTotalFalhas());
        assertEquals(1, resultado.getErros().size());

        verifyNoInteractions(createUsuarioUseCase);
    }

    @Test
    void deveRejeitarNomeVazio() {
        String csv = """
                name,email,password
                ,ana@gmail.com,senha1234
                """;

        MockMultipartFile arquivo = criarArquivo(csv);

        ImportacaoUsuarioResponseDTO resultado =
                importarUsuariosCsvService.importar(arquivo);

        assertEquals(1, resultado.getTotalFalhas());
        assertTrue(resultado.getErros().get(0).contains("nome"));
    }

    @Test
    void deveRejeitarEmailInvalido() {
        String csv = """
                name,email,password
                Ana,email-invalido,senha1234
                """;

        MockMultipartFile arquivo = criarArquivo(csv);

        ImportacaoUsuarioResponseDTO resultado =
                importarUsuariosCsvService.importar(arquivo);

        assertEquals(1, resultado.getTotalFalhas());
        assertTrue(resultado.getErros().get(0).contains("email"));
    }

    @Test
    void deveRejeitarSenhaComMenosDeOitoCaracteres() {
        String csv = """
                name,email,password
                Ana,ana@gmail.com,123
                """;

        MockMultipartFile arquivo = criarArquivo(csv);

        ImportacaoUsuarioResponseDTO resultado =
                importarUsuariosCsvService.importar(arquivo);

        assertEquals(1, resultado.getTotalFalhas());
        assertTrue(resultado.getErros().get(0).contains("senha"));
    }

    @Test
    void deveIgnorarLinhasEmBranco() {
        String csv = """
                name,email,password

                Ana,ana@gmail.com,senha1234

                """;

        MockMultipartFile arquivo = criarArquivo(csv);

        ImportacaoUsuarioResponseDTO resultado =
                importarUsuariosCsvService.importar(arquivo);

        assertEquals(1, resultado.getTotalSucesso());
        assertEquals(0, resultado.getTotalFalhas());

        verify(createUsuarioUseCase, times(1))
                .executar(any());
    }

    @Test
    void deveAceitarCabecalhoComBOM() {
        String csv = "\uFEFFname,email,password\n"
                + "Ana,ana@gmail.com,senha1234";

        MockMultipartFile arquivo = criarArquivo(csv);

        ImportacaoUsuarioResponseDTO resultado =
                importarUsuariosCsvService.importar(arquivo);

        assertEquals(1, resultado.getTotalSucesso());
        assertEquals(0, resultado.getTotalFalhas());
    }

    private MockMultipartFile criarArquivo(String conteudo) {
        return new MockMultipartFile(
                "arquivo",
                "usuarios.csv",
                "text/csv",
                conteudo.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
    }
}