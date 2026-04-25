# DocuMind 🧠
### AI-Powered Document Q&A System using RAG + Spring AI + Gemini

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen?style=flat-square&logo=springboot)
![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0--M6-green?style=flat-square)
![Gemini](https://img.shields.io/badge/Gemini-2.5%20Flash-blue?style=flat-square&logo=google)
![RAG](https://img.shields.io/badge/Architecture-RAG-purple?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)

> Upload any PDF and ask questions about it in natural language.
> DocuMind uses Retrieval-Augmented Generation (RAG) to find the most
> relevant content and answer intelligently using Google Gemini AI.

## 📌 Features

- 📄 Upload any PDF document via REST API
- 🔍 Semantic search using Gemini Embeddings + Cosine Similarity
- 🤖 Intelligent answers powered by Google Gemini 2.5 Flash
- ⚡ In-memory vector store — no database or Docker setup required
- 🏗️ Clean RAG pipeline built with Spring AI + Spring Boot

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.3.5 |
| AI Framework | Spring AI 1.0.0-M6 |
| LLM | Google Gemini 2.5 Flash |
| Embeddings | Gemini Embedding API (direct REST) |
| Vector Search | Custom Cosine Similarity (Java) |
| PDF Parsing | Spring AI PDF Document Reader |
| Language | Java 17 |
| Build Tool | Maven |

## 🏛️ How RAG Works in DocuMind

PDF Upload
    │
    ▼
┌──────────────────────┐
│  PDF Reader           │  ← Spring AI PagePdfDocumentReader
│  (page by page)      │
└────────┬─────────────┘
         │
    ┌────▼─────────────────┐
    │  Gemini Embedding API │  ← Direct REST call per page
    └────┬─────────────────┘
         │
    ┌────▼─────────────────┐
    │  In-Memory            │  ← Chunks + Vectors stored in List
    │  Vector Store         │
    └────┬─────────────────┘
         │
    User asks a question
         │
    ┌────▼─────────────────┐
    │  Cosine Similarity    │  ← Top 3 relevant chunks retrieved
    │  Search               │
    └────┬─────────────────┘
         │
    ┌────▼─────────────────┐
    │  Gemini 2.5 Flash     │  ← Context + Question → Answer
    │  via Spring AI        │
    └──────────────────────┘

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- Google Gemini API Key — free.

### 1. Clone the repository
git clone https://github.com/vikassk01/DocuMind.git
cd DocuMind

### 2. Configure API Key
Create `src/main/resources/application.properties`:

spring.ai.openai.api-key=YOUR_GEMINI_API_KEY_HERE
spring.ai.openai.base-url=https://generativelanguage.googleapis.com/v1beta/openai
spring.ai.openai.chat.options.model=gemini-2.5-flash
spring.ai.openai.embedding.enabled=false
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
server.port=8080

### 3. Run the application

mvn spring-boot:run
App starts at `http://localhost:8080`

## 📡 API Reference

### 1. Upload a PDF Document
http
POST /api/documents/upload
Content-Type: multipart/form-data

file: <your-pdf-file>
**Response:**
Document uploaded and indexed! Pages indexed: 35

### 2. Ask a Question
http
GET /api/documents/ask?question=what is this document about

**Response:**

This document is about Artificial Intelligence. It covers topics
such as agentic AI, AI in drug discovery, ethical considerations,
and more...

## 📁 Project Structure

src/main/java/com/documind/demo/
├── DocuMindApplication.java        ← Spring Boot entry point
├── config/
│   └── VectorStoreConfig.java      ← Vector store configuration
├── controller/
│   └── DocumentController.java     ← REST API endpoints
└── service/
    └── DocumentService.java        ← Core RAG pipeline logic

## 🔮 Future Enhancements

- [ ] Persistent vector store using pgvector or Pinecone
- [ ] Multi-document support
- [ ] Chat history and follow-up questions
- [ ] Frontend UI with React
- [ ] Docker containerization

## 👨‍💻 Author

Built by **Vikas Koganoor**

## 📄 License

This project is licensed under the MIT License.
