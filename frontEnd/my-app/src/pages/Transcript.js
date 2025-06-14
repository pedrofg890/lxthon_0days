import React from 'react';
import TranscriptPage from "../components/transcriptPage/TranscriptPage";

/**
 * Transcript component.
 *
 * Acts as the route wrapper for the TranscriptPage component.
 * Renders the full transcript view showing cleaned transcript segments
 * with their timecodes.
 *
 * @component
 * @example
 * return (
 *   <Transcript />
 * );
 */
function Transcript() {
    return (
        <>
            <TranscriptPage/>
        </>

    );
}

export default Transcript;