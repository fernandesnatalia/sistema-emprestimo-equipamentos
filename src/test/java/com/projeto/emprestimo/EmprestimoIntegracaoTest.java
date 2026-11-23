package com.projeto.emprestimo;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmprestimoIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    private String tokenAdmin() throws Exception {
        String resposta = mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "usuario": "admin",
                          "senha": "senha-admin-exclusiva-de-teste"
                        }
                        """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(resposta, "$.token");
    }

    private Long cadastrarEquipamento(String token) throws Exception {
        String patrimonio = "INT-" + UUID.randomUUID();

        String resposta = mockMvc.perform(post("/equipamentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "patrimonio": "%s",
                              "nome": "Notebook para empréstimo",
                              "tipo": "NOTEBOOK"
                            }
                            """.formatted(patrimonio)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number id = JsonPath.read(resposta, "$.id");

        return id.longValue();
    }

    private Long cadastrarColaborador(String token) throws Exception {
        String email = "integracao-" + UUID.randomUUID() + "@teste.com";

        String resposta = mockMvc.perform(post("/colaboradores")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "nome": "Colaborador de integração",
                              "email": "%s",
                              "cargo": "Analista"
                            }
                            """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number id = JsonPath.read(resposta, "$.id");

        return id.longValue();
    }

    private Long realizarEmprestimo(String token, Long equipamentoId, Long colaboradorId) throws Exception {

        String resposta = mockMvc.perform(post("/emprestimos")
                        .header("Authorization", "Bearer " + token)
                        .param("equipamentoId", equipamentoId.toString())
                        .param("colaboradorId", colaboradorId.toString()))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number id = JsonPath.read(resposta, "$.id");

        return id.longValue();
    }

    @Test
    void deveEmprestarEdevolverEquipamento() throws Exception {
        String token = tokenAdmin();
        Long equipamentoId = cadastrarEquipamento(token);
        Long colaboradorId = cadastrarColaborador(token);
        Long emprestimoId = realizarEmprestimo(token, equipamentoId, colaboradorId);

        mockMvc.perform(get("/equipamentos/{id}", equipamentoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponivel").value(false));

        mockMvc.perform(put("/emprestimos/{id}/devolucao", emprestimoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataDevolucao").exists());

        mockMvc.perform(get("/equipamentos/{id}", equipamentoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponivel").value(true));
    }

    @Test
    void deveRetornar409AoEmprestarEquipamentoIndisponivel() throws Exception {
        String token = tokenAdmin();
        Long equipamentoId = cadastrarEquipamento(token);
        Long colaboradorId = cadastrarColaborador(token);

        realizarEmprestimo(token, equipamentoId, colaboradorId);

        mockMvc.perform(post("/emprestimos")
                        .header("Authorization", "Bearer " + token)
                        .param("equipamentoId", equipamentoId.toString())
                        .param("colaboradorId", colaboradorId.toString()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }
}