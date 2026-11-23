# Sistema de Empréstimo de Equipamentos

API REST desenvolvida em **Java 25 e Spring Boot** para gerenciamento de empréstimos e devoluções de equipamentos corporativos.

## Tecnologias utilizadas

- Java 25
- Spring Boot 4.1.2
- Spring Web MVC
- Spring Data JPA / Hibernate
- Spring Security
- JWT (JSON Web Token)
- H2 Database
- Bean Validation
- JUnit
- Maven
- Swagger / OpenAPI
- Docker

## Funcionalidades

- Cadastro, consulta, atualização e exclusão de equipamentos.
- Cadastro e consulta de colaboradores.
- Registro de empréstimos e devoluções.
- Controle automático da disponibilidade dos equipamentos.
- Prazo de devolução de 14 dias.
- Identificação de empréstimos atrasados.
- Bloqueio de empréstimos para colaboradores com pendências.
- Histórico de empréstimos por colaborador.
- Autenticação JWT e autorização por perfis `ADMIN` e `COLABORADOR`.

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

## Executando localmente

**Pré-requisitos:**
- JDK 25
- Apache Maven 3.9.9 ou superior, na série 3.9.x
- Variáveis de ambiente configuradas para autenticação JWT

A aplicação utiliza H2 em arquivo para desenvolvimento local. Não é necessário instalar um servidor de banco de dados separado.


Clone o repositório:

```bash
git clone https://github.com/SEU_USUARIO/sistema-emprestimo-equipamentos.git
cd sistema-emprestimo-equipamentos
```

Configure as seguintes variáveis de ambiente:

| Variável | Finalidade |
|---|---|
| `JWT_SEGREDO` | Chave de assinatura dos tokens JWT |
| `ADMIN_USUARIO` | Usuário administrador |
| `ADMIN_SENHA` | Senha do administrador |
| `COLAB_USUARIO` | Usuário colaborador |
| `COLAB_SENHA` | Senha do colaborador |
| `H2_CONSOLE_ENABLED` | Habilitar console H2 localmente (opcional) |
| `JPA_SHOW_SQL` | Exibir comandos SQL (opcional) |

Execute:

```bash
mvn spring-boot:run
```

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

Também foram realizados testes manuais dos endpoints REST, das dez regras de negócio e dos controles de autenticação e autorização.

## Estrutura do projeto

```text
## Estrutura do projeto

```text
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
.gitignore
```

O projeto segue uma arquitetura em camadas, separando controladores REST, serviços responsáveis pelas regras de negócio, repositórios de persistência, entidades JPA e componentes de segurança JWT. Os testes automatizados ficam em `src/test`.

## Banco de dados

O projeto utiliza **H2 Database** em dois modos:

- **Desenvolvimento:** banco persistente em arquivo, configurado em `src/main/resources/application.properties`, com URL `jdbc:h2:file:./dados/emprestimo`.
- **Testes automatizados:** banco em memória, configurado em `src/test/resources/application.properties`, com URL `jdbc:h2:mem:emprestimo_test;DB_CLOSE_DELAY=-1`.

O H2 Console pode ser habilitado localmente pela variável `H2_CONSOLE_ENABLED=true`.

Os arquivos gerados pelo banco local são excluídos do versionamento pelo `.gitignore`.

## Deploy

O projeto possui Dockerfile para empacotamento e execução em contêiner.

As configurações sensíveis são fornecidas por variáveis de ambiente. O banco de dados e a persistência devem ser configurados de acordo com o ambiente de hospedagem.

---

**Projeto desenvolvido para o ElasTech — Sistema de Empréstimo de Equipamentos.**