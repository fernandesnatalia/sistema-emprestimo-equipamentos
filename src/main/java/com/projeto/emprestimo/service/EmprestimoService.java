package com.projeto.emprestimo.service;

import com.projeto.emprestimo.exception.ConflitoException;
import com.projeto.emprestimo.exception.NaoEncontradoException;
import com.projeto.emprestimo.model.Emprestimo;
import com.projeto.emprestimo.repository.EquipamentoRepository;
import com.projeto.emprestimo.repository.ColaboradorRepository;
import com.projeto.emprestimo.model.Equipamento;
import com.projeto.emprestimo.repository.EmprestimoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class EmprestimoService {

    private final EmprestimoRepository emprestimoRepository;
    private final EquipamentoRepository equipamentoRepository;
    private final ColaboradorRepository colaboradorRepository;

    public EmprestimoService(
            EmprestimoRepository emprestimoRepository,
            EquipamentoRepository equipamentoRepository,
            ColaboradorRepository colaboradorRepository
    ) {
        this.emprestimoRepository = emprestimoRepository;
        this.equipamentoRepository = equipamentoRepository;
        this.colaboradorRepository = colaboradorRepository;
    }


    public List<Emprestimo> listar(Boolean atrasados) {
        if (Boolean.TRUE.equals(atrasados)) {
            return emprestimoRepository.findByDataDevolucaoIsNullAndDataPrevistaDevolucaoBefore(LocalDate.now());
        }
        return emprestimoRepository.findAll();
    }

    public Emprestimo buscar(Long id) {
        return emprestimoRepository.findById(id).orElseThrow(
                () -> new NaoEncontradoException("Empréstimo não encontrado"));
    }


    public List<Emprestimo> listarDoColaborador(Long colaboradorId) {
        if (!colaboradorRepository.existsById(colaboradorId)) {
            throw new NaoEncontradoException("Colaborador não encontrado");
        }

        return emprestimoRepository.findByColaboradorId(colaboradorId);
    }

    @Transactional
    public Emprestimo emprestar(Long equipamentoId, Long colaboradorId) {

        Equipamento equipamento = equipamentoRepository
                .findById(equipamentoId)
                .orElseThrow(() -> new NaoEncontradoException("Equipamento não encontrado"));

        if (!colaboradorRepository.existsById(colaboradorId)) {
            throw new NaoEncontradoException("Colaborador não encontrado");
        }

        if (emprestimoRepository
                .existsByColaboradorIdAndDataDevolucaoIsNullAndDataPrevistaDevolucaoBefore(
                        colaboradorId, LocalDate.now())) {
            throw new ConflitoException("Colaborador possui empréstimo atrasado");
        }

        if (!equipamento.isDisponivel()) {
            throw new ConflitoException("Equipamento indisponível");
        }

        equipamento.emprestar();

        Emprestimo emprestimo = new Emprestimo(equipamentoId, colaboradorId, LocalDate.now());

        equipamentoRepository.save(equipamento);

        return emprestimoRepository.save(emprestimo);
    }


    @Transactional
    public Emprestimo devolver(Long id) {
        Emprestimo emprestimo = buscar(id);

        if (emprestimo.getDataDevolucao() != null) {
            throw new ConflitoException("Empréstimo já devolvido");
        }

        Equipamento equipamento = equipamentoRepository
                .findById(emprestimo.getEquipamentoId())
                .orElseThrow(() -> new NaoEncontradoException("Equipamento não encontrado"));

        emprestimo.devolver(LocalDate.now());
        equipamento.devolver();

        equipamentoRepository.save(equipamento);

        return emprestimoRepository.save(emprestimo);
    }
}