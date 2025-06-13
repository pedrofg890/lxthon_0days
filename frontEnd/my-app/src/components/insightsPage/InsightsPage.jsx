import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../../styles/HomePage.css';
import '../../styles/RequestButtom.css';
import '../../styles/BelowBarButtom.css';
import '../../styles/GeneralButtom.css';

export default function InsightsPage() {
    const navigate = useNavigate();



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
                        <ul style={{ listStyle: 'disc', color: '#fff', paddingLeft: '1.5rem', margin: 0, fontSize: '1.5rem', lineHeight: 2, display: 'flex', flexDirection: 'column', gap: '1.2rem' }}>
                            <li><strong>Symmetry in Physics:</strong> Many physical laws are based on symmetry principles, such as conservation of energy and momentum, which are deeply connected to Noether's theorem.</li>
                            <li><strong>Wave-Particle Duality:</strong> Quantum mechanics reveals that particles like electrons exhibit both wave-like and particle-like properties, challenging classical intuition.</li>
                            <li><strong>Mathematical Models:</strong> Differential equations, such as Schrödinger's equation and Maxwell's equations, are fundamental tools for describing physical phenomena.</li>
                            <li><strong>Dimensional Analysis:</strong> This technique helps physicists check the consistency of equations and derive relationships between physical quantities.</li>
                            <li><strong>Group Theory:</strong> Used extensively in quantum mechanics and particle physics to describe symmetries and conservation laws.</li>
                            <li><strong>Fourier Analysis:</strong> Decomposes complex signals into simpler sinusoidal components, essential in solving partial differential equations in physics.</li>
                            <li><strong>Topology in Physics:</strong> Concepts from topology explain phenomena like the quantum Hall effect and topological insulators.</li>
                        </ul>
                    </div>
                </div>
            </div>
        </section>
    );
}