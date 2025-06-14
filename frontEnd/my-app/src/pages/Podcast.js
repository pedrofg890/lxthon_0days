import React from 'react';
import PodcastPage from '../components/podcastPage/PodcastPage';

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
function Podcast() {
    return (
        <>
            <PodcastPage />
        </>
    );
}

export default Podcast;