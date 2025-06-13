const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8081';

export async function getTranscript() {
    const response = await fetch(`${API_URL}/quizz`);
    const data = await response.json();
    if (!response.ok) {
        throw new Error('Failed to fetch quizz');
    }
    return data._embedded ? data._embedded.quizz : [];
}