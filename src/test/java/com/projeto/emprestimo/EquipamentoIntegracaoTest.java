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
class EquipamentoIntegracaoTest {

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

    @Test
    void deveCadastrarEquipamento() throws Exception {
        String patrimonio = "INT-" + UUID.randomUUID();

        mockMvc.perform(post("/equipamentos")
                    .header("Authorization", "Bearer " + tokenAdmin())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "patrimonio": "%s",
                          "nome": "Notebook de integração",
                          "tipo": "NOTEBOOK"
                        }
                        """.formatted(patrimonio)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.patrimonio").value(patrimonio))
            .andExpect(jsonPath("$.disponivel").value(true)
            )
        ;
    }

    @Test
    void deveRetornar404ParaEquipamentoInexistente() throws Exception {
        mockMvc.perform(get("/equipamentos/999999999")
                .header("Authorization", "Bearer " + tokenAdmin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists()
                )
        ;
    }
}