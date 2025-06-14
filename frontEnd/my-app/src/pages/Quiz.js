import React from 'react';
import QuizPage from '../components/quiz/QuizPage';

/**
 * Quiz component.
 *
 * Route wrapper for the QuizPage component.
 * Renders the interactive quiz UI where users can answer questions generated from the video.
 *
 * @component
 * @example
 * return (
 *   <Quiz />
 * );
 */
function Quiz() {
    return (
        <>
            <QuizPage />
        </>
    );
}

export default Quiz;