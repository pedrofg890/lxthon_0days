const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api/videos';

export async function getQuizFromVideo(videoUrl, numQuestions = 5) {
    const resp = await fetch(
        `${API_URL}/quiz?url=${encodeURIComponent(videoUrl)}&numQuestions=${numQuestions}`
    );
    if (!resp.ok) {
        const txt = await resp.text();
        throw new Error(txt || 'Erro ao gerar quiz');
    }
    return await resp.json();
}
