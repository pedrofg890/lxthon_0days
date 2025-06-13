import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getTranscript } from '../../services/transcriptService';
import '../../styles/HomePage.css'
import '../../styles/RequestButtom.css';
import '../../styles/BelowBarButtom.css';

export default function HomePage() {
    const [url, setUrl] = useState("");
    const [transcript, setTranscript] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState(false);
    const navigate = useNavigate();

    const handleInputChange = (e) => setUrl(e.target.value);

    const handleRequest = async () => {
        setLoading(true);
        setError("");
        setTranscript(null);
        setSuccess(false);
        try {
            const transcriptData = await getTranscript(url);
            setTranscript(transcriptData);
            setSuccess(true);
        } catch (err) {
            setError("Could not fetch transcript. Please check the URL.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <section className="home-page-cta-section">

            <div style={{ width: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
                <h1 style={{ color: '#fff', textAlign: 'center', marginBottom: '2rem', fontSize: '3rem' }}>Got a YouTube video? Feed us the link!</h1>
                <div style={{ width: '100%', maxWidth: '900px', display: 'flex', alignItems: 'center', background: '#1a1a1a', borderRadius: '36px', border: '1px solid #333', padding: '0.75rem 1.5rem', boxShadow: '0 2px 12px rgba(0,0,0,0.2)' }}>
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
                    >
                        {loading ? '...' : '▶'}
                    </button>
                </div>
                <div style={{ width: '100%', maxWidth: '900px', display: 'flex', gap: '1.5rem', marginTop: '1.5rem', justifyContent: 'center' }}>
                    <button
                        className="belowBarButton" onClick={() => navigate('/transcript')}>
                        Get Transcript
                    </button>
                    <button className="belowBarButton" onClick={() => navigate('/insights')}>
                        Get Insights
                    </button>
                    <button className="belowBarButton" onClick={() => navigate('/quizz')}>
                        Generate Test
                    </button>
                </div>
                {error && <div style={{ color: 'red', marginTop: '1rem' }}>{error}</div>}
                {success && (
                    <div style={{ color: 'lightgreen', marginTop: '1rem', fontWeight: 'bold' }}>Transcript generated</div>
                )}
            </div>
        </section>
    )
}