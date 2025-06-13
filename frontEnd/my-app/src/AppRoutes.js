// AppRoutes.js
import { Routes, Route } from 'react-router-dom';
import Home from './pages/Home';
import Transcript from './pages/Transcript';

function AppRoutes() {
    return (
        <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/transcript" element={<Transcript />} />
        </Routes>
    );
}

export default AppRoutes;
