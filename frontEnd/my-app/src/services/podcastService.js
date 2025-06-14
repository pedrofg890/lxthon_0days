
const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api/videos';

let lastQuiz = null;


export async function getPodcast(videoUrl, numQuestions = 5, signal) {
    const resp = await fetch(
        `${API_URL}/quiz?url=${encodeURIComponent(videoUrl)}&numQuestions=${numQuestions}`,
        signal ? { signal } : undefined
    );
    if (!resp.ok) {
        const txt = await resp.text();
        throw new Error(txt || 'Erro ao gerar quiz');
    }
    const quiz = await resp.json();
    lastQuiz = quiz;
    return quiz;
}

export function getLastQuiz() {
    return lastQuiz;
}
