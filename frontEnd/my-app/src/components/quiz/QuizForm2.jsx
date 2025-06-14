import React, { useState } from 'react';

function QuizForm2({ quiz, onSubmit }) {
    const [answers, setAnswers] = useState({});

    const handleChange = (qid, idx) => {
        setAnswers((prev) => ({ ...prev, [qid]: idx }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        onSubmit(answers);
    };

    return (
        <form onSubmit={handleSubmit}>
            <h2 style={{ textAlign: 'center', marginBottom: '1rem' }}>{quiz.title}</h2>

            {quiz.questions.map((q) => (
                <div key={q.id} style={{ marginBottom: '1.5rem' }}>
                    <p style={{ fontWeight: 'bold' }}>{q.id}. {q.question}</p>
                    <div style={{ marginLeft: '1rem' }}>
                        {q.choices.map((choice, i) => (
                            <label key={i} style={{ display: 'block', margin: '0.25rem 0' }}>
                                <input
                                    type="radio"
                                    name={q.id}
                                    value={i}
                                    checked={answers[q.id] === i}
                                    onChange={() => handleChange(q.id, i)}
                                    style={{ marginRight: '0.5rem' }}
                                />
                                {choice}
                            </label>
                        ))}
                    </div>
                </div>
            ))}

            <button
                type="submit"
                style={{ display: 'block', margin: '1rem auto', padding: '0.75rem 1.5rem', background: '#4F46E5', color: 'white', border: 'none', borderRadius: 4 }}
            >
                Submeter Respostas
            </button>
        </form>
    );
}
export default QuizForm2;