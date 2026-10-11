# Project Brief: RamLaal

## Overview
**RamLaal** is an autonomous personal AI assistant and workflow orchestration platform. It is designed to act as a proactive, capable digital copilot rather than a passive question-answering chatbot.

## Core Vision & Objectives
1. **Intelligent Personal Assistant**:
   - Manage real-world user tasks, calendars, reminders, alarms, and scheduled jobs.
   - Execute multi-step operations autonomously while maintaining safety guards on destructive actions.
2. **Context-Aware Hybrid Retrieval (RAG)**:
   - Ingest and index personal documents and notes.
   - Combine dense embeddings (pgvector / Chroma) with keyword retrieval (BM25) and query expansion.
3. **Durable Multi-Tier Memory**:
   - Distinguish transient chat history from long-term factual user memory (preferences, habits, recurring commitments).
   - Post-conversation learning layer that extracts candidate memories, deduplicates, and resolves conflicts.
4. **Extensible Tool Engine**:
   - Clean, modular tool architecture supporting Spring AI `@Tool`, LangGraph tools, and Model Context Protocol (MCP) servers.

## Key Stakeholder & Target Architecture
- Architecture blueprint: Documented in `refrence_chatgpt_response.txt` and `.agents/rules/01-architecture-guidelines.md`.
- Target runtime: Modular backend (Java 21 + Spring AI or Python + LangGraph/FastAPI), relational/vector storage (PostgreSQL + pgvector), and modern frontend (React/Next.js).
