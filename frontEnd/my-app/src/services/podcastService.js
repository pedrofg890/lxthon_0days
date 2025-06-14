/**
 * @module podcastService
 */
const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/podcast-api/chat';

let lastQuiz = null;

/**
 * Sends a POST request to generate a podcast from a video URL.
 *
 * @async
 * @function getPodcast
 * @param {string} videoUrl
 *   The YouTube (or other) video URL to convert into a podcast.
 * @returns {Promise<Object>}
 *   Resolves to the JSON response from the server, which includes fields such as:
 *   - success: boolean
 *   - podcastId: string
 *   - videoUrl: string
 *   - script: string
 *   - hosts: object
 *   - audioSizeBytes: number
 *   - downloadUrl: string
 *   - streamUrl: string
 *   - message: string
 * @throws {Error}
 *   Throws if the HTTP response is not OK. The server’s response text is used as the error message.
 */
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

/**
 * Returns the last generated quiz object stored in memory.
 *
 * @function getLastQuiz
 * @returns {any}
 *   The last quiz returned by getQuiz (if any), or null.
 */
export function getLastQuiz() {
    return lastQuiz;
}

/**
 * Downloads the podcast audio as an MP3 blob.
 *
 * @async
 * @function downloadPodcast
 * @param {string} podcastId
 *   The identifier of the podcast to download.
 * @returns {Promise<Blob>}
 *   Resolves to a binary Blob containing the MP3 data.
 * @throws {Error}
 *   Throws if the HTTP response is not OK. The server’s response text is used as the error message.
 */
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

/**
 * Streams the podcast audio for immediate playback.
 *
 * @async
 * @function streamPodcast
 * @param {string} podcastId
 *   The identifier of the podcast to stream.
 * @returns {Promise<Blob>}
 *   Resolves to a binary Blob containing the MP3 data, suitable for streaming.
 * @throws {Error}
 *   Throws if the HTTP response is not OK. The server’s response text is used as the error message.
 */
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
