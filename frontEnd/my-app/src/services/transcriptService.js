const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api/videos';

export async function getTranscript(url) {
    const response = await fetch(`${API_URL}/clean-transcript?url=${encodeURIComponent(url)}`);
    const data = await response.json();
    if (!response.ok) {
        throw new Error('Failed to fetch transcript');
    }
    return data._embedded ? data._embedded.transcript : [];
}