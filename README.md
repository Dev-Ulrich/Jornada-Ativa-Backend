# 🧭 Jornada Ativa — Backend

API do projeto **Jornada Ativa**, desenvolvida em **Spring Boot 3.4.3** com **Java 21**, responsável por gerenciar toda a lógica e integração entre o banco de dados e os aplicativos Web e Mobile da plataforma.

---

## 🚀 Visão Geral

O **Jornada Ativa** é um projeto voltado à promoção da **saúde, atividade física e bem-estar**, oferecendo aos usuários um acompanhamento completo de treinos, histórico de desempenho e eventos esportivos, com suporte a autenticação segura via **JWT** e persistência em **SQL Server (Azure)**.

Este repositório contém o **backend** da aplicação — responsável pela API RESTful, autenticação, manipulação de dados e integração com o frontend (React) e o aplicativo mobile (React Native).

---

## 🧩 Principais Tecnologias

| Categoria | Tecnologias |
|------------|-------------|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.4.3 |
| Banco de Dados | Azure SQL Server |
| ORM | Spring Data JPA / Hibernate |
| Segurança | Spring Security + JWT |
| Build | Maven |
| Deploy | Render (Deploy Automático via Docker) |
| Outras | Lombok, Validation API, Dockerfile, Swagger |

---

## 🧱 Estrutura do Projeto

```
Jornada-Ativa-Backend/
├── src/
│   ├── main/
│   │   ├── java/com/jornadaativa/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── model/
│   │   │   ├── dto/
│   │   │   └── security/
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-prod.properties
│   └── test/
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 🔐 Autenticação e Segurança

O sistema utiliza **JWT (JSON Web Token)** para autenticação e autorização de usuários.

- Rota pública: `/auth/register` e `/auth/login`
- Rotas protegidas: `/usuarios`, `/treinos`, `/eventos`, etc.
- Controle de acesso por **roles** (`ADMIN` / `USER`)

---

## 💾 Banco de Dados

O backend está conectado a um **banco SQL Server hospedado no Azure**, com tabelas como:

- `usuarios`
- `treinos`
- `historico_treino`
- `treino_pontosGPS`
- `eventos`
- `roles`
- `usuarios_roles`
- `tokens`

O schema é versionado e modelado conforme as boas práticas de normalização e integridade referencial.

---

## 🧰 Endpoints Principais

| Método | Endpoint | Descrição |
|--------|-----------|-----------|
| `POST` | `/auth/register` | Criação de novo usuário |
| `POST` | `/auth/login` | Autenticação e geração de token JWT |
| `GET` | `/usuarios` | Listagem de usuários (somente ADMIN) |
| `GET` | `/usuarios/{id}` | Consulta de usuário por ID |
| `PUT` | `/usuarios/{id}` | Atualização de perfil |
| `DELETE` | `/usuarios/{id}` | Exclusão de conta |
| `GET` | `/eventos` | Listagem de eventos esportivos |
| `GET` | `/treinos` | Histórico e registros de treino |

> 🔎 Documentação Swagger disponível em `/swagger-ui.html` (em ambientes habilitados).

---

## 🐳 Deploy no Render

O deploy é realizado automaticamente no **Render**, com build via **Dockerfile** e integração contínua.

### Variáveis de ambiente essenciais:
```
DB_URL=<string>
DB_USERNAME=<string>
DB_PASSWORD=<string>
JWT_SECRET=<string>
```

---

## ⚙️ Como Executar Localmente

### Pré-requisitos:
- Java 21+
- Maven 3.9+
- SQL Server ou outro banco compatível
- Docker (opcional)

### Passos:
```bash
# Clonar o repositório
git clone https://github.com/Dev-Ulrich/Jornada-Ativa-Backend.git
cd Jornada-Ativa-Backend

# Compilar o projeto
mvn clean package -DskipTests

# Rodar localmente
mvn spring-boot:run
```

A API estará disponível em:
```
http://localhost:8080
```
ou em
```
https://jornada-ativa-api.onrender.com
```

---

## 🧾 Licença

Este projeto é de uso acadêmico e sem fins lucrativos.  
Todos os direitos reservados © 2025 — **Jornada Ativa**

---

## 🌐 Repositórios Relacionados

- **[Frontend (Web)](https://github.com/Dev-Ulrich/Jornada-Ativa-Frontend)**
- **[Mobile (App)](https://github.com/Dev-Ulrich/Jornada-Ativa-Mobile)**

---

### 💡 Observação
> O projeto faz parte do **TCC do curso Técnico em Informática – ITB Brasílio Flores de Azevedo**, com integração total entre os módulos Web, Mobile e Banco de Dados, representando um ecossistema completo de desenvolvimento Full Stack moderno.
