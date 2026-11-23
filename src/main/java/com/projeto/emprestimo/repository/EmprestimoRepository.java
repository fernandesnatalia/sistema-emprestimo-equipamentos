package com.projeto.emprestimo.repository;

import com.projeto.emprestimo.model.Emprestimo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    List<Emprestimo> findByColaborador_Id(Long colaboradorId);

    List<Emprestimo> findByDataDevolucaoIsNullAndDataPrevistaDevolucaoBefore(LocalDate data);

    boolean existsByEquipamento_Id(Long equipamentoId);

    boolean existsByColaborador_IdAndDataDevolucaoIsNullAndDataPrevistaDevolucaoBefore(
            Long colaboradorId, LocalDate data
    );
}