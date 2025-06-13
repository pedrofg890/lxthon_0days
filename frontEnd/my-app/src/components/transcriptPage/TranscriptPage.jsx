import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getLastTranscript } from '../../services/transcriptService';
import '../../styles/HomePage.css';
import '../../styles/RequestButtom.css';
import '../../styles/BelowBarButtom.css';
import '../../styles/GeneralButtom.css';

export default function TranscriptPage() {
    const [data, setData] = useState([]);
    const navigate = useNavigate();

    useEffect(() => {
        // getLastTranscript is synchronous, but we want to update if the user navigates back and forth
        const interval = setInterval(() => {
            setData(getLastTranscript());
        }, 500);
        return () => clearInterval(interval);
    }, []);

    // Map normalizedText to text for display, fallback to text if not present
    // Use data directly for rendering

    return (
        <section className="transcript-page-section" style={{ minHeight: '80vh', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'flex-start', paddingTop: '3rem' }}>
            <div style={{ width: '100%', maxWidth: '900px', display: 'flex', flexDirection: 'row', alignItems: 'center', marginBottom: '2rem' }}>
                <button className="generalButton" style={{ margin: 0 }} onClick={() => navigate('/')}>Back to Home</button>
                <div style={{ flex: 1, display: 'flex', justifyContent: 'center' }}>
                    <h1 style={{ color: '#fff', fontSize: '2.5rem', textAlign: 'center' }}>YouTube Video Transcript</h1>
                </div>
            </div>
            <div style={{
                display: 'flex',
                flexDirection: 'column',
                width: '100%',
                maxWidth: '900px',
                background: '#232323',
                borderRadius: '18px',
                boxShadow: '0 2px 12px rgba(0,0,0,0.15)',
                padding: '2rem',
                marginBottom: '2rem',
            }}>
                <div style={{
                    display: 'grid',
                    gridTemplateColumns: '0.7fr 0.7fr 3fr',
                    columnGap: '0.25rem',
                    rowGap: '0.5rem',
                    fontWeight: 'bold',
                    color: '#fff',
                    borderBottom: '1px solid #444',
                    paddingBottom: '0.5rem',
                    marginBottom: '1rem',
                    fontSize: '2rem',
                }}>
                    <div>Start Time</div>
                    <div>End Time</div>
                    <div>Text</div>
                </div>
                {data.length === 0 ? (
                    <div style={{ color: '#fff', textAlign: 'center', marginTop: '2rem' }}>
                        No transcript data to display.
                    </div>
                ) : (
                    data.map((line, idx) => (
                        <div key={idx} style={{
                            display: 'grid',
                            gridTemplateColumns: '0.7fr 0.7fr 3fr',
                            columnGap: '0.25rem',
                            rowGap: '0.5rem',
                            color: '#fff',
                            padding: '0.5rem 0',
                            borderBottom: '1px solid #333',
                            alignItems: 'center',
                            fontSize: '1.6rem',
                        }}>
                            <div>{line.startTime}</div>
                            <div>{line.endTime}</div>
                            <div>{line.normalizedText}</div>
                        </div>
                    ))
                )}
            </div>
        </section>
    );
}