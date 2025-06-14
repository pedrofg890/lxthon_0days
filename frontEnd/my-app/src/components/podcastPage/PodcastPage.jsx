import React, { useState, useRef } from 'react';
import '../../styles/BelowBarButtom.css';

export default function PodcastPage() {
    const [url, setUrl] = useState("");
    const [audioSrc, setAudioSrc] = useState("");
    const [downloading, setDownloading] = useState(false);
    const [error, setError] = useState("");
    const audioRef = useRef(null);

    // Dummy download handler (replace with real backend call)
    const handleDownload = async () => {
        setError("");
        setDownloading(true);
        try {
            // Simulate fetching audio from the URL
            // Replace this with your backend call to get the audio file
            // For demo, just set a sample audio file
            // Example: const response = await fetch(`/api/audio?url=${encodeURIComponent(url)}`);
            // const blob = await response.blob();
            // const audioUrl = URL.createObjectURL(blob);
            // setAudioSrc(audioUrl);
            setTimeout(() => {
                setAudioSrc("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3");
                setDownloading(false);
            }, 1500);
        } catch (e) {
            setError("Failed to download audio.");
            setDownloading(false);
        }
    };

    const handlePlay = () => {
        if (audioRef.current) {
            audioRef.current.play();
        }
    };

    return (
        <section style={{ minHeight: '80vh', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'flex-start', paddingTop: '3rem' }}>
            <div style={{ width: '100%', maxWidth: 500, background: '#232323', borderRadius: 18, boxShadow: '0 2px 12px rgba(0,0,0,0.15)', padding: '2rem', marginBottom: '2rem', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                <button onClick={() => window.location.href = '/'} className="generalButton" style={{ marginBottom: '1.5rem', alignSelf: 'flex-start' }}>Back to Home</button>
                <h1 style={{ color: '#fff', fontSize: '2rem', marginBottom: '1.5rem' }}>Podcast Audio Tool</h1>
                <input
                    type="text"
                    placeholder="Paste a link here..."
                    value={url}
                    onChange={e => setUrl(e.target.value)}
                    style={{ width: '100%', padding: '0.75rem', fontSize: '1.1rem', borderRadius: 8, border: '1px solid #444', marginBottom: '1.5rem', background: '#181818', color: '#fff' }}
                />
                <div style={{ display: 'flex', gap: '1rem', width: '100%', justifyContent: 'center', marginBottom: '1.5rem' }}>
                    <button
                        className="belowBarButton"
                        onClick={handleDownload}
                        disabled={!url.trim() || downloading}
                        style={{ minWidth: 160, display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                    >
                        {downloading ? 'Downloading...' : 'Download Audio'}
                    </button>
                    <button
                        className="belowBarButton"
                        onClick={handlePlay}
                        disabled={!audioSrc}
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
