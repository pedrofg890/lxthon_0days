# LXThon AITA Study Assistant 

*An educational assistant tool leveraging YouTube videos to aid in learning and comprehension.*

This monorepo includes:
- **Backend**: Spring Boot service for transcript extraction, cleaning, summaries, quizzes, and podcast generation.
- **Frontend**: React application to interact with all features via a user-friendly UI.

---

## Prerequisites

### Backend
1. **Java 21** (check with `java --version`).
2. **Maven** (for building).
3. **yt-dlp** installed and on your system `PATH`:
   ```bash
   # macOS (Homebrew)
   brew install yt-dlp

   # or via pip
   pip install yt-dlp

   yt-dlp --version
   ```
4. A valid **OpenAI/GitHub AI API key** (set in `.env`).

### Frontend
1. **Node.js** (v16+).
2. **npm** or **yarn**.

---

## Configuration

### Environment Variables

#### Backend `.env`
```dotenv
OPENAI_API_KEY=your_api_key_here
```
Location: project root.

#### Frontend `.env``
```dotenv
REACT_APP_API_URL=http://localhost:8080/api/videos
```
Location: `frontend/` directory.

---

## Build & Run

### Backend
```bash
# From project root
mvn clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar
```
Server listens on `http://localhost:8080`.

### Frontend
```bash
cd frontend
npm install    # or yarn install\ nnpm start      # or yarn start
```
App runs at `http://localhost:3000`.

---

## REST API (Backend)

Base path: `/api/videos`

| Endpoint                                | Method | Description                                            |
| --------------------------------------- | ------ | ------------------------------------------------------ |
| `/info?url={videoUrl}`                  | GET    | Fetch raw video metadata                               |
| `/download?url={videoUrl}&format={fmt}` | GET    | Download video in chosen format                        |
| `/transcript?url={videoUrl}`            | GET    | Raw transcript segments                                 |
| `/clean-transcript?url={videoUrl}`      | GET    | Cleaned transcript (normalized)                        |
| `/summary?url={videoUrl}`               | GET    | AI-generated summary                                   |
| `/quiz?url={videoUrl}&numQuestions={n}` | GET    | Generate a multiple-choice quiz                        |

#### Podcast `/podcast-api/chat`
| Endpoint                                         | Method | Description                                    |
| ------------------------------------------------ | ------ | ---------------------------------------------- |
| `/test-openai`                                   | GET    | Simple AI API connectivity test                |
| `/completion`                                    | POST   | Forward arbitrary prompt to AI                 |
| `/generate-podcast?url=&hostA=&hostB=`           | POST   | Full podcast script (and audio size info)      |

---

## Frontend Features

- **Home Page**: Enter YouTube link and trigger transcript, summary, quiz concurrently.
- **Transcript View**: Paginated, cleaned transcript with timecodes.
- **Summary View**: Concise AI-generated summary.
- **Quiz View**: Interactive multiple-choice quiz UI.

Ensure backend CORS allows `http://localhost:3000` (configured in `CorsConfig`).

---

## Contributing

1. Fork the repo and create a feature branch.
2. Write tests for new functionality.
3. Submit a pull request with clear description.

---

## Future Improvements
- Support multiple languages for transcripts and quizzes.
- Add user authentication and saved sessions.
- UI enhancements: progress bars, feedback.

---

## License

AITA Study Assistant © 0days
