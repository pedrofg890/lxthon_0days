const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/podcast-api/chat';

let lastQuiz = null;


export async function getPodcast(videoUrl) {
    const resp = await fetch(
        `${API_URL}/generate-podcast`,
        {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `url=${encodeURIComponent(videoUrl)}`
        }
    );
    if (!resp.ok) {
        const txt = await resp.text();
        throw new Error(txt || 'Erro ao gerar podcast');
    }
    // The backend returns a JSON object with all podcast info fields
    // Example response fields:
    // success, podcastId, videoUrl, script, hosts, audioSizeBytes, downloadUrl, streamUrl, message
    const response = await resp.json();
    return response;
}

export function getLastQuiz() {
    return lastQuiz;
}

export async function downloadPodcast(podcastId) {
    const resp = await fetch(`${API_URL}/download/${podcastId}`);
    if (!resp.ok) {
        const txt = await resp.text();
        throw new Error(txt || 'Erro ao fazer download do podcast');
    }
    // Return the audio as a Blob
    const blob = await resp.blob();
    return blob;
}

export async function streamPodcast(podcastId) {
    const resp = await fetch(`${API_URL}/stream/${podcastId}`);
    if (!resp.ok) {
        const txt = await resp.text();
        throw new Error(txt || 'Erro ao fazer stream do podcast');
    }
    // Return the audio as a Blob (for streaming)
    const blob = await resp.blob();
    return blob;
}
