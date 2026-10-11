# 🌌 Origin — Autonomous Personal AI Assistant & Workflow Orchestrator

**Origin** is an autonomous personal AI assistant and proactive digital copilot. Rather than a passive question-answering chatbot, Origin is engineered with **durable multi-tier memory**, **hybrid vector retrieval (RAG)**, **autonomous tool execution** (calendar, priority tasks, reminders, alarms, cron jobs), and an editorial SaaS frontend interface featuring an interactive schedule glance sidebar and seamless Light/Dark mode.

---

## 🏗️ Architecture & Technology Stack

- **Backend**: Java 21, Spring Boot 3.3.4, Spring AI, Hibernate/JPA, Maven.
- **Relational & Vector Storage**: PostgreSQL 16 + `pgvector` (cosine similarity search `<=>`).
- **Frontend**: React 18, Vite, TypeScript, Lucide Icons, CSS Custom Properties (Warm Ivory / Dark slate theme).
- **LLM & Embeddings Provider**: OpenAI-compatible endpoint (AWS Bedrock Mantle with `zai.glm-5` and `zai.glm-4.7` embeddings).
- **Demo Seeder**: Pure standard-library Python (zero external pip packages required).

---

## 📋 Prerequisites

Before running the project, ensure you have the following installed on your machine:

