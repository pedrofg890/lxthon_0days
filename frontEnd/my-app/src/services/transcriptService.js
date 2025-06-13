const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

export async function getTranscript() {
    const response = await fetch(`${API_URL}/clean-transcript`);
    const data = await response.json();
    if (!response.ok) {
        throw new Error('Failed to fetch trasncript');
    }
    return data._embedded ? data._embedded.transcript : [];
}