import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getSummary } from '../../services/insightsService';
import '../../styles/HomePage.css';
import '../../styles/RequestButtom.css';
import '../../styles/BelowBarButtom.css';
import '../../styles/GeneralButtom.css';

export default function InsightsPage() {
    const navigate = useNavigate();
    const [summary, setSummary] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    React.useEffect(() => {
        setLoading(true);
        getSummary()
            .then(data => setSummary(data))
            .catch(err => setError("Could not fetch summary."))
            .finally(() => setLoading(false));
    }, []);

    return (
        <section className="transcript-page-section" style={{ minHeight: '80vh', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'flex-start', paddingTop: '3rem' }}>
            <div style={{ width: '100%', maxWidth: '900px', display: 'flex', flexDirection: 'row', alignItems: 'center', marginBottom: '2rem' }}>
                <button className="generalButton" style={{ margin: 0 }} onClick={() => navigate('/')}>Back to Home</button>
                <div style={{ flex: 1, display: 'flex', justifyContent: 'left', marginLeft: '20rem' }}>
                    <h1 style={{ color: '#fff', fontSize: '2.5rem', textAlign: 'center' }}>Insights</h1>
                </div>
            </div>
            <div style={{
                width: '100%',
                maxWidth: '900px',
                background: '#232323',
                borderRadius: '18px',
                boxShadow: '0 2px 12px rgba(0,0,0,0.15)',
                overflow: 'hidden',
            }}>
                {/* Right column: Transcript text (now full width) */}
                <div style={{
                    width: '100%',
                    color: '#fff',
                    padding: '2rem',
                    fontSize: '1.15rem',
                    lineHeight: 1.7,
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '1.5rem',
                    alignItems: 'center',
                    boxSizing: 'border-box',
                    overflowY: 'auto',
                    minHeight: '300px',
                }}>
                    <div style={{ width: '100%', overflowWrap: 'break-word', wordBreak: 'break-word' }}>
                        {loading ? (
                            <div style={{ color: '#fff', fontSize: '1.5rem' }}>Loading...</div>
                        ) : error ? (
                            <div style={{ color: 'red', fontSize: '1.5rem' }}>{error}</div>
                        ) : summary.length === 0 ? (
                            <div style={{ color: '#fff', fontSize: '1.5rem' }}>No summary available.</div>
                        ) : (
                            <ul style={{ listStyle: 'disc', color: '#fff', paddingLeft: '1.5rem', margin: 0, fontSize: '1.5rem', lineHeight: 2, display: 'flex', flexDirection: 'column', gap: '1.2rem' }}>
                                {summary.map((item, idx) => (
                                    <li key={idx}>{item}</li>
                                ))}
                            </ul>
                        )}
                    </div>
                </div>
            </div>
        </section>
    );
}