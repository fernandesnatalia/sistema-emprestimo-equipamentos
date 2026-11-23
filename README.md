# Sistema de Empréstimo de Equipamentos

API REST desenvolvida em **Java 25 e Spring Boot** para gerenciamento de empréstimos e devoluções de equipamentos corporativos.

O sistema implementa regras de negócio para controle de disponibilidade, prevenção de empréstimos irregulares e acompanhamento de pendências, com autenticação JWT e autorização por perfis de acesso.

## Tecnologias utilizadas

- Java 25
- Spring Boot 4.1.2
- Spring Web MVC
- Spring Data JPA / Hibernate
- Spring Security
- Spring Boot Actuator
- JWT (JSON Web Token)
- H2 Database
- Bean Validation
- JUnit 5 / Spring Boot Test
- Maven
- Swagger / OpenAPI
- Docker
- MockMvc
- GitHub Actions

## Funcionalidades

- Gestão de equipamentos: cadastro, consulta, listagem com filtros, atualização e exclusão, respeitando as restrições de integridade e o histórico de empréstimos.
- Gestão de colaboradores: cadastro, consulta, listagem e acesso ao histórico individual de empréstimos.
- Gestão de empréstimos: registro de retiradas e devoluções, consulta de operações e acompanhamento da situação dos empréstimos.
- Controle de disponibilidade: atualização automática do estado dos equipamentos conforme as operações realizadas.
- Validação de operações: prevenção de empréstimos de equipamentos indisponíveis, devoluções duplicadas e novas retiradas por colaboradores com pendências vencidas.
- Autenticação e autorização: emissão de tokens JWT, proteção de endpoints e controle de permissões pelos perfis `ADMIN` e `COLABORADOR`.
- Documentação interativa: exploração e teste dos endpoints por meio do Swagger UI.
- Validação e tratamento de erros: verificação dos dados recebidos e respostas HTTP adequadas às situações de erro.

## Regras de negócio

| Regra | Descrição |
|---|---|
| R1 | O patrimônio de cada equipamento deve ser único. |
| R2 | Todo equipamento novo é cadastrado como disponível. |
| R3 | O e-mail de cada colaborador deve ser único. |
| R4 | Apenas equipamentos disponíveis podem ser emprestados. |
| R5 | O empréstimo torna o equipamento indisponível e estabelece prazo de 14 dias. |
| R6 | Um empréstimo não pode ser devolvido duas vezes. |
| R7 | A devolução registra a data e libera o equipamento. |
| R8 | Empréstimos não devolvidos após o prazo são considerados atrasados. |
| R9 | Equipamentos com histórico de empréstimos não podem ser excluídos. |
| R10 | Colaboradores com empréstimos atrasados não podem realizar novos empréstimos. |

## Endpoints principais

### Equipamentos

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/equipamentos` | Listar e filtrar equipamentos |
| GET | `/equipamentos/{id}` | Consultar equipamento |
| POST | `/equipamentos` | Cadastrar equipamento |
| PUT | `/equipamentos/{id}` | Atualizar equipamento |
| DELETE | `/equipamentos/{id}` | Excluir equipamento |

### Colaboradores

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/colaboradores` | Listar colaboradores |
| GET | `/colaboradores/{id}` | Consultar colaborador |
| POST | `/colaboradores` | Cadastrar colaborador |
| GET | `/colaboradores/{id}/emprestimos` | Consultar histórico |

### Empréstimos

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/emprestimos` | Listar empréstimos |
| GET | `/emprestimos/{id}` | Consultar empréstimo |
| POST | `/emprestimos` | Registrar empréstimo |
| PUT | `/emprestimos/{id}/devolucao` | Registrar devolução |

### Autenticação

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/auth/login` | Autenticar usuário e obter JWT |

As permissões de acesso variam conforme o perfil autenticado.

## Executando localmente

