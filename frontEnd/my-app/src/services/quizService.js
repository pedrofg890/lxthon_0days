const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

export async function getQuizFromVideo (videoUrl, numQuestions = 5) {
    const response = await fetch(`${API_URL}/quiz?url=${encodeURIComponent(videoUrl)}&numQuestions=${numQuestions}`);

    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Erro ao obter quiz: ${errorText}`);
    }

    return await response.json();
}
