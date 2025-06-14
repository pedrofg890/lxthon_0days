import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { getQuizFromVideo } from '../../services/quizService';
import QuizForm2 from './QuizForm2';
import '../../styles/GeneralButtom.css';


function QuizPage2() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
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
        return <p style={{ color: 'white', textAlign: 'center' }}>Go back to the home page and paste the video link.</p>;
    }
    if (loading) {
        return <p style={{ color: 'white', textAlign: 'center' }}>Generating quiz… this may take 20–30 seconds</p>;
    }
    if (error) {
        return <p style={{ color: 'red', textAlign: 'center' }}>{error}</p>;
    }
    if (!quiz) {
        return null; 
    }

    return (
        
        <div style={{ maxWidth: 800, margin: '2rem auto', color: 'white' }}>
            <button className="generalButton" style={{ margin: 0 }} onClick={() => navigate('/')}>Back to Home</button>
            <QuizForm2
                quiz={quiz}
                onSubmit={(answers) => {
                    console.log('Respostas submetidas:', answers);
                }}
            />


        </div>
    );
}


export default QuizPage2;