**Pré-requisitos:**
- JDK 25
- Apache Maven 3.9.9 ou superior, na série 3.9.x

O projeto utiliza banco de dados H2, dispensando a instalação de um servidor de banco de dados externo.

Clone o repositório:

```bash
git clone https://github.com/fernandesnatalia/sistema-emprestimo-equipamentos
cd sistema-emprestimo-equipamentos
```

### Configuração das variáveis de ambiente

A aplicação precisa das seguintes variáveis:

| Variável | Finalidade |
|---|---|
| `JWT_SEGREDO` | Chave de assinatura dos tokens JWT |
| `ADMIN_USUARIO` | Usuário administrador |
| `ADMIN_SENHA` | Senha do administrador |
| `COLAB_USUARIO` | Usuário colaborador |
| `COLAB_SENHA` | Senha do colaborador |
| `H2_CONSOLE_ENABLED` | Habilitar console H2 localmente (opcional) |
| `JPA_SHOW_SQL` | Exibir comandos SQL (opcional) |

1. Gere a chave JWT executando no terminal:

```bash
openssl rand -base64 32
```

Copie a chave gerada.

2. Configure as variáveis no IntelliJ IDEA:

Em `Run → Edit Configurations`, no campo `Environment variables`, insira:

```text
JWT_SEGREDO={chave-gerada};ADMIN_USUARIO=admin;ADMIN_SENHA=admin123;COLAB_USUARIO=colaborador;COLAB_SENHA=colab123
```
Substitua `{chave-gerada}` pela chave gerada no passo anterior. Os nomes de usuário e as senhas são exemplos para execução local e podem ser alterados.


A aplicação estará disponível em:

`http://localhost:8081`

**Swagger UI:** `http://localhost:8081/swagger-ui/index.html`

**H2 Console:** `http://localhost:8081/h2-console` (quando habilitado).

## Autenticação e autorização

A API utiliza JWT para proteger os endpoints.

Os perfis disponíveis são:

- **ADMIN:** acesso às operações administrativas e de empréstimo.
- **COLABORADOR:** acesso às consultas e operações de empréstimo e devolução autorizadas.

Para acessar os endpoints protegidos, realize login em `/auth/login` e envie o token no cabeçalho:

```http
Authorization: Bearer SEU_TOKEN
```

## Testes

O projeto contém testes automatizados com JUnit e Spring Boot Test.

Execute:

```bash
mvn test
```
### Testes automatizados

A suíte utiliza JUnit 5 e Spring Boot Test, incluindo testes de integração com `@SpringBootTest` e `@AutoConfigureMockMvc`.

Os testes de integração verificam:

- Cadastro de equipamentos.
- Consulta de equipamento inexistente (HTTP 404).
- Empréstimo e devolução de equipamento.
- Bloqueio de empréstimo de equipamento indisponível (HTTP 409).
- Persistência e serialização dos relacionamentos `@ManyToOne`.

Os testes utilizam H2 em memória, com configurações próprias em `src/test/resources/application.properties`.

### Testes com HTTP Client do IntelliJ IDEA

O projeto utiliza o **HTTP Client do IntelliJ IDEA** para executar requisições REST diretamente pela IDE, sem necessidade de ferramentas externas como Postman.

O arquivo `requests.http` contém requisições para testar localmente os endpoints, as regras de negócio, a validação de dados e a autenticação JWT, incluindo cenários de sucesso e respostas HTTP 400, 401, 403, 404 e 409.

Para executar os testes:

1. Inicie a aplicação localmente ou utilize a API publicada no Render.
2. Abra o arquivo `requests.http` no IntelliJ IDEA.
3. Execute as requisições pelo ícone **Run** ao lado de cada uma.
4. Consulte o código HTTP e o corpo da resposta exibidos pela IDE.

As requisições autenticadas utilizam o token JWT obtido pelo endpoint `/auth/login`, enviado no cabeçalho `Authorization: Bearer`.

