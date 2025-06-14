import React from 'react';
import InsightsPage from "../components/insightsPage/InsightsPage";

/**
 * Insights component.
 *
 * Serves as the route wrapper for the InsightsPage component.
 * It simply renders InsightsPage to display AI-generated summaries.
 *
 * @component
 * @example
 * return (
 *   <Insights />
 * );
 */
function Insights() {
    return (
        <>
            <InsightsPage/>
        </>

    );
}

export default Insights;