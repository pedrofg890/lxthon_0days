import React, {useEffect, useState} from 'react';
import { useNavigate } from 'react-router-dom';
import { getLastSummary } from '../../services/insightsService';
import '../../styles/HomePage.css';
import '../../styles/RequestButtom.css';
import '../../styles/BelowBarButtom.css';
import '../../styles/GeneralButtom.css';

export default function InsightsPage() {
    const [data, setData] = useState([]);
    const navigate = useNavigate();
    const [summary, setSummary] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        // getLastTranscript is synchronous, but we want to update if the user navigates back and forth
        const interval = setInterval(() => {
            setData(getLastSummary());
        }, 500);
        return () => clearInterval(interval);
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
                    <div style={{ width: '100%', overflowWrap: 'break-word', wordBreak: 'break-word', color: '#fff', fontSize: '1.5rem' }}>
                        {data && data.length > 0 ? data : 'No summary available.'}
                    </div>
                </div>
            </div>
        </section>
    );
}