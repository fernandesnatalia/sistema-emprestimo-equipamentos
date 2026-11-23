const formLogin = document.getElementById("form-login");
const mensagemLogin = document.getElementById("mensagem-login");
const botaoLogin = document.getElementById("botao-login");
const botaoSair = document.getElementById("botao-sair");
const telaLogin = document.getElementById("tela-login");
const telaSistema = document.getElementById("tela-sistema");
const perfilUsuario = document.getElementById("perfil-usuario");

let token = null;

formLogin.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    const usuario = document.getElementById("usuario").value.trim();
    const senha = document.getElementById("senha").value;

    mensagemLogin.textContent = "";
    botaoLogin.disabled = true;
    botaoLogin.textContent = "Entrando...";

    try {
        const resposta = await fetch("/auth/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ usuario, senha })
        });

        if (!resposta.ok) {
            if (resposta.status === 401) {
                throw new Error("Usuário ou senha inválidos.");
            }
            throw new Error("Não foi possível realizar o login.");
        }

        const dados = await resposta.json();

        if (!dados.token) {
            throw new Error("Token não recebido da API.");
        }

        token = dados.token;

        const perfil = obterPerfilJwt(token);

        const admin = perfil === "ADMIN";

        document.getElementById("secao-colaboradores").classList.toggle("oculto", !admin);
        document.getElementById("secao-emprestimos").classList.toggle("oculto", !admin);

        document.getElementById("area-admin").classList.toggle("oculto", perfil !== "ADMIN");
        document.getElementById("area-admin-colaboradores").classList.toggle("oculto", perfil !== "ADMIN");

        await carregarEquipamentos();

        if (admin) {
            await Promise.all([
                carregarColaboradores(),
                carregarEmprestimos(),
                carregarOpcoesEmprestimo()
            ]);
        }

        perfilUsuario.textContent = `Usuário: ${usuario}`;
        telaLogin.classList.add("oculto");
        telaSistema.classList.remove("oculto");

        formLogin.reset();

    } catch (erro) {
        mensagemLogin.textContent = erro.message;
    } finally {
        botaoLogin.disabled = false;
        botaoLogin.textContent = "Entrar";
    }
});

botaoSair.addEventListener("click", () => {
    token = null;

    document.getElementById("lista-equipamentos").replaceChildren();
    document.getElementById("mensagem-equipamentos").textContent = "";
    document.getElementById("area-admin").classList.add("oculto");

    telaSistema.classList.add("oculto");
    telaLogin.classList.remove("oculto");

    perfilUsuario.textContent = "";
    mensagemLogin.textContent = "";

    document.getElementById("lista-colaboradores").replaceChildren();
    document.getElementById("mensagem-colaboradores").textContent = "";
    document.getElementById("area-admin-colaboradores").classList.add("oculto");
    document.getElementById("historico-colaborador").classList.add("oculto");

    document.getElementById("lista-historico").replaceChildren();
    document.getElementById("lista-emprestimos").replaceChildren();

    document.getElementById("mensagem-emprestimos").textContent = "";

    document.getElementById("emprestimo-equipamento").replaceChildren();
    document.getElementById("emprestimo-colaborador").replaceChildren();

    document.getElementById("form-emprestimo").reset();
    document.getElementById("filtro-atrasados").value = "";
});

const listaEquipamentos = document.getElementById("lista-equipamentos");
const mensagemEquipamentos = document.getElementById("mensagem-equipamentos");
const formEquipamento = document.getElementById("form-equipamento");

function obterPerfilJwt(jwt) {
    try {
        const parte = jwt.split(".")[1];
        const base64 = parte.replace(/-/g, "+").replace(/_/g, "/");
        const dados = JSON.parse(atob(base64));
        return dados.perfil;
    } catch {
        return null;
    }
}

function usuarioAdmin() {
    return token !== null && obterPerfilJwt(token) === "ADMIN";
}

function mostrarMensagem(texto, sucesso = false) {
    mensagemEquipamentos.textContent = texto;
    mensagemEquipamentos.classList.toggle("sucesso", sucesso);
}