1. **Docker Desktop** (for running PostgreSQL + `pgvector`)
2. **Java 21 JDK** (OpenJDK 21 or Eclipse Temurin 21)
3. **Maven 3.8+** (or use your IDE's built-in Maven)
4. **Node.js 18+** & **npm**
5. **Python 3.8+** (for running the demo data seeder script)

---

## 🚀 Quick Start Guide (5 Simple Steps)

### Step 1: Environment Configuration

Copy the example environment file to `.env` in the project root:

```bash
cp .env.example .env
```

> **Note**: The `.env` file is pre-configured with default credentials and a working AWS Bedrock Mantle endpoint. You can run it out-of-the-box without modifying anything.

---

### Step 2: Start PostgreSQL with pgvector

Ensure Docker Desktop is running, then run:

```bash
docker compose up -d
```

This starts the `Origin-postgres` container on port `5432` and automatically enables the `vector` extension.

To verify the database container is healthy:
```bash
docker compose ps
```

---

### Step 3: Start the Backend (Spring Boot)

Open a terminal and start the Spring Boot backend:

```bash
cd backend
mvn spring-boot:run
```

*(Alternatively, open the repository in **IntelliJ IDEA**, **Eclipse**, or **VS Code**, and run `OriginApplication.java`).*

The backend will start on **`http://localhost:8080`**.

---

### Step 4: Start the Frontend (React + Vite)

Open a second terminal window:

```bash
cd frontend
npm install
npm run dev
```

Open your browser at **`http://localhost:5173`**.

The Vite development server automatically proxies all `/api` requests to the Spring Boot backend (`http://localhost:8080`).

---

### Step 5: Seed Demo Data (The "Bhumi" Persona)

To instantly populate the assistant with rich, realistic data for a live demo, open a third terminal and run:

```bash
python seed_demo_data.py
```

*Optional flags:*
- `python seed_demo_data.py --clear` *(clears existing records before re-seeding)*
- `python seed_demo_data.py --url http://localhost:8080/api` *(custom backend URL)*

---

## 👩‍💼 Manager Demo Walkthrough Guide

When presenting Origin to a manager or stakeholder, follow this curated walkthrough script:

### 1. 🧠 Durable Memory Bank (Long-Term Personal Learning)
- Click on **Memory Bank** in the left sidebar.
- Show that Origin retains durable user facts across sessions:
  - **Identity**: Bhumi (Senior Product Designer).
  - **Pet Dog**: Leo (2-year-old Golden Retriever).
  - **Food & Drink**: Loves dark chocolate truffle cake; oat milk cappuccino with cinnamon.
  - **Meeting Habits**: Prefers scheduling meetings **after 2:00 PM** to preserve morning focus time.
  - **Daily Habit**: Morning yoga & meditation at 7:00 AM.

### 2. ⚡ Interactive Right Sidebar ("Glance & Schedule")
- Switch to the **Assistant** (Chat) view.
- Notice the **Right Sidebar** on the right side:
  - **⚡ Happening Next (Spotlight Card)**: Dynamically pinpoints the single nearest upcoming meeting/task with live countdown tags (e.g. `in 25m`, `Today 2:30 PM`).
  - **🔔 Reminders Accordion**: Time-sensitive reminders (hydration, feeding Leo, bakery pickup) with quick dismiss actions.
  - **⏰ Armed Alarms Accordion**: Configured alarms (Morning Yoga, Demo Prep).
  - **✅ Priority Tasks Accordion**: Tasks labeled with priority badges (`URGENT`, `HIGH`, `MEDIUM`). Click any checkbox to mark it completed live!
  - **Filter Tabs**: Click **Today**, **Urgent**, or **All** to see the view filter dynamically.
  - **Collapse Toggle**: Click the top toggle to collapse the sidebar into a compact 50px icon rail.

### 3. 📄 Knowledge Base (Vector RAG)
- Click on **Knowledge Base** in the left sidebar.
- Show that `project_aurora_design_brief.md` has been ingested into PostgreSQL vector storage with chunking status and semantic index ready.

### 4. 💬 Live AI Chat Prompts to Demonstrate
Type these prompts in the chat box to demonstrate the agent's capabilities:

| Prompt to Ask | What It Demonstrates |
| :--- | :--- |
| **"What do you remember about my personal preferences and pets?"** | Recalls Bhumi's identity, dog Leo, love for cats, and chocolate truffle cake from durable vector memory. |
| **"Plan my schedule for today considering my meeting preferences."** | Inspects today's calendar and task list while strictly respecting her preference for meetings after 2 PM. |
| **"What is Project Aurora and what are its core design principles?"** | Performs hybrid RAG retrieval from the ingested knowledge base markdown document. |
| **"Set a reminder to buy cake for Leo's birthday tomorrow at 3 PM"** | Executes the backend reminder tool relative to the real current system time. |
| **"Mark my Figma mockup task as completed"** | Modifies the task state without needing any technical IDs or UUIDs. |

---

## 📂 Repository Structure

```
origin/
├── .agents/                    # Multi-tier memory bank, architectural guidelines & skills
│   ├── memory/                 # Persistent memory bank (activeContext, projectbrief, progress)
│   └── rules/                  # Operating standards and core behavior
├── backend/                    # Spring Boot 3.3.4 backend
│   ├── src/main/java/com/origin/
│   │   ├── api/controller/     # REST controllers (Chat, Dashboard, Memory, Documents)
│   │   ├── domain/entity/      # JPA entities (Session, Message, Memory, Task, Reminder, etc.)
│   │   ├── domain/repository/  # Spring Data JPA repositories with pgvector queries
│   │   ├── service/            # Orchestration, RAG vector retrieval, Learning layer
│   │   ├── tool/               # Spring AI tool callbacks (Calendar, Task, Reminder, Alarm)
│   │   └── util/               # DateTime parsing and vector formatting utilities
│   └── pom.xml                 # Maven build definition
├── frontend/                   # React 18 + Vite + TypeScript application
│   ├── src/
│   │   ├── components/         # ChatView, RightSidebar, DashboardView, MemoryView, DocumentsView
│   │   ├── api.ts              # REST client methods
│   │   ├── types.ts            # Strongly typed TypeScript interfaces
│   │   └── index.css           # Editorial SaaS theme tokens (Light & Dark mode)
│   └── package.json            # Frontend dependencies
├── data/
│   └── documents/              # Sample documents scanned on backend boot
│       └── project_aurora_design_brief.md
├── docker-compose.yml          # PostgreSQL 16 + pgvector container definition
├── seed_demo_data.py           # Zero-dependency demo data generator (Bhumi persona)
├── test_api.py                 # LLM endpoint diagnostic test script
└── README.md                   # This project guide
```

---

## 🧪 Verification & Build Commands

- **Backend compilation**:
  ```bash
  cd backend
  mvn test-compile -DskipTests
  ```

- **Frontend production build**:
  ```bash
  cd frontend
  npm run build
  ```

- **API Connectivity Diagnostic**:
  ```bash
  python test_api.py
  ```

---

## 🛡️ License & Credits

Built with Spring AI, PostgreSQL `pgvector`, and React. Internal demo project for **Origin Personal AI**.

