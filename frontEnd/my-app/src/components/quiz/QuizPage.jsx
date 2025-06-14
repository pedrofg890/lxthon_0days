import React, {useEffect, useState} from 'react';
import { useNavigate } from 'react-router-dom';
import { getLastQuiz } from '../../services/quizService';
import '../../styles/HomePage.css';
import '../../styles/RequestButtom.css';
import '../../styles/BelowBarButtom.css';
import '../../styles/GeneralButtom.css';

export default function QuizPage() {
    const [quiz, setQuiz] = useState(null);
    const [current, setCurrent] = useState(0);
    const [answers, setAnswers] = useState([]); // user answers (index per question)
    const [showResult, setShowResult] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        setQuiz(getLastQuiz());
    }, []);

    if (!quiz || !quiz.questions) {
        return (
            <section className="transcript-page-section" style={{ minHeight: '80vh', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', paddingTop: '3rem' }}>
                <div style={{ color: '#fff', fontSize: '2rem', textAlign: 'center' }}>
                    No quiz data to display.
                </div>
                <button className="generalButton" style={{ marginTop: '2rem' }} onClick={() => navigate('/')}>Back to Home</button>
            </section>
        );
    }

    const handleAnswer = (choiceIdx) => {
        const updated = [...answers];
        updated[current] = choiceIdx;
        setAnswers(updated);
        if (current < quiz.questions.length - 1) {
            setCurrent(current + 1);
        } else {
            setShowResult(true);
        }
    };

    const correctCount = answers.reduce((acc, ans, idx) =>
        ans === quiz.questions[idx].correctIndex ? acc + 1 : acc, 0
    );

    return (
        <section className="transcript-page-section" style={{ minHeight: '80vh', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'flex-start', paddingTop: '3rem' }}>
            <div style={{ width: '100%', maxWidth: '900px', display: 'flex', flexDirection: 'row', alignItems: 'center', marginBottom: '2rem' }}>
                <button className="generalButton" style={{ margin: 0 }} onClick={() => navigate('/')}>Back to Home</button>
                <div style={{ flex: 1, display: 'flex', justifyContent: 'left', marginLeft: '20rem' }}>
                    <h1 style={{ color: '#fff', fontSize: '2.5rem', textAlign: 'center' }}>Quiz</h1>
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
                        <h2 style={{ color: '#fff', fontSize: '2rem', marginBottom: '1.5rem' }}>{quiz.title}</h2>
                        {!showResult ? (
                            <div>
                                <div style={{ fontSize: '1.4rem', marginBottom: '1rem' }}>
                                    <b>Question {current + 1} of {quiz.questions.length}:</b><br />
                                    {quiz.questions[current].question}
                                </div>
                                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                                    {quiz.questions[current].choices.map((choice, idx) => (
                                        <button
                                            key={idx}
                                            className="generalButton"
                                            style={{ fontSize: '1.2rem', textAlign: 'left', padding: '0.75rem 1.5rem', background: '#333', color: '#fff', border: '1px solid #555', borderRadius: '8px' }}
                                            onClick={() => handleAnswer(idx)}
                                            disabled={answers[current] !== undefined}
                                        >
                                            {choice}
                                        </button>
                                    ))}
                                </div>
                                {answers[current] !== undefined && (
                                    <div style={{ marginTop: '1.5rem', color: '#aaa' }}>
                                        <b>Selected:</b> {quiz.questions[current].choices[answers[current]]}
                                    </div>
                                )}
                            </div>
                        ) : (
                            <div style={{ textAlign: 'center' }}>
                                <h2 style={{ color: '#90ee90' }}>Quiz Complete!</h2>
                                <div style={{ fontSize: '1.3rem', margin: '1rem 0' }}>
                                    You got <b>{correctCount}</b> out of <b>{quiz.questions.length}</b> correct.
                                </div>
                                <div style={{ marginTop: '2rem' }}>
                                    {quiz.questions.map((q, idx) => (
                                        <div key={q.id} style={{ marginBottom: '1.2rem', background: '#222', borderRadius: '8px', padding: '1rem' }}>
                                            <div><b>Q{idx + 1}:</b> {q.question}</div>
                                            <div><b>Your answer:</b> {q.choices[answers[idx]] || <span style={{ color: 'red' }}>No answer</span>}</div>
                                            <div><b>Correct answer:</b> {q.choices[q.correctIndex]}</div>
                                            {answers[idx] === q.correctIndex ? (
                                                <div style={{ color: '#90ee90' }}>Correct</div>
                                            ) : (
                                                <div style={{ color: '#ff6961' }}>Incorrect</div>
                                            )}
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </section>
    );
}