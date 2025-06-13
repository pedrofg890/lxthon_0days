import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../../styles/HomePage.css';
import '../../styles/RequestButtom.css';
import '../../styles/BelowBarButtom.css';
import '../../styles/GeneralButtom.css';

export default function TranscriptPage() {
    const navigate = useNavigate();
    // Example transcript data
    const [transcript, setTranscript] = useState([
        { timestamp: '00:00', text: 'Welcome to the video.' },
        { timestamp: '00:10', text: 'Today we will learn about React.' },
        { timestamp: '00:20', text: 'Let us start with components.' },
        // ... more transcript lines ...
    ]);

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
                flexDirection: 'row',
                width: '100%',
                maxWidth: '900px',
                background: '#232323',
                borderRadius: '18px',
                boxShadow: '0 2px 12px rgba(0,0,0,0.15)',
                overflow: 'hidden',
            }}>
                {/* Left column: Timestamps */}
                <div style={{
                    flex: '0 0 120px',
                    background: '#1a1a1a',
                    color: '#aaa',
                    padding: '2rem 1rem',
                    borderRight: '1px solid #333',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    fontSize: '1.5rem',
                    gap: '1.5rem',
                }}>
                    {transcript.map((line, idx) => (
                        <span key={idx}>{line.timestamp}</span>
                    ))}
                </div>
                {/* Right column: Transcript text */}
                <div style={{
                    flex: 1,
                    color: '#fff',
                    padding: '2rem',
                    fontSize: '1.5rem',
                    lineHeight: 1.7,
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '1.5rem',
                }}>
                    {transcript.map((line, idx) => (
                        <span key={idx}>{line.text}</span>
                    ))}
                </div>
            </div>
        </section>
    );
}