package com.projeto.emprestimo.model;

import com.projeto.emprestimo.exception.ConflitoException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EquipamentoTest {

    @Test
    void emprestarEquipamentoDisponivel() {
        Equipamento equipamento = new Equipamento();
        equipamento.emprestar();
        assertFalse(equipamento.isDisponivel());
    }

    @Test
    void emprestarEquipamentoIndisponivel() {
        Equipamento equipamento = new Equipamento();
        equipamento.setDisponivel(false);
        assertThrows(ConflitoException.class, () -> equipamento.emprestar());
    }

    @Test
    void devolverEquipamento() {
        Equipamento equipamento = new Equipamento();
        equipamento.setDisponivel(false);
        equipamento.devolver();
        assertTrue(equipamento.isDisponivel());
    }
}