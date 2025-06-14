import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getTranscript } from '../../services/transcriptService';
import { getSummary } from '../../services/insightsService';
import { getQuiz } from '../../services/quizService';
import '../../styles/HomePage.css'
import '../../styles/RequestButtom.css';
import '../../styles/BelowBarButtom.css';

/**
 * HomePage component.
 *
 * Allows the user to input a YouTube URL and then:
 *  - Fetch raw transcript
 *  - Generate summary
 *  - Generate quiz
 *
 * Manages individual loading states for each operation and allows
 * cancelling in‐flight requests.
 */
export default function HomePage() {
    const [url, setUrl] = useState("");
    const [transcript, setTranscript] = useState(null);
    const [summary, setSummary] = useState(null);
    const [quiz, setQuiz] = useState(null);
    const [loading, setLoading] = useState(false);
    const [loadingTranscript, setLoadingTranscript] = useState(false);
    const [loadingSummary, setLoadingSummary] = useState(false);
    const [loadingQuiz, setLoadingQuiz] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState(false);
    const [abortController, setAbortController] = useState(null);
    const navigate = useNavigate();

    /**
     * Handle URL input change.
     * @param {React.ChangeEvent<HTMLInputElement>} e
     */
    const handleInputChange = (e) => setUrl(e.target.value);

    /**
     * Kick off transcript, summary and (delayed) quiz fetches in parallel.
     * Allows cancellation via AbortController.
     */
    const handleRequest = async () => {
        setLoading(true);
        setError("");
        setTranscript(null);
        setSuccess(false);
        setSummary(null);
        setQuiz(null);
        setLoadingTranscript(true);
        setLoadingSummary(true);
        setLoadingQuiz(true);
        const controller = new AbortController();
        setAbortController(controller);
        try {
            // Start transcript request immediately
            const transcriptPromise = (async () => { const res = await getTranscript(url, controller.signal); setLoadingTranscript(false); return res; })();
            // Wait 15 seconds before starting summary request
            const summaryPromise = (async () => {
                await new Promise(resolve => setTimeout(resolve, 15000));
                const res = await getSummary(url, controller.signal);
                setLoadingSummary(false);
                return res;
            })();
            // Wait 30 seconds before starting quiz request
            const quizPromise = (async () => {
                await new Promise(resolve => setTimeout(resolve, 30000));
                const res = await getQuiz(url, 5, controller.signal);
                setLoadingQuiz(false);
                return res;
            })();
            // Wait for all
            const [transcriptData, summaryData, quizData] = await Promise.all([
                transcriptPromise,
                summaryPromise,
                quizPromise
            ]);
            setTranscript(transcriptData);
            setSummary(summaryData);
            setQuiz(quizData);
            setSuccess(true);
        } catch (err) {
            setLoadingTranscript(false);
            setLoadingSummary(false);
            setLoadingQuiz(false);
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

    /**
     * Cancels any in‐flight requests.
     */
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
                        className="belowBarButton"
                        onClick={() => navigate('/transcript')}
                        disabled={loadingTranscript}
                        style={{ position: 'relative', minWidth: 160, display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                    >
                        Get Transcript
                        {loadingTranscript && (
                            <span className="loading-spinner" style={{
                                marginLeft: 12,
                                width: 22, height: 22, border: '3px solid #fff', borderTop: '3px solid #888', borderRadius: '50%', animation: 'spin 1s linear infinite', display: 'inline-block',
                            }} />
                        )}
                    </button>
                    <button
                        className="belowBarButton"
                        onClick={() => navigate('/insights')}
                        disabled={loadingSummary}
                        style={{ position: 'relative', minWidth: 160, display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                    >
                        Get Summary
                        {loadingSummary && (
                            <span className="loading-spinner" style={{
                                marginLeft: 12,
                                width: 22, height: 22, border: '3px solid #fff', borderTop: '3px solid #888', borderRadius: '50%', animation: 'spin 1s linear infinite', display: 'inline-block',
                            }} />
                        )}
                    </button>
                    <button
                        className="belowBarButton"
                        onClick={() => navigate('/quiz')}
                        disabled={loadingQuiz}
                        style={{ position: 'relative', minWidth: 160, display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                    >
                        Generate Quizz
                        {loadingQuiz && (
                            <span className="loading-spinner" style={{
                                marginLeft: 12,
                                width: 22, height: 22, border: '3px solid #fff', borderTop: '3px solid #888', borderRadius: '50%', animation: 'spin 1s linear infinite', display: 'inline-block',
                            }} />
                        )}
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