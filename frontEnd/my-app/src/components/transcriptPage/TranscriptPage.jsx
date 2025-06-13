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
        { startTime: '00:00', endTime: '00:10', text: 'Welcome to the video.' },
        { startTime: '00:10', endTime: '00:20', text: 'Today we will learn about React.' },
        { startTime: '00:20', endTime: '00:30', text: 'Let us start with components.' },
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
                    gridTemplateColumns: '0.7fr 0.7fr 3.6fr',
                    columnGap: '0.25rem',
                    rowGap: '0.5rem',
                    fontWeight: 'bold',
                    color: '#fff',
                    borderBottom: '1px solid #444',
                    paddingBottom: '0.5rem',
                    marginBottom: '1rem',
                    fontSize: '1.5rem', // increased font size for header
                }}>
                    <div>Start Time</div>
                    <div>End Time</div>
                    <div>Text</div>
                </div>
                {transcript.map((line, idx) => (
                    <div key={idx} style={{
                        display: 'grid',
                        gridTemplateColumns: '0.7fr 0.7fr 3.6fr',
                        columnGap: '0.25rem',
                        rowGap: '0.5rem',
                        color: '#fff',
                        padding: '0.5rem 0',
                        borderBottom: '1px solid #333',
                        alignItems: 'center',
                        fontSize: '1.5rem', // increased font size for rows
                    }}>
                        <div>{line.startTime}</div>
                        <div>{line.endTime}</div>
                        <div>{line.text}</div>
                    </div>
                ))}
            </div>
        </section>
    );
}