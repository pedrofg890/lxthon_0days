// AppRoutes.js
import { Routes, Route } from 'react-router-dom';
import Home from './pages/Home';
import Transcript from './pages/Transcript';
import Insights from './pages/Insights';
import Quizz from './pages/Quizz';


function AppRoutes() {
    return (
        <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/transcript" element={<Transcript />} />
            <Route path="/insights" element={<Insights />} />
            <Route path="/test" element={<Quizz />} />
        </Routes>
    );
}

export default AppRoutes;
