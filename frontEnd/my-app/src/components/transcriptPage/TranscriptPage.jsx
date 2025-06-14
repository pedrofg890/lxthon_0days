import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getLastTranscript } from '../../services/transcriptService';
import '../../styles/HomePage.css';
import '../../styles/RequestButtom.css';
import '../../styles/BelowBarButtom.css';
import '../../styles/GeneralButtom.css';

/**
 * TranscriptPage component.
 *
 * Continuously polls the latest transcript segments from local storage (via getLastTranscript),
 * and displays them in a responsive grid with start time, end time, and normalized text.
 * Includes a "Back to Home" button.
 *
 * @component
 */
export default function TranscriptPage() {
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    /**
     * Poll getLastTranscript() every 500ms to update displayed segments
     * Cleans up the interval on unmount
     */
    useEffect(() => {
        setLoading(true);
        // getLastTranscript is synchronous, but we want to update if the user navigates back and forth
        const interval = setInterval(() => {
            const result = getLastTranscript();
            setData(result);
            setLoading(false);
        }, 500);
        return () => clearInterval(interval);
    }, []);

    // Helper to format seconds to mm:ss:ms or ss:ms, always 2 digits for min/sec, 3 for ms
    function formatTime(time) {
        // Accepts time as string or number (seconds, possibly with decimals)
        let t = typeof time === 'string' ? parseFloat(time) : time;
        if (isNaN(t)) return time;
        const minutes = Math.floor(t / 60);
        const seconds = Math.floor(t % 60);
        const milliseconds = Math.floor((t - Math.floor(t)) * 1000);
        const minStr = minutes.toString().padStart(2, '0');
        const secStr = seconds.toString().padStart(2, '0');
        const msStr = milliseconds.toString().padStart(3, '0').slice(0, 3);
        if (minutes > 0) {
            return `${minStr}:${secStr}:${msStr}`;
        } else {
            return `${secStr}:${msStr}`;
        }
    }

    return (
        <section className="transcript-page-section" style={{ minHeight: '80vh', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'flex-start', paddingTop: '3rem' }}>
            {loading ? (
                <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh', width: '100%' }}>
                    <span className="loading-spinner" style={{
                        width: 48,
                        height: 48,
                        border: '6px solid #fff',
                        borderTop: '6px solid #888',
                        borderRadius: '50%',
                        animation: 'spin 1s linear infinite',
                        display: 'inline-block',
                    }} />
                </div>
            ) : (
                <>
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
                                <div>{formatTime(line.startTime)}</div>
                                <div>{formatTime(line.endTime)}</div>
                                <div>{line.normalizedText}</div>
                            </div>
                        ))
                    )}
                </div>
                </>
            )}
        </section>
    );
}