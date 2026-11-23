package com.projeto.emprestimo.model;

import com.projeto.emprestimo.exception.ConflitoException;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class EmprestimoTest {

    @Test
    void emprestimoComDataDeDevolucao() {
        LocalDate hoje = LocalDate.of(2026, 11, 14);

        Emprestimo emprestimo = new Emprestimo(1L, 1L, hoje);

        assertEquals(hoje, emprestimo.getDataEmprestimo());
        assertEquals(LocalDate.of(2026, 11, 28), emprestimo.getDataPrevistaDevolucao());
    }

    @Test
    void devolverEmprestimoAberto() {
        Emprestimo emprestimo = new Emprestimo(1L, 1L, LocalDate.of(2026, 11, 14));

        emprestimo.devolver(LocalDate.of(2026, 11, 20));

        assertEquals(LocalDate.of(2026, 11, 20), emprestimo.getDataDevolucao());
    }

    @Test
    void devolverDuasVezes() {
        Emprestimo emprestimo = new Emprestimo(1L, 1L, LocalDate.of(2026, 11, 14));

        emprestimo.devolver(LocalDate.of(2026, 11, 20));

        assertThrows(ConflitoException.class, () -> emprestimo.devolver(LocalDate.of(2026, 11, 21)));
    }

    @Test
    void emprestimoVencidoENaoDevolvidoEstaAtrasado() {
        Emprestimo emprestimo = new Emprestimo(1L, 1L, LocalDate.of(2026, 10, 27));

        assertTrue(emprestimo.estaAtrasadoEm(LocalDate.of(2026, 11, 14)));
    }

    @Test
    void emprestimoDentroDoPrazoNaoEstaAtrasado() {
        Emprestimo emprestimo = new Emprestimo(1L, 1L, LocalDate.of(2026, 11, 6));

        assertFalse(emprestimo.estaAtrasadoEm(LocalDate.of(2026, 11, 14)));
    }

    @Test
    void emprestimoDevolvidoNaoEstaAtrasado() {
        Emprestimo emprestimo = new Emprestimo(1L, 1L, LocalDate.of(2026, 10, 27));

        emprestimo.devolver(LocalDate.of(2026, 11, 12));

        assertFalse(emprestimo.estaAtrasadoEm(LocalDate.of(2026, 11, 14)));
    }

    @Test
    void emprestimoNoDiaDoPrazoNaoEstaAtrasado() {
        Emprestimo emprestimo = new Emprestimo(1L, 1L, LocalDate.of(2026, 10, 31));

        assertFalse(emprestimo.estaAtrasadoEm(LocalDate.of(2026, 11, 14)));
    }
}