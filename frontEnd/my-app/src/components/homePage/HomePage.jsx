import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getTranscript } from '../../services/transcriptService';
import { getSummary } from '../../services/insightsService';
import { getQuiz } from '../../services/quizService';
import '../../styles/HomePage.css'
import '../../styles/RequestButtom.css';
import '../../styles/BelowBarButtom.css';

export default function HomePage() {
    const [url, setUrl] = useState("");
    const [transcript, setTranscript] = useState(null);
    const [summary, setSummary] = useState(null);
    const [quiz, setQuiz] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState(false);
    const [abortController, setAbortController] = useState(null);
    const navigate = useNavigate();

    const handleInputChange = (e) => setUrl(e.target.value);

    const handleRequest = async () => {
        setLoading(true);
        setError("");
        setTranscript(null);
        setSuccess(false);
        setSummary(null);
        setQuiz(null);
        const controller = new AbortController();
        setAbortController(controller);
        try {
            const [transcriptData, summaryData, quizData] = await Promise.all([
                getTranscript(url, controller.signal),
                getSummary(url, controller.signal),
                getQuiz(url, 5, controller.signal)
            ]);
            setTranscript(transcriptData);
            setSummary(summaryData);
            setQuiz(quizData);
            setSuccess(true);
        } catch (err) {
            if (err.name === 'AbortError') {
                setError('Requests cancelled.');
            } else {
                setError("Could not fetch transcript or summary. Please check the URL.");
            }
        } finally {
            setLoading(false);
            setAbortController(null);
        }
    };

    const handleStop = () => {
        if (abortController) {
            abortController.abort();
        }
    };

    // Add spinner animation to the global CSS
    const spinnerStyle = `@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }`;
    if (typeof document !== 'undefined' && !document.getElementById('spinner-style')) {
        const style = document.createElement('style');
        style.id = 'spinner-style';
        style.innerHTML = spinnerStyle;
        document.head.appendChild(style);
    }

    return (
        <section className="home-page-cta-section">

            <div style={{ width: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
                <h1 style={{ color: '#fff', textAlign: 'center', marginBottom: '2rem', fontSize: '3rem' }}>Got a YouTube video? Feed us the link!</h1>
                <div style={{ width: '100%', maxWidth: '900px', display: 'flex', alignItems: 'center', background: '#1a1a1a', borderRadius: '36px', border: '1px solid #333', padding: '0.75rem 1.5rem', boxShadow: '0 2px 12px rgba(0,0,0,0.2)', position: 'relative' }}>
                    <input
                        type="text"
                        placeholder="Paste a YouTube URL here..."
                        value={url}
                        onChange={handleInputChange}
                        style={{
                            background: 'transparent',
                            color: '#fff',
                            border: 'none',
                            outline: 'none',
                            fontSize: '1.875rem',
                            flex: 1,
                            padding: '1.125rem 0',
                        }}
                    />
                    <button
                        className="requestButton"
                        aria-label="Send"
                        onClick={handleRequest}
                        disabled={loading || !url.trim()}
                        style={{ position: 'relative', width: 60, height: 60, display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                    >
                        {loading ? (
                            <span className="loading-spinner" style={{
                                width: 28,
                                height: 28,
                                border: '4px solid #fff',
                                borderTop: '4px solid #888',
                                borderRadius: '50%',
                                animation: 'spin 1s linear infinite',
                                display: 'inline-block',
                            }} />
                        ) : '▶'}
                    </button>
                    {loading && (
                        <button
                            onClick={handleStop}
                            aria-label="Stop"
                            style={{
                                position: 'absolute',
                                right: -70,
                                top: '50%',
                                transform: 'translateY(-50%)',
                                width: 40,
                                height: 40,
                                background: '#ff6961',
                                border: 'none',
                                borderRadius: 8,
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                cursor: 'pointer',
                                boxShadow: '0 2px 8px rgba(0,0,0,0.15)'
                            }}
                        >
                            <span style={{
                                display: 'block',
                                width: 18,
                                height: 18,
                                background: '#fff',
                                borderRadius: 3
                            }} />
                        </button>
                    )}
                </div>
                <div style={{ width: '100%', maxWidth: '900px', display: 'flex', gap: '1.5rem', marginTop: '1.5rem', justifyContent: 'center' }}>
                    <button
                        className="belowBarButton" onClick={() => navigate('/transcript')}>
                        Get Transcript
                    </button>
                    <button className="belowBarButton" onClick={() => navigate('/insights')}>
                        Get Summary
                    </button>
                    <button className="belowBarButton" onClick={() => navigate('/quiz')}>
                        Generate Quizz
                    </button>
                </div>
                {error && <div style={{ color: 'red', marginTop: '1rem' }}>{error}</div>}
                {success && (
                    <div style={{ color: 'lightgreen', marginTop: '1rem', fontWeight: 'bold' }}>
                        Transcript and summary generated
                    </div>
                )}
            </div>
        </section>
    )
}