import React, { useState, useRef } from 'react';
import { getPodcast, downloadPodcast, streamPodcast } from '../../services/podcastService';
import '../../styles/BelowBarButtom.css';
import '../../styles/RequestButtom.css';

/**
 * PodcastPage
 *
 * A React component that allows the user to:
 * 1. Submit a YouTube (or other) URL to generate a podcast audio.
 * 2. Download the generated audio as an MP3.
 * 3. Stream (play) the generated audio directly in the browser.
 *
 * @component
 * @example
 * return <PodcastPage />;
 *
 * @returns {JSX.Element} The rendered PodcastPage.
 */
export default function PodcastPage() {
    const [url, setUrl] = useState("");
    const [audioSrc, setAudioSrc] = useState("");
    const [downloading, setDownloading] = useState(false);
    const [loadingPodcast, setLoadingPodcast] = useState(false);
    const [error, setError] = useState("");
    const [podcastInfo, setPodcastInfo] = useState(null);
    const audioRef = useRef(null);


    /**
     * handleRequestPodcast
     *
     * Invokes getPodcast() to generate a podcast based on the provided URL.
     * On success, stores the returned info and initializes audioSrc.
     * On failure, displays an error message.
     *
     * @async
     * @function
     * @returns {Promise<void>}
     */
    const handleRequestPodcast = async () => {
        setError("");
        setLoadingPodcast(true);
        try {
            // Use getPodcast with only the url
            const response = await getPodcast(url);
            setPodcastInfo(response);
            setAudioSrc(response.streamUrl ? response.streamUrl : "");
        } catch (e) {
            setError("Failed to generate podcast audio.");
        } finally {
            setLoadingPodcast(false);
        }
    };

    /**
     * handleDownload
     *
     * Uses downloadPodcast() to fetch the MP3 blob for the current podcastId,
     * then triggers a browser download via an <a> element.
     *
     * @async
     * @function
     * @returns {Promise<void>}
     */
    const handleDownload = async () => {
        setError("");
        setDownloading(true);
        try {
            if (!podcastInfo || !podcastInfo.podcastId) throw new Error("No podcast generated yet.");
            // Use downloadPodcast with podcastId
            const blob = await downloadPodcast(podcastInfo.podcastId);
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = 'podcast.mp3';
            document.body.appendChild(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(url);
        } catch (e) {
            setError("Failed to download audio.");
        } finally {
            setDownloading(false);
        }
    };

    /**
     * handlePlay
     *
     * Uses streamPodcast() to fetch the MP3 blob for immediate playback,
     * then sets audioSrc to the blob URL and plays the audio.
     *
     * @async
     * @function
     * @returns {Promise<void>}
     */
    const handlePlay = async () => {
        if (!podcastInfo || !podcastInfo.podcastId) return;
        try {
            const blob = await streamPodcast(podcastInfo.podcastId);
            const url = window.URL.createObjectURL(blob);
            setAudioSrc(url);
            setTimeout(() => {
                if (audioRef.current) {
                    audioRef.current.play();
                }
            }, 100); // Ensure audio element updates src before playing
        } catch (e) {
            setError("Failed to stream audio.");
        }
    };

    return (
        <section style={{ minHeight: '80vh', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'flex-start', paddingTop: '3rem' }}>
            <div style={{ width: '100%', maxWidth: 500, background: '#232323', borderRadius: 18, boxShadow: '0 2px 12px rgba(0,0,0,0.15)', padding: '2rem', marginBottom: '2rem', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                <button onClick={() => window.location.href = '/'} className="generalButton" style={{ marginBottom: '1.5rem', alignSelf: 'flex-start' }}>Back to Home</button>
                <h1 style={{ color: '#fff', fontSize: '2rem', marginBottom: '1.5rem' }}>Podcast Audio Tool</h1>
                <div style={{ width: '100%', display: 'flex', alignItems: 'center', background: '#1a1a1a', borderRadius: '36px', border: '1px solid #333', padding: '0.75rem 1.5rem', boxShadow: '0 2px 12px rgba(0,0,0,0.2)', position: 'relative', marginBottom: '1.5rem' }}>
                    <input
                        type="text"
                        placeholder="Paste a link here..."
                        value={url}
                        onChange={e => setUrl(e.target.value)}
                        style={{
                            background: 'transparent',
                            color: '#fff',
                            border: 'none',
                            outline: 'none',
                            fontSize: '1.5rem', // Increased font size
                            flex: 1,
                            padding: '1.5rem 0', // Increased vertical padding
                        }}
                    />
                    <button
                        className="requestButton"
                        onClick={handleRequestPodcast}
                        disabled={!url.trim() || loadingPodcast}
                        style={{ position: 'relative', width: 60, height: 60, display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                    >
                        {loadingPodcast ? (
                            <span className="loading-spinner" style={{
                                width: 28,
                                height: 28,
                                border: '4px solid #fff',
                                borderTop: '4px solid #888',
                                borderRadius: '50%',
                                animation: 'spin 1s linear infinite',
                                display: 'inline-block',
                            }} />
                        ) : (
                            <span role="img" aria-label="Send">▶</span>
                        )}
                    </button>
                </div>
                <div style={{ display: 'flex', gap: '1rem', width: '100%', justifyContent: 'center', marginBottom: '1.5rem' }}>
                    <button
                        className="belowBarButton"
                        onClick={handleDownload}
                        disabled={!podcastInfo || !podcastInfo.podcastId || downloading}
                        style={{ minWidth: 160, display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                    >
                        {downloading ? 'Downloading...' : 'Download Audio'}
                    </button>
                    <button
                        className="belowBarButton"
                        onClick={handlePlay}
                        disabled={!podcastInfo || !podcastInfo.podcastId}
                        style={{ minWidth: 160, display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                    >
                        Play Audio
                    </button>
                </div>
                {audioSrc && (
                    <audio ref={audioRef} src={audioSrc} controls style={{ width: '100%' }} />
                )}
                {error && <div style={{ color: 'red', marginTop: '1rem' }}>{error}</div>}
            </div>
        </section>
    );
}
