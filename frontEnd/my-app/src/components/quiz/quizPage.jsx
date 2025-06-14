// components/quiz/quizPage.jsx
import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { getQuizFromVideo } from '../../services/quizService';
import QuizForm from './quizForm';

export default function QuizPage() {
    const [searchParams] = useSearchParams();
    const videoUrl     = searchParams.get('url') || '';
    const numQuestions = parseInt(searchParams.get('numQuestions') || '5', 10);

    const [quiz, setQuiz]       = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError]     = useState('');

    useEffect(() => {
        if (!videoUrl) return;
        setLoading(true);
        setError('');
        getQuizFromVideo(videoUrl, numQuestions)
            .then((q) => setQuiz(q))
            .catch((err) => setError(err.message || 'Erro ao gerar quiz'))
            .finally(() => setLoading(false));
    }, [videoUrl, numQuestions]);

    if (!videoUrl) {
        return <p style={{ color: 'white', textAlign: 'center' }}>Por favor volte à Home e cole o link do vídeo.</p>;
    }
    if (loading) {
        return <p style={{ color: 'white', textAlign: 'center' }}>Gerando quiz… isto pode demorar 20–30 segundos</p>;
    }
    if (error) {
        return <p style={{ color: 'red', textAlign: 'center' }}>{error}</p>;
    }
    if (!quiz) {
        return null; // antes de disparar o fetch
    }

    return (
        <div style={{ maxWidth: 800, margin: '2rem auto', color: 'white' }}>
            <QuizForm
                quiz={quiz}
                onSubmit={(answers) => {
                    console.log('Respostas submetidas:', answers);
                    // aqui podes dar feedback
                }}
            />
        </div>
    );
}
