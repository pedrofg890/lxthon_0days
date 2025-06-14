// AppRoutes.js
import { Routes, Route } from 'react-router-dom';
import Home from './pages/Home';
import Transcript from './pages/Transcript';
import Insights from './pages/Insights';
import Quiz from './pages/Quiz';
import Podcast from './pages/Podcast';


function AppRoutes() {
    return (
        <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/transcript" element={<Transcript />} />
            <Route path="/summary" element={<Insights />} />
            <Route path="/quiz" element={<Quiz />} />
            <Route path="/podcast" element={<Podcast />} />
            {/* Add more routes as needed */}
        </Routes>
    );
}

export default AppRoutes;
