# LXThon AITA Study Assistant

*An educational assistant tool leveraging YouTube videos to aid in learning and comprehension.*

This project provides a backend service for processing YouTube videos: extracting transcripts, cleaning and normalizing them, generating summaries, quizzes, and even full podcast scripts/audio. 
It leverages **yt-dlp** for subtitle extraction and an LLM (via a GitHub AI / OpenAI-compatible endpoint) for AI-powered processing.

---

## Prerequisites

1. **Java 21** (ensure `java --version` returns `21.x`)
2. **Maven** (for build management)
3. **yt-dlp** installed and available on your system `PATH`:
   ```bash
   # On macOS (with Homebrew)
   brew install yt-dlp

   # Or via pip
   pip install yt-dlp

   # Verify installation
   yt-dlp --version
   ```
4. A valid **OpenAI/GitHub AI API key** (set in `.env`)

---

## Configuration

Create a `.env` file in the project root with:

```dotenv
# .env
OPENAI_API_KEY=your_api_key_here
```

You can also override other properties via `application.properties`:

```properties
openai.api-key=${OPENAI_API_KEY}
openai.endpoint=https://api.openai.com/v1/chat/completions
openai.model=gpt-4
```

---

## Building and Running (Backend)

```bash
# Build
mvn clean package

# Run (Spring Boot)
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

The backend will start on `http://localhost:8080` by default.

---

## REST API Endpoints (Backend)

All endpoints are under `/api/videos`:

| Endpoint                                | Method | Description                                            |
| --------------------------------------- | ------ | ------------------------------------------------------ |
| `/info?url={videoUrl}`                  | GET    | Returns raw video metadata JSON                        |
| `/download?url={videoUrl}&format={fmt}` | GET    | Downloads video in specified format (e.g., mp4, mp3)   |
| `/transcript?url={videoUrl}`            | GET    | Retrieves raw transcript segments                      |
| `/clean-transcript?url={videoUrl}`      | GET    | Returns normalized transcript segments asynchronously  |
| `/summary?url={videoUrl}`               | GET    | Generates a concise summary of the transcript          |
| `/quiz?url={videoUrl}&numQuestions={n}` | GET    | Generates a multiple-choice quiz (default 5 questions) |

### Podcast Generation (under `/podcast-api/chat`)

| Endpoint                                                       | Method | Description                                           |
| -------------------------------------------------------------- | ------ | ----------------------------------------------------- |
| `/test-openai`                                                 | GET    | Simple test call to AI API                            |
| `/completion`                                                  | POST   | Forwards prompt body to AI API and returns completion |
| `/generate-podcast?url={videoUrl}&hostA={nameA}&hostB={nameB}` | POST   | Generates podcast script and audio size info          |

---

## Frontend

This repository also includes a React-based frontend for interacting with the backend API.

### Prerequisites

1. **Node.js** (v16+ recommended)
2. **npm** or **yarn**

### Setup & Run (Frontend)

```bash
# Navigate to the frontend directory\ ncd frontend

# Install dependencies
npm install
# or
# yarn install

# Start the dev server
npm start
# or
# yarn start
```

The frontend will run on `http://localhost:3000` by default and communicate with the backend on port `8080`.

### Features

- **Home Page**: Input YouTube URL and choose actions: Transcript, Summary, Quiz
- **Transcript View**: Displays raw transcript segments
- **Summary View**: Shows AI-generated summary
- **Quiz View**: Presents an interactive multiple-choice quiz

Ensure CORS is configured (`CorsConfig`) to allow requests from `http://localhost:3000`.

---

## CORS Configuration (Backend)

The backend allows cross-origin requests from `http://localhost:3000`. To modify, update `CorsConfig`.

---

## Project Structure

- **Backend**
    - `lxthon.backend.Service` – business logic
    - `lxthon.backend.Domain` – data classes
    - `lxthon.backend.Controller` – REST controllers
    - `lxthon.backend.config` – configuration (EnvLoader, OpenAIConfig, CORS)
    - `lxthon.backend.Main` – application entry point
- **Frontend**
    - `src/` – React components, service layer to call API
    - `public/` – static assets
    - `package.json` – dependencies & scripts

---

## License

AITA Study Assistant © 0days

