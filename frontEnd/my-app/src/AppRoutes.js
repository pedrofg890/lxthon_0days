// AppRoutes.js
import { Routes, Route } from 'react-router-dom';
import Home from './pages/Home';
import Transcript from './pages/Transcript';
import Insights from './pages/Insights';
import Quiz from './pages/Quiz';


function AppRoutes() {
    return (
        <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/transcript" element={<Transcript />} />
            <Route path="/insights" element={<Insights />} />
            <Route path="/quiz" element={<Quiz />} />
        </Routes>
    );
}

export default AppRoutes;
