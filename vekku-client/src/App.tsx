import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Home from './Home';
import TagSuggestion from './components/TagSuggestion';
import { AuthProvider } from './components/auth/AuthProvider';
import SignupPage from './components/auth/SignupPage';
import VerifyPage from './components/auth/VerifyPage';
import LoginPage from './components/auth/LoginPage';
import './App.css';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/tag-suggestion" element={<TagSuggestion />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/verify" element={<VerifyPage />} />
          <Route path="/login" element={<LoginPage />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
