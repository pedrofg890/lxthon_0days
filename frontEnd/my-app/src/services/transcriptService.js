// import * as test from "node:test";

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api/videos';

let lastTranscript = [];

export async function getTranscript(url) {
    const response = await fetch(`${API_URL}/clean-transcript?url=${encodeURIComponent(url)}`);
    const data = await response.json();
    if (!response.ok) {
        throw new Error('Failed to fetch transcript');
    }
    // If the response is already an array, use it directly
    if (Array.isArray(data)) {
        lastTranscript = data;
    } else {
        lastTranscript = data._embedded ? data._embedded.transcript : [];
    }
    return lastTranscript;
}

export function getLastTranscript() {
    return lastTranscript;
}