### Integração contínua - GitHub Actions

O projeto utiliza GitHub Actions para executar automaticamente os testes a cada `push` ou `pull request`.

O workflow está definido em `.github/workflows/ci.yml` e realiza:

- Checkout do repositório.
- Configuração do Java 25 (Temurin).
- Configuração do cache do Maven.
- Execução dos testes automatizados com `mvn -B test`.

A execução pode ser acompanhada na aba [Actions](https://github.com/fernandesnatalia/sistema-emprestimo-equipamentos/actions) do GitHub.

## Estrutura do projeto

```text
.github/
src/
├── main/
│   ├── java/com/projeto/emprestimo/
│   │   ├── EmprestimoApplication.java
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── exception/
│   │   ├── model/
│   │   ├── repository/
│   │   ├── security/
│   │   └── service/
│   └── resources/
│       └── application.properties
└── test/
    ├── java/com/projeto/emprestimo/
    └── resources/
        └── application.properties

Dockerfile
pom.xml
README.md
requests.http
.gitignore
```

O projeto segue uma arquitetura em camadas, separando controladores REST, serviços responsáveis pelas regras de negócio, repositórios de persistência, entidades JPA e componentes de segurança JWT. Os testes automatizados ficam em `src/test`.

## Banco de dados

O projeto utiliza **H2 Database** em dois modos:

- **Desenvolvimento:** banco persistente em arquivo, configurado em `src/main/resources/application.properties`, com URL `jdbc:h2:file:./dados/emprestimo`.
- **Testes automatizados:** banco em memória, configurado em `src/test/resources/application.properties`, com URL `jdbc:h2:mem:emprestimo_test;DB_CLOSE_DELAY=-1`.

O H2 Console pode ser habilitado localmente pela variável `H2_CONSOLE_ENABLED=true`.

### Relacionamentos JPA

A entidade `Emprestimo` possui dois relacionamentos `@ManyToOne`:

- **N:1 com `Equipamento`:** um equipamento pode possuir vários empréstimos registrados ao longo do tempo.
- **N:1 com `Colaborador`:** um colaborador pode possuir vários empréstimos registrados.

Cada empréstimo referencia um equipamento e um colaborador. Os relacionamentos são persistidos por meio de chaves estrangeiras e os objetos relacionados são incluídos nas respostas JSON.

## Deploy

A API está publicada no **Render**, utilizando Docker para construção e execução da aplicação.

As configurações sensíveis são fornecidas por variáveis de ambiente. O banco de dados e a persistência devem ser configurados de acordo com o ambiente de hospedagem.

- **API REST:** [Acessar aplicação](https://sistema-emprestimo-equipamentos-r5rp.onrender.com)
- **Swagger UI:** [Acessar documentação interativa e testar os endpoints](https://sistema-emprestimo-equipamentos-r5rp.onrender.com/swagger-ui/index.html)
- **OpenAPI JSON:** [Visualizar especificação da API em formato JSON](https://sistema-emprestimo-equipamentos-r5rp.onrender.com/v3/api-docs)
- **Health Check:** [Consultar status da aplicação](https://sistema-emprestimo-equipamentos-r5rp.onrender.com/actuator/health)

---

### Configuração do ambiente

O repositório contém um `Dockerfile` utilizado para construir a imagem da aplicação e iniciar o servidor Spring Boot.

No Render, as configurações de autenticação são fornecidas por variáveis de ambiente, incluindo o segredo JWT e as credenciais dos perfis ADMIN e COLABORADOR.

A porta de execução é definida pela variável `PORT`, com valor padrão `8081` quando não informada.

A aplicação utiliza H2 em arquivo. Como o armazenamento local de serviços hospedados pode ser temporário, a persistência dos registros entre reinicializações depende da configuração de armazenamento do ambiente.

**Projeto desenvolvido para o ElasTech - Sistema de Empréstimo de Equipamentos.**