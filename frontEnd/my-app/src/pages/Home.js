import React from 'react';
import HomePage from "../components/homePage/HomePage";

/**
 * Home component.
 *
 * Wraps the HomePage component and serves as the default landing route.
 * It simply renders the HomePage, which handles all user interactions
 * for fetching transcripts, summaries, and quizzes.
 *
 * @component
 */
function Home() {
    return (
        <>
            <HomePage/>
        </>

    );
}

export default Home;