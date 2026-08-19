# 📋 Gerenciador de Tarefas e Projetos - API RESTful

API RESTful completa desenvolvida em **Java 21** e **Spring Boot 3** para gerenciamento de projetos e tarefas, com autenticação JWT, controle de acesso baseado em papéis (*Role-Based Access Control*), filtros dinâmicos com paginação e regras de negócio corporativas (limite de WIP, transições de status e hierarquia de perfis).

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem**: Java 21 (LTS)
- **Framework**: Spring Boot 3.4+ / 4.x
- **Persistência**: Spring Data JPA & Hibernate
- **Banco de Dados**: H2 Database (em memória para execução imediata)
- **Segurança**: Spring Security & JWT (*java-jwt / Auth0*) com BCrypt
- **Validação**: Jakarta Bean Validation (Hibernate Validator)
- **Documentação Interativa**: SpringDoc OpenAPI 3 / Swagger UI
- **Testes**: JUnit 5, AssertJ, `@SpringBootTest` (Testes de Integração)
- **Produtividade**: Lombok & Java Records

---

## 🚀 Instruções para Rodar o Projeto

### 📌 Pré-requisitos
- **Java JDK 21** instalado e configurado no `PATH` / `JAVA_HOME`.
- **Git** instalado.
- *(O Maven Wrapper já está incluído no repositório, não sendo necessário instalar o Maven globalmente)*.

### 📥 1. Clonar o Repositório
```bash
git clone https://github.com/misaelorelio/gerenciador-tarefa.git
cd gerenciador-tarefa
```

### ▶️ 2. Executar a Aplicação
No terminal (Windows PowerShell ou Bash):
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / Mac
./mvnw spring-boot:run
```
A aplicação iniciará na porta **`8080`**.

### 🧪 3. Executar os Testes Automatizados
Para rodar toda a suíte de testes de integração com `@SpringBootTest`:
```bash
# Windows
.\mvnw.cmd test

# Linux / Mac
./mvnw test
```

---

## 🌐 Links e Acessos Locais

Com a aplicação rodando, acesse:

| Recurso | URL | Descrição |
|---|---|---|
| **Swagger UI** | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) | Documentação interativa e execução de requisições com suporte a Bearer Token |
| **OpenAPI Docs (JSON)** | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) | Esquema OpenAPI 3.0 em JSON |
| **Console H2** | [http://localhost:8080/h2-console](http://localhost:8080/h2-console) | Visualização do banco de dados em memória |

> 🔑 **Credenciais do H2 Console**:
> - **JDBC URL**: `jdbc:h2:mem:tarefasdb`
> - **User Name**: `sa`
> - **Password**: *(em branco)*

---

## 📐 Decisões Técnicas e seus Tradeoffs

### 1. Banco de Dados H2 (Em Memória)
- **Decisão**: Utilizar o H2 Database configurado em memória (`jdbc:h2:mem:tarefasdb`).
- **Ganhos (Prós)**: Praticidade e rapidez de configuração. 
- **Tradeoff (Contras)**: Ao encerrar o processo da aplicação, os dados são perdidos.

### 2. Java Records para DTOs (*Data Transfer Objects*)
- **Decisão**: Usar Records nativos do Java para todos os DTOs de entrada e saída.
- **Ganhos (Prós)**: código limpo, sem boilerplate de getters/setters/construtores.

## 📚 Visão Geral dos Endpoints

### 🔐 Autenticação (`/api/auth`)
- `POST /api/auth/registro` - Cadastra um novo usuário (`ADMIN` ou `MEMBRO`) e retorna o token JWT.
- `POST /api/auth/login` - Autentica com e-mail e senha e retorna o token JWT.

### 📁 Projetos (`/api/projetos`) - *Requer perfil ADMIN*
- `POST /api/projetos` - Cria um novo projeto.
- `GET /api/projetos` - Lista os projetos do usuário.
- `GET /api/projetos/{id}` - Obtém detalhes de um projeto por ID.
- `PUT /api/projetos/{id}` - Atualiza nome e descrição do projeto.
- `POST /api/projetos/{id}/membros` - Adiciona novos membros ao projeto.
- `DELETE /api/projetos/{id}/membros/{usuarioId}` - Remove um membro do projeto.
- `DELETE /api/projetos/{id}` - Exclui o projeto e suas tarefas.

### 📝 Tarefas (`/api/projetos/{projetoId}/tarefas`) - *Acessível por ADMIN e MEMBRO*
- `POST /api/projetos/{projetoId}/tarefas` - Cria uma nova tarefa no projeto.
- `GET /api/projetos/{projetoId}/tarefas` - Lista tarefas com paginação e filtros dinâmicos (`status`, `prioridade`, `responsavelId`, `prazoInicio`, `prazoFim`, `busca`).
- `GET /api/projetos/{projetoId}/tarefas/{tarefaId}` - Obtém detalhes de uma tarefa.
- `PUT /api/projetos/{projetoId}/tarefas/{tarefaId}` - Atualiza dados da tarefa.
- `PATCH /api/projetos/{projetoId}/tarefas/{tarefaId}/status` - Atualiza o status da tarefa respeitando as regras de negócio.
- `DELETE /api/projetos/{projetoId}/tarefas/{tarefaId}` - Remove uma tarefa.
- `GET /api/projetos/{projetoId}/relatorio` - Retorna contadores agregados por status e por prioridade do projeto.