async function requisicaoApi(caminho, opcoes = {}) {
    const resposta = await fetch(caminho, {
        ...opcoes,
        headers: {
            ...opcoes.headers,
            Authorization: `Bearer ${token}`
        }
    });

    if (!resposta.ok) {
        let mensagem = `Erro HTTP ${resposta.status}`;

        try {
            const erro = await resposta.json();
            mensagem = erro.message || mensagem;
        } catch {
            // Mantém a mensagem HTTP.
        }

        throw new Error(mensagem);
    }

    if (resposta.status === 204) {
        return null;
    }

    return resposta.json();
}

async function carregarEquipamentos() {
    mostrarMensagem("Carregando equipamentos...");

    const tipo = document.getElementById("filtro-tipo").value;
    const disponivel =
        document.getElementById("filtro-disponibilidade").value;

    const parametros = new URLSearchParams();

    if (tipo) {
        parametros.set("tipo", tipo);
    }

    if (disponivel !== "") {
        parametros.set("disponivel", disponivel);
    }

    try {
        const caminho = "/equipamentos?" + parametros.toString();
        const equipamentos = await requisicaoApi(caminho);

        listaEquipamentos.replaceChildren();

        for (const equipamento of equipamentos) {
            const linha = document.createElement("tr");

            const valores = [
                equipamento.id,
                equipamento.patrimonio,
                equipamento.nome,
                equipamento.tipo,
                equipamento.disponivel ? "Disponível" : "Emprestado"
            ];

            for (const valor of valores) {
                const celula = document.createElement("td");
                celula.textContent = String(valor);
                linha.appendChild(celula);
            }

            const celulaAcoes = document.createElement("td");

            if (usuarioAdmin()) {
                const acoes = document.createElement("div");
                acoes.className = "acoes";

                const editar = document.createElement("button");
                editar.type = "button";
                editar.textContent = "Editar";
                editar.className = "secundario";
                editar.addEventListener("click", () => editarEquipamento(equipamento));

                const excluir = document.createElement("button");
                excluir.type = "button";
                excluir.textContent = "Excluir";
                excluir.className = "botao-excluir";
                excluir.addEventListener("click", () => excluirEquipamento(equipamento.id));

                acoes.append(editar, excluir);
                celulaAcoes.appendChild(acoes);
            } else {
                celulaAcoes.textContent = "Somente consulta";
            }

            linha.appendChild(celulaAcoes);
            listaEquipamentos.appendChild(linha);
        }

        mostrarMensagem(
            `${equipamentos.length} equipamento(s) encontrado(s).`,
            true
        );

    } catch (erro) {
        mostrarMensagem(erro.message);
    }
}

formEquipamento.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    if (!usuarioAdmin()) return;

    const equipamento = {
        patrimonio: document.getElementById("equipamento-patrimonio").value.trim(),
        nome: document.getElementById("equipamento-nome").value.trim(),
        tipo: document.getElementById("equipamento-tipo").value
    };

    const botaoSalvarEquipamento = document.getElementById("salvar-equipamento");

    botaoSalvarEquipamento.disabled = true;

    try {
        await requisicaoApi("/equipamentos", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(equipamento)
        });

        formEquipamento.reset();
        await carregarEquipamentos();
        mostrarMensagem("Equipamento cadastrado com sucesso.", true);
    } catch (erro) {
        mostrarMensagem(
            erro.message.includes("Unique index")
                ? "Patrimônio já cadastrado."
                : erro.message
        );
    } finally {
        botaoSalvarEquipamento.disabled = false;
    }
});

async function editarEquipamento(equipamento) {
    if (!usuarioAdmin()) return;

    const nome = prompt("Nome do equipamento:", equipamento.nome);
    if (nome === null) return;

    const tipo = prompt("Tipo (NOTEBOOK, MONITOR, MOUSE, TECLADO, HEADSET, OUTRO):", equipamento.tipo);
    if (tipo === null) return;

    const tiposValidos = [
        "NOTEBOOK", "MONITOR", "MOUSE", "TECLADO", "HEADSET", "OUTRO"
    ];

    if (!nome.trim() || !tiposValidos.includes(tipo.trim().toUpperCase())) {
        mostrarMensagem("Nome ou tipo inválido.");
        return;
    }

    try {
        await requisicaoApi(`/equipamentos/${equipamento.id}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                patrimonio: equipamento.patrimonio,
                nome: nome.trim(),
                tipo: tipo.trim().toUpperCase()
            })
        });

        await carregarEquipamentos();
        mostrarMensagem("Equipamento atualizado com sucesso.", true);

    } catch (erro) {
        mostrarMensagem(erro.message);
    }
}

async function excluirEquipamento(id) {
    if (!usuarioAdmin()) return;

    if (!confirm(`Deseja excluir o equipamento ${id}?`)) {
        return;
    }

    try {
        await requisicaoApi(`/equipamentos/${id}`, {
            method: "DELETE"
        });

        await carregarEquipamentos();
        mostrarMensagem("Equipamento excluído com sucesso.", true);

    } catch (erro) {
        mostrarMensagem(erro.message);
    }
}

document.getElementById("atualizar-equipamentos").addEventListener("click", carregarEquipamentos);
document.getElementById("filtro-tipo").addEventListener("change", carregarEquipamentos);
document.getElementById("filtro-disponibilidade").addEventListener("change", carregarEquipamentos);

/* Gestão de colaboradores */

const listaColaboradores = document.getElementById("lista-colaboradores");
const mensagemColaboradores = document.getElementById("mensagem-colaboradores");
const formColaborador = document.getElementById("form-colaborador");

function mostrarMensagemColaboradores(texto, sucesso = false) {
    mensagemColaboradores.textContent = texto;
    mensagemColaboradores.classList.toggle("sucesso", sucesso);
}

function criarCelula(linha, valor) {
    const celula = document.createElement("td");
    celula.textContent = valor == null ? "-" : String(valor);
    linha.appendChild(celula);
    return celula;
}

async function carregarColaboradores() {
    mostrarMensagemColaboradores("Carregando colaboradores...");

    try {
        const colaboradores = await requisicaoApi("/colaboradores");

        listaColaboradores.replaceChildren();

        for (const colaborador of colaboradores) {
            const linha = document.createElement("tr");

            criarCelula(linha, colaborador.id);
            criarCelula(linha, colaborador.nome);
            criarCelula(linha, colaborador.email);
            criarCelula(linha, colaborador.cargo);

            const acoes = document.createElement("td");

            const botaoHistorico = document.createElement("button");
            botaoHistorico.type = "button";
            botaoHistorico.className = "secundario";
            botaoHistorico.textContent = "Empréstimos";

            botaoHistorico.addEventListener("click", () => {
                carregarHistoricoColaborador(colaborador);
            });

            acoes.appendChild(botaoHistorico);
            linha.appendChild(acoes);
            listaColaboradores.appendChild(linha);
        }

        mostrarMensagemColaboradores(
            `${colaboradores.length} colaborador(es) encontrado(s).`,
            true
        );

    } catch (erro) {
        mostrarMensagemColaboradores(erro.message);
    }
}

formColaborador.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    if (!usuarioAdmin()) return;

    const colaborador = {
        nome: document.getElementById("colaborador-nome").value.trim(),
        email: document.getElementById("colaborador-email").value.trim(),
        cargo: document.getElementById("colaborador-cargo").value.trim()
    };

    const botaoSalvar =
        document.getElementById("salvar-colaborador");

    botaoSalvar.disabled = true;

    try {
        await requisicaoApi("/colaboradores", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(colaborador)
        });

        formColaborador.reset();

        await carregarColaboradores();

        mostrarMensagemColaboradores("Colaborador cadastrado com sucesso.", true);

    } catch (erro) {
        mostrarMensagemColaboradores(erro.message);
    } finally {
        botaoSalvar.disabled = false;
    }
});

async function carregarHistoricoColaborador(colaborador) {
    const painel = document.getElementById("historico-colaborador");
    const titulo = document.getElementById("titulo-historico");
    const mensagem = document.getElementById("mensagem-historico");
    const lista = document.getElementById("lista-historico");

    painel.classList.remove("oculto");
    titulo.textContent = `Empréstimos de ${colaborador.nome}`;
    mensagem.textContent = "Carregando histórico...";
    lista.replaceChildren();

    try {
        const emprestimos = await requisicaoApi(
            `/colaboradores/${colaborador.id}/emprestimos`
        );

        for (const emprestimo of emprestimos) {
            const linha = document.createElement("tr");

            criarCelula(linha, emprestimo.id);
            criarCelula(linha, emprestimo.equipamento?.nome ?? emprestimo.equipamentoId);
            criarCelula(linha, emprestimo.dataEmprestimo);
            criarCelula(linha, emprestimo.dataPrevistaDevolucao);
            criarCelula(linha, emprestimo.dataDevolucao ?? "Pendente");

            lista.appendChild(linha);
        }

        mensagem.textContent = emprestimos.length === 0
            ? "Nenhum empréstimo registrado."
            : emprestimos.length === 1
                ? "1 empréstimo encontrado."
                : `${emprestimos.length} empréstimos encontrados.`;

    } catch (erro) {
        mensagem.textContent = erro.message;
    }
}

document.getElementById("atualizar-colaboradores").addEventListener("click", carregarColaboradores);
document.getElementById("fechar-historico").addEventListener("click", () => {
    document.getElementById("historico-colaborador").classList.add("oculto");
});

/* Gestão de empréstimos */

const listaEmprestimos = document.getElementById("lista-emprestimos");
const mensagemEmprestimos = document.getElementById("mensagem-emprestimos");
const formEmprestimo = document.getElementById("form-emprestimo");

function mostrarMensagemEmprestimos(texto, sucesso = false) {
    mensagemEmprestimos.textContent = texto;
    mensagemEmprestimos.classList.toggle("sucesso", sucesso);
}

function formatarData(data) {
    if (!data) return "-";

    const partes = data.split("-");
    if (partes.length !== 3) return data;

    return `${partes[2]}/${partes[1]}/${partes[0]}`;
}

function situacaoEmprestimo(emprestimo) {
    if (emprestimo.dataDevolucao) {
        return {
            texto: "Devolvido",
            classe: "situacao-devolvido"
        };
    }

    if (emprestimo.atrasado) {
        return {
            texto: "Atrasado",
            classe: "situacao-atrasado"
        };
    }

    return {
        texto: "Em aberto",
        classe: "situacao-aberto"
    };
}

async function carregarEmprestimos() {
    mostrarMensagemEmprestimos("Carregando empréstimos...");

    const somenteAtrasados = document.getElementById("filtro-atrasados").value === "true";

    const caminho = somenteAtrasados
        ? "/emprestimos?atrasados=true"
        : "/emprestimos";

    try {
        const emprestimos = await requisicaoApi(caminho);

        listaEmprestimos.replaceChildren();

        for (const emprestimo of emprestimos) {
            const linha = document.createElement("tr");

            criarCelula(linha, emprestimo.id);
            criarCelula(linha, emprestimo.equipamento?.nome ?? emprestimo.equipamentoId);
            criarCelula(linha, emprestimo.colaborador?.nome ?? emprestimo.colaboradorId);

            criarCelula(linha, formatarData(emprestimo.dataEmprestimo));
            criarCelula(linha, formatarData(emprestimo.dataPrevistaDevolucao));
            criarCelula(linha, formatarData(emprestimo.dataDevolucao));

            const celulaSituacao = document.createElement("td");
            const indicador = document.createElement("span");
            const situacao = situacaoEmprestimo(emprestimo);

            indicador.className = `situacao ${situacao.classe}`;
            indicador.textContent = situacao.texto;

            celulaSituacao.appendChild(indicador);
            linha.appendChild(celulaSituacao);

            const celulaAcoes = document.createElement("td");

            if (!emprestimo.dataDevolucao) {
                const botaoDevolver = document.createElement("button");

                botaoDevolver.type = "button";
                botaoDevolver.className = "secundario";
                botaoDevolver.textContent = "Devolver";

                botaoDevolver.addEventListener("click", () => {
                    devolverEmprestimo(emprestimo.id);
                });

                celulaAcoes.appendChild(botaoDevolver);
            } else {
                celulaAcoes.textContent = "-";
            }

            linha.appendChild(celulaAcoes);
            listaEmprestimos.appendChild(linha);
        }

        mostrarMensagemEmprestimos(`${emprestimos.length} empréstimo(s) encontrado(s).`, true);

    } catch (erro) {
        mostrarMensagemEmprestimos(erro.message);
    }
}

function preencherSelect(select, registros, texto, rotulo) {
    select.replaceChildren();

    const primeiraOpcao = document.createElement("option");
    primeiraOpcao.value = "";
    primeiraOpcao.textContent = texto;

    select.appendChild(primeiraOpcao);

    for (const registro of registros) {
        const opcao = document.createElement("option");
        opcao.value = String(registro.id);
        opcao.textContent = rotulo(registro);

        select.appendChild(opcao);
    }
}

async function carregarOpcoesEmprestimo() {
    const selectEquipamento = document.getElementById("emprestimo-equipamento");
    const selectColaborador = document.getElementById("emprestimo-colaborador");

    try {
        const [equipamentos, colaboradores] =
            await Promise.all([
                requisicaoApi("/equipamentos?disponivel=true"), requisicaoApi("/colaboradores")
            ]);

        preencherSelect(
            selectEquipamento,
            equipamentos,
            "Selecione um equipamento",
            equipamento => `${equipamento.patrimonio} - ${equipamento.nome}`
        );

        preencherSelect(
            selectColaborador,
            colaboradores,
            "Selecione um colaborador",
            colaborador => `${colaborador.nome} - ${colaborador.email}`
        );

    } catch (erro) {
        mostrarMensagemEmprestimos(`Erro ao carregar opções: ${erro.message}`);
    }
}

formEmprestimo.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    const equipamentoId = document.getElementById("emprestimo-equipamento").value;
    const colaboradorId = document.getElementById("emprestimo-colaborador").value;

    if (!equipamentoId || !colaboradorId) {
        mostrarMensagemEmprestimos("Selecione um equipamento e um colaborador.");
        return;
    }

    const botaoSalvar = document.getElementById("salvar-emprestimo");

    botaoSalvar.disabled = true;

    try {
        const parametros = new URLSearchParams({equipamentoId, colaboradorId});

        await requisicaoApi(`/emprestimos?${parametros}`, {
            method: "POST"
        });

        formEmprestimo.reset();

        await Promise.all([
            carregarEmprestimos(),
            carregarEquipamentos(),
            carregarOpcoesEmprestimo()
        ]);

        mostrarMensagemEmprestimos(
            "Empréstimo registrado com sucesso.",
            true
        );

    } catch (erro) {
        mostrarMensagemEmprestimos(erro.message);
    } finally {
        botaoSalvar.disabled = false;
    }
});

async function devolverEmprestimo(id) {
    if (!confirm(`Confirmar devolução do empréstimo ${id}?`)) {
        return;
    }

    try {
        await requisicaoApi(`/emprestimos/${id}/devolucao`, {
            method: "PUT"
        });

        await Promise.all([
            carregarEmprestimos(),
            carregarEquipamentos(),
            carregarOpcoesEmprestimo()
        ]);

        mostrarMensagemEmprestimos("Devolução registrada com sucesso.", true);

    } catch (erro) {
        mostrarMensagemEmprestimos(erro.message);
    }
}

document.getElementById("atualizar-emprestimos").addEventListener("click", async () => {
    await Promise.all([carregarEmprestimos(), carregarOpcoesEmprestimo()]);
});

document.getElementById("filtro-atrasados").addEventListener("change", carregarEmprestimos);