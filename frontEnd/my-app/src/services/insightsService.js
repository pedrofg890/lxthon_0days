const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api/videos';

export async function getSummary() {
    const response = await fetch(`${API_URL}/summary`);
    const data = await response.json();
    if (!response.ok) {
        throw new Error('Failed to fetch insights');
    }
    return data._embedded ? data._embedded.insights : [];
}