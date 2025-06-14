/**
 * videoService.js
 *
 * Provides functions to send a YouTube URL to the backend for processing
 * and to retrieve the processed video information.
 */
const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8081';

/**
 * Sends a video URL to the backend `/video` endpoint for processing.
 *
 * @param {object} payload
 * @param {string} payload.url        The YouTube video URL to process.
 * @param {...any} [payload.options]  Any additional options required by the backend.
 * @returns {Promise<object|null>}    Resolves with the response JSON (if any),
 *                                    or null if the response had no JSON body.
 * @throws {Error}                    If the network request fails or the server returns a non-OK status.
 *                                    The thrown error will contain the backend’s error message if provided.
 */

export async function sendURL(teacherDTO) {
    const response = await fetch(`${API_URL}/video`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(teacherDTO)
    });

    let responseData = null;

    if (!response.ok) {
        const contentType = response.headers.get("content-type");
        if (contentType && contentType.includes("application/json")) {
            responseData = await response.json();
        } else {
            responseData = await response.text();
        }
        throw new Error(responseData.message || responseData);
    }

    try {
        responseData = await response.json();
    } catch (error) {
        console.warn("Resposta sem corpo JSON");
    }

    return responseData;
}

/**
 * Retrieves the latest processed video information from the backend `/video` endpoint.
 *
 * @returns {Promise<object[]>}  Resolves with an array of video information objects,
 *                               or an empty array if none is embedded.
 * @throws {Error}               If the network request fails or the server returns a non-OK status.
 */
export async function getResponse() {
    const response = await fetch(`${API_URL}/video`);
    const data = await response.json();
    if (!response.ok) {
        throw new Error('Failed to fetch video information');
    }
    return data._embedded ? data._embedded.videoInformation : [];
}