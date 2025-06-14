const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api/videos';

let dataReceived;

export async function getSummary(url) {
    const response = await fetch(`${API_URL}/summary?url=${encodeURIComponent(url)}`);
    const data = await response.text(); // get as string
    if (!response.ok) {
        throw new Error('Failed to fetch insights');
    }
    dataReceived = data; // store the received data
    return data;
}

export function getLastSummary() {
    return dataReceived;